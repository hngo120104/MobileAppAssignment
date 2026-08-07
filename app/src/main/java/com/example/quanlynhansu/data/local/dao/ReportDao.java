package com.example.quanlynhansu.data.local.dao;

import android.database.Cursor;

import com.example.quanlynhansu.data.local.ClassroomDatabase;
import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.CourseStatistic;
import com.example.quanlynhansu.domain.model.Enrollment;
import com.example.quanlynhansu.domain.model.Student;

import java.util.ArrayList;
import java.util.List;

public final class ReportDao {
    private final ClassroomDatabase database;

    public ReportDao(ClassroomDatabase database) {
        this.database = database;
    }

    public List<Student> getPythonBasicStudentsAged10To12() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT DISTINCT s.id,s.code,s.name,s.age,s.scratch_level " +
                "FROM students s " +
                "JOIN enrollments e ON e.student_id=s.id " +
                "JOIN courses c ON c.id=e.course_id " +
                "WHERE s.age BETWEEN 10 AND 12 AND c.language=? AND c.level=? " +
                "ORDER BY s.name";
        try (Cursor cursor = database.getReadableDatabase().rawQuery(
                sql,
                new String[]{Course.LANGUAGE_PYTHON, Course.LEVEL_BASIC}
        )) {
            while (cursor.moveToNext()) {
                students.add(EntityCursorMapper.mapStudent(cursor, 0));
            }
        }
        return students;
    }

    public List<Student> getUnenrolledStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.id,s.code,s.name,s.age,s.scratch_level " +
                "FROM students s " +
                "WHERE NOT EXISTS(" +
                "SELECT 1 FROM enrollments e WHERE e.student_id=s.id) " +
                "ORDER BY s.name";
        try (Cursor cursor = database.getReadableDatabase().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                students.add(EntityCursorMapper.mapStudent(cursor, 0));
            }
        }
        return students;
    }

    public List<Student> getStudentsByAgeRange(int minimumAge, int maximumAge) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT id,code,name,age,scratch_level FROM students " +
                "WHERE age BETWEEN ? AND ? ORDER BY age,name";
        try (Cursor cursor = database.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(minimumAge), String.valueOf(maximumAge)}
        )) {
            while (cursor.moveToNext()) {
                students.add(EntityCursorMapper.mapStudent(cursor, 0));
            }
        }
        return students;
    }

    public List<CourseStatistic> getCourseStatisticsByLanguage() {
        return getCourseStatistics("c.language");
    }

    public List<CourseStatistic> getCourseStatisticsByLevel() {
        return getCourseStatistics("c.level");
    }

    public List<Enrollment> getEnrollmentsByDateRange(String startDate, String endDate) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT s.id,s.code,s.name,s.age,s.scratch_level," +
                "c.id,c.code,c.name,c.language,c.level,c.start_date,c.end_date," +
                "e.enrolled_at " +
                "FROM enrollments e " +
                "JOIN students s ON s.id=e.student_id " +
                "JOIN courses c ON c.id=e.course_id " +
                "WHERE date(e.enrolled_at) BETWEEN date(?) AND date(?) " +
                "ORDER BY e.enrolled_at DESC,s.name";
        try (Cursor cursor = database.getReadableDatabase().rawQuery(
                sql,
                new String[]{startDate, endDate}
        )) {
            while (cursor.moveToNext()) {
                enrollments.add(new Enrollment(
                        EntityCursorMapper.mapStudent(cursor, 0),
                        EntityCursorMapper.mapCourse(cursor, 5),
                        cursor.getString(12)
                ));
            }
        }
        return enrollments;
    }

    private List<CourseStatistic> getCourseStatistics(String groupColumn) {
        List<CourseStatistic> statistics = new ArrayList<>();
        String sql = "SELECT " + groupColumn + ",COUNT(DISTINCT c.id)," +
                "COUNT(e.student_id) FROM courses c " +
                "LEFT JOIN enrollments e ON e.course_id=c.id " +
                "GROUP BY " + groupColumn + " ORDER BY " + groupColumn;
        try (Cursor cursor = database.getReadableDatabase().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                statistics.add(new CourseStatistic(
                        cursor.getString(0),
                        cursor.getInt(1),
                        cursor.getInt(2)
                ));
            }
        }
        return statistics;
    }
}
