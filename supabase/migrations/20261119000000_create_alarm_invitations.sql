-- Alarm invitations: a requester proposes a destination alarm to a friend.
-- The server stores only the proposed alarm settings and the invitation status.
-- It never stores the target's location, arrival, or alarm-fired state
-- (see docs/DESTINATION_ALARM_PRIVACY.md).

create table if not exists public.alarm_invitations (
  id uuid primary key default gen_random_uuid(),
  requester_id uuid not null references public.user_profiles(id) on delete cascade,
  target_id uuid not null references public.user_profiles(id) on delete cascade,
  label text not null default 'alarm',
  target_address text,
  target_lat double precision not null,
  target_lng double precision not null,
  radius_km numeric(5,2) not null default 0.5
    check (radius_km >= 0.01 and radius_km <= 50),
  status text not null default 'pending'
    check (status in ('pending', 'accepted', 'rejected', 'expired', 'cancelled')),
  expires_at timestamptz,
  created_at timestamptz default now(),
  accepted_at timestamptz,
  rejected_at timestamptz,
  check (requester_id <> target_id)
);

create index if not exists alarm_invitations_requester_id_idx on public.alarm_invitations(requester_id);
create index if not exists alarm_invitations_target_id_idx on public.alarm_invitations(target_id);

alter table public.alarm_invitations enable row level security;

create or replace function public.validate_alarm_invitation_update()
returns trigger as $$
begin
  if old.requester_id <> new.requester_id or old.target_id <> new.target_id then
    raise exception 'alarm invitation participants cannot be changed';
  end if;

  if old.label <> new.label
     or old.target_lat <> new.target_lat
     or old.target_lng <> new.target_lng
     or old.radius_km <> new.radius_km
     or coalesce(old.target_address, '') <> coalesce(new.target_address, '') then
    raise exception 'alarm invitation settings cannot be changed after creation';
  end if;

  if old.status <> 'pending' and new.status <> old.status then
    raise exception 'only a pending alarm invitation can change status';
  end if;

  if new.status in ('accepted', 'rejected') and auth.uid() <> old.target_id then
    raise exception 'only the target can accept or reject an alarm invitation';
  end if;

  if new.status = 'cancelled' and auth.uid() <> old.requester_id then
    raise exception 'only the requester can cancel an alarm invitation';
  end if;

  if new.status = 'accepted' and new.accepted_at is null then
    new.accepted_at := now();
  end if;

  if new.status = 'rejected' and new.rejected_at is null then
    new.rejected_at := now();
  end if;

  return new;
end;
$$ language plpgsql security definer;

drop trigger if exists validate_alarm_invitation_update_trigger on public.alarm_invitations;
create trigger validate_alarm_invitation_update_trigger
  before update on public.alarm_invitations
  for each row execute procedure public.validate_alarm_invitation_update();

drop policy if exists alarm_invitations_participant_select on public.alarm_invitations;
drop policy if exists alarm_invitations_permitted_insert on public.alarm_invitations;
drop policy if exists alarm_invitations_participant_update on public.alarm_invitations;
drop policy if exists alarm_invitations_requester_delete on public.alarm_invitations;

create policy alarm_invitations_participant_select
  on public.alarm_invitations for select
  using (auth.uid() = requester_id or auth.uid() = target_id);

create policy alarm_invitations_permitted_insert
  on public.alarm_invitations for insert
  with check (
    auth.uid() = requester_id
    and requester_id <> target_id
    and status = 'pending'
    and exists (
      select 1 from public.alarm_permissions
      where alarm_permissions.requester_id = auth.uid()
        and alarm_permissions.target_id = alarm_invitations.target_id
        and alarm_permissions.status = 'accepted'
    )
  );

create policy alarm_invitations_participant_update
  on public.alarm_invitations for update
  using (auth.uid() = requester_id or auth.uid() = target_id)
  with check (auth.uid() = requester_id or auth.uid() = target_id);

create policy alarm_invitations_requester_delete
  on public.alarm_invitations for delete
  using (auth.uid() = requester_id);

notify pgrst, 'reload schema';
