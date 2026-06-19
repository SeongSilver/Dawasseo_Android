create or replace function public.validate_alarm_permission_update()
returns trigger as $$
begin
  if old.requester_id <> new.requester_id or old.target_id <> new.target_id then
    raise exception 'alarm permission participants cannot be changed';
  end if;

  if old.status = 'pending'
     and new.status in ('accepted', 'rejected')
     and auth.uid() <> old.target_id then
    raise exception 'only the target can accept or reject an alarm permission request';
  end if;

  return new;
end;
$$ language plpgsql security definer;

drop trigger if exists validate_alarm_permission_update_trigger on public.alarm_permissions;
create trigger validate_alarm_permission_update_trigger
  before update on public.alarm_permissions
  for each row execute procedure public.validate_alarm_permission_update();

drop policy if exists alarm_permissions_participant_all on public.alarm_permissions;
drop policy if exists alarm_permissions_participant_select on public.alarm_permissions;
drop policy if exists alarm_permissions_request_insert on public.alarm_permissions;
drop policy if exists alarm_permissions_participant_update on public.alarm_permissions;
drop policy if exists alarm_permissions_participant_delete on public.alarm_permissions;

create policy alarm_permissions_participant_select
  on public.alarm_permissions for select
  using (auth.uid() = requester_id or auth.uid() = target_id);

create policy alarm_permissions_request_insert
  on public.alarm_permissions for insert
  with check (
    auth.uid() = requester_id
    and requester_id <> target_id
    and status = 'pending'
  );

create policy alarm_permissions_participant_update
  on public.alarm_permissions for update
  using (auth.uid() = requester_id or auth.uid() = target_id)
  with check (auth.uid() = requester_id or auth.uid() = target_id);

create policy alarm_permissions_participant_delete
  on public.alarm_permissions for delete
  using (auth.uid() = requester_id or auth.uid() = target_id);

notify pgrst, 'reload schema';
