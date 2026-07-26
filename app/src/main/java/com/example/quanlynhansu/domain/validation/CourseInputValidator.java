package com.example.quanlynhansu.domain.validation;

public final class CourseInputValidator {
    public Error validate(String code, String name) {
        if (code.isEmpty()) {
            return Error.CODE_REQUIRED;
        }
        if (name.isEmpty()) {
            return Error.NAME_REQUIRED;
        }
        return Error.NONE;
    }

    public enum Error {
        NONE,
        CODE_REQUIRED,
        NAME_REQUIRED
    }
}
