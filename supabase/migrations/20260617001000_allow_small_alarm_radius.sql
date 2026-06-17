alter table public.alarms
  drop constraint if exists alarms_radius_km_check;

alter table public.alarms
  add constraint alarms_radius_km_check
  check (radius_km >= 0.01 and radius_km <= 50);

notify pgrst, 'reload schema';

