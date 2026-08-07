package com.example.quanlynhansu.presentation.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quanlynhansu.ClassroomApplication;
import com.example.quanlynhansu.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class BaseMvvmListActivity<T, VM extends BaseListViewModel<T>> extends AppCompatActivity {
    protected BaseAdapter adapter;
    protected VM viewModel;
    
    protected List<T> items = new ArrayList<>();
    private boolean selectionMode = false;
    private Set<Long> selectedIds = new HashSet<>();
    private Consumer<Integer> selectionListener = ignored -> { };

    protected abstract Class<VM> getViewModelClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ClassroomApplication application = (ClassroomApplication) getApplication();
        viewModel = new ViewModelProvider(
                this,
                application.getViewModelFactory()
        ).get(getViewModelClass());

        viewModel.getItems().observe(this, newItems -> {
            this.items = newItems != null ? newItems : new ArrayList<>();
            if (adapter != null) adapter.notifyDataSetChanged();
        });

        viewModel.getSelectionMode().observe(this, mode -> {
            this.selectionMode = mode != null ? mode : false;
            if (adapter != null) adapter.notifyDataSetChanged();
            notifySelectionChanged();
        });

        viewModel.getSelectedIds().observe(this, ids -> {
            this.selectedIds = ids != null ? ids : new HashSet<>();
            if (adapter != null) adapter.notifyDataSetChanged();
            notifySelectionChanged();
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                showListLoading();
            } else {
                showListContent(items.isEmpty());
            }
        });

        viewModel.getErrorEvent().observe(this, event -> {
            String message = event == null ? null : event.consume();
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void setContentView(@LayoutRes int layoutResId) {
        super.setContentView(layoutResId);
        SystemBars.applyInsets(findViewById(android.R.id.content));
        Boolean loading = viewModel.getIsLoading().getValue();
        if (Boolean.TRUE.equals(loading)) {
            showListLoading();
        } else {
            showListContent(items.isEmpty());
        }
    }

    protected void setupList(ListView listView) {
        setupList(listView, null, null);
    }

    protected void setupList(
            ListView listView,
            BiConsumer<View, T> onEditClick,
            BiConsumer<View, T> onDeleteClick
    ) {
        adapter = new BaseAdapter() {
            @Override public int getCount() { return items.size(); }
            @Override public T getItem(int position) { return items.get(position); }
            @Override public long getItemId(int position) { return stableId(items.get(position)); }
            @Override public boolean hasStableIds() { return true; }

            @Override
            public View getView(int position, View recycledView, ViewGroup parent) {
                View row = recycledView == null
                        ? LayoutInflater.from(BaseMvvmListActivity.this).inflate(
                                R.layout.item_list_row,
                                parent,
                                false
                        )
                        : recycledView;
                bindRow(row, position, items.get(position), onEditClick, onDeleteClick);
                return row;
            }
        };
        listView.setAdapter(adapter);
    }

    private void bindRow(
            View row,
            int position,
            T item,
            BiConsumer<View, T> onEditClick,
            BiConsumer<View, T> onDeleteClick
    ) {
        ListRow content = render(item);
        ((TextView) row.findViewById(R.id.rowTitle)).setText(content.getTitle());
        bindOptionalText(row.findViewById(R.id.rowSubtitle), content.getSubtitle());
        bindOptionalText(row.findViewById(R.id.rowMetadata), content.getMetadata());

        CheckBox selection = row.findViewById(R.id.rowSelection);
        selection.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        selection.setChecked(selectedIds.contains(stableId(item)));

        bindAction(row.findViewById(R.id.rowEdit), item, onEditClick);
        bindAction(row.findViewById(R.id.rowDelete), item, onDeleteClick);
    }

    private void bindAction(
            ImageButton button,
            T item,
            BiConsumer<View, T> listener
    ) {
        boolean visible = listener != null && !selectionMode;
        button.setVisibility(visible ? View.VISIBLE : View.GONE);
        button.setOnClickListener(visible ? view -> listener.accept(view, item) : null);
    }

    private void bindOptionalText(TextView view, String text) {
        boolean hasText = text != null && !text.isEmpty();
        view.setVisibility(hasText ? View.VISIBLE : View.GONE);
        if (hasText) {
            view.setText(text);
        }
    }

    protected final void setSelectionListener(Consumer<Integer> listener) {
        selectionListener = listener;
    }

    protected final boolean isSelectionMode() {
        return selectionMode;
    }

    protected final void setSelectionMode(boolean enabled) {
        viewModel.setSelectionMode(enabled);
    }

    protected final void toggleSelection(int position) {
        viewModel.toggleSelection(items.get(position));
    }

    protected final void selectAllItems() {
        viewModel.selectAllItems();
    }

    protected final List<T> getSelectedItems() {
        return viewModel.getSelectedItemsList();
    }

    private void notifySelectionChanged() {
        if (selectionListener != null) {
            selectionListener.accept(selectedIds.size());
        }
    }

    protected abstract ListRow render(T item);

    protected abstract long stableId(T item);

    protected final void showListLoading() {
        View loading = findViewById(R.id.loading);
        View list = findViewById(R.id.list);
        View empty = findViewById(R.id.empty);
        if (loading != null) loading.setVisibility(View.VISIBLE);
        if (list != null) list.setVisibility(View.INVISIBLE);
        if (empty != null) empty.setVisibility(View.GONE);
    }

    protected final void showListContent(boolean isEmpty) {
        View loading = findViewById(R.id.loading);
        View list = findViewById(R.id.list);
        View empty = findViewById(R.id.empty);
        if (loading != null) loading.setVisibility(View.GONE);
        if (list != null) list.setVisibility(View.VISIBLE);
        if (empty != null) empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
