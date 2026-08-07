package com.example.quanlynhansu.presentation.report;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.CourseStatistic;
import com.example.quanlynhansu.domain.model.CourseSummary;
import com.example.quanlynhansu.domain.model.Enrollment;
import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.usecase.ReportUseCase;
import com.example.quanlynhansu.domain.validation.ReportFilterValidator;
import com.example.quanlynhansu.presentation.common.BaseListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ReportViewModel extends BaseListViewModel<ReportRow> {
    public static final int REPORT_NONE = 0;
    public static final int REPORT_COURSE = 1;
    public static final int REPORT_ALL_COURSES = 2;
    public static final int REPORT_UNENROLLED = 3;
    public static final int REPORT_AGE_RANGE = 4;
    public static final int REPORT_LANGUAGE = 5;
    public static final int REPORT_LEVEL = 6;
    public static final int REPORT_ENROLLMENT_TIME = 7;
    public static final int REPORT_PYTHON = 8;

    private final ReportUseCase useCase;
    private final MutableLiveData<List<Course>> courses = new MutableLiveData<>();
    private final MutableLiveData<String> resultTitle = new MutableLiveData<>();
    private int currentReport = REPORT_NONE;
    private long currentCourseId = -1;

    public ReportViewModel(ReportUseCase useCase) {
        this.useCase = useCase;
    }

    public LiveData<List<Course>> getCourses() {
        return courses;
    }

    public LiveData<String> getResultTitle() {
        return resultTitle;
    }

    @Override
    protected void onInitialLoad() {
        loadCourses();
    }

    @Override
    protected long stableId(ReportRow item) {
        return item.getId();
    }

    public void loadCourses() {
        showLoading();
        executeDatabase(useCase::getCourses, loadedCourses -> {
            courses.setValue(loadedCourses);
            hideLoading();
        }, () -> {
            hideLoading();
            showError("Lỗi tải danh sách khóa học.");
        });
    }

    public void showStudentsByCourse(Course course) {
        if (course == null) {
            return;
        }
        currentReport = REPORT_COURSE;
        currentCourseId = course.getId();
        loadReport(
                () -> useCase.getStudentsByCourse(course.getId()),
                this::studentRows,
                students -> "Thuộc " + course.getName() + " (" + students.size() + ")",
                "Lỗi tải báo cáo theo khóa học."
        );
    }

    public void showAllCourseSummaries() {
        currentReport = REPORT_ALL_COURSES;
        currentCourseId = -1;
        loadReport(
                useCase::getAllCourseSummaries,
                this::courseSummaryRows,
                summaries -> "Tất cả khóa học (" + summaries.size() + ")",
                "Lỗi tải báo cáo tổng hợp khóa học."
        );
    }

    public void showUnenrolledStudents() {
        currentReport = REPORT_UNENROLLED;
        currentCourseId = -1;
        loadReport(
                useCase::getUnenrolledStudents,
                this::studentRows,
                students -> "Học viên chưa ghi danh (" + students.size() + ")",
                "Lỗi tải học viên chưa ghi danh."
        );
    }

    public ReportFilterValidator.Error showStudentsByAgeRange(
            String minimumText,
            String maximumText
    ) {
        ReportFilterValidator.AgeRange range = useCase.prepareAgeRange(
                minimumText,
                maximumText
        );
        if (!range.isValid()) {
            return range.getError();
        }
        currentReport = REPORT_AGE_RANGE;
        currentCourseId = -1;
        loadReport(
                () -> useCase.getStudentsByAgeRange(range.getMinimum(), range.getMaximum()),
                this::studentRows,
                students -> "Học viên " + range.getMinimum() + "-" + range.getMaximum() +
                        " tuổi (" + students.size() + ")",
                "Lỗi tải báo cáo theo độ tuổi."
        );
        return ReportFilterValidator.Error.NONE;
    }

    public void showStatisticsByLanguage() {
        currentReport = REPORT_LANGUAGE;
        currentCourseId = -1;
        loadStatistics(
                useCase::getCourseStatisticsByLanguage,
                "Thống kê theo ngôn ngữ"
        );
    }

    public void showStatisticsByLevel() {
        currentReport = REPORT_LEVEL;
        currentCourseId = -1;
        loadStatistics(
                useCase::getCourseStatisticsByLevel,
                "Thống kê theo cấp độ"
        );
    }

    public ReportFilterValidator.Error showEnrollmentsByDateRange(
            String startText,
            String endText
    ) {
        ReportFilterValidator.DateRange range = useCase.prepareDateRange(
                startText,
                endText
        );
        if (!range.isValid()) {
            return range.getError();
        }
        currentReport = REPORT_ENROLLMENT_TIME;
        currentCourseId = -1;
        loadReport(
                () -> useCase.getEnrollmentsByDateRange(
                        range.getStartDate(),
                        range.getEndDate()
                ),
                this::enrollmentRows,
                enrollments -> "Ghi danh từ " + range.getStartDate() + " đến " +
                        range.getEndDate() + " (" + enrollments.size() + ")",
                "Lỗi tải báo cáo theo thời gian."
        );
        return ReportFilterValidator.Error.NONE;
    }

    public void showPythonBasicStudents() {
        currentReport = REPORT_PYTHON;
        currentCourseId = -1;
        loadReport(
                useCase::getPythonBasicStudentsAged10To12,
                this::studentRows,
                students -> "10-12 tuổi học Python cơ bản (" + students.size() + ")",
                "Lỗi tải báo cáo Python cơ bản."
        );
    }

    private void loadStatistics(
            Supplier<List<CourseStatistic>> operation,
            String title
    ) {
        loadReport(
                operation,
                this::statisticRows,
                statistics -> title + " (" + statistics.size() + " nhóm)",
                "Lỗi tải thống kê khóa học."
        );
    }

    private <T> void loadReport(
            Supplier<List<T>> operation,
            Function<List<T>, List<ReportRow>> rowMapper,
            Function<List<T>, String> titleMapper,
            String errorMessage
    ) {
        showLoading();
        executeDatabase(operation, results -> {
            replaceItems(rowMapper.apply(results));
            resultTitle.setValue(titleMapper.apply(results));
            hideLoading();
        }, () -> {
            hideLoading();
            showError(errorMessage);
        });
    }

    private List<ReportRow> studentRows(List<Student> students) {
        List<ReportRow> rows = new ArrayList<>();
        for (Student student : students) {
            rows.add(new ReportRow(
                    student.getId(),
                    student.getName(),
                    student.getCode(),
                    student.getAge() + " tuổi • Scratch: " + student.getScratchLevel()
            ));
        }
        return rows;
    }

    private List<ReportRow> courseSummaryRows(List<CourseSummary> summaries) {
        List<ReportRow> rows = new ArrayList<>();
        for (CourseSummary summary : summaries) {
            Course course = summary.getCourse();
            rows.add(new ReportRow(
                    course.getId(),
                    course.getName(),
                    course.getCode() + " • " + course.getLanguage() + " • " + course.getLevel(),
                    summary.getStudentCount() + " học viên • " + course.getStartDate() +
                            " → " + course.getEndDate()
            ));
        }
        return rows;
    }

    private List<ReportRow> statisticRows(List<CourseStatistic> statistics) {
        List<ReportRow> rows = new ArrayList<>();
        for (CourseStatistic statistic : statistics) {
            rows.add(new ReportRow(
                    statistic.getGroupName().hashCode(),
                    statistic.getGroupName(),
                    statistic.getCourseCount() + " khóa học",
                    statistic.getEnrollmentCount() + " lượt ghi danh"
            ));
        }
        return rows;
    }

    private List<ReportRow> enrollmentRows(List<Enrollment> enrollments) {
        List<ReportRow> rows = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            Course course = enrollment.getCourse();
            rows.add(new ReportRow(
                    student.getId() * 1_000_003L + course.getId(),
                    student.getName(),
                    student.getCode() + " • " + course.getCode() + " • " + course.getName(),
                    "Ghi danh: " + enrollment.getEnrolledAt() + " • Khóa: " +
                            course.getStartDate() + " → " + course.getEndDate()
            ));
        }
        return rows;
    }

    public int getCurrentReport() {
        return currentReport;
    }

    public long getCurrentCourseId() {
        return currentCourseId;
    }
}
