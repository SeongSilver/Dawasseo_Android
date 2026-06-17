do $$
declare
  constraint_name text;
begin
  for constraint_name in
    select con.conname
    from pg_constraint con
    join pg_class rel on rel.oid = con.conrelid
    join pg_namespace nsp on nsp.oid = rel.relnamespace
    where nsp.nspname = 'public'
      and rel.relname = 'alarms'
      and con.contype = 'c'
      and (
        con.conname in ('alarms_radius_km_check', 'alrams_radius_km_check')
        or pg_get_constraintdef(con.oid) ilike '%radius_km%'
      )
  loop
    execute format('alter table public.alarms drop constraint if exists %I', constraint_name);
  end loop;
end $$;

alter table public.alarms
  add constraint alarms_radius_km_check
  check (radius_km >= 0.01 and radius_km <= 50);

notify pgrst, 'reload schema';
