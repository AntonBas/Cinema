ALTER TABLE bonus_transactions
    ADD COLUMN balance_after INTEGER;

UPDATE bonus_transactions bt
SET balance_after = sub.balance_after
FROM (
    SELECT id,
           SUM(points_change) OVER (
               PARTITION BY bonus_card_id
               ORDER BY id
               ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
           ) AS balance_after
    FROM bonus_transactions
) sub
WHERE bt.id = sub.id;

ALTER TABLE bonus_transactions
    ALTER COLUMN balance_after SET NOT NULL;
