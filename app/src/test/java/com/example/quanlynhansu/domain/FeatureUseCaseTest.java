package com.example.quanlynhansu.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.CourseStatistic;
import com.example.quanlynhansu.domain.model.CourseSummary;
import com.example.quanlynhansu.domain.model.Enrollment;
import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.repository.ClassroomRepository;
import com.example.quanlynhansu.domain.usecase.CourseUseCase;
import com.example.quanlynhansu.domain.usecase.ReportUseCase;
import com.example.quanlynhansu.domain.usecase.StudentUseCase;
import com.example.quanlynhansu.domain.validation.CourseInputValidator;
import com.example.quanlynhansu.domain.validation.ReportFilterValidator;
import com.example.quanlynhansu.domain.validation.StudentInputValidator;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

public final class FeatureUseCaseTest {
    @Test
    public void studentUseCase_validatesAndCreatesDomainModel() {
        StudentUseCase useCase = new StudentUseCase(
                new FakeRepository(),
                new StudentInputValidator()
        );

        StudentUseCase.PreparedStudent invalid = useCase.prepareStudent(
                -1,
                "",
                "Học viên",
                "10",
                "Cơ bản"
        );
        StudentUseCase.PreparedStudent valid = useCase.prepareStudent(
                -1,
                "HV100",
                "Học viên",
                "10",
                "Cơ bản"
        );

        assertFalse(invalid.isValid());
        assertEquals(StudentInputValidator.Error.CODE_REQUIRED, invalid.getError());
        assertTrue(valid.isValid());
        assertEquals("HV100", valid.getStudent().getCode());
        assertEquals(10, valid.getStudent().getAge());
    }

    @Test
    public void courseUseCase_validatesAndCreatesDomainModel() {
        CourseUseCase useCase = new CourseUseCase(
                new FakeRepository(),
                new CourseInputValidator()
        );

        CourseUseCase.PreparedCourse result = useCase.prepareCourse(
                -1,
                "KH100",
                "Python nhập môn",
                Course.LANGUAGE_PYTHON,
                Course.LEVEL_BASIC,
                "2026-08-01",
                "2026-10-31"
        );

        assertTrue(result.isValid());
        assertEquals("KH100", result.getCourse().getCode());
        assertEquals(Course.LANGUAGE_PYTHON, result.getCourse().getLanguage());
        assertEquals("2026-08-01", result.getCourse().getStartDate());
    }

    @Test
    public void reportUseCase_delegatesDataAccessToRepository() {
        FakeRepository repository = new FakeRepository();
        ReportUseCase useCase = new ReportUseCase(
                repository,
                new ReportFilterValidator()
        );

        useCase.getStudentsByCourse(42);
        useCase.getPythonBasicStudentsAged10To12();

        assertEquals(42, repository.requestedCourseId);
        assertTrue(repository.pythonReportRequested);
    }

    private static final class FakeRepository implements ClassroomRepository {
        private long requestedCourseId = -1;
        private boolean pythonReportRequested;

        @Override public List<Student> getStudents() { return Collections.emptyList(); }
        @Override public long saveStudent(Student student) { return 1; }
        @Override public boolean deleteStudent(long studentId) { return true; }
        @Override public boolean deleteStudents(List<Long> studentIds) { return true; }
        @Override public List<Course> getCourses() { return Collections.emptyList(); }
        @Override public List<CourseSummary> getCourseSummaries() {
            return Collections.emptyList();
        }
        @Override public long saveCourse(Course course) { return 1; }
        @Override public boolean deleteCourse(long courseId) { return true; }
        @Override public boolean deleteCourses(List<Long> courseIds) { return true; }
        @Override public boolean enrollStudents(List<Long> studentIds, long courseId) {
            return true;
        }
        @Override public boolean unenrollStudents(List<Long> studentIds, long courseId) {
            return true;
        }
        @Override public List<Student> getStudentsByCourse(long courseId) {
            requestedCourseId = courseId;
            return Collections.emptyList();
        }
        @Override public List<Student> getPythonBasicStudentsAged10To12() {
            pythonReportRequested = true;
            return Collections.emptyList();
        }
        @Override public List<Student> getUnenrolledStudents() {
            return Collections.emptyList();
        }
        @Override public List<Student> getStudentsByAgeRange(int minimumAge, int maximumAge) {
            return Collections.emptyList();
        }
        @Override public List<CourseStatistic> getCourseStatisticsByLanguage() {
            return Collections.emptyList();
        }
        @Override public List<CourseStatistic> getCourseStatisticsByLevel() {
            return Collections.emptyList();
        }
        @Override public List<Enrollment> getEnrollmentsByDateRange(
                String startDate,
                String endDate
        ) {
            return Collections.emptyList();
        }
    }
}
