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

-- budgets
CREATE TABLE IF NOT EXISTS budgets (
                                       id                      varchar(36) PRIMARY KEY,
    kind                    varchar(16) NOT NULL,      -- 'monthly' | 'trip'
    currency                varchar(8)  NOT NULL,
    amount                  numeric(12,2) NOT NULL,
    year                    int,
    month                   int,
    trip_id                 varchar(64),
    name                    text,
    linked_monthly_budget_id varchar(36)
    );
CREATE INDEX IF NOT EXISTS idx_budgets_kind ON budgets(kind);

-- settlements (for "Settle Up")
CREATE TABLE IF NOT EXISTS settlements (
                                           id         varchar(36) PRIMARY KEY,
    trip_id    varchar(64) NOT NULL,
    payer      varchar(128) NOT NULL,
    payee      varchar(128) NOT NULL,
    currency   varchar(8) NOT NULL,
    amount     numeric(12,2) NOT NULL,
    note       text,
    created_at timestamp NOT NULL DEFAULT now()
    );

