INSERT INTO users (email, password, first_name, last_name, date_of_birth, city, phone_number, user_role, enabled,
                   email_verified, verification_status)
SELECT 'loadtest-' || n || '@test.com',
       '$2a$12$QLK.lKjq8abk2Hgq6BZcn.u23We0eM2Y1Ir/QvxN6IDX3Ybz3iRHK',
       'Load',
       'Test' || n,
       '2000-01-01',
       'Lviv',
       '+38099' || lpad(n::text, 7, '0'),
       'ROLE_USER',
       true,
       true,
       'VERIFIED'
FROM generate_series(1, 500) AS n
ON CONFLICT (email) DO NOTHING;
