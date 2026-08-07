package com.example.quanlynhansu.presentation.common;

import java.util.Objects;

public final class UiEvent<T> {
    private final T content;
    private boolean handled;

    public UiEvent(T content) {
        this.content = Objects.requireNonNull(content);
    }

    public synchronized T consume() {
        if (handled) {
            return null;
        }
        handled = true;
        return content;
    }
}
