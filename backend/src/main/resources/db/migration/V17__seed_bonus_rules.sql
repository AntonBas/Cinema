INSERT INTO bonus_rules (bonus_type, points, active)
VALUES ('WELCOME_BONUS', 150, true)
ON CONFLICT (bonus_type) DO NOTHING;

INSERT INTO bonus_rules (bonus_type, points, active)
VALUES ('BIRTHDAY_BONUS', 200, true)
ON CONFLICT (bonus_type) DO NOTHING;

INSERT INTO bonus_rules (bonus_type, min_points_per_transaction, max_points_per_transaction, active)
VALUES ('BOOKING_SPEND', 100, 1000, true)
ON CONFLICT (bonus_type) DO NOTHING;

INSERT INTO bonus_rules (bonus_type, money_ratio, min_points_per_transaction, active)
VALUES ('PAYMENT_ACCRUAL', 0.05, 10, true)
ON CONFLICT (bonus_type) DO NOTHING;

INSERT INTO bonus_rules (bonus_type, active)
VALUES ('REFUND_RETURN', true)
ON CONFLICT (bonus_type) DO NOTHING;
