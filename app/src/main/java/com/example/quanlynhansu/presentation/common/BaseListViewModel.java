package com.example.quanlynhansu.presentation.common;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseListViewModel<T> extends ViewModel {
    private final MutableLiveData<List<T>> items = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> selectionMode = new MutableLiveData<>(false);
    private final MutableLiveData<Set<Long>> selectedIds = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<UiEvent<String>> errorEvent = new MutableLiveData<>();

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean initialLoadRequested;
    private volatile boolean cleared;

    public LiveData<List<T>> getItems() {
        return items;
    }

    public LiveData<Boolean> getSelectionMode() {
        return selectionMode;
    }

    public LiveData<Set<Long>> getSelectedIds() {
        return selectedIds;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<UiEvent<String>> getErrorEvent() {
        return errorEvent;
    }

    public final void loadInitialData() {
        if (initialLoadRequested) {
            return;
        }
        initialLoadRequested = true;
        onInitialLoad();
    }

    protected abstract void onInitialLoad();

    protected abstract long stableId(T item);

    public void setSelectionMode(boolean enabled) {
        selectionMode.setValue(enabled);
        Set<Long> currentSelected = selectedIds.getValue();
        if (currentSelected != null && !currentSelected.isEmpty()) {
            selectedIds.setValue(new HashSet<>());
        }
    }

    public void toggleSelection(T item) {
        long id = stableId(item);
        Set<Long> currentSelected = new HashSet<>(selectedIds.getValue() != null ? selectedIds.getValue() : new HashSet<>());
        if (currentSelected.contains(id)) {
            currentSelected.remove(id);
        } else {
            currentSelected.add(id);
        }
        selectedIds.setValue(currentSelected);
    }

    public void selectAllItems() {
        List<T> currentItems = items.getValue();
        if (currentItems == null) return;
        
        Set<Long> allIds = new HashSet<>();
        for (T item : currentItems) {
            allIds.add(stableId(item));
        }
        selectedIds.setValue(allIds);
    }

    public List<T> getSelectedItemsList() {
        List<T> currentItems = items.getValue();
        Set<Long> currentSelected = selectedIds.getValue();
        List<T> result = new ArrayList<>();
        if (currentItems == null || currentSelected == null) return result;

        for (T item : currentItems) {
            if (currentSelected.contains(stableId(item))) {
                result.add(item);
            }
        }
        return result;
    }

    protected void replaceItems(List<T> newItems) {
        Set<Long> currentSelected = selectedIds.getValue();
        if (currentSelected != null && !currentSelected.isEmpty()) {
            Set<Long> validSelected = new HashSet<>();
            for (T item : newItems) {
                long id = stableId(item);
                if (currentSelected.contains(id)) {
                    validSelected.add(id);
                }
            }
            if (validSelected.size() != currentSelected.size()) {
                selectedIds.setValue(validSelected);
            }
        }
        items.setValue(newItems);
    }

    protected void showLoading() {
        isLoading.setValue(true);
    }

    protected void hideLoading() {
        isLoading.setValue(false);
    }

    protected void showError(String message) {
        errorEvent.setValue(new UiEvent<>(message));
    }

    protected <R> void executeDatabase(Supplier<R> operation, Consumer<R> onSuccess) {
        executeDatabase(
                operation,
                onSuccess,
                () -> showError("Không thể truy cập dữ liệu. Vui lòng thử lại.")
        );
    }

    protected <R> void executeDatabase(Supplier<R> operation, Consumer<R> onSuccess, Runnable onError) {
        databaseExecutor.execute(() -> {
            try {
                R result = operation.get();
                postToMain(() -> onSuccess.accept(result));
            } catch (RuntimeException exception) {
                postToMain(onError);
            }
        });
    }

    private void postToMain(Runnable callback) {
        mainHandler.post(() -> {
            if (!cleared) {
                callback.run();
            }
        });
    }

    @Override
    protected void onCleared() {
        cleared = true;
        databaseExecutor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
        super.onCleared();
    }
}
