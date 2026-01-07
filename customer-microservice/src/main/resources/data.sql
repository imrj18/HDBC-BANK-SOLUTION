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
          UNHEX(REPLACE('c0a8015e-7a2b-4c4f-9a01-44bafedb61e2', '-', '')),
          'Ritik Sharma',
          'ritikS@example.com',
          '9876543210',
          '123412341234',

          'ACTIVE',
          '$2a$12$WlLSd1kkwie9tgE8To7gO.SBUZFrG7PRI3B6T0aWENr92wPdhTJ56', -- bcrypt dummy //R@123
          NOW(),
          NOW(),
          "HIG-259,Kunti Vihar, Shanti Nagar, Bhilai, Durg, Chhattisgarh"
      ),
      (
          UNHEX(REPLACE('9d6b8a7f-3c2e-4b0d-9a1f-7f2c5d4f1a9b', '-', '')),
          'Ritik Sharma',
          'ritik1@example.com',
          '9123456789',
          '567856785678',
          'ACTIVE',
          '$2a$12$WlLSd1kkwie9tgE8To7gO.SBUZFrG7PRI3B6T0aWENr92wPdhTJ56',
          NOW(),
          NOW(),
          "HIG-259,Kunti Vihar, Shanti Nagar, Bhilai, Durg, Chhattisgarh"
      ),
      (
          UNHEX(REPLACE('9d6b8a7f-3c2e-4b0d-9a1f-7e2c5d4f1a9b', '-', '')),
          'Amit Sharma',
          'amit@example.com',
          '9123456779',
          '567856785679',
          'ACTIVE',
          '$2a$10$D86mddrmfdbGVH5HK4t2B.xrajz2ISl.G24JAMgrzH2pg6aCoyMeW',
          NOW(),
          NOW(),
          "HIG-269,Kunti Vihar, Shanti Nagar, Bhilai, Durg, Chhattisgarh"
      );
