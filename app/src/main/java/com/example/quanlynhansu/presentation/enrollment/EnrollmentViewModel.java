package com.example.quanlynhansu.presentation.enrollment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.CourseSummary;
import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.usecase.EnrollmentCandidates;
import com.example.quanlynhansu.domain.usecase.EnrollmentUseCase;
import com.example.quanlynhansu.presentation.common.BaseListViewModel;
import com.example.quanlynhansu.presentation.common.UiEvent;

import java.util.List;

public final class EnrollmentViewModel extends BaseListViewModel<CourseSummary> {
    private final EnrollmentUseCase useCase;
    private final MutableLiveData<List<Course>> courseChoices = new MutableLiveData<>();
    private final MutableLiveData<EnrollmentCandidates> studentCandidates = new MutableLiveData<>();
    private final MutableLiveData<List<Student>> enrolledStudents = new MutableLiveData<>();
    private final MutableLiveData<UiEvent<Boolean>> enrollEvent = new MutableLiveData<>();
    private final MutableLiveData<UiEvent<Boolean>> unenrollEvent = new MutableLiveData<>();

    public EnrollmentViewModel(EnrollmentUseCase useCase) {
        this.useCase = useCase;
    }

    public LiveData<List<Course>> getCourseChoices() {
        return courseChoices;
    }

    public LiveData<EnrollmentCandidates> getStudentCandidates() {
        return studentCandidates;
    }

    public LiveData<List<Student>> getEnrolledStudents() {
        return enrolledStudents;
    }

    public LiveData<UiEvent<Boolean>> getEnrollEvent() {
        return enrollEvent;
    }

    public LiveData<UiEvent<Boolean>> getUnenrollEvent() {
        return unenrollEvent;
    }

    @Override
    protected void onInitialLoad() {
        loadCourses();
    }

    @Override
    protected long stableId(CourseSummary item) {
        return item.getCourse().getId();
    }

    public void loadCourses() {
        showLoading();
        executeDatabase(useCase::getCourseSummaries, summaries -> {
            replaceItems(summaries);
            hideLoading();
        }, () -> {
            hideLoading();
            showError("Lỗi tải danh sách ghi danh.");
        });
    }

    public void fetchCourseChoices() {
        executeDatabase(useCase::getCourses, courseChoices::setValue);
    }

    public void fetchStudentCandidates(long courseId) {
        executeDatabase(
                () -> useCase.getCandidates(courseId),
                studentCandidates::setValue
        );
    }

    public void fetchEnrolledStudents(long courseId) {
        executeDatabase(
                () -> useCase.getEnrolledStudents(courseId),
                enrolledStudents::setValue
        );
    }

    public void enrollStudents(long courseId, List<Long> studentIds) {
        executeDatabase(
                () -> useCase.enroll(courseId, studentIds),
                enrolled -> {
                    enrollEvent.setValue(new UiEvent<>(enrolled));
                    if (enrolled) {
                        loadCourses();
                    }
                },
                () -> {
                    enrollEvent.setValue(new UiEvent<>(false));
                    showError("Không thể ghi danh. Vui lòng thử lại.");
                }
        );
    }

    public void unenrollStudents(long courseId, List<Long> studentIds) {
        executeDatabase(() -> useCase.unenroll(courseId, studentIds), removed -> {
            unenrollEvent.setValue(new UiEvent<>(removed));
            if (removed) {
                loadCourses();
            }
        });
    }
}
