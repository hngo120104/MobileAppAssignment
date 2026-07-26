package com.example.quanlynhansu.presentation.enrollment;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.quanlynhansu.R;
import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.CourseSummary;
import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.usecase.EnrollmentCandidates;
import com.example.quanlynhansu.domain.usecase.EnrollmentUseCase;
import com.example.quanlynhansu.presentation.common.BaseListActivity;

import java.util.ArrayList;
import java.util.List;

public final class EnrollmentActivity extends BaseListActivity<CourseSummary> {
    private EnrollmentUseCase enrollmentUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enrollmentUseCase = new EnrollmentUseCase(repository);
        setContentView(R.layout.activity_entity_list);
        setTitle("Ghi danh học viên");

        ListView listView = findViewById(R.id.list);
        setupList(listView);
        listView.setOnItemClickListener((parent, view, position, id) ->
                showEnrolledStudents(items.get(position).getCourse()));

        Button addButton = findViewById(R.id.btnAdd);
        addButton.setText(R.string.choose_course_to_enroll);
        addButton.setOnClickListener(view -> chooseCourse());

        loadCourses();
    }

    private void loadCourses() {
        executeDatabase(repository::getCourseSummaries, summaries -> {
            replaceItems(summaries);
            findViewById(R.id.empty).setVisibility(
                    items.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE
            );
        });
    }

    @Override
    protected String render(CourseSummary summary) {
        Course course = summary.getCourse();
        return course.getCode() + " • " + course.getName() + "\n"
                + course.getLanguage() + " • " + course.getLevel() + "  |  "
                + summary.getStudentCount() + " học viên\nChạm để xem danh sách lớp";
    }

    private void showEnrolledStudents(Course course) {
        executeDatabase(
                () -> repository.getStudentsByCourse(course.getId()),
                students -> displayEnrolledStudents(course, students)
        );
    }

    private void displayEnrolledStudents(Course course, List<Student> students) {
        if (students.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(course.getName())
                    .setMessage("Khóa học này chưa có học viên.")
                    .setNegativeButton("Đóng", null)
                    .setPositiveButton("Gán học viên", (dialog, which) -> chooseStudents(course))
                    .show();
            return;
        }

        String[] labels = studentLabels(students, true);
        new AlertDialog.Builder(this)
                .setTitle(course.getName() + " • " + students.size()
                        + " học viên\nChạm học viên để hủy ghi danh")
                .setItems(labels, (dialog, position) ->
                        confirmUnenroll(course, students.get(position)))
                .setNegativeButton("Đóng", null)
                .setPositiveButton("Gán thêm", (dialog, which) -> chooseStudents(course))
                .show();
    }

    private void chooseCourse() {
        executeDatabase(repository::getCourses, this::displayCourseChoices);
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

        new AlertDialog.Builder(this)
                .setTitle("Bước 1/2 • Chọn khóa học")
                .setItems(labels, (dialog, position) -> chooseStudents(courses.get(position)))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void chooseStudents(Course course) {
        executeDatabase(
                () -> enrollmentUseCase.getCandidates(course.getId()),
                candidates -> displayStudentChoices(course, candidates)
        );
    }

    private void displayStudentChoices(
            Course course,
            EnrollmentCandidates candidates
    ) {
        switch (candidates.getStatus()) {
            case NO_STUDENTS:
                showNoStudentsMessage();
                return;
            case ALL_ENROLLED:
                showAllStudentsEnrolledMessage(course);
                return;
            case AVAILABLE:
                showStudentSelectionDialog(course, candidates.getStudents());
                return;
            default:
                throw new IllegalStateException("Unsupported enrollment candidate status");
        }
    }

    private void showNoStudentsMessage() {
        Toast.makeText(this, "Chưa có học viên để ghi danh", Toast.LENGTH_SHORT).show();
    }

    private void showAllStudentsEnrolledMessage(Course course) {
        new AlertDialog.Builder(this)
                .setTitle("Không còn học viên")
                .setMessage("Tất cả học viên đã được gán vào khóa “" + course.getName() + "”.")
                .setNegativeButton("Chọn khóa khác", (dialog, which) -> chooseCourse())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showStudentSelectionDialog(Course course, List<Student> availableStudents) {
        String[] labels = studentLabels(availableStudents, true);
        boolean[] selected = new boolean[availableStudents.size()];
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Bước 2/2 • Chọn học viên\n" + course.getName())
                .setMultiChoiceItems(labels, selected,
                        (currentDialog, position, checked) -> selected[position] = checked)
                .setNegativeButton("Quay lại", (currentDialog, which) -> chooseCourse())
                .setPositiveButton("Xác nhận", null)
                .create();

        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> confirmEnrollment(
                        dialog, course, availableStudents, selected
                )));
        dialog.show();
    }

    private String[] studentLabels(List<Student> students, boolean includeAge) {
        String[] labels = new String[students.size()];
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            labels[i] = student.getCode() + " • " + student.getName()
                    + (includeAge ? " • " + student.getAge() + " tuổi" : "");
        }
        return labels;
    }

    private void confirmEnrollment(AlertDialog dialog, Course course,
                                   List<Student> students, boolean[] selected) {
        List<Long> selectedStudentIds = getSelectedStudentIds(students, selected);
        final int selectedCount = selectedStudentIds.size();
        if (selectedCount == 0) {
            Toast.makeText(this, "Hãy chọn ít nhất một học viên", Toast.LENGTH_SHORT).show();
            return;
        }

        executeDatabase(
                () -> enrollmentUseCase.enroll(course.getId(), selectedStudentIds),
                enrolled -> {
                    if (!enrolled) {
                        Toast.makeText(
                                this,
                                "Không thể ghi danh. Dữ liệu chưa được thay đổi.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    dialog.dismiss();
                    loadCourses();
                    Toast.makeText(this, "Đã gán " + selectedCount + " học viên vào “"
                            + course.getName() + "”", Toast.LENGTH_SHORT).show();
                }
        );
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

    private void confirmUnenroll(Course course, Student student) {
        new AlertDialog.Builder(this)
                .setTitle("Hủy ghi danh")
                .setMessage("Hủy " + student.getName() + " khỏi " + course.getName() + "?")
                .setNegativeButton("Không", null)
                .setPositiveButton("Hủy ghi danh", (dialog, which) -> {
                    executeDatabase(
                            () -> repository.unenroll(student.getId(), course.getId()),
                            removed -> {
                                if (!removed) {
                                    Toast.makeText(
                                            this,
                                            "Ghi danh không còn tồn tại",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }
                                loadCourses();
                                showEnrolledStudents(course);
                            }
                    );
                })
                .show();
    }

}
