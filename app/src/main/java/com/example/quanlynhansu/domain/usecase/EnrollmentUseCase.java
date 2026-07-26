package com.example.quanlynhansu.domain.usecase;

import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.repository.ClassroomRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EnrollmentUseCase {
    private final ClassroomRepository repository;

    public EnrollmentUseCase(ClassroomRepository repository) {
        this.repository = repository;
    }

    public EnrollmentCandidates getCandidates(long courseId) {
        List<Student> allStudents = repository.getStudents();
        if (allStudents.isEmpty()) {
            return EnrollmentCandidates.noStudents();
        }

        Set<Long> enrolledIds = getEnrolledIds(courseId);
        List<Student> availableStudents = filterAvailableStudents(allStudents, enrolledIds);
        if (availableStudents.isEmpty()) {
            return EnrollmentCandidates.allEnrolled();
        }
        return EnrollmentCandidates.available(availableStudents);
    }

    public boolean enroll(long courseId, List<Long> studentIds) {
        return repository.enrollStudents(studentIds, courseId);
    }

    private Set<Long> getEnrolledIds(long courseId) {
        Set<Long> enrolledIds = new HashSet<>();
        for (Student student : repository.getStudentsByCourse(courseId)) {
            enrolledIds.add(student.getId());
        }
        return enrolledIds;
    }

    private List<Student> filterAvailableStudents(
            List<Student> allStudents,
            Set<Long> enrolledIds
    ) {
        List<Student> availableStudents = new ArrayList<>();
        for (Student student : allStudents) {
            if (!enrolledIds.contains(student.getId())) {
                availableStudents.add(student);
            }
        }
        return availableStudents;
    }
}
