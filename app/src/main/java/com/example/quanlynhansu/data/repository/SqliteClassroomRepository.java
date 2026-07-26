package com.example.quanlynhansu.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.quanlynhansu.data.local.ClassroomDatabase;
import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.CourseSummary;
import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.repository.ClassroomRepository;

import java.util.ArrayList;
import java.util.List;

public final class SqliteClassroomRepository implements ClassroomRepository {
    private final ClassroomDatabase database;

    public SqliteClassroomRepository(ClassroomDatabase database) {
        this.database = database;
    }

    @Override
    public List<Student> getStudents() {
        return queryStudents("SELECT id,code,name,age,scratch_level FROM students ORDER BY name", null);
    }

    @Override
    public long saveStudent(Student student) {
        ContentValues values = new ContentValues();
        values.put("code", student.getCode());
        values.put("name", student.getName());
        values.put("age", student.getAge());
        values.put("scratch_level", student.getScratchLevel());
        SQLiteDatabase writableDatabase = database.getWritableDatabase();
        if (student.getId() < 0) {
            return writableDatabase.insert("students", null, values);
        }
        return writableDatabase.update(
                "students", values, "id=?", new String[]{String.valueOf(student.getId())}
        );
    }

    @Override
    public boolean deleteStudent(long studentId) {
        return database.getWritableDatabase().delete(
                "students", "id=?", new String[]{String.valueOf(studentId)}
        ) > 0;
    }

    @Override
    public List<Course> getCourses() {
        List<Course> courses = new ArrayList<>();
        try (Cursor cursor = database.getReadableDatabase().rawQuery(
                "SELECT id,code,name,language,level FROM courses ORDER BY name", null)) {
            while (cursor.moveToNext()) {
                courses.add(mapCourse(cursor, 0));
            }
        }
        return courses;
    }

    @Override
    public List<CourseSummary> getCourseSummaries() {
        List<CourseSummary> summaries = new ArrayList<>();
        String sql = "SELECT c.id,c.code,c.name,c.language,c.level,COUNT(e.student_id) " +
                "FROM courses c " +
                "LEFT JOIN enrollments e ON e.course_id=c.id " +
                "GROUP BY c.id,c.code,c.name,c.language,c.level " +
                "ORDER BY c.name";
        try (Cursor cursor = database.getReadableDatabase().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                summaries.add(new CourseSummary(mapCourse(cursor, 0), cursor.getInt(5)));
            }
        }
        return summaries;
    }

    @Override
    public long saveCourse(Course course) {
        ContentValues values = new ContentValues();
        values.put("code", course.getCode());
        values.put("name", course.getName());
        values.put("language", course.getLanguage());
        values.put("level", course.getLevel());
        SQLiteDatabase writableDatabase = database.getWritableDatabase();
        if (course.getId() < 0) {
            return writableDatabase.insert("courses", null, values);
        }
        return writableDatabase.update(
                "courses", values, "id=?", new String[]{String.valueOf(course.getId())}
        );
    }

    @Override
    public boolean deleteCourse(long courseId) {
        return database.getWritableDatabase().delete(
                "courses", "id=?", new String[]{String.valueOf(courseId)}
        ) > 0;
    }

    @Override
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
                writableDatabase.insertOrThrow("enrollments", null, values);
            }
            writableDatabase.setTransactionSuccessful();
            return true;
        } catch (SQLiteException exception) {
            return false;
        } finally {
            writableDatabase.endTransaction();
        }
    }

    @Override
    public boolean unenroll(long studentId, long courseId) {
        return database.getWritableDatabase().delete(
                "enrollments",
                "student_id=? AND course_id=?",
                new String[]{String.valueOf(studentId), String.valueOf(courseId)}
        ) > 0;
    }

    @Override
    public List<Student> getStudentsByCourse(long courseId) {
        String sql = "SELECT s.id,s.code,s.name,s.age,s.scratch_level " +
                "FROM students s JOIN enrollments e ON e.student_id=s.id " +
                "WHERE e.course_id=? ORDER BY s.name";
        return queryStudents(sql, new String[]{String.valueOf(courseId)});
    }

    @Override
    public List<Student> getPythonBasicStudentsAged10To12() {
        String sql = "SELECT DISTINCT s.id,s.code,s.name,s.age,s.scratch_level " +
                "FROM students s " +
                "JOIN enrollments e ON e.student_id=s.id " +
                "JOIN courses c ON c.id=e.course_id " +
                "WHERE s.age BETWEEN 10 AND 12 AND c.language=? AND c.level=? " +
                "ORDER BY s.name";
        return queryStudents(sql, new String[]{Course.LANGUAGE_PYTHON, Course.LEVEL_BASIC});
    }

    private List<Student> queryStudents(String sql, String[] arguments) {
        List<Student> students = new ArrayList<>();
        try (Cursor cursor = database.getReadableDatabase().rawQuery(sql, arguments)) {
            while (cursor.moveToNext()) {
                students.add(mapStudent(cursor, 0));
            }
        }
        return students;
    }

    private Student mapStudent(Cursor cursor, int offset) {
        return new Student(
                cursor.getLong(offset),
                cursor.getString(offset + 1),
                cursor.getString(offset + 2),
                cursor.getInt(offset + 3),
                cursor.getString(offset + 4)
        );
    }

    private Course mapCourse(Cursor cursor, int offset) {
        return new Course(
                cursor.getLong(offset),
                cursor.getString(offset + 1),
                cursor.getString(offset + 2),
                cursor.getString(offset + 3),
                cursor.getString(offset + 4)
        );
    }
}
