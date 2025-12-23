INSERT INTO customers (
    customer_id,
    name,
    email,
    phone,
    aadhar,
    bank_id,
    bank_status,
    password_hash,
    created_at,
    updated_at
) VALUES
      (
          UNHEX(REPLACE('c0a8015e-7a2b-4c4f-9a01-44bafedb61e2', '-', '')),
          'Ritik Jani',
          'ritik@example.com',
          '9876543210',
          '123412341234',
          1,
          'ACTIVE',
          '$2a$12$WlLSd1kkwie9tgE8To7gO.SBUZFrG7PRI3B6T0aWENr92wPdhTJ56', -- bcrypt dummy //R@123
          NOW(),
          NOW()
      ),
      (
          UNHEX(REPLACE('9d6b8a7f-3c2e-4b0d-9a1f-7f2c5d4f1a9b', '-', '')),
          'Ritik Sharma',
          'ritik@example.com',
          '9123456789',
          '567856785678',
          2,
          'ACTIVE',
          '$2a$12$WlLSd1kkwie9tgE8To7gO.SBUZFrG7PRI3B6T0aWENr92wPdhTJ56',
          NOW(),
          NOW()
      ),
      (
          UNHEX(REPLACE('9d6b8a7f-3c2e-4b0d-9a1f-7e2c5d4f1a9b', '-', '')),
          'Amit Sharma',
          'amit@example.com',
          '9123456789',
          '567856785678',
          1,
          'ACTIVE',
          '$2a$10$D86mddrmfdbGVH5HK4t2B.xrajz2ISl.G24JAMgrzH2pg6aCoyMeW',
          NOW(),
          NOW()
      );
