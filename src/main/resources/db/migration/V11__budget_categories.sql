CREATE TABLE IF NOT EXISTS budget_categories (
    id                varchar(36) PRIMARY KEY,
    name              varchar(255) NOT NULL,
    type              varchar(20)  NOT NULL,
    planned_amount    numeric(12,2) NOT NULL DEFAULT 0,
    actual_amount     numeric(12,2) NOT NULL DEFAULT 0,
    display_order     int,
    parent_id         varchar(36),
    monthly_budget_id varchar(36) NOT NULL,
    CONSTRAINT fk_budget_category_budget
        FOREIGN KEY (monthly_budget_id) REFERENCES budgets(id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_category_parent
        FOREIGN KEY (parent_id) REFERENCES budget_categories(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_budget_category_budget ON budget_categories(monthly_budget_id);
CREATE INDEX IF NOT EXISTS idx_budget_category_parent ON budget_categories(parent_id);
