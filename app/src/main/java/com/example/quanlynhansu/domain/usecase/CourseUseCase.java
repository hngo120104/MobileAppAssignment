package com.example.quanlynhansu.domain.usecase;

import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.repository.ClassroomRepository;
import com.example.quanlynhansu.domain.validation.CourseInputValidator;

import java.util.List;
import java.util.Objects;

public final class CourseUseCase {
    private final ClassroomRepository repository;
    private final CourseInputValidator validator;

    public CourseUseCase(
            ClassroomRepository repository,
            CourseInputValidator validator
    ) {
        this.repository = repository;
        this.validator = validator;
    }

    public List<Course> getCourses() {
        return repository.getCourses();
    }

    public PreparedCourse prepareCourse(
            long id,
            String code,
            String name,
            String language,
            String level,
            String startDate,
            String endDate
    ) {
        CourseInputValidator.Error error = validator.validate(
                code,
                name,
                startDate,
                endDate
        );
        if (error != CourseInputValidator.Error.NONE) {
            return PreparedCourse.invalid(error);
        }
        return PreparedCourse.valid(new Course(
                id,
                code,
                name,
                language,
                level,
                startDate,
                endDate
        ));
    }

    public boolean saveCourse(Course course) {
        return repository.saveCourse(course) > 0;
    }

    public boolean deleteCourse(long courseId) {
        return repository.deleteCourse(courseId);
    }

    public boolean deleteCourses(List<Long> courseIds) {
        return repository.deleteCourses(courseIds);
    }

    public static final class PreparedCourse {
        private final CourseInputValidator.Error error;
        private final Course course;

        private PreparedCourse(CourseInputValidator.Error error, Course course) {
            this.error = error;
            this.course = course;
        }

        private static PreparedCourse valid(Course course) {
            return new PreparedCourse(CourseInputValidator.Error.NONE, course);
        }

        private static PreparedCourse invalid(CourseInputValidator.Error error) {
            return new PreparedCourse(error, null);
        }

        public boolean isValid() {
            return error == CourseInputValidator.Error.NONE;
        }

        public CourseInputValidator.Error getError() {
            return error;
        }

        public Course getCourse() {
            if (!isValid()) {
                throw new IllegalStateException("Course is unavailable for invalid input");
            }
            return Objects.requireNonNull(course);
        }
    }
}
