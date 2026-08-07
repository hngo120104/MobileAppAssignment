package com.example.quanlynhansu.data.local.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlynhansu.data.local.ClassroomDatabase;
import com.example.quanlynhansu.domain.model.Course;

import java.util.ArrayList;
import java.util.List;

public final class CourseDao {
    private static final String TABLE = "courses";

    private final ClassroomDatabase database;

    public CourseDao(ClassroomDatabase database) {
        this.database = database;
    }

    public List<Course> getCourses() {
        List<Course> courses = new ArrayList<>();
        try (Cursor cursor = database.getReadableDatabase().rawQuery(
                "SELECT id,code,name,language,level,start_date,end_date " +
                        "FROM courses ORDER BY name",
                null)) {
            while (cursor.moveToNext()) {
                courses.add(EntityCursorMapper.mapCourse(cursor, 0));
            }
        }
        return courses;
    }

    public long saveCourse(Course course) {
        ContentValues values = new ContentValues();
        values.put("code", course.getCode());
        values.put("name", course.getName());
        values.put("language", course.getLanguage());
        values.put("level", course.getLevel());
        values.put("start_date", course.getStartDate());
        values.put("end_date", course.getEndDate());

        SQLiteDatabase writableDatabase = database.getWritableDatabase();
        if (course.getId() < 0) {
            return writableDatabase.insert(TABLE, null, values);
        }
        return writableDatabase.update(
                TABLE,
                values,
                "id=?",
                new String[] { String.valueOf(course.getId()) });
    }

    public boolean deleteCourse(long courseId) {
        return database.getWritableDatabase().delete(
                TABLE,
                "id=?",
                new String[] { String.valueOf(courseId) }) > 0;
    }

    public boolean deleteCourses(List<Long> courseIds) {
        return SqliteBatchDelete.deleteByIds(database, TABLE, courseIds);
    }
}
