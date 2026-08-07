package com.example.quanlynhansu.domain.validation;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Locale;

final class IsoDateValidator {
    private IsoDateValidator() {
    }

    static boolean isValid(String value) {
        if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        return format.parse(value, position) != null && position.getIndex() == value.length();
    }

    static boolean isOrdered(String startDate, String endDate) {
        return startDate.compareTo(endDate) <= 0;
    }
}
