create extension if not exists "pgcrypto";

create table if not exists attendees (
    id uuid primary key default gen_random_uuid(),
    name varchar(200) not null unique,
    entry_scanned boolean not null default false,
    food_scanned boolean not null default false,
    gift_scanned boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create or replace function set_updated_at()
returns trigger as $$
begin
  new.updated_at = now();
  return new;
end;
$$ language plpgsql;

drop trigger if exists trg_attendees_updated_at on attendees;
create trigger trg_attendees_updated_at
before update on attendees
for each row execute function set_updated_at();
