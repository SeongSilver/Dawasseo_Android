do $$
begin
  create type public.friend_status as enum ('pending', 'accepted', 'rejected', 'blocked');
exception
  when duplicate_object then null;
end $$;

alter table public.friends
  add column if not exists status public.friend_status not null default 'accepted';

do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conname = 'friends_no_self_check'
      and conrelid = 'public.friends'::regclass
  ) then
    alter table public.friends
      add constraint friends_no_self_check
      check (user_id <> friend_id);
  end if;
end $$;

create index if not exists friends_friend_id_idx on public.friends(friend_id);
create index if not exists friends_status_idx on public.friends(status);

with duplicate_friends as (
  select id,
    row_number() over (
      partition by least(user_id, friend_id), greatest(user_id, friend_id)
      order by created_at asc, id asc
    ) as row_number
  from public.friends
)
delete from public.friends
where id in (
  select id from duplicate_friends where row_number > 1
);

create unique index if not exists friends_pair_unique_idx
  on public.friends (least(user_id, friend_id), greatest(user_id, friend_id));

create or replace function public.validate_friend_update()
returns trigger as $$
begin
  if old.user_id <> new.user_id or old.friend_id <> new.friend_id then
    raise exception 'friend participants cannot be changed';
  end if;

  if old.status = 'pending'
     and new.status in ('accepted', 'rejected')
     and auth.uid() <> old.friend_id then
    raise exception 'only the request recipient can accept or reject a friend request';
  end if;

  return new;
end;
$$ language plpgsql security definer;

drop trigger if exists validate_friend_update_trigger on public.friends;
create trigger validate_friend_update_trigger
  before update on public.friends
  for each row execute procedure public.validate_friend_update();

drop policy if exists friends_self_all on public.friends;
drop policy if exists friends_participant_select on public.friends;
drop policy if exists friends_request_insert on public.friends;
drop policy if exists friends_participant_update on public.friends;
drop policy if exists friends_participant_delete on public.friends;

create policy friends_participant_select
  on public.friends for select
  using (auth.uid() = user_id or auth.uid() = friend_id);

create policy friends_request_insert
  on public.friends for insert
  with check (
    auth.uid() = user_id
    and user_id <> friend_id
    and status = 'pending'
  );

create policy friends_participant_update
  on public.friends for update
  using (auth.uid() = user_id or auth.uid() = friend_id)
  with check (auth.uid() = user_id or auth.uid() = friend_id);

create policy friends_participant_delete
  on public.friends for delete
  using (auth.uid() = user_id or auth.uid() = friend_id);

notify pgrst, 'reload schema';
