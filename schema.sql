-- ============================================================================
-- library-api: PostgreSQL Database Schema & Initial Seed Data
-- ============================================================================

-- 1. Database Creation (Run as postgres superuser before connecting to library_db)
-- CREATE DATABASE library_db;
-- \c library_db;

-- Clean existing tables if resetting the environment
DROP TABLE IF EXISTS loan_details CASCADE;
DROP TABLE IF EXISTS loans CASCADE;
DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS members CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================================
-- 2. Table Definitions (DDL)
-- ============================================================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(150) NOT NULL,
    total_copies INTEGER NOT NULL CHECK (total_copies >= 0),
    available_copies INTEGER NOT NULL CHECK (available_copies >= 0 AND available_copies <= total_copies),
    late_fee_per_day NUMERIC(10, 2) NOT NULL DEFAULT 1.50 CHECK (late_fee_per_day >= 0),
    category_id BIGINT NOT NULL,
    CONSTRAINT fk_books_category FOREIGN KEY (category_id)
        REFERENCES categories (id) ON DELETE RESTRICT
);

CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY,
    member_code VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(25),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    registered_at DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    loan_date DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date DATE NOT NULL,
    return_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    estimated_fee NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_loans_member FOREIGN KEY (member_id)
        REFERENCES members (id) ON DELETE RESTRICT,
    CONSTRAINT chk_loans_dates CHECK (due_date >= loan_date),
    CONSTRAINT chk_loans_status CHECK (status IN ('ACTIVE', 'RETURNED', 'OVERDUE'))
);

CREATE TABLE loan_details (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    daily_fee_snapshot NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_loan_details_loan FOREIGN KEY (loan_id)
        REFERENCES loans (id) ON DELETE CASCADE,
    CONSTRAINT fk_loan_details_book FOREIGN KEY (book_id)
        REFERENCES books (id) ON DELETE RESTRICT,
    CONSTRAINT uq_loan_book UNIQUE (loan_id, book_id)
);

-- ============================================================================
-- 3. Indexes for Query Performance
-- ============================================================================

CREATE INDEX idx_books_category_id ON books (category_id);
CREATE INDEX idx_loans_member_id ON loans (member_id);
CREATE INDEX idx_loans_status ON loans (status);
CREATE INDEX idx_loan_details_loan_id ON loan_details (loan_id);
CREATE INDEX idx_loan_details_book_id ON loan_details (book_id);

-- ============================================================================
-- 4. Seed Data (Initial Test Records)
-- ============================================================================

-- Note: Passwords should be registered via POST /api/auth/register so BCrypt hashes match your encoder.
-- Placeholder BCrypt hashes below represent 'LibraryPass123!'
INSERT INTO users (username, password, role) VALUES
    ('admin_demo', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_ADMIN'),
    ('user_demo',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER');

INSERT INTO categories (name, description) VALUES
    ('Software Engineering', 'Architecture, design patterns, clean code, and testing practices'),
    ('Databases', 'Relational modeling, SQL optimization, and distributed storage systems'),
    ('Computer Science', 'Algorithms, data structures, operating systems, and networking');

INSERT INTO books (isbn, title, author, total_copies, available_copies, late_fee_per_day, category_id) VALUES
    ('978-0132350884', 'Clean Code', 'Robert C. Martin', 5, 4, 2.00, 1),
    ('978-0134685991', 'Effective Java', 'Joshua Bloch', 4, 3, 2.50, 1),
    ('978-1449373320', 'Designing Data-Intensive Applications', 'Martin Kleppmann', 3, 3, 3.00, 2),
    ('978-0262046305', 'Introduction to Algorithms', 'Thomas H. Cormen', 6, 6, 2.50, 3);

INSERT INTO members (member_code, full_name, email, phone, active, registered_at) VALUES
    ('MEM-2026-001', 'Carlos Mendoza', 'carlos.mendoza@example.com', '+52-999-123-4567', TRUE, '2026-09-15'),
    ('MEM-2026-002', 'Sofía Herrera', 'sofia.herrera@example.com', '+52-999-234-5678', TRUE, '2026-10-02'),
    ('MEM-2026-003', 'Luis Ramírez', 'luis.ramirez@example.com', '+52-999-345-6789', FALSE, '2026-08-20');

-- Sample Master-Detail Active Loan (reflects 1 copy borrowed of Clean Code and Effective Java)
INSERT INTO loans (member_id, loan_date, due_date, return_date, status, estimated_fee) VALUES
    (1, '2026-11-20', '2026-11-27', NULL, 'ACTIVE', 31.50);

INSERT INTO loan_details (loan_id, book_id, quantity, daily_fee_snapshot) VALUES
    (1, 1, 1, 2.00),
    (1, 2, 1, 2.50);