package com.example.quanlynhansu.presentation.course;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.quanlynhansu.R;
import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.validation.CourseInputValidator;
import com.example.quanlynhansu.presentation.common.BaseMvvmListActivity;
import com.example.quanlynhansu.presentation.common.FormViews;
import com.example.quanlynhansu.presentation.common.ListRow;
import com.example.quanlynhansu.presentation.common.SelectionControls;

import java.util.List;

public final class CourseActivity extends BaseMvvmListActivity<Course, CourseViewModel> {
    private SelectionControls selectionControls;
    private AlertDialog activeDialog;

    @Override
    protected Class<CourseViewModel> getViewModelClass() {
        return CourseViewModel.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_list);
        setTitle("Quản lý khóa học");
        ((TextView) findViewById(R.id.txtScreenTitle)).setText(R.string.course_management_title);
        ((TextView) findViewById(R.id.txtInstruction)).setText(
                R.string.course_list_instruction
        );
        ListView listView = findViewById(R.id.list);
        setupList(
                listView,
                (anchor, course) -> showForm(course),
                (anchor, course) -> confirmDelete(course)
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
    protected ListRow render(Course course) {
        return new ListRow(
                course.getName(),
                course.getCode(),
                course.getLanguage() + " • " + course.getLevel() + " • " +
                        course.getStartDate() + " → " + course.getEndDate()
        );
    }

    @Override
    protected long stableId(Course course) {
        return course.getId();
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

    private void showForm(Course current) {
        LinearLayout form = FormViews.container(this);
        EditText code = FormViews.input(form, "Mã khóa học", false);
        EditText name = FormViews.input(form, "Tên khóa học", false);
        Spinner language = FormViews.spinner(
                form, getString(R.string.programming_language), new String[]{"Scratch", "Python"}
        );
        Spinner level = FormViews.spinner(
                form, getString(R.string.course_level),
                new String[]{"Cơ bản", "Trung cấp", "Nâng cao"}
        );
        EditText startDate = FormViews.input(form, "Ngày bắt đầu (yyyy-MM-dd)", false);
        EditText endDate = FormViews.input(form, "Ngày kết thúc (yyyy-MM-dd)", false);
        if (current != null) {
            code.setText(current.getCode());
            name.setText(current.getName());
            FormViews.select(language, current.getLanguage());
            FormViews.select(level, current.getLevel());
            startDate.setText(current.getStartDate());
            endDate.setText(current.getEndDate());
        }

        activeDialog = new AlertDialog.Builder(this)
                .setTitle(current == null ? "Thêm khóa học" : "Sửa khóa học")
                .setView(FormViews.scrollable(form))
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        activeDialog.setOnShowListener(ignored -> activeDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> saveCourse(
                        activeDialog,
                        current,
                        code,
                        name,
                        language,
                        level,
                        startDate,
                        endDate
                )));
        activeDialog.show();
    }

    private void saveCourse(AlertDialog dialog, Course current, EditText codeInput,
                            EditText nameInput, Spinner languageInput, Spinner levelInput,
                            EditText startDateInput, EditText endDateInput) {
        String code = codeInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        CourseInputValidator.Error error = viewModel.saveCourse(
                current == null ? -1 : current.getId(),
                code,
                name,
                languageInput.getSelectedItem().toString(),
                levelInput.getSelectedItem().toString(),
                startDateInput.getText().toString().trim(),
                endDateInput.getText().toString().trim()
        );
        if (!showValidationError(
                error,
                codeInput,
                nameInput,
                startDateInput,
                endDateInput
        )) {
            return;
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private boolean showValidationError(
            CourseInputValidator.Error error,
            EditText codeInput,
            EditText nameInput,
            EditText startDateInput,
            EditText endDateInput
    ) {
        switch (error) {
            case CODE_REQUIRED:
                codeInput.setError("Bắt buộc");
                return false;
            case NAME_REQUIRED:
                nameInput.setError("Bắt buộc");
                return false;
            case START_DATE_REQUIRED:
                startDateInput.setError("Bắt buộc");
                return false;
            case START_DATE_INVALID:
                startDateInput.setError("Định dạng yyyy-MM-dd");
                return false;
            case END_DATE_REQUIRED:
                endDateInput.setError("Bắt buộc");
                return false;
            case END_DATE_INVALID:
                endDateInput.setError("Định dạng yyyy-MM-dd");
                return false;
            case DATE_RANGE_INVALID:
                endDateInput.setError("Ngày kết thúc phải từ ngày bắt đầu trở đi");
                return false;
            case NONE:
            default:
                return true;
        }
    }

    private void confirmDelete(Course course) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa khóa học")
                .setMessage("Xóa " + course.getName() + " và các ghi danh liên quan?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteCourse(course.getId());
                })
                .show();
    }

    private void confirmDeleteSelected() {
        List<Course> selectedCourses = getSelectedItems();
        if (selectedCourses.isEmpty()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Xóa nhiều khóa học")
                .setMessage("Xóa " + selectedCourses.size()
                        + " khóa học và tất cả ghi danh liên quan?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) ->
                        viewModel.deleteSelectedCourses())
                .show();
    }
}
