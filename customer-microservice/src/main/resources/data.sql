INSERT INTO customers (
    customer_id,
    name,
    email,
    phone,
    aadhar,
    customer_status,
    password_hash,
    created_at,
    updated_at,
    address
) VALUES
      (
          UNHEX(REPLACE('11111111-1111-1111-1111-111111111111', '-', '')),
          'Demo User One',
          'user1@example.com',
          '9000000001',
          '111122223333',
          'ACTIVE',
          '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiF0o8aE4fP6Z6FzYFJqZcZ5tR0ZpLa', --//123456
          NOW(),
          NOW(),
          'Demo Address Line 1, Demo City, Demo State'
      ),
      (
          UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')),
          'Demo User Two',
          'user2@example.com',
          '9000000002',
          '444455556666',
          'ACTIVE',
          '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiF0o8aE4fP6Z6FzYFJqZcZ5tR0ZpLa',
          NOW(),
          NOW(),
          'Demo Address Line 2, Demo City, Demo State'
      ),
      (
          UNHEX(REPLACE('33333333-3333-3333-3333-333333333333', '-', '')),
          'Demo User Three',
          'user3@example.com',
          '9000000003',
          '777788889999',
          'ACTIVE',
          '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiF0o8aE4fP6Z6FzYFJqZcZ5tR0ZpLa',
          NOW(),
          NOW(),
          'Demo Address Line 3, Demo City, Demo State'
      );
