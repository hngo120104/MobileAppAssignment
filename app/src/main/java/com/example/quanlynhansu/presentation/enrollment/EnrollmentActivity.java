package com.example.quanlynhansu.presentation.enrollment;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.quanlynhansu.R;
import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.CourseSummary;
import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.usecase.EnrollmentCandidates;
import com.example.quanlynhansu.presentation.common.BaseMvvmListActivity;
import com.example.quanlynhansu.presentation.common.ListRow;

import java.util.ArrayList;
import java.util.List;

public final class EnrollmentActivity extends BaseMvvmListActivity<CourseSummary, EnrollmentViewModel> {
    private Button addButton;
    private AlertDialog activeDialog;
    private Course currentSelectedCourse;

    @Override
    protected Class<EnrollmentViewModel> getViewModelClass() {
        return EnrollmentViewModel.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_list);
        setTitle("Ghi danh học viên");
        ((TextView) findViewById(R.id.txtScreenTitle)).setText(
                R.string.enrollment_management_title
        );
        ((TextView) findViewById(R.id.txtInstruction)).setText(
                R.string.enrollment_list_instruction
        );

        ListView listView = findViewById(R.id.list);
        setupList(listView);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            currentSelectedCourse = items.get(position).getCourse();
            viewModel.fetchEnrolledStudents(currentSelectedCourse.getId());
        });

        addButton = findViewById(R.id.btnAdd);
        addButton.setText(R.string.choose_course_to_enroll);
        addButton.setOnClickListener(view -> chooseCourse());

        setupObservers();

        viewModel.loadInitialData();
    }

    private void setupObservers() {
        viewModel.getCourseChoices().observe(this, courses -> {
            if (courses != null) {
                addButton.setEnabled(true);
                displayCourseChoices(courses);
            }
        });

        viewModel.getStudentCandidates().observe(this, candidates -> {
            if (candidates != null && currentSelectedCourse != null) {
                displayStudentChoices(currentSelectedCourse, candidates);
            }
        });

        viewModel.getEnrolledStudents().observe(this, students -> {
            if (students != null && currentSelectedCourse != null) {
                displayEnrolledStudents(currentSelectedCourse, students);
            }
        });

        viewModel.getEnrollEvent().observe(this, event -> {
            Boolean success = event == null ? null : event.consume();
            if (success == null) {
                return;
            }
            if (activeDialog != null && activeDialog.isShowing()) {
                if (success) {
                    activeDialog.dismiss();
                    activeDialog = null;
                    Toast.makeText(this, "Ghi danh thành công", Toast.LENGTH_SHORT).show();
                } else {
                    activeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    Toast.makeText(this, "Không thể ghi danh. Dữ liệu chưa được thay đổi.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getUnenrollEvent().observe(this, event -> {
            Boolean success = event == null ? null : event.consume();
            if (success == null) {
                return;
            }
            if (!success) {
                Toast.makeText(this, "Ghi danh không còn tồn tại", Toast.LENGTH_SHORT).show();
            } else {
                if (activeDialog != null && activeDialog.isShowing()) {
                    activeDialog.dismiss();
                    activeDialog = null;
                }
                if (currentSelectedCourse != null) {
                    viewModel.fetchEnrolledStudents(currentSelectedCourse.getId());
                }
            }
        });
    }

    @Override
    protected ListRow render(CourseSummary summary) {
        Course course = summary.getCourse();
        return new ListRow(
                course.getName(),
                course.getCode() + " • " + course.getLanguage() + " • " + course.getLevel(),
                summary.getStudentCount() + " học viên • Chạm để xem danh sách"
        );
    }

    @Override
    protected long stableId(CourseSummary summary) {
        return summary.getCourse().getId();
    }

    private void chooseCourse() {
        addButton.setEnabled(false);
        viewModel.fetchCourseChoices();
    }

    private void displayCourseChoices(List<Course> courses) {
        if (courses.isEmpty()) {
            Toast.makeText(this, "Chưa có khóa học để ghi danh", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] labels = new String[courses.size()];
        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);
            labels[i] = course.getCode() + " • " + course.getName();
        }

        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
        }

        activeDialog = new AlertDialog.Builder(this)
                .setTitle("Bước 1/2 • Chọn khóa học")
                .setItems(labels, (dialog, position) -> {
                    currentSelectedCourse = courses.get(position);
                    viewModel.fetchStudentCandidates(currentSelectedCourse.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void displayStudentChoices(Course course, EnrollmentCandidates candidates) {
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
        }

        switch (candidates.getStatus()) {
            case NO_STUDENTS:
                Toast.makeText(this, "Chưa có học viên để ghi danh", Toast.LENGTH_SHORT).show();
                return;
            case ALL_ENROLLED:
                activeDialog = new AlertDialog.Builder(this)
                        .setTitle("Không còn học viên")
                        .setMessage("Tất cả học viên đã được gán vào khóa “" + course.getName() + "”.")
                        .setNegativeButton("Chọn khóa khác", (dialog, which) -> chooseCourse())
                        .setPositiveButton("Đóng", null)
                        .show();
                return;
            case AVAILABLE:
                showStudentSelectionDialog(course, candidates.getStudents());
                return;
        }
    }

    private void showStudentSelectionDialog(Course course, List<Student> availableStudents) {
        String[] labels = studentLabels(availableStudents);
        boolean[] selected = new boolean[availableStudents.size()];
        activeDialog = new AlertDialog.Builder(this)
                .setTitle("Bước 2/2 • Chọn học viên\n" + course.getName())
                .setMultiChoiceItems(labels, selected,
                        (currentDialog, position, checked) -> selected[position] = checked)
                .setNegativeButton("Quay lại", (currentDialog, which) -> chooseCourse())
                .setPositiveButton("Xác nhận", null)
                .create();

        activeDialog.setOnShowListener(ignored -> activeDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> confirmEnrollment(
                        activeDialog, course, availableStudents, selected
                )));
        activeDialog.show();
    }

    private void displayEnrolledStudents(Course course, List<Student> students) {
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
        }

        if (students.isEmpty()) {
            activeDialog = new AlertDialog.Builder(this)
                    .setTitle(course.getName())
                    .setMessage("Khóa học này chưa có học viên.")
                    .setNegativeButton("Đóng", null)
                    .setPositiveButton("Gán học viên", (dialog, which) -> {
                        currentSelectedCourse = course;
                        viewModel.fetchStudentCandidates(course.getId());
                    })
                    .show();
            return;
        }

        String[] labels = studentLabels(students);
        boolean[] selected = new boolean[students.size()];
        activeDialog = new AlertDialog.Builder(this)
                .setTitle(course.getName() + " • " + students.size()
                        + " học viên\nChọn nhiều để hủy ghi danh")
                .setMultiChoiceItems(labels, selected,
                        (currentDialog, position, checked) -> selected[position] = checked)
                .setNegativeButton("Đóng", null)
                .setNeutralButton("Gán thêm", (currentDialog, which) -> {
                    currentSelectedCourse = course;
                    viewModel.fetchStudentCandidates(course.getId());
                })
                .setPositiveButton("Hủy đã chọn", null)
                .create();
        activeDialog.setOnShowListener(ignored -> activeDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view ->
                        confirmUnenrollSelected(course, students, selected)));
        activeDialog.show();
    }

    private String[] studentLabels(List<Student> students) {
        String[] labels = new String[students.size()];
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            labels[i] = student.getCode() + " • " + student.getName()
                    + "\n" + student.getAge() + " tuổi • Scratch: "
                    + student.getScratchLevel();
        }
        return labels;
    }

    private void confirmEnrollment(AlertDialog dialog, Course course,
                                   List<Student> students, boolean[] selected) {
        List<Long> selectedStudentIds = getSelectedStudentIds(students, selected);
        if (selectedStudentIds.isEmpty()) {
            Toast.makeText(this, "Hãy chọn ít nhất một học viên", Toast.LENGTH_SHORT).show();
            return;
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        viewModel.enrollStudents(course.getId(), selectedStudentIds);
    }

    private List<Long> getSelectedStudentIds(List<Student> students, boolean[] selected) {
        List<Long> selectedStudentIds = new ArrayList<>();
        for (int index = 0; index < students.size(); index++) {
            if (selected[index]) {
                selectedStudentIds.add(students.get(index).getId());
            }
        }
        return selectedStudentIds;
    }

    private void confirmUnenrollSelected(
            Course course,
            List<Student> students,
            boolean[] selected
    ) {
        List<Long> selectedStudentIds = getSelectedStudentIds(students, selected);
        if (selectedStudentIds.isEmpty()) {
            Toast.makeText(this, "Hãy chọn ít nhất một học viên", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Hủy nhiều ghi danh")
                .setMessage("Hủy " + selectedStudentIds.size() + " học viên khỏi "
                        + course.getName() + "?")
                .setNegativeButton("Không", null)
                .setPositiveButton("Hủy ghi danh", (dialog, which) -> {
                    viewModel.unenrollStudents(course.getId(), selectedStudentIds);
                })
                .show();
    }
}
