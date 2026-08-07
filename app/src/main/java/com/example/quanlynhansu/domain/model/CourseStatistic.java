package com.example.quanlynhansu.domain.model;

import java.util.Objects;

public final class CourseStatistic {
    private final String groupName;
    private final int courseCount;
    private final int enrollmentCount;

    public CourseStatistic(String groupName, int courseCount, int enrollmentCount) {
        this.groupName = Objects.requireNonNull(groupName);
        if (courseCount < 0 || enrollmentCount < 0) {
            throw new IllegalArgumentException("Statistic counts must not be negative");
        }
        this.courseCount = courseCount;
        this.enrollmentCount = enrollmentCount;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getCourseCount() {
        return courseCount;
    }

    public int getEnrollmentCount() {
        return enrollmentCount;
    }
}
