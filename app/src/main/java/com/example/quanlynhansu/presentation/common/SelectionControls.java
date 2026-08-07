package com.example.quanlynhansu.presentation.common;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.example.quanlynhansu.R;

public final class SelectionControls {
    private final Activity activity;
    private final Button selectButton;
    private final Button selectAllButton;
    private final Button deleteSelectedButton;
    private final View addButton;

    public SelectionControls(
            Activity activity,
            Runnable toggleSelectionMode,
            Runnable selectAll,
            Runnable deleteSelected
    ) {
        this.activity = activity;
        selectButton = activity.findViewById(R.id.btnSelect);
        selectAllButton = activity.findViewById(R.id.btnSelectAll);
        deleteSelectedButton = activity.findViewById(R.id.btnDeleteSelected);
        addButton = activity.findViewById(R.id.btnAdd);

        selectButton.setVisibility(View.VISIBLE);
        selectButton.setOnClickListener(view -> toggleSelectionMode.run());
        selectAllButton.setOnClickListener(view -> selectAll.run());
        deleteSelectedButton.setOnClickListener(view -> deleteSelected.run());
    }

    public void update(boolean selectionMode, int selectedCount) {
        selectButton.setText(selectionMode ? R.string.cancel : R.string.select);
        selectAllButton.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        deleteSelectedButton.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        addButton.setVisibility(selectionMode ? View.GONE : View.VISIBLE);
        deleteSelectedButton.setEnabled(selectedCount > 0);
        deleteSelectedButton.setText(
                activity.getString(R.string.delete_selected_count, selectedCount)
        );
    }
}
