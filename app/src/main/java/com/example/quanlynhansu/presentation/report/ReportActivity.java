package com.example.quanlynhansu.presentation.report;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quanlynhansu.R;
import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.presentation.common.BaseListActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReportActivity extends BaseListActivity<Student> {
    private List<Course> courses = Collections.emptyList();
    private Spinner courseSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        setTitle("Báo cáo & truy vấn");
        setupList(findViewById(R.id.list));
        setupCourseSpinner();
        findViewById(R.id.btnByCourse).setOnClickListener(view -> showStudentsByCourse());
        findViewById(R.id.btnPython).setOnClickListener(
                view -> executeDatabase(
                        repository::getPythonBasicStudentsAged10To12,
                        this::showResults
                )
        );
    }

    private void setupCourseSpinner() {
        courseSpinner = findViewById(R.id.spCourse);
        executeDatabase(repository::getCourses, this::displayCourses);
    }

    private void displayCourses(List<Course> loadedCourses) {
        courses = loadedCourses;
        List<String> labels = new ArrayList<>();
        for (Course course : courses) labels.add(course.getCode() + " • " + course.getName());
        courseSpinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, labels
        ));
    }

    private void showStudentsByCourse() {
        if (courses.isEmpty()) {
            Toast.makeText(this, "Chưa có khóa học", Toast.LENGTH_SHORT).show();
            return;
        }
        Course course = courses.get(courseSpinner.getSelectedItemPosition());
        executeDatabase(
                () -> repository.getStudentsByCourse(course.getId()),
                this::showResults
        );
    }

    private void showResults(List<Student> students) {
        replaceItems(students);
        ((TextView) findViewById(R.id.txtCount)).setText(
                getString(R.string.student_result_count, students.size())
        );
    }

    @Override
    protected String render(Student student) {
        return student.getCode() + " • " + student.getName() +
                "\nTuổi: " + student.getAge() + "  |  Scratch: " + student.getScratchLevel();
    }
}
