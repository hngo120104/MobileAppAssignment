package com.example.quanlynhansu.domain.model;

import java.util.Objects;

public final class CourseSummary {
    private final Course course;
    private final int studentCount;

    public CourseSummary(Course course, int studentCount) {
        this.course = Objects.requireNonNull(course);
        if (studentCount < 0) {
            throw new IllegalArgumentException("studentCount must not be negative");
        }
        this.studentCount = studentCount;
    }

    public Course getCourse() {
        return course;
    }

    public int getStudentCount() {
        return studentCount;
    }
}
