-- trips
CREATE TABLE IF NOT EXISTS trips (
                                     id              varchar(36) PRIMARY KEY,
    name            varchar(255) NOT NULL,
    start_date      date,
    end_date        date,
    currency        varchar(3) NOT NULL,
    initial_budget  numeric(12,2) DEFAULT 0,
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now()
    );

-- participants in a trip
CREATE TABLE IF NOT EXISTS trip_participants (
                                                 trip_id     varchar(36) NOT NULL,
    participant varchar(255) NOT NULL,
    PRIMARY KEY (trip_id, participant),
    CONSTRAINT fk_tp_trip
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
    );
CREATE INDEX IF NOT EXISTS idx_tp_trip ON trip_participants(trip_id);

-- expenses
CREATE TABLE IF NOT EXISTS expenses (
                                        id          varchar(36) PRIMARY KEY,
    trip_id     varchar(36) NOT NULL,
    title       varchar(255),
    amount      numeric(12,2) NOT NULL,
    category    varchar(255),
    date        timestamptz,
    paid_by     varchar(255),
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_exp_trip
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
    );
CREATE INDEX IF NOT EXISTS idx_exp_trip ON expenses(trip_id);

-- expense split
CREATE TABLE IF NOT EXISTS expense_shared_with (
                                                   expense_id  varchar(36) NOT NULL,
    participant varchar(255) NOT NULL,
    PRIMARY KEY (expense_id, participant),
    CONSTRAINT fk_esw_exp
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE
    );
CREATE INDEX IF NOT EXISTS idx_esw_expense ON expense_shared_with(expense_id);

-- budgets
CREATE TABLE IF NOT EXISTS budgets (
                                       id                        varchar(36) PRIMARY KEY,
    kind                      varchar(16)  NOT NULL,        -- 'monthly' | 'trip'
    currency                  varchar(3)   NOT NULL,
    amount                    numeric(12,2) NOT NULL,
    year                      int,
    month                     int,
    trip_id                   varchar(36),
    name                      text,
    linked_monthly_budget_id  varchar(36),
    CONSTRAINT fk_budget_trip
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE SET NULL
    );
CREATE INDEX IF NOT EXISTS idx_budgets_kind    ON budgets(kind);
CREATE INDEX IF NOT EXISTS idx_budgets_trip_id ON budgets(trip_id);

-- settlements (for "Settle Up")
CREATE TABLE IF NOT EXISTS settlements (
                                           id         varchar(36) PRIMARY KEY,
    trip_id    varchar(36) NOT NULL,
    payer      varchar(128) NOT NULL,
    payee      varchar(128) NOT NULL,
    currency   varchar(3)   NOT NULL,
    amount     numeric(12,2) NOT NULL,
    note       text,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_settle_trip
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
    );
CREATE INDEX IF NOT EXISTS idx_settlements_trip_id ON settlements(trip_id);
