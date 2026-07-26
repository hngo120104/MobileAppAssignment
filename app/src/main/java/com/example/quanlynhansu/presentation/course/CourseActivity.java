package com.example.quanlynhansu.presentation.course;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.quanlynhansu.R;
import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.validation.CourseInputValidator;
import com.example.quanlynhansu.presentation.common.BaseListActivity;
import com.example.quanlynhansu.presentation.common.FormViews;

public final class CourseActivity extends BaseListActivity<Course> {
    private final CourseInputValidator validator = new CourseInputValidator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_list);
        setTitle("Quản lý khóa học");
        ListView listView = findViewById(R.id.list);
        setupList(listView);
        findViewById(R.id.btnAdd).setOnClickListener(view -> showForm(null));
        listView.setOnItemClickListener((parent, view, position, id) -> showForm(items.get(position)));
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(items.get(position));
            return true;
        });
        loadCourses();
    }

    private void loadCourses() {
        executeDatabase(repository::getCourses, courses -> {
            replaceItems(courses);
            findViewById(R.id.empty).setVisibility(items.isEmpty()
                    ? android.view.View.VISIBLE : android.view.View.GONE);
        });
    }

    @Override
    protected String render(Course course) {
        return course.getCode() + " • " + course.getName() +
                "\n" + course.getLanguage() + "  |  " + course.getLevel();
    }

    private void showForm(Course current) {
        LinearLayout form = FormViews.container(this);
        EditText code = FormViews.input(form, "Mã khóa học", false);
        EditText name = FormViews.input(form, "Tên khóa học", false);
        Spinner language = FormViews.spinner(form, new String[]{"Scratch", "Python"});
        Spinner level = FormViews.spinner(form, new String[]{"Cơ bản", "Trung cấp", "Nâng cao"});
        if (current != null) {
            code.setText(current.getCode());
            name.setText(current.getName());
            FormViews.select(language, current.getLanguage());
            FormViews.select(level, current.getLevel());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(current == null ? "Thêm khóa học" : "Sửa khóa học")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> saveCourse(dialog, current, code, name, language, level)));
        dialog.show();
    }

    private void saveCourse(AlertDialog dialog, Course current, EditText codeInput,
                            EditText nameInput, Spinner languageInput, Spinner levelInput) {
        String code = codeInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        CourseInputValidator.Error error = validator.validate(code, name);
        if (!showValidationError(error, codeInput, nameInput)) {
            return;
        }

        Course course = new Course(
                current == null ? -1 : current.getId(), code, name,
                languageInput.getSelectedItem().toString(), levelInput.getSelectedItem().toString()
        );
        persistCourse(dialog, course);
    }

    private boolean showValidationError(
            CourseInputValidator.Error error,
            EditText codeInput,
            EditText nameInput
    ) {
        switch (error) {
            case CODE_REQUIRED:
                codeInput.setError("Bắt buộc");
                return false;
            case NAME_REQUIRED:
                nameInput.setError("Bắt buộc");
                return false;
            case NONE:
            default:
                return true;
        }
    }

    private void persistCourse(AlertDialog dialog, Course course) {
        executeDatabase(() -> repository.saveCourse(course), result -> {
            if (result < 1) {
                Toast.makeText(this, "Mã khóa học đã tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            loadCourses();
        });
    }

    private void confirmDelete(Course course) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa khóa học")
                .setMessage("Xóa " + course.getName() + " và các ghi danh liên quan?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    executeDatabase(
                            () -> repository.deleteCourse(course.getId()),
                            deleted -> {
                                if (deleted) {
                                    loadCourses();
                                } else {
                                    Toast.makeText(
                                            this,
                                            "Khóa học không còn tồn tại",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                    );
                })
                .show();
    }
}
