package com.example.quanlynhansu.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.quanlynhansu.domain.validation.CourseInputValidator;
import com.example.quanlynhansu.domain.validation.StudentInputValidator;

import org.junit.Test;

public final class DomainValidationTest {
    private final StudentInputValidator studentValidator = new StudentInputValidator();
    private final CourseInputValidator courseValidator = new CourseInputValidator();

    @Test
    public void studentValidation_returnsParsedAgeForValidInput() {
        StudentInputValidator.Result result = studentValidator.validate(
                "HV100",
                "Học viên",
                "12"
        );

        assertTrue(result.isValid());
        assertEquals(12, result.getAge());
    }

    @Test
    public void studentValidation_rejectsInvalidAndOutOfRangeAge() {
        StudentInputValidator.Result invalidAge = studentValidator.validate(
                "HV100",
                "Học viên",
                "abc"
        );
        StudentInputValidator.Result outOfRangeAge = studentValidator.validate(
                "HV100",
                "Học viên",
                "19"
        );

        assertFalse(invalidAge.isValid());
        assertEquals(StudentInputValidator.Error.AGE_INVALID, invalidAge.getError());
        assertEquals(
                StudentInputValidator.Error.AGE_OUT_OF_RANGE,
                outOfRangeAge.getError()
        );
    }

    @Test
    public void validation_reportsTheFirstRequiredField() {
        assertEquals(
                StudentInputValidator.Error.CODE_REQUIRED,
                studentValidator.validate("", "Học viên", "10").getError()
        );
        assertEquals(
                CourseInputValidator.Error.NAME_REQUIRED,
                courseValidator.validate("KH100", "")
        );
    }
}
