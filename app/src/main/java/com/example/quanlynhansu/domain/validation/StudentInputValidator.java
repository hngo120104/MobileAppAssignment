package com.example.quanlynhansu.domain.validation;

public final class StudentInputValidator {
    public Result validate(String code, String name, String ageText) {
        if (code.isEmpty()) {
            return Result.error(Error.CODE_REQUIRED);
        }
        if (name.isEmpty()) {
            return Result.error(Error.NAME_REQUIRED);
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException exception) {
            return Result.error(Error.AGE_INVALID);
        }
        if (age < 5 || age > 18) {
            return Result.error(Error.AGE_OUT_OF_RANGE);
        }
        return Result.valid(age);
    }

    public enum Error {
        NONE,
        CODE_REQUIRED,
        NAME_REQUIRED,
        AGE_INVALID,
        AGE_OUT_OF_RANGE
    }

    public static final class Result {
        private final Error error;
        private final int age;

        private Result(Error error, int age) {
            this.error = error;
            this.age = age;
        }

        private static Result valid(int age) {
            return new Result(Error.NONE, age);
        }

        private static Result error(Error error) {
            return new Result(error, -1);
        }

        public boolean isValid() {
            return error == Error.NONE;
        }

        public Error getError() {
            return error;
        }

        public int getAge() {
            if (!isValid()) {
                throw new IllegalStateException("Age is unavailable for invalid input");
            }
            return age;
        }
    }
}
