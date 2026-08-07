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

    public Error validate(String code, String name, String startDate, String endDate) {
        Error baseError = validate(code, name);
        if (baseError != Error.NONE) {
            return baseError;
        }
        if (startDate.isEmpty()) {
            return Error.START_DATE_REQUIRED;
        }
        if (endDate.isEmpty()) {
            return Error.END_DATE_REQUIRED;
        }

        if (!IsoDateValidator.isValid(startDate)) {
            return Error.START_DATE_INVALID;
        }
        if (!IsoDateValidator.isValid(endDate)) {
            return Error.END_DATE_INVALID;
        }
        return IsoDateValidator.isOrdered(startDate, endDate)
                ? Error.NONE
                : Error.DATE_RANGE_INVALID;
    }

    public enum Error {
        NONE,
        CODE_REQUIRED,
        NAME_REQUIRED,
        START_DATE_REQUIRED,
        START_DATE_INVALID,
        END_DATE_REQUIRED,
        END_DATE_INVALID,
        DATE_RANGE_INVALID
    }
}
