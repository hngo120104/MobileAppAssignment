package com.example.quanlynhansu.domain.model;

import java.util.Objects;

public final class Student {
    private final long id;
    private final String code;
    private final String name;
    private final int age;
    private final String scratchLevel;

    public Student(long id, String code, String name, int age, String scratchLevel) {
        this.id = id;
        this.code = Objects.requireNonNull(code);
        this.name = Objects.requireNonNull(name);
        this.age = age;
        this.scratchLevel = Objects.requireNonNull(scratchLevel);
    }

    public long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getScratchLevel() { return scratchLevel; }
}
