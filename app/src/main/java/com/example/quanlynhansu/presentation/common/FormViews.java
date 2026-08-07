package com.example.quanlynhansu.presentation.common;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public final class FormViews {
    private FormViews() { }

    public static LinearLayout container(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(context, 40), dp(context, 8), dp(context, 40), 0);
        return layout;
    }

    public static EditText input(LinearLayout parent, String hint, boolean numeric) {
        EditText input = new EditText(parent.getContext());
        input.setHint(hint);
        input.setSingleLine(true);
        input.setSelectAllOnFocus(true);
        if (numeric) input.setInputType(InputType.TYPE_CLASS_NUMBER);
        parent.addView(input);
        return input;
    }

    public static Spinner spinner(LinearLayout parent, String labelText, String[] options) {
        TextView label = new TextView(parent.getContext());
        label.setText(labelText);
        label.setPadding(0, dp(parent.getContext(), 12), 0, dp(parent.getContext(), 4));
        parent.addView(label);

        Spinner spinner = new Spinner(parent.getContext());
        spinner.setId(View.generateViewId());
        spinner.setMinimumHeight(dp(parent.getContext(), 48));
        spinner.setContentDescription(labelText);
        label.setLabelFor(spinner.getId());
        spinner.setAdapter(new ArrayAdapter<>(
                parent.getContext(), android.R.layout.simple_spinner_dropdown_item, options
        ));
        parent.addView(spinner);
        return spinner;
    }

    public static ScrollView scrollable(LinearLayout form) {
        ScrollView scrollView = new ScrollView(form.getContext());
        scrollView.setFillViewport(true);
        scrollView.addView(form);
        return scrollView;
    }

    public static void select(Spinner spinner, String value) {
        for (int index = 0; index < spinner.getCount(); index++) {
            if (spinner.getItemAtPosition(index).toString().equals(value)) {
                spinner.setSelection(index);
                return;
            }
        }
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
