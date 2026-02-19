CREATE DATABASE IF NOT EXISTS bank_db;
CREATE DATABASE IF NOT EXISTS customer_db;

CREATE USER IF NOT EXISTS 'hdbc_user'@'%' IDENTIFIED BY 'hdbc_pass';

GRANT ALL PRIVILEGES ON bank_db.* TO 'hdbc_user'@'%';
GRANT ALL PRIVILEGES ON customer_db.* TO 'hdbc_user'@'%';

FLUSH PRIVILEGES;
