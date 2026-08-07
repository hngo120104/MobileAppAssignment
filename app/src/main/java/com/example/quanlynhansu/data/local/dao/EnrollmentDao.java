package com.example.quanlynhansu.data.local.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.quanlynhansu.data.local.ClassroomDatabase;
import com.example.quanlynhansu.domain.model.CourseSummary;
import com.example.quanlynhansu.domain.model.Student;

import java.util.ArrayList;
import java.util.List;

public final class EnrollmentDao {
    private static final String TABLE = "enrollments";

    private final ClassroomDatabase database;

    public EnrollmentDao(ClassroomDatabase database) {
        this.database = database;
    }

    public List<CourseSummary> getCourseSummaries() {
        List<CourseSummary> summaries = new ArrayList<>();
        String sql = "SELECT c.id,c.code,c.name,c.language,c.level," +
                "c.start_date,c.end_date,COUNT(e.student_id) " +
                "FROM courses c " +
                "LEFT JOIN enrollments e ON e.course_id=c.id " +
                "GROUP BY c.id,c.code,c.name,c.language,c.level,c.start_date,c.end_date " +
                "ORDER BY c.name";
        try (Cursor cursor = database.getReadableDatabase().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                summaries.add(new CourseSummary(
                        EntityCursorMapper.mapCourse(cursor, 0),
                        cursor.getInt(7)
                ));
            }
        }
        return summaries;
    }

    public boolean enrollStudents(List<Long> studentIds, long courseId) {
        if (studentIds.isEmpty()) {
            return false;
        }

        SQLiteDatabase writableDatabase = database.getWritableDatabase();
        writableDatabase.beginTransaction();
        try {
            for (long studentId : studentIds) {
                ContentValues values = new ContentValues();
                values.put("student_id", studentId);
                values.put("course_id", courseId);
                writableDatabase.insertOrThrow(TABLE, null, values);
            }
            writableDatabase.setTransactionSuccessful();
            return true;
        } catch (SQLiteException exception) {
            return false;
        } finally {
            writableDatabase.endTransaction();
        }
    }

    public boolean unenrollStudents(List<Long> studentIds, long courseId) {
        if (studentIds.isEmpty()) {
            return false;
        }

        SQLiteDatabase writableDatabase = database.getWritableDatabase();
        writableDatabase.beginTransaction();
        try {
            for (long studentId : studentIds) {
                int deleted = writableDatabase.delete(
                        TABLE,
                        "student_id=? AND course_id=?",
                        new String[]{String.valueOf(studentId), String.valueOf(courseId)}
                );
                if (deleted != 1) {
                    throw new SQLiteException("Enrollment no longer exists");
                }
            }
            writableDatabase.setTransactionSuccessful();
            return true;
        } catch (SQLiteException exception) {
            return false;
        } finally {
            writableDatabase.endTransaction();
        }
    }

    public List<Student> getStudentsByCourse(long courseId) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.id,s.code,s.name,s.age,s.scratch_level " +
                "FROM students s JOIN enrollments e ON e.student_id=s.id " +
                "WHERE e.course_id=? ORDER BY s.name";
        try (Cursor cursor = database.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(courseId)}
        )) {
            while (cursor.moveToNext()) {
                students.add(EntityCursorMapper.mapStudent(cursor, 0));
            }
        }
        return students;
    }
}
