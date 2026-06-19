# SCHEMA.md — 다왔어 Supabase DB 스키마

> Supabase PostgreSQL 기반. 모든 테이블 RLS(Row Level Security) 적용.
> Android 네이티브 전환 후에도 기존 스키마와 RLS 정책은 유지한다.

---

## 테이블 개요

```
user_profiles     — 유저 프로필 (auth.users 확장)
alarms            — 위치 알람
friends           — 친구 관계/요청 (단일 관계 행)
alarm_permissions — 대리 알람 권한
```

---

## 1. user_profiles

```sql
create table public.user_profiles (
  id           uuid primary key references auth.users(id) on delete cascade,
  email        text unique not null,
  nickname     text not null,
  avatar_url   text,
  push_token   text,                        -- FCM 토큰
  created_at   timestamptz default now()
);

-- 신규 가입 시 프로필 자동 생성
create function public.handle_new_user()
returns trigger as $$
begin
  insert into public.user_profiles (id, email, nickname)
  values (new.id, new.email, split_part(new.email, '@', 1));
  return new;
end;
$$ language plpgsql security definer;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();
```

---

## 2. alarms

```sql
create table public.alarms (
  id               uuid primary key default gen_random_uuid(),
  owner_id         uuid not null references public.user_profiles(id) on delete cascade,
  created_by       uuid not null references public.user_profiles(id),  -- 본인 or 친구
  label            text not null default '알람',
  target_lat       double precision not null,
  target_lng       double precision not null,
  target_address   text,
  radius_km        numeric(5,2) not null default 0.5
                   check (radius_km >= 0.01 and radius_km <= 50),
  is_active        boolean not null default true,
  sound_type       text not null default 'default'   -- 'default' | 'custom'
                   check (sound_type in ('default', 'custom')),
  sound_uri        text,                              -- custom일 때 Storage 경로
  created_at       timestamptz default now()
);

create index alarms_owner_id_idx on public.alarms(owner_id);
create index alarms_is_active_idx on public.alarms(is_active);
```

| 컬럼 | 타입 | 설명 |
|------|------|------|
| owner_id | uuid | 알람이 울릴 대상 (소유자) |
| created_by | uuid | 알람을 설정한 사람 (본인 또는 친구) |
| radius_km | numeric | 트리거 반경 (DB 허용 0.01 ~ 50km, 현재 UI 옵션 10m ~ 10km) |
| sound_type | text | default(기본음) / custom(녹음) |
| sound_uri | text | custom일 때 Storage 파일 경로 |

서버 `alarms` 테이블은 알람 설정값만 저장한다. 목적지 도착 여부, 목적지 근처 여부, 알람 울림 여부, 알람 울림 시각, 확인 여부는 저장하지 않는다. 반경 진입 판단과 알림 실행은 소유자 기기 내부에서만 처리한다.

---

## 3. friends

```sql
create table public.friends (
  id          uuid primary key default gen_random_uuid(),
  user_id     uuid not null references public.user_profiles(id) on delete cascade, -- 요청자
  friend_id   uuid not null references public.user_profiles(id) on delete cascade, -- 요청 대상
  status      friend_status not null default 'pending',
  created_at  timestamptz default now(),
  check (user_id <> friend_id)
);

create index friends_user_id_idx on public.friends(user_id);
create index friends_friend_id_idx on public.friends(friend_id);
create index friends_status_idx on public.friends(status);
create unique index friends_pair_unique_idx
  on public.friends (least(user_id, friend_id), greatest(user_id, friend_id));
```

| 컬럼 | 설명 |
|------|------|
| user_id | 친구 요청을 보낸 사용자 |
| friend_id | 친구 요청을 받은 사용자 |
| status | pending / accepted / rejected / blocked |

> 친구 관계는 한 쌍당 하나의 행만 저장한다. `accepted` 상태는 양방향 친구 관계로 해석한다.
> 기존 단순 친구 행은 마이그레이션에서 `accepted`로 간주한다.

---

## 4. alarm_permissions

```sql
create type permission_status as enum ('pending', 'accepted', 'rejected');

create type friend_status as enum ('pending', 'accepted', 'rejected', 'blocked');

create table public.alarm_permissions (
  id             uuid primary key default gen_random_uuid(),
  requester_id   uuid not null references public.user_profiles(id) on delete cascade,
  target_id      uuid not null references public.user_profiles(id) on delete cascade,
  status         permission_status not null default 'pending',
  expires_at     timestamptz,                       -- null = 영구
  created_at     timestamptz default now(),
  unique(requester_id, target_id)
);
```

