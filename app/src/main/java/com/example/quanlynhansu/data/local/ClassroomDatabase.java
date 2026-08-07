package com.example.quanlynhansu.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class ClassroomDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "LopHocLapTrinh.db";
    private static final int DATABASE_VERSION = 5;

    public ClassroomDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase database) {
        super.onConfigure(database);
        database.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        createTables(database);
        createIndexes(database);
        seed(database);
    }

    private void createTables(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE students(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "code TEXT NOT NULL UNIQUE," +
                "name TEXT NOT NULL," +
                "age INTEGER NOT NULL CHECK(age BETWEEN 5 AND 18)," +
                "scratch_level TEXT NOT NULL)");
        database.execSQL("CREATE TABLE courses(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "code TEXT NOT NULL UNIQUE," +
                "name TEXT NOT NULL," +
                "language TEXT NOT NULL CHECK(language IN ('Scratch','Python'))," +
                "level TEXT NOT NULL," +
                "start_date TEXT NOT NULL," +
                "end_date TEXT NOT NULL," +
                "CHECK(start_date<=end_date))");
        database.execSQL("CREATE TABLE enrollments(" +
                "student_id INTEGER NOT NULL," +
                "course_id INTEGER NOT NULL," +
                "enrolled_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY(student_id,course_id)," +
                "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE," +
                "FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE)");
    }

    private void createIndexes(SQLiteDatabase database) {
        database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_enrollments_course_id ON enrollments(course_id)"
        );
        database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_enrollments_enrolled_at " +
                        "ON enrollments(enrolled_at)"
        );
    }

    private void seed(SQLiteDatabase database) {
        database.execSQL("INSERT OR IGNORE INTO students(code,name,age,scratch_level) VALUES " +
                "('HV001','Nguyễn Minh An',10,'Cơ bản')," +
                "('HV002','Trần Gia Hân',12,'Khá')," +
                "('HV003','Lê Hoàng Nam',13,'Nâng cao')," +
                "('HV004','Phạm Khánh Linh',11,'Cơ bản')," +
                "('HV005','Võ Đức Anh',10,'Chưa học')," +
                "('HV006','Bùi Ngọc Mai',9,'Khá')," +
                "('HV007','Đặng Quốc Bảo',14,'Nâng cao')," +
                "('HV008','Hoàng Tú Uyên',8,'Cơ bản')," +
                "('HV009','Đỗ Minh Khang',12,'Khá')," +
                "('HV010','Lý Thanh Trúc',15,'Nâng cao')," +
                "('HV011','Ngô Hải Đăng',11,'Cơ bản')," +
                "('HV012','Trương Bảo Ngọc',7,'Chưa học')");
        database.execSQL("INSERT OR IGNORE INTO courses(" +
                "code,name,language,level,start_date,end_date) VALUES " +
                "('KH001','Python cho người mới','Python','Cơ bản','2026-06-01','2026-08-31')," +
                "('KH002','Sáng tạo game Scratch','Scratch','Cơ bản','2026-06-15','2026-09-15')," +
                "('KH003','Python ứng dụng','Python','Trung cấp','2026-07-01','2026-10-01')," +
                "('KH004','Scratch nâng cao','Scratch','Nâng cao','2026-07-15','2026-10-15')," +
                "('KH005','Thuật toán với Python','Python','Nâng cao','2026-08-01','2026-11-01')," +
                "('KH006','Thiết kế hoạt hình Scratch','Scratch','Trung cấp','2026-08-15','2026-11-15')");
        seedEnrollment(database, "HV001", "KH001", "2026-06-02 08:00:00");
        seedEnrollment(database, "HV002", "KH001", "2026-06-03 09:00:00");
        seedEnrollment(database, "HV004", "KH001", "2026-06-04 10:00:00");
        seedEnrollment(database, "HV005", "KH001", "2026-06-05 11:00:00");
        seedEnrollment(database, "HV003", "KH002", "2026-06-16 08:30:00");
        seedEnrollment(database, "HV006", "KH002", "2026-06-17 09:30:00");
        seedEnrollment(database, "HV008", "KH002", "2026-06-18 10:30:00");
        seedEnrollment(database, "HV009", "KH003", "2026-07-02 08:00:00");
        seedEnrollment(database, "HV011", "KH003", "2026-07-03 09:00:00");
        seedEnrollment(database, "HV007", "KH004", "2026-07-16 08:00:00");
        seedEnrollment(database, "HV010", "KH005", "2026-08-02 08:00:00");
        seedEnrollment(database, "HV012", "KH006", "2026-08-16 08:00:00");
        seedEnrollment(database, "HV001", "KH006", "2026-08-17 08:00:00");
    }

    private void seedEnrollment(
            SQLiteDatabase database,
            String studentCode,
            String courseCode,
            String enrolledAt
    ) {
        database.execSQL(
                "INSERT OR IGNORE INTO enrollments(student_id,course_id,enrolled_at) " +
                        "SELECT s.id,c.id,? FROM students s,courses c " +
                        "WHERE s.code=? AND c.code=?",
                new Object[]{enrolledAt, studentCode, courseCode}
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            database.execSQL(
                    "ALTER TABLE courses ADD COLUMN " +
                            "start_date TEXT NOT NULL DEFAULT '2026-01-01'"
            );
            database.execSQL(
                    "ALTER TABLE courses ADD COLUMN " +
                            "end_date TEXT NOT NULL DEFAULT '2026-12-31'"
            );
            database.execSQL(
                    "ALTER TABLE enrollments ADD COLUMN enrolled_at TEXT NOT NULL " +
                            "DEFAULT '2026-01-01 00:00:00'"
            );
        }
        createIndexes(database);
        if (oldVersion < 4) {
            seed(database);
        }
    }
}
