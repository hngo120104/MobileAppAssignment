package com.example.quanlynhansu.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.CourseSummary;
import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.repository.ClassroomRepository;
import com.example.quanlynhansu.domain.usecase.EnrollmentCandidates;
import com.example.quanlynhansu.domain.usecase.EnrollmentUseCase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class EnrollmentUseCaseTest {
    @Test
    public void candidates_excludeStudentsAlreadyInTheCourse() {
        Student enrolled = student(1, "HV001");
        Student available = student(2, "HV002");
        FakeRepository repository = new FakeRepository(
                Arrays.asList(enrolled, available),
                Collections.singletonList(enrolled)
        );

        EnrollmentCandidates result = new EnrollmentUseCase(repository).getCandidates(10);

        assertEquals(EnrollmentCandidates.Status.AVAILABLE, result.getStatus());
        assertEquals(1, result.getStudents().size());
        assertEquals(available.getId(), result.getStudents().get(0).getId());
    }

    @Test
    public void candidates_distinguishNoStudentsFromAllEnrolled() {
        FakeRepository emptyRepository = new FakeRepository(
                Collections.emptyList(),
                Collections.emptyList()
        );
        Student student = student(1, "HV001");
        FakeRepository fullyEnrolledRepository = new FakeRepository(
                Collections.singletonList(student),
                Collections.singletonList(student)
        );

        assertEquals(
                EnrollmentCandidates.Status.NO_STUDENTS,
                new EnrollmentUseCase(emptyRepository).getCandidates(10).getStatus()
        );
        assertEquals(
                EnrollmentCandidates.Status.ALL_ENROLLED,
                new EnrollmentUseCase(fullyEnrolledRepository).getCandidates(10).getStatus()
        );
    }

    @Test
    public void enroll_delegatesSelectedIdsToRepository() {
        FakeRepository repository = new FakeRepository(
                Collections.emptyList(),
                Collections.emptyList()
        );
        List<Long> selectedIds = Arrays.asList(1L, 2L);

        assertTrue(new EnrollmentUseCase(repository).enroll(20, selectedIds));
        assertEquals(20, repository.enrolledCourseId);
        assertEquals(selectedIds, repository.enrolledStudentIds);
    }

    private static Student student(long id, String code) {
        return new Student(id, code, "Học viên", 10, "Cơ bản");
    }

    private static final class FakeRepository implements ClassroomRepository {
        private final List<Student> allStudents;
        private final List<Student> enrolledStudents;
        private List<Long> enrolledStudentIds = new ArrayList<>();
        private long enrolledCourseId = -1;

        private FakeRepository(List<Student> allStudents, List<Student> enrolledStudents) {
            this.allStudents = allStudents;
            this.enrolledStudents = enrolledStudents;
        }

        @Override public List<Student> getStudents() { return allStudents; }
        @Override public long saveStudent(Student student) { return 0; }
        @Override public boolean deleteStudent(long studentId) { return false; }
        @Override public List<Course> getCourses() { return Collections.emptyList(); }
        @Override public List<CourseSummary> getCourseSummaries() {
            return Collections.emptyList();
        }
        @Override public long saveCourse(Course course) { return 0; }
        @Override public boolean deleteCourse(long courseId) { return false; }
        @Override
        public boolean enrollStudents(List<Long> studentIds, long courseId) {
            enrolledStudentIds = studentIds;
            enrolledCourseId = courseId;
            return true;
        }

        @Override public boolean unenroll(long studentId, long courseId) { return false; }
        @Override public List<Student> getStudentsByCourse(long courseId) {
            return enrolledStudents;
        }
        @Override public List<Student> getPythonBasicStudentsAged10To12() {
            return Collections.emptyList();
        }
    }
}
