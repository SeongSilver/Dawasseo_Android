-- Arrival and alarm-trigger state must stay on the owner's device only.
-- The server stores alarm configuration/invitation state, not arrival state.
alter table public.alarms
  drop column if exists triggered_at;

notify pgrst, 'reload schema';
