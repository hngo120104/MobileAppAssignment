package com.example.quanlynhansu.domain.model;

import java.util.Objects;

public final class Course {
    public static final String LANGUAGE_SCRATCH = "Scratch";
    public static final String LANGUAGE_PYTHON = "Python";
    public static final String LEVEL_BASIC = "Cơ bản";

    private final long id;
    private final String code;
    private final String name;
    private final String language;
    private final String level;
    private final String startDate;
    private final String endDate;

    public Course(long id, String code, String name, String language, String level) {
        this(id, code, name, language, level, "", "");
    }

    public Course(
            long id,
            String code,
            String name,
            String language,
            String level,
            String startDate,
            String endDate
    ) {
        this.id = id;
        this.code = Objects.requireNonNull(code);
        this.name = Objects.requireNonNull(name);
        this.language = Objects.requireNonNull(language);
        this.level = Objects.requireNonNull(level);
        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = Objects.requireNonNull(endDate);
    }

    public long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getLanguage() { return language; }
    public String getLevel() { return level; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
}
