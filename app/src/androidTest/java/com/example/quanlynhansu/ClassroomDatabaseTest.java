package com.example.quanlynhansu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.quanlynhansu.data.local.ClassroomDatabase;
import com.example.quanlynhansu.data.repository.SqliteClassroomRepository;
import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.CourseStatistic;
import com.example.quanlynhansu.domain.model.CourseSummary;
import com.example.quanlynhansu.domain.model.Enrollment;
import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.repository.ClassroomRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public final class ClassroomDatabaseTest {
    private static final String DATABASE_NAME = "LopHocLapTrinh.db";

    private Context context;
    private ClassroomDatabase database;
    private ClassroomRepository repository;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(DATABASE_NAME);
        database = new ClassroomDatabase(context);
        repository = new SqliteClassroomRepository(database);
    }

    @After
    public void tearDown() {
        if (database != null) {
            database.close();
        }
        context.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void batchEnrollment_isAtomicWhenOneEnrollmentIsDuplicated() {
        long studentId = repository.saveStudent(
                new Student(-1, "HV100", "Học viên 100", 10, "Cơ bản")
        );
        long anotherStudentId = repository.saveStudent(
                new Student(-1, "HV101", "Học viên 101", 11, "Khá")
        );
        long courseId = repository.saveCourse(
                new Course(-1, "KH100", "Khóa kiểm thử", "Python", "Cơ bản")
        );

        assertFalse(repository.enrollStudents(
                Arrays.asList(studentId, studentId, anotherStudentId),
                courseId
        ));
        assertTrue(repository.getStudentsByCourse(courseId).isEmpty());
    }

    @Test
    public void courseSummaries_includeCoursesWithoutEnrollmentAndCorrectCounts() {
        long studentId = repository.saveStudent(
                new Student(-1, "HV100", "Học viên 100", 10, "Cơ bản")
        );
        long courseId = repository.saveCourse(
                new Course(-1, "KH100", "Khóa kiểm thử", "Python", "Cơ bản")
        );
        repository.saveCourse(
                new Course(-1, "KH101", "Khóa chưa ghi danh", "Scratch", "Cơ bản")
        );

        assertTrue(repository.enrollStudents(List.of(studentId), courseId));

        int matchingCount = 0;
        boolean foundEmptyCourse = false;
        for (CourseSummary summary : repository.getCourseSummaries()) {
            if (summary.getCourse().getId() == courseId) {
                matchingCount = summary.getStudentCount();
            }
            if ("KH101".equals(summary.getCourse().getCode())) {
                foundEmptyCourse = summary.getStudentCount() == 0;
            }
        }
        assertEquals(1, matchingCount);
        assertTrue(foundEmptyCourse);
    }

    @Test
    public void reportQueries_filterAndAggregatePersistedData() {
        long enrolledStudentId = repository.saveStudent(
                new Student(-1, "HV100", "Học viên đã ghi danh", 10, "Cơ bản")
        );
        repository.saveStudent(
                new Student(-1, "HV101", "Học viên chưa ghi danh", 17, "Chưa học")
        );
        long courseId = repository.saveCourse(
                new Course(
                        -1,
                        "KH100",
                        "Khóa kiểm thử báo cáo",
                        "Python",
                        "Cơ bản",
                        "2026-08-01",
                        "2026-10-31"
                )
        );
        assertTrue(repository.enrollStudents(List.of(enrolledStudentId), courseId));

        Course savedCourse = repository.getCourses().stream()
                .filter(course -> "KH100".equals(course.getCode()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertEquals("2026-08-01", savedCourse.getStartDate());
        assertEquals("2026-10-31", savedCourse.getEndDate());

        assertTrue(repository.getUnenrolledStudents().stream()
                .anyMatch(student -> "HV101".equals(student.getCode())));
        assertFalse(repository.getUnenrolledStudents().stream()
                .anyMatch(student -> "HV100".equals(student.getCode())));

        List<Student> studentsAged9To11 = repository.getStudentsByAgeRange(9, 11);
        assertTrue(studentsAged9To11.stream()
                .anyMatch(student -> "HV100".equals(student.getCode())));
        assertFalse(studentsAged9To11.stream()
                .anyMatch(student -> "HV101".equals(student.getCode())));

        CourseStatistic pythonStatistic = repository.getCourseStatisticsByLanguage().stream()
                .filter(statistic -> "Python".equals(statistic.getGroupName()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertTrue(pythonStatistic.getCourseCount() >= 1);
        assertTrue(pythonStatistic.getEnrollmentCount() >= 1);

        CourseStatistic basicStatistic = repository.getCourseStatisticsByLevel().stream()
                .filter(statistic -> "Cơ bản".equals(statistic.getGroupName()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertTrue(basicStatistic.getCourseCount() >= 1);
        assertTrue(basicStatistic.getEnrollmentCount() >= 1);

        List<Enrollment> enrollments = repository.getEnrollmentsByDateRange(
                "2000-01-01",
                "2100-01-01"
        );
        Enrollment savedEnrollment = enrollments.stream()
                .filter(enrollment -> "HV100".equals(enrollment.getStudent().getCode()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertFalse(savedEnrollment.getEnrolledAt().isEmpty());
        assertEquals("KH100", savedEnrollment.getCourse().getCode());
    }

    @Test
    public void batchDeleteStudents_rollsBackWhenOneStudentDoesNotExist() {
        long studentId = repository.saveStudent(
                new Student(-1, "HV100", "Học viên 100", 10, "Cơ bản")
        );

        assertFalse(repository.deleteStudents(Arrays.asList(studentId, 999999L)));

        boolean studentStillExists = false;
        for (Student student : repository.getStudents()) {
            if (student.getId() == studentId) {
                studentStillExists = true;
            }
        }
        assertTrue(studentStillExists);
    }

    @Test
    public void batchUnenroll_rollsBackWhenSelectionContainsDuplicate() {
        long studentId = repository.saveStudent(
                new Student(-1, "HV100", "Học viên 100", 10, "Cơ bản")
        );
        long courseId = repository.saveCourse(
                new Course(-1, "KH100", "Khóa kiểm thử", "Python", "Cơ bản")
        );
        assertTrue(repository.enrollStudents(List.of(studentId), courseId));

        assertFalse(repository.unenrollStudents(
                Arrays.asList(studentId, studentId),
                courseId
        ));
        assertEquals(1, repository.getStudentsByCourse(courseId).size());
    }

    @Test
    public void upgradeFromVersion2_preservesDataAndCreatesCourseIndex() {
        database.close();
        database = null;
        context.deleteDatabase(DATABASE_NAME);

        SQLiteDatabase version2 = context.openOrCreateDatabase(DATABASE_NAME, 0, null);
        version2.execSQL("CREATE TABLE students(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "code TEXT NOT NULL UNIQUE," +
                "name TEXT NOT NULL," +
                "age INTEGER NOT NULL CHECK(age BETWEEN 5 AND 18)," +
                "scratch_level TEXT NOT NULL)");
        version2.execSQL("CREATE TABLE courses(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "code TEXT NOT NULL UNIQUE," +
                "name TEXT NOT NULL," +
                "language TEXT NOT NULL CHECK(language IN ('Scratch','Python'))," +
                "level TEXT NOT NULL)");
        version2.execSQL("CREATE TABLE enrollments(" +
                "student_id INTEGER NOT NULL," +
                "course_id INTEGER NOT NULL," +
                "PRIMARY KEY(student_id,course_id)," +
                "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE," +
                "FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE)");
        version2.execSQL(
                "INSERT INTO students(code,name,age,scratch_level) " +
                        "VALUES('LEGACY','Dữ liệu cũ',12,'Cơ bản')"
        );
        version2.setVersion(2);
        version2.close();

        database = new ClassroomDatabase(context);
        SQLiteDatabase upgraded = database.getWritableDatabase();

        try (Cursor studentCursor = upgraded.rawQuery(
                "SELECT COUNT(*) FROM students WHERE code='LEGACY'", null
        )) {
            assertTrue(studentCursor.moveToFirst());
            assertEquals(1, studentCursor.getInt(0));
        }
        try (Cursor indexCursor = upgraded.rawQuery(
                "SELECT COUNT(*) FROM sqlite_master " +
                        "WHERE type='index' AND name='index_enrollments_course_id'",
                null
        )) {
            assertTrue(indexCursor.moveToFirst());
            assertEquals(1, indexCursor.getInt(0));
        }
        try (Cursor courseCursor = upgraded.rawQuery(
                "SELECT start_date,end_date FROM courses WHERE code='KH001'", null
        )) {
            assertTrue(courseCursor.moveToFirst());
            assertFalse(courseCursor.getString(0).isEmpty());
            assertFalse(courseCursor.getString(1).isEmpty());
        }
        try (Cursor enrollmentCursor = upgraded.rawQuery(
                "SELECT enrolled_at FROM enrollments LIMIT 1", null
        )) {
            assertTrue(enrollmentCursor.moveToFirst());
            assertFalse(enrollmentCursor.getString(0).isEmpty());
        }
    }
}
