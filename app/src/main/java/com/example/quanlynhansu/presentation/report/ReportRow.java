package com.example.quanlynhansu.presentation.report;

import java.util.Objects;

public final class ReportRow {
    private final long id;
    private final String title;
    private final String subtitle;
    private final String metadata;

    public ReportRow(long id, String title, String subtitle, String metadata) {
        this.id = id;
        this.title = Objects.requireNonNull(title);
        this.subtitle = Objects.requireNonNull(subtitle);
        this.metadata = Objects.requireNonNull(metadata);
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getMetadata() { return metadata; }
}