| 컬럼 | 설명 |
|------|------|
| requester_id | 권한을 요청한 친구 |
| target_id | 권한을 부여하는 대상 (나) |
| status | pending / accepted / rejected |
| expires_at | MVP에서는 null로 저장하며, null은 만료 없음 |

---

## RLS 정책

```sql
-- user_profiles
alter table public.user_profiles enable row level security;

create policy "본인 프로필 전체 접근"
  on public.user_profiles for all
  using (auth.uid() = id);

create policy "닉네임/아바타 공개 읽기"
  on public.user_profiles for select
  using (true);

-- alarms
alter table public.alarms enable row level security;

create policy "본인 알람 전체 접근"
  on public.alarms for all
  using (auth.uid() = owner_id);

create policy "권한 있는 친구가 알람 생성 가능"
  on public.alarms for insert
  with check (
    auth.uid() = created_by and
    exists (
      select 1 from public.alarm_permissions
      where requester_id = auth.uid()
        and target_id = owner_id
        and status = 'accepted'
    )
  );

-- friends
alter table public.friends enable row level security;

create policy "본인 친구 목록 접근"
  on public.friends for select
  using (auth.uid() = user_id or auth.uid() = friend_id);

create policy "친구 요청 생성"
  on public.friends for insert
  with check (
    auth.uid() = user_id
    and user_id <> friend_id
    and status = 'pending'
  );

create policy "친구 관계 참여자 수정"
  on public.friends for update
  using (auth.uid() = user_id or auth.uid() = friend_id)
  with check (auth.uid() = user_id or auth.uid() = friend_id);

create policy "친구 관계 참여자 삭제"
  on public.friends for delete
  using (auth.uid() = user_id or auth.uid() = friend_id);

-- validate_friend_update_trigger
-- user_id/friend_id 변경 방지
-- pending -> accepted/rejected는 요청 대상(friend_id)만 가능

-- alarm_permissions
alter table public.alarm_permissions enable row level security;

create policy "알람 권한 참여자 조회"
  on public.alarm_permissions for select
  using (auth.uid() = requester_id or auth.uid() = target_id);

create policy "알람 권한 요청 생성"
  on public.alarm_permissions for insert
  with check (
    auth.uid() = requester_id
    and requester_id <> target_id
    and status = 'pending'
  );

create policy "알람 권한 참여자 수정"
  on public.alarm_permissions for update
  using (auth.uid() = requester_id or auth.uid() = target_id)
  with check (auth.uid() = requester_id or auth.uid() = target_id);

create policy "알람 권한 참여자 삭제"
  on public.alarm_permissions for delete
  using (auth.uid() = requester_id or auth.uid() = target_id);

-- validate_alarm_permission_update_trigger
-- requester_id/target_id 변경 방지
-- pending -> accepted/rejected는 권한 부여 대상(target_id)만 가능
```

---

## Storage 버킷

### alarm-sounds
커스텀 녹음 알람음 저장용.

```
버킷명: alarm-sounds
공개 여부: private (인증된 유저만 접근)
파일 경로 규칙: {userId}/{timestamp}.m4a
```

### Storage RLS 정책

```sql
-- 본인 폴더에만 업로드 가능
create policy "본인 폴더 업로드"
  on storage.objects for insert
  with check (
    bucket_id = 'alarm-sounds'
    and (storage.foldername(name))[1] = auth.uid()::text
  );

-- 본인 파일 읽기
create policy "본인 파일 읽기"
  on storage.objects for select
  using (
    bucket_id = 'alarm-sounds'
    and (storage.foldername(name))[1] = auth.uid()::text
  );

-- 본인 파일 삭제
create policy "본인 파일 삭제"
  on storage.objects for delete
  using (
    bucket_id = 'alarm-sounds'
    and (storage.foldername(name))[1] = auth.uid()::text
  );
```

> 친구가 대리로 설정한 알람의 사운드는 owner가 접근할 수 있도록 별도 정책이 필요할 수 있다.
> 초기 버전에서는 대리 알람은 기본 알람음만 사용하는 것으로 단순화 가능.

---

## ERD

```
auth.users
    │
    └── user_profiles (1:1)
            │
            ├── alarms (1:N)            owner_id, created_by
            ├── friends (1:N)           user_id, friend_id, status
            └── alarm_permissions (1:N) requester_id, target_id

storage:
    alarm-sounds/{userId}/{timestamp}.m4a
```

---

## 마이그레이션 적용

```powershell
npx supabase db push
```

또는 Supabase SQL Editor에 위 DDL을 순서대로 실행:
`user_profiles → alarms → friends → alarm_permissions → RLS → Storage`

