package com.example.quanlynhansu.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class ClassroomDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "LopHocLapTrinh.db";
    private static final int DATABASE_VERSION = 3;

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
                "level TEXT NOT NULL)");
        database.execSQL("CREATE TABLE enrollments(" +
                "student_id INTEGER NOT NULL," +
                "course_id INTEGER NOT NULL," +
                "PRIMARY KEY(student_id,course_id)," +
                "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE," +
                "FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE)");
    }

    private void createIndexes(SQLiteDatabase database) {
        database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_enrollments_course_id ON enrollments(course_id)"
        );
    }

    private void seed(SQLiteDatabase database) {
        database.execSQL("INSERT INTO students(code,name,age,scratch_level) VALUES " +
                "('HV001','Nguyễn Minh An',10,'Cơ bản')," +
                "('HV002','Trần Gia Hân',12,'Khá')," +
                "('HV003','Lê Hoàng Nam',13,'Nâng cao')");
        database.execSQL("INSERT INTO courses(code,name,language,level) VALUES " +
                "('KH001','Python cho người mới','Python','Cơ bản')," +
                "('KH002','Sáng tạo game Scratch','Scratch','Cơ bản')");
        database.execSQL("INSERT INTO enrollments(student_id,course_id) VALUES (1,1),(2,1),(3,2)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            createIndexes(database);
        }
    }
}
