INSERT INTO users (email, password, first_name, last_name, date_of_birth, city, phone_number, user_role, enabled,
                   verification_status)
VALUES ('admin@test.com', '$2a$12$dNzLycNvxuVgn/AMAWL9TOQJp9wkRUVs2I0uWmZNM9g6ZORPpYypC', 'Admin', 'User', '2001-08-21',
        'Lviv', '+380961794123', 'ROLE_ADMIN', true, 'VERIFIED'),
       ('cashier@test.com', '$2a$12$Y1HYsURTy3el7DfYwAThVO9jK.aEw3zVpEyk9mP6bRSgSgJMo4sN2', 'Cashier', 'User',
        '2001-08-21', 'Lviv', '+380961791111', 'ROLE_CASHIER', true, 'VERIFIED'),
       ('manager@test.com', '$2a$12$77TemLwSq2I8s.o.wX/UYeCbWyR8nTsb1VZUQIz1QSa1AcOrbHTOO', 'Content', 'Manager',
        '2001-08-21', 'Lviv', '+380961794111', 'ROLE_CONTENT_MANAGER', true, 'VERIFIED'),
       ('user@test.com', '$2a$12$QLK.lKjq8abk2Hgq6BZcn.u23We0eM2Y1Ir/QvxN6IDX3Ybz3iRHK', 'Test', 'User', '2001-08-21',
        'Lviv', '+380961794111', 'ROLE_USER', true, 'VERIFIED');

INSERT INTO bonus_cards (user_id, points_balance, welcome_bonus_received)
SELECT id, 500, true
FROM users
WHERE email = 'admin@test.com'
UNION ALL
SELECT id, 300, true
FROM users
WHERE email = 'cashier@test.com'
UNION ALL
SELECT id, 200, true
FROM users
WHERE email = 'manager@test.com'
UNION ALL
SELECT id, 100, true
FROM users
WHERE email = 'user@test.com';