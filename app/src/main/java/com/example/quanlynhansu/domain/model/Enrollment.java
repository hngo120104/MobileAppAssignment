package com.example.quanlynhansu.domain.model;

import java.util.Objects;

public final class Enrollment {
    private final Student student;
    private final Course course;

    public Enrollment(Student student, Course course) {
        this.student = Objects.requireNonNull(student);
        this.course = Objects.requireNonNull(course);
    }

    public Student getStudent() { return student; }
    public Course getCourse() { return course; }
}
