package com.example.quanlynhansu.presentation.course;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.usecase.CourseUseCase;
import com.example.quanlynhansu.domain.validation.CourseInputValidator;
import com.example.quanlynhansu.presentation.common.BaseListViewModel;
import com.example.quanlynhansu.presentation.common.UiEvent;

import java.util.ArrayList;
import java.util.List;

public final class CourseViewModel extends BaseListViewModel<Course> {
    private final CourseUseCase useCase;
    private final MutableLiveData<UiEvent<Boolean>> saveEvent = new MutableLiveData<>();

    public CourseViewModel(CourseUseCase useCase) {
        this.useCase = useCase;
    }

    public LiveData<UiEvent<Boolean>> getSaveEvent() {
        return saveEvent;
    }

    @Override
    protected void onInitialLoad() {
        loadCourses();
    }

    @Override
    protected long stableId(Course item) {
        return item.getId();
    }

    public void loadCourses() {
        showLoading();
        executeDatabase(useCase::getCourses, courses -> {
            replaceItems(courses);
            hideLoading();
        }, () -> {
            hideLoading();
            showError("Lỗi tải danh sách khóa học.");
        });
    }

    public CourseInputValidator.Error saveCourse(
            long id,
            String code,
            String name,
            String language,
            String level,
            String startDate,
            String endDate
    ) {
        CourseUseCase.PreparedCourse prepared = useCase.prepareCourse(
                id,
                code,
                name,
                language,
                level,
                startDate,
                endDate
        );
        if (!prepared.isValid()) {
            return prepared.getError();
        }

        executeDatabase(
                () -> useCase.saveCourse(prepared.getCourse()),
                saved -> {
                    saveEvent.setValue(new UiEvent<>(saved));
                    if (saved) {
                        loadCourses();
                    } else {
                        showError("Lưu thất bại. Mã đã tồn tại hoặc có lỗi xảy ra.");
                    }
                },
                () -> {
                    saveEvent.setValue(new UiEvent<>(false));
                    showError("Không thể lưu khóa học. Vui lòng thử lại.");
                }
        );
        return CourseInputValidator.Error.NONE;
    }

    public void deleteCourse(long courseId) {
        executeDatabase(() -> useCase.deleteCourse(courseId), deleted -> {
            if (deleted) {
                loadCourses();
            } else {
                showError("Khóa học không còn tồn tại.");
            }
        });
    }

    public void deleteSelectedCourses() {
        List<Course> selected = getSelectedItemsList();
        if (selected.isEmpty()) {
            return;
        }

        List<Long> ids = new ArrayList<>();
        for (Course course : selected) {
            ids.add(course.getId());
        }

        executeDatabase(() -> useCase.deleteCourses(ids), deleted -> {
            if (deleted) {
                setSelectionMode(false);
                loadCourses();
            } else {
                showError("Không thể xóa toàn bộ khóa học đã chọn.");
            }
        });
    }
}
