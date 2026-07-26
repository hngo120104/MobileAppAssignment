package com.example.quanlynhansu.presentation.student;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.quanlynhansu.R;
import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.validation.StudentInputValidator;
import com.example.quanlynhansu.presentation.common.BaseListActivity;
import com.example.quanlynhansu.presentation.common.FormViews;

public final class StudentActivity extends BaseListActivity<Student> {
    private final StudentInputValidator validator = new StudentInputValidator();
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_list);
        setTitle("Quản lý học viên");
        listView = findViewById(R.id.list);
        setupList(listView);
        findViewById(R.id.btnAdd).setOnClickListener(view -> showForm(null));
        listView.setOnItemClickListener((parent, view, position, id) -> showForm(items.get(position)));
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(items.get(position));
            return true;
        });
        loadStudents();
    }

    private void loadStudents() {
        executeDatabase(repository::getStudents, students -> {
            replaceItems(students);
            findViewById(R.id.empty).setVisibility(items.isEmpty()
                    ? android.view.View.VISIBLE : android.view.View.GONE);
        });
    }

    @Override
    protected String render(Student student) {
        return student.getCode() + " • " + student.getName() +
                "\nTuổi: " + student.getAge() + "  |  Scratch: " + student.getScratchLevel();
    }

    private void showForm(Student current) {
        LinearLayout form = FormViews.container(this);
        EditText code = FormViews.input(form, "Mã học viên", false);
        EditText name = FormViews.input(form, "Họ tên", false);
        EditText age = FormViews.input(form, "Tuổi (5–18)", true);
        Spinner scratchLevel = FormViews.spinner(
                form, new String[]{"Chưa học", "Cơ bản", "Khá", "Nâng cao"}
        );
        if (current != null) {
            code.setText(current.getCode());
            name.setText(current.getName());
            age.setText(String.valueOf(current.getAge()));
            FormViews.select(scratchLevel, current.getScratchLevel());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(current == null ? "Thêm học viên" : "Sửa học viên")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> saveStudent(dialog, current, code, name, age, scratchLevel)));
        dialog.show();
    }

    private void saveStudent(AlertDialog dialog, Student current, EditText codeInput,
                             EditText nameInput, EditText ageInput, Spinner levelInput) {
        String code = codeInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        StudentInputValidator.Result validation = validator.validate(
                code,
                name,
                ageInput.getText().toString().trim()
        );
        if (!showValidationError(validation, codeInput, nameInput, ageInput)) {
            return;
        }

        Student student = new Student(
                current == null ? -1 : current.getId(), code, name, validation.getAge(),
                levelInput.getSelectedItem().toString()
        );
        persistStudent(dialog, student);
    }

    private boolean showValidationError(
            StudentInputValidator.Result result,
            EditText codeInput,
            EditText nameInput,
            EditText ageInput
    ) {
        switch (result.getError()) {
            case CODE_REQUIRED:
                codeInput.setError("Bắt buộc");
                return false;
            case NAME_REQUIRED:
                nameInput.setError("Bắt buộc");
                return false;
            case AGE_INVALID:
                ageInput.setError("Tuổi không hợp lệ");
                return false;
            case AGE_OUT_OF_RANGE:
                ageInput.setError("Tuổi từ 5 đến 18");
                return false;
            case NONE:
            default:
                return true;
        }
    }

    private void persistStudent(AlertDialog dialog, Student student) {
        executeDatabase(() -> repository.saveStudent(student), result -> {
            if (result < 1) {
                Toast.makeText(this, "Mã học viên đã tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            loadStudents();
        });
    }

    private void confirmDelete(Student student) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa học viên")
                .setMessage("Xóa " + student.getName() + " và các ghi danh liên quan?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    executeDatabase(
                            () -> repository.deleteStudent(student.getId()),
                            deleted -> {
                                if (deleted) {
                                    loadStudents();
                                } else {
                                    Toast.makeText(
                                            this,
                                            "Học viên không còn tồn tại",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                    );
                })
                .show();
    }
}
