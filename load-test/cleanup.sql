DELETE FROM seat_reservations
WHERE booking_id IS NULL
  AND user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest-%@test.com');

DELETE FROM users
WHERE email LIKE 'loadtest-%@test.com';
