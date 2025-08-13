create table trips (
  id varchar(36) primary key,
  name varchar(255),
  start_date date,
  end_date date,
  currency varchar(3),
  initial_budget double,
  created_at timestamp,
  updated_at timestamp
);

create table trip_participants (
  trip_id varchar(36),
  participant varchar(255)
);

create table expenses (
  id varchar(36) primary key,
  trip_id varchar(36) not null,
  title varchar(255),
  amount double not null,
  category varchar(255),
  date timestamp,
  paid_by varchar(255),
  created_at timestamp,
  updated_at timestamp
);

create table expense_shared_with (
  expense_id varchar(36),
  participant varchar(255)
);

