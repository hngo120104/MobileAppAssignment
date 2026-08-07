package com.example.quanlynhansu.domain.model;

import java.util.Objects;

public final class Enrollment {
    private final Student student;
    private final Course course;
    private final String enrolledAt;

    public Enrollment(Student student, Course course) {
        this(student, course, "");
    }

    public Enrollment(Student student, Course course, String enrolledAt) {
        this.student = Objects.requireNonNull(student);
        this.course = Objects.requireNonNull(course);
        this.enrolledAt = Objects.requireNonNull(enrolledAt);
    }

    public Student getStudent() { return student; }
    public Course getCourse() { return course; }
    public String getEnrolledAt() { return enrolledAt; }
}
