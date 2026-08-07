package com.example.quanlynhansu.domain.validation;

public final class ReportFilterValidator {
    public AgeRange validateAgeRange(String minimumText, String maximumText) {
        int minimum;
        int maximum;
        try {
            minimum = Integer.parseInt(minimumText);
        } catch (NumberFormatException exception) {
            return AgeRange.invalid(Error.MINIMUM_AGE_INVALID);
        }
        try {
            maximum = Integer.parseInt(maximumText);
        } catch (NumberFormatException exception) {
            return AgeRange.invalid(Error.MAXIMUM_AGE_INVALID);
        }
        if (minimum < 5 || maximum > 18 || minimum > maximum) {
            return AgeRange.invalid(Error.AGE_RANGE_INVALID);
        }
        return AgeRange.valid(minimum, maximum);
    }

    public DateRange validateDateRange(String startText, String endText) {
        if (!IsoDateValidator.isValid(startText)) {
            return DateRange.invalid(Error.START_DATE_INVALID);
        }
        if (!IsoDateValidator.isValid(endText)) {
            return DateRange.invalid(Error.END_DATE_INVALID);
        }
        if (!IsoDateValidator.isOrdered(startText, endText)) {
            return DateRange.invalid(Error.DATE_RANGE_INVALID);
        }
        return DateRange.valid(startText, endText);
    }

    public enum Error {
        NONE,
        MINIMUM_AGE_INVALID,
        MAXIMUM_AGE_INVALID,
        AGE_RANGE_INVALID,
        START_DATE_INVALID,
        END_DATE_INVALID,
        DATE_RANGE_INVALID
    }

    public static final class AgeRange {
        private final Error error;
        private final int minimum;
        private final int maximum;

        private AgeRange(Error error, int minimum, int maximum) {
            this.error = error;
            this.minimum = minimum;
            this.maximum = maximum;
        }

        private static AgeRange valid(int minimum, int maximum) {
            return new AgeRange(Error.NONE, minimum, maximum);
        }

        private static AgeRange invalid(Error error) {
            return new AgeRange(error, -1, -1);
        }

        public boolean isValid() { return error == Error.NONE; }
        public Error getError() { return error; }
        public int getMinimum() { return minimum; }
        public int getMaximum() { return maximum; }
    }

    public static final class DateRange {
        private final Error error;
        private final String startDate;
        private final String endDate;

        private DateRange(Error error, String startDate, String endDate) {
            this.error = error;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        private static DateRange valid(String startDate, String endDate) {
            return new DateRange(Error.NONE, startDate, endDate);
        }

        private static DateRange invalid(Error error) {
            return new DateRange(error, "", "");
        }

        public boolean isValid() { return error == Error.NONE; }
        public Error getError() { return error; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
    }
}
