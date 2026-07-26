package com.example.quanlynhansu.presentation.common;

import android.content.Context;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

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
        if (numeric) input.setInputType(InputType.TYPE_CLASS_NUMBER);
        parent.addView(input);
        return input;
    }

    public static Spinner spinner(LinearLayout parent, String[] options) {
        Spinner spinner = new Spinner(parent.getContext());
        spinner.setAdapter(new ArrayAdapter<>(
                parent.getContext(), android.R.layout.simple_spinner_dropdown_item, options
        ));
        parent.addView(spinner);
        return spinner;
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
