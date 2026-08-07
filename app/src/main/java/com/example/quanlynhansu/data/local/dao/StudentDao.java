package com.example.quanlynhansu.data.local.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlynhansu.data.local.ClassroomDatabase;
import com.example.quanlynhansu.domain.model.Student;

import java.util.ArrayList;
import java.util.List;

public final class StudentDao {
    private static final String TABLE = "students";

    private final ClassroomDatabase database;

    public StudentDao(ClassroomDatabase database) {
        this.database = database;
    }

    public List<Student> getStudents() {
        List<Student> students = new ArrayList<>();
        try (Cursor cursor = database.getReadableDatabase().rawQuery(
                "SELECT id,code,name,age,scratch_level FROM students ORDER BY name",
                null
        )) {
            while (cursor.moveToNext()) {
                students.add(EntityCursorMapper.mapStudent(cursor, 0));
            }
        }
        return students;
    }

    public long saveStudent(Student student) {
        ContentValues values = new ContentValues();
        values.put("code", student.getCode());
        values.put("name", student.getName());
        values.put("age", student.getAge());
        values.put("scratch_level", student.getScratchLevel());

        SQLiteDatabase writableDatabase = database.getWritableDatabase();
        if (student.getId() < 0) {
            return writableDatabase.insert(TABLE, null, values);
        }
        return writableDatabase.update(
                TABLE,
                values,
                "id=?",
                new String[]{String.valueOf(student.getId())}
        );
    }

    public boolean deleteStudent(long studentId) {
        return database.getWritableDatabase().delete(
                TABLE,
                "id=?",
                new String[]{String.valueOf(studentId)}
        ) > 0;
    }

    public boolean deleteStudents(List<Long> studentIds) {
        return SqliteBatchDelete.deleteByIds(database, TABLE, studentIds);
    }
}
