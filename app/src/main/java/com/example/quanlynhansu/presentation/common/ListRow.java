package com.example.quanlynhansu.presentation.common;

public final class ListRow {
    private final String title;
    private final String subtitle;
    private final String metadata;

    public ListRow(String title, String subtitle, String metadata) {
        this.title = title;
        this.subtitle = subtitle;
        this.metadata = metadata;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getMetadata() {
        return metadata;
    }
}
