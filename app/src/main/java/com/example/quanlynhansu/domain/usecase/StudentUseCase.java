package com.example.quanlynhansu.domain.usecase;

import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.repository.ClassroomRepository;
import com.example.quanlynhansu.domain.validation.StudentInputValidator;

import java.util.List;
import java.util.Objects;

public final class StudentUseCase {
    private final ClassroomRepository repository;
    private final StudentInputValidator validator;

    public StudentUseCase(
            ClassroomRepository repository,
            StudentInputValidator validator
    ) {
        this.repository = repository;
        this.validator = validator;
    }

    public List<Student> getStudents() {
        return repository.getStudents();
    }

    public PreparedStudent prepareStudent(
            long id,
            String code,
            String name,
            String ageText,
            String scratchLevel
    ) {
        StudentInputValidator.Result validation = validator.validate(code, name, ageText);
        if (!validation.isValid()) {
            return PreparedStudent.invalid(validation.getError());
        }
        return PreparedStudent.valid(new Student(
                id,
                code,
                name,
                validation.getAge(),
                scratchLevel
        ));
    }

    public boolean saveStudent(Student student) {
        return repository.saveStudent(student) > 0;
    }

    public boolean deleteStudent(long studentId) {
        return repository.deleteStudent(studentId);
    }

    public boolean deleteStudents(List<Long> studentIds) {
        return repository.deleteStudents(studentIds);
    }

    public static final class PreparedStudent {
        private final StudentInputValidator.Error error;
        private final Student student;

        private PreparedStudent(StudentInputValidator.Error error, Student student) {
            this.error = error;
            this.student = student;
        }

        private static PreparedStudent valid(Student student) {
            return new PreparedStudent(StudentInputValidator.Error.NONE, student);
        }

        private static PreparedStudent invalid(StudentInputValidator.Error error) {
            return new PreparedStudent(error, null);
        }

        public boolean isValid() {
            return error == StudentInputValidator.Error.NONE;
        }

        public StudentInputValidator.Error getError() {
            return error;
        }

        public Student getStudent() {
            if (!isValid()) {
                throw new IllegalStateException("Student is unavailable for invalid input");
            }
            return Objects.requireNonNull(student);
        }
    }
}
