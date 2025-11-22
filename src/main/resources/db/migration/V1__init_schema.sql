-- Initial schema for healing_backend
-- Creates minimal tables required by the application: doctor, book

CREATE TABLE IF NOT EXISTS doctor (
  id VARCHAR(36) NOT NULL,
  email VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  specialization VARCHAR(255),
  phone_number VARCHAR(50),
  about TEXT,
  location_latitude DOUBLE,
  location_longitude DOUBLE,
  verified BOOLEAN DEFAULT FALSE,
  rating DOUBLE DEFAULT 0,
  reviews_count INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_doctor_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS book (
  id VARCHAR(36) NOT NULL,
  doctor_id VARCHAR(36) NOT NULL,
  email_user VARCHAR(255),
  phone_number_user VARCHAR(50),
  name_user VARCHAR(255),
  appointment_time DATETIME,
  status VARCHAR(50) DEFAULT 'PENDING',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_booking_doctor_id (doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index for geo queries (simple composite index)
CREATE INDEX IF NOT EXISTS idx_doctor_location ON doctor (location_latitude, location_longitude);

COMMIT;
