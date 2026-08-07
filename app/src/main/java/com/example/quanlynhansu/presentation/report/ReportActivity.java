package com.example.quanlynhansu.presentation.report;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quanlynhansu.R;
import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.validation.ReportFilterValidator;
import com.example.quanlynhansu.presentation.common.BaseMvvmListActivity;
import com.example.quanlynhansu.presentation.common.ListRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReportActivity extends BaseMvvmListActivity<ReportRow, ReportViewModel> {
    private List<Course> currentCourses = Collections.emptyList();
    private Course selectedCourse;
    private Spinner reportTypeInput;
    private AutoCompleteTextView courseInput;
    private LinearLayout courseFilter;
    private LinearLayout ageFilter;
    private LinearLayout dateFilter;
    private EditText minimumAgeInput;
    private EditText maximumAgeInput;
    private EditText startDateInput;
    private EditText endDateInput;
    private Button runButton;

    @Override
    protected Class<ReportViewModel> getViewModelClass() {
        return ReportViewModel.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        setTitle("Báo cáo & truy vấn");
        setupList(findViewById(R.id.list));
        bindViews();
        setupReportTypes();
        setupCourseDropdown();
        setupObservers();
        runButton.setOnClickListener(view -> runSelectedReport());
        viewModel.loadInitialData();
    }

    private void bindViews() {
        reportTypeInput = findViewById(R.id.reportTypeInput);
        courseInput = findViewById(R.id.courseInput);
        courseFilter = findViewById(R.id.courseFilter);
        ageFilter = findViewById(R.id.ageFilter);
        dateFilter = findViewById(R.id.dateFilter);
        minimumAgeInput = findViewById(R.id.minimumAgeInput);
        maximumAgeInput = findViewById(R.id.maximumAgeInput);
        startDateInput = findViewById(R.id.startDateInput);
        endDateInput = findViewById(R.id.endDateInput);
        runButton = findViewById(R.id.btnRunReport);
    }

    private void setupReportTypes() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.report_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportTypeInput.setAdapter(adapter);
        reportTypeInput.setSelection(viewModel.getCurrentReport());
        reportTypeInput.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        showFilterFor(position);
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                        showFilterFor(ReportViewModel.REPORT_NONE);
                    }
                }
        );
        showFilterFor(viewModel.getCurrentReport());
    }

    private void setupObservers() {
        viewModel.getCourses().observe(this, courses -> {
            if (courses != null) {
                displayCourses(courses);
            }
        });
        viewModel.getResultTitle().observe(this, title -> {
            if (title != null) {
                ((TextView) findViewById(R.id.txtCount)).setText(title);
            }
        });
        viewModel.getIsLoading().observe(this, loading ->
                setReportControlsEnabled(!Boolean.TRUE.equals(loading))
        );
    }

    private void setupCourseDropdown() {
        courseInput.setThreshold(0);
        courseInput.setOnClickListener(view -> courseInput.showDropDown());
        courseInput.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLabel = parent.getItemAtPosition(position).toString();
            selectedCourse = findCourseByLabel(selectedLabel);
        });
    }

    private void displayCourses(List<Course> loadedCourses) {
        currentCourses = loadedCourses;
        List<String> labels = new ArrayList<>();
        for (Course course : currentCourses) {
            labels.add(courseLabel(course));
        }
        courseInput.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                labels
        ));

        if (viewModel.getCurrentReport() == ReportViewModel.REPORT_COURSE) {
            for (Course course : currentCourses) {
                if (course.getId() == viewModel.getCurrentCourseId()) {
                    selectedCourse = course;
                    courseInput.setText(courseLabel(course), false);
                    break;
                }
            }
        }
        setReportControlsEnabled(true);
    }

    private void showFilterFor(int reportType) {
        courseFilter.setVisibility(
                reportType == ReportViewModel.REPORT_COURSE ? View.VISIBLE : View.GONE
        );
        ageFilter.setVisibility(
                reportType == ReportViewModel.REPORT_AGE_RANGE ? View.VISIBLE : View.GONE
        );
        dateFilter.setVisibility(
                reportType == ReportViewModel.REPORT_ENROLLMENT_TIME ? View.VISIBLE : View.GONE
        );
        runButton.setEnabled(reportType != ReportViewModel.REPORT_NONE);
    }

    private void runSelectedReport() {
        int reportType = reportTypeInput.getSelectedItemPosition();
        switch (reportType) {
            case ReportViewModel.REPORT_COURSE:
                Course course = findCourseByLabel(courseInput.getText().toString());
                if (course == null) {
                    Toast.makeText(this, "Hãy chọn một khóa học", Toast.LENGTH_SHORT).show();
                } else {
                    selectedCourse = course;
                    viewModel.showStudentsByCourse(course);
                }
                return;
            case ReportViewModel.REPORT_ALL_COURSES:
                viewModel.showAllCourseSummaries();
                return;
            case ReportViewModel.REPORT_UNENROLLED:
                viewModel.showUnenrolledStudents();
                return;
            case ReportViewModel.REPORT_AGE_RANGE:
                showAgeReport();
                return;
            case ReportViewModel.REPORT_LANGUAGE:
                viewModel.showStatisticsByLanguage();
                return;
            case ReportViewModel.REPORT_LEVEL:
                viewModel.showStatisticsByLevel();
                return;
            case ReportViewModel.REPORT_ENROLLMENT_TIME:
                showEnrollmentTimeReport();
                return;
            case ReportViewModel.REPORT_PYTHON:
                viewModel.showPythonBasicStudents();
                return;
            case ReportViewModel.REPORT_NONE:
            default:
                Toast.makeText(this, "Hãy chọn loại báo cáo", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAgeReport() {
        ReportFilterValidator.Error error = viewModel.showStudentsByAgeRange(
                minimumAgeInput.getText().toString().trim(),
                maximumAgeInput.getText().toString().trim()
        );
        switch (error) {
            case MINIMUM_AGE_INVALID:
                minimumAgeInput.setError("Tuổi tối thiểu không hợp lệ");
                break;
            case MAXIMUM_AGE_INVALID:
                maximumAgeInput.setError("Tuổi tối đa không hợp lệ");
                break;
            case AGE_RANGE_INVALID:
                maximumAgeInput.setError("Khoảng tuổi phải nằm trong 5-18");
                break;
            case NONE:
            default:
                break;
        }
    }

    private void showEnrollmentTimeReport() {
        ReportFilterValidator.Error error = viewModel.showEnrollmentsByDateRange(
                startDateInput.getText().toString().trim(),
                endDateInput.getText().toString().trim()
        );
        switch (error) {
            case START_DATE_INVALID:
                startDateInput.setError("Định dạng yyyy-MM-dd");
                break;
            case END_DATE_INVALID:
                endDateInput.setError("Định dạng yyyy-MM-dd");
                break;
            case DATE_RANGE_INVALID:
                endDateInput.setError("Ngày kết thúc phải từ ngày bắt đầu trở đi");
                break;
            case NONE:
            default:
                break;
        }
    }

    private Course findCourseByLabel(String selectedLabel) {
        for (Course course : currentCourses) {
            if (courseLabel(course).equals(selectedLabel)) {
                return course;
            }
        }
        return null;
    }

    private String courseLabel(Course course) {
        return course.getCode() + " • " + course.getName();
    }

    private void setReportControlsEnabled(boolean enabled) {
        reportTypeInput.setEnabled(enabled);
        courseInput.setEnabled(enabled && !currentCourses.isEmpty());
        minimumAgeInput.setEnabled(enabled);
        maximumAgeInput.setEnabled(enabled);
        startDateInput.setEnabled(enabled);
        endDateInput.setEnabled(enabled);
        runButton.setEnabled(
                enabled && reportTypeInput.getSelectedItemPosition() != ReportViewModel.REPORT_NONE
        );
    }

    @Override
    protected ListRow render(ReportRow row) {
        return new ListRow(row.getTitle(), row.getSubtitle(), row.getMetadata());
    }

    @Override
    protected long stableId(ReportRow row) {
        return row.getId();
    }
}
