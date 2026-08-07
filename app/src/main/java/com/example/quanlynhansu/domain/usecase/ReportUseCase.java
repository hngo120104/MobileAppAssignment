package com.example.quanlynhansu.domain.usecase;

import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.CourseStatistic;
import com.example.quanlynhansu.domain.model.CourseSummary;
import com.example.quanlynhansu.domain.model.Enrollment;
import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.repository.ClassroomRepository;
import com.example.quanlynhansu.domain.validation.ReportFilterValidator;

import java.util.List;

public final class ReportUseCase {
    private final ClassroomRepository repository;
    private final ReportFilterValidator validator;

    public ReportUseCase(
            ClassroomRepository repository,
            ReportFilterValidator validator
    ) {
        this.repository = repository;
        this.validator = validator;
    }

    public List<Course> getCourses() {
        return repository.getCourses();
    }

    public List<Student> getStudentsByCourse(long courseId) {
        return repository.getStudentsByCourse(courseId);
    }

    public List<Student> getPythonBasicStudentsAged10To12() {
        return repository.getPythonBasicStudentsAged10To12();
    }

    public List<CourseSummary> getAllCourseSummaries() {
        return repository.getCourseSummaries();
    }

    public List<Student> getUnenrolledStudents() {
        return repository.getUnenrolledStudents();
    }

    public ReportFilterValidator.AgeRange prepareAgeRange(
            String minimumText,
            String maximumText
    ) {
        return validator.validateAgeRange(minimumText, maximumText);
    }

    public List<Student> getStudentsByAgeRange(int minimum, int maximum) {
        return repository.getStudentsByAgeRange(minimum, maximum);
    }

    public List<CourseStatistic> getCourseStatisticsByLanguage() {
        return repository.getCourseStatisticsByLanguage();
    }

    public List<CourseStatistic> getCourseStatisticsByLevel() {
        return repository.getCourseStatisticsByLevel();
    }

    public ReportFilterValidator.DateRange prepareDateRange(
            String startText,
            String endText
    ) {
        return validator.validateDateRange(startText, endText);
    }

    public List<Enrollment> getEnrollmentsByDateRange(String startDate, String endDate) {
        return repository.getEnrollmentsByDateRange(startDate, endDate);
    }
}
