package com.example.quanlynhansu.domain.usecase;

import com.example.quanlynhansu.domain.model.Student;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public final class EnrollmentCandidates {
    private final Status status;
    private final List<Student> students;

    private EnrollmentCandidates(Status status, List<Student> students) {
        this.status = status;
        this.students = Collections.unmodifiableList(new ArrayList<>(students));
    }

    public static EnrollmentCandidates noStudents() {
        return new EnrollmentCandidates(Status.NO_STUDENTS, Collections.emptyList());
    }

    public static EnrollmentCandidates allEnrolled() {
        return new EnrollmentCandidates(Status.ALL_ENROLLED, Collections.emptyList());
    }

    public static EnrollmentCandidates available(List<Student> students) {
        return new EnrollmentCandidates(Status.AVAILABLE, students);
    }

    public Status getStatus() {
        return status;
    }

    public List<Student> getStudents() {
        return students;
    }

    public enum Status {
        AVAILABLE,
        NO_STUDENTS,
        ALL_ENROLLED
    }
}
