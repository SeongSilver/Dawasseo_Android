alter table public.alarms
  add column if not exists sound_type text not null default 'default'
    check (sound_type in ('default', 'custom'));

alter table public.alarms
  add column if not exists sound_uri text;

notify pgrst, 'reload schema';

