SELECT 'double_bookings' AS check_name, count(*) AS violations
FROM (SELECT session_id, seat_id
      FROM seat_reservations
      WHERE status IN ('PENDING', 'CONFIRMED')
        AND session_id IN (SELECT DISTINCT session_id
                           FROM seat_reservations
                           WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest-%@test.com'))
      GROUP BY session_id, seat_id
      HAVING count(*) > 1) AS duplicates;
