UPDATE users u
SET email = lower(u.email)
WHERE u.email <> lower(u.email)
  AND NOT EXISTS (SELECT 1
                  FROM users other
                  WHERE other.id <> u.id
                    AND lower(other.email) = lower(u.email));

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email_lower ON users (lower(email));
