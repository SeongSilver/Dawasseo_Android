create extension if not exists pgcrypto;

create table if not exists public.user_profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  email text unique not null,
  nickname text not null,
  avatar_url text,
  push_token text,
  created_at timestamptz default now()
);

create or replace function public.handle_new_user()
returns trigger as $$
begin
  insert into public.user_profiles (id, email, nickname)
  values (new.id, new.email, split_part(new.email, '@', 1))
  on conflict (id) do nothing;
  return new;
end;
$$ language plpgsql security definer;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();

do $$
begin
  create type public.permission_status as enum ('pending', 'accepted', 'rejected');
exception
  when duplicate_object then null;
end $$;

create table if not exists public.alarm_permissions (
  id uuid primary key default gen_random_uuid(),
  requester_id uuid not null references public.user_profiles(id) on delete cascade,
  target_id uuid not null references public.user_profiles(id) on delete cascade,
  status public.permission_status not null default 'pending',
  expires_at timestamptz,
  created_at timestamptz default now(),
  unique(requester_id, target_id)
);

create table if not exists public.alarms (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null references public.user_profiles(id) on delete cascade,
  created_by uuid not null references public.user_profiles(id),
  label text not null default 'alarm',
  target_lat double precision not null,
  target_lng double precision not null,
  target_address text,
  radius_km numeric(5,2) not null default 0.5 check (radius_km >= 0.1 and radius_km <= 50),
  is_active boolean not null default true,
  triggered_at timestamptz,
  sound_type text not null default 'default' check (sound_type in ('default', 'custom')),
  sound_uri text,
  created_at timestamptz default now()
);

create table if not exists public.friends (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.user_profiles(id) on delete cascade,
  friend_id uuid not null references public.user_profiles(id) on delete cascade,
  created_at timestamptz default now(),
  unique(user_id, friend_id)
);

create index if not exists alarms_owner_id_idx on public.alarms(owner_id);
create index if not exists alarms_is_active_idx on public.alarms(is_active);
create index if not exists friends_user_id_idx on public.friends(user_id);

alter table public.user_profiles enable row level security;
alter table public.alarms enable row level security;
alter table public.friends enable row level security;
alter table public.alarm_permissions enable row level security;

drop policy if exists user_profiles_self_all on public.user_profiles;
create policy user_profiles_self_all
  on public.user_profiles for all
  using (auth.uid() = id)
  with check (auth.uid() = id);

drop policy if exists user_profiles_public_read on public.user_profiles;
create policy user_profiles_public_read
  on public.user_profiles for select
  using (true);

drop policy if exists alarms_owner_all on public.alarms;
create policy alarms_owner_all
  on public.alarms for all
  using (auth.uid() = owner_id)
  with check (auth.uid() = owner_id and auth.uid() = created_by);

drop policy if exists alarms_delegate_insert on public.alarms;
create policy alarms_delegate_insert
  on public.alarms for insert
  with check (
    auth.uid() = created_by
    and exists (
      select 1
      from public.alarm_permissions
      where requester_id = auth.uid()
        and target_id = owner_id
        and status = 'accepted'
        and (expires_at is null or expires_at > now())
    )
  );

drop policy if exists friends_self_all on public.friends;
create policy friends_self_all
  on public.friends for all
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

drop policy if exists alarm_permissions_participant_all on public.alarm_permissions;
create policy alarm_permissions_participant_all
  on public.alarm_permissions for all
  using (auth.uid() = requester_id or auth.uid() = target_id)
  with check (auth.uid() = requester_id or auth.uid() = target_id);
