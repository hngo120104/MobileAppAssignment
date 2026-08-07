package com.example.quanlynhansu.presentation.student;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.quanlynhansu.R;
import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.validation.StudentInputValidator;
import com.example.quanlynhansu.presentation.common.BaseMvvmListActivity;
import com.example.quanlynhansu.presentation.common.FormViews;
import com.example.quanlynhansu.presentation.common.ListRow;
import com.example.quanlynhansu.presentation.common.SelectionControls;

import java.util.List;

public final class StudentActivity extends BaseMvvmListActivity<Student, StudentViewModel> {
    private ListView listView;
    private SelectionControls selectionControls;
    private AlertDialog activeDialog;

    @Override
    protected Class<StudentViewModel> getViewModelClass() {
        return StudentViewModel.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_list);
        setTitle("Quản lý học viên");
        ((TextView) findViewById(R.id.txtScreenTitle)).setText(R.string.student_management_title);
        ((TextView) findViewById(R.id.txtInstruction)).setText(
                R.string.student_list_instruction
        );
        listView = findViewById(R.id.list);
        setupList(
                listView,
                (anchor, student) -> showForm(student),
                (anchor, student) -> confirmDelete(student)
        );
        setupBulkSelection();
        findViewById(R.id.btnAdd).setOnClickListener(view -> showForm(null));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (isSelectionMode()) {
                toggleSelection(position);
            } else {
                showForm(items.get(position));
            }
        });

        viewModel.getSaveEvent().observe(this, event -> {
            Boolean success = event == null ? null : event.consume();
            if (success == null) {
                return;
            }
            if (activeDialog != null && activeDialog.isShowing()) {
                if (success) {
                    activeDialog.dismiss();
                    activeDialog = null;
                } else {
                    activeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        viewModel.loadInitialData();
    }

    @Override
    protected ListRow render(Student student) {
        return new ListRow(
                student.getName(),
                student.getCode(),
                student.getAge() + " tuổi • Scratch: " + student.getScratchLevel()
        );
    }

    @Override
    protected long stableId(Student student) {
        return student.getId();
    }

    private void setupBulkSelection() {
        selectionControls = new SelectionControls(
                this,
                () -> setSelectionMode(!isSelectionMode()),
                this::selectAllItems,
                this::confirmDeleteSelected
        );
        setSelectionListener(count -> selectionControls.update(isSelectionMode(), count));
        selectionControls.update(false, 0);
    }

    private void showForm(Student current) {
        LinearLayout form = FormViews.container(this);
        EditText code = FormViews.input(form, "Mã học viên", false);
        EditText name = FormViews.input(form, "Họ tên", false);
        EditText age = FormViews.input(form, "Tuổi (5–18)", true);
        Spinner scratchLevel = FormViews.spinner(
                form,
                getString(R.string.scratch_level),
                new String[]{"Chưa học", "Cơ bản", "Khá", "Nâng cao"}
        );
        if (current != null) {
            code.setText(current.getCode());
            name.setText(current.getName());
            age.setText(String.valueOf(current.getAge()));
            FormViews.select(scratchLevel, current.getScratchLevel());
        }

        activeDialog = new AlertDialog.Builder(this)
                .setTitle(current == null ? "Thêm học viên" : "Sửa học viên")
                .setView(FormViews.scrollable(form))
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        activeDialog.setOnShowListener(ignored -> activeDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> saveStudent(activeDialog, current, code, name, age, scratchLevel)));
        activeDialog.show();
    }

    private void saveStudent(AlertDialog dialog, Student current, EditText codeInput,
                             EditText nameInput, EditText ageInput, Spinner levelInput) {
        String code = codeInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        StudentInputValidator.Error error = viewModel.saveStudent(
                current == null ? -1 : current.getId(),
                code,
                name,
                ageInput.getText().toString().trim(),
                levelInput.getSelectedItem().toString()
        );
        if (!showValidationError(error, codeInput, nameInput, ageInput)) {
            return;
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private boolean showValidationError(
            StudentInputValidator.Error error,
            EditText codeInput,
            EditText nameInput,
            EditText ageInput
    ) {
        switch (error) {
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

    private void confirmDelete(Student student) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa học viên")
                .setMessage("Xóa " + student.getName() + " và các ghi danh liên quan?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteStudent(student.getId());
                })
                .show();
    }

    private void confirmDeleteSelected() {
        List<Student> selectedStudents = getSelectedItems();
        if (selectedStudents.isEmpty()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Xóa nhiều học viên")
                .setMessage("Xóa " + selectedStudents.size()
                        + " học viên và tất cả ghi danh liên quan?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) ->
                        viewModel.deleteSelectedStudents())
                .show();
    }
}
