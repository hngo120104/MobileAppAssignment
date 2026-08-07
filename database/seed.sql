PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS students(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    age INTEGER NOT NULL CHECK(age BETWEEN 5 AND 18),
    scratch_level TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS courses(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    language TEXT NOT NULL CHECK(language IN ('Scratch','Python')),
    level TEXT NOT NULL,
    start_date TEXT NOT NULL,
    end_date TEXT NOT NULL,
    CHECK(start_date<=end_date)
);

CREATE TABLE IF NOT EXISTS enrollments(
    student_id INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    enrolled_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(student_id,course_id),
    FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS index_enrollments_course_id
ON enrollments(course_id);

CREATE INDEX IF NOT EXISTS index_enrollments_enrolled_at
ON enrollments(enrolled_at);

INSERT OR IGNORE INTO students(code,name,age,scratch_level) VALUES
    ('HV001','Nguyễn Minh An',10,'Cơ bản'),
    ('HV002','Trần Gia Hân',12,'Khá'),
    ('HV003','Lê Hoàng Nam',13,'Nâng cao'),
    ('HV004','Phạm Khánh Linh',11,'Cơ bản'),
    ('HV005','Võ Đức Anh',10,'Chưa học'),
    ('HV006','Bùi Ngọc Mai',9,'Khá'),
    ('HV007','Đặng Quốc Bảo',14,'Nâng cao'),
    ('HV008','Hoàng Tú Uyên',8,'Cơ bản'),
    ('HV009','Đỗ Minh Khang',12,'Khá'),
    ('HV010','Lý Thanh Trúc',15,'Nâng cao'),
    ('HV011','Ngô Hải Đăng',11,'Cơ bản'),
    ('HV012','Trương Bảo Ngọc',7,'Chưa học');

INSERT OR IGNORE INTO courses(code,name,language,level,start_date,end_date) VALUES
    ('KH001','Python cho người mới','Python','Cơ bản','2026-06-01','2026-08-31'),
    ('KH002','Sáng tạo game Scratch','Scratch','Cơ bản','2026-06-15','2026-09-15'),
    ('KH003','Python ứng dụng','Python','Trung cấp','2026-07-01','2026-10-01'),
    ('KH004','Scratch nâng cao','Scratch','Nâng cao','2026-07-15','2026-10-15'),
    ('KH005','Thuật toán với Python','Python','Nâng cao','2026-08-01','2026-11-01'),
    ('KH006','Thiết kế hoạt hình Scratch','Scratch','Trung cấp','2026-08-15','2026-11-15');

INSERT OR IGNORE INTO enrollments(student_id,course_id,enrolled_at)
SELECT s.id,c.id,'2026-06-02 08:00:00' FROM students s,courses c
WHERE s.code='HV001' AND c.code='KH001';
INSERT OR IGNORE INTO enrollments(student_id,course_id,enrolled_at)
SELECT s.id,c.id,'2026-06-03 09:00:00' FROM students s,courses c
WHERE s.code='HV002' AND c.code='KH001';
INSERT OR IGNORE INTO enrollments(student_id,course_id,enrolled_at)
SELECT s.id,c.id,'2026-06-04 10:00:00' FROM students s,courses c
WHERE s.code='HV004' AND c.code='KH001';
INSERT OR IGNORE INTO enrollments(student_id,course_id,enrolled_at)
SELECT s.id,c.id,'2026-06-05 11:00:00' FROM students s,courses c
WHERE s.code='HV005' AND c.code='KH001';
INSERT OR IGNORE INTO enrollments(student_id,course_id,enrolled_at)
SELECT s.id,c.id,'2026-06-16 08:30:00' FROM students s,courses c
WHERE s.code='HV003' AND c.code='KH002';
INSERT OR IGNORE INTO enrollments(student_id,course_id,enrolled_at)
SELECT s.id,c.id,'2026-06-17 09:30:00' FROM students s,courses c
WHERE s.code='HV006' AND c.code='KH002';
INSERT OR IGNORE INTO enrollments(student_id,course_id,enrolled_at)
SELECT s.id,c.id,'2026-06-18 10:30:00' FROM students s,courses c
WHERE s.code='HV008' AND c.code='KH002';
INSERT OR IGNORE INTO enrollments(student_id,course_id,enrolled_at)
SELECT s.id,c.id,'2026-07-02 08:00:00' FROM students s,courses c
WHERE s.code='HV009' AND c.code='KH003';
INSERT OR IGNORE INTO enrollments(student_id,course_id,enrolled_at)
SELECT s.id,c.id,'2026-07-03 09:00:00' FROM students s,courses c
WHERE s.code='HV011' AND c.code='KH003';
INSERT OR IGNORE INTO enrollments(student_id,course_id,enrolled_at)
SELECT s.id,c.id,'2026-07-16 08:00:00' FROM students s,courses c
WHERE s.code='HV007' AND c.code='KH004';
INSERT OR IGNORE INTO enrollments(student_id,course_id,enrolled_at)
SELECT s.id,c.id,'2026-08-02 08:00:00' FROM students s,courses c
WHERE s.code='HV010' AND c.code='KH005';
INSERT OR IGNORE INTO enrollments(student_id,course_id,enrolled_at)
SELECT s.id,c.id,'2026-08-16 08:00:00' FROM students s,courses c
WHERE s.code='HV012' AND c.code='KH006';
INSERT OR IGNORE INTO enrollments(student_id,course_id,enrolled_at)
SELECT s.id,c.id,'2026-08-17 08:00:00' FROM students s,courses c
WHERE s.code='HV001' AND c.code='KH006';

PRAGMA user_version = 5;

COMMIT;
