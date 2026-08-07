package com.example.quanlynhansu.data.local.dao;

import android.database.Cursor;

import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.Student;

final class EntityCursorMapper {
    private EntityCursorMapper() {
    }

    static Student mapStudent(Cursor cursor, int offset) {
        return new Student(
                cursor.getLong(offset),
                cursor.getString(offset + 1),
                cursor.getString(offset + 2),
                cursor.getInt(offset + 3),
                cursor.getString(offset + 4)
        );
    }

    static Course mapCourse(Cursor cursor, int offset) {
        return new Course(
                cursor.getLong(offset),
                cursor.getString(offset + 1),
                cursor.getString(offset + 2),
                cursor.getString(offset + 3),
                cursor.getString(offset + 4),
                cursor.getString(offset + 5),
                cursor.getString(offset + 6)
        );
    }
}
