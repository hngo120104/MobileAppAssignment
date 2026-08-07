package com.example.quanlynhansu.presentation.student;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.usecase.StudentUseCase;
import com.example.quanlynhansu.domain.validation.StudentInputValidator;
import com.example.quanlynhansu.presentation.common.BaseListViewModel;
import com.example.quanlynhansu.presentation.common.UiEvent;

import java.util.ArrayList;
import java.util.List;

public final class StudentViewModel extends BaseListViewModel<Student> {
    private final StudentUseCase useCase;
    private final MutableLiveData<UiEvent<Boolean>> saveEvent = new MutableLiveData<>();

    public StudentViewModel(StudentUseCase useCase) {
        this.useCase = useCase;
    }

    public LiveData<UiEvent<Boolean>> getSaveEvent() {
        return saveEvent;
    }

    @Override
    protected void onInitialLoad() {
        loadStudents();
    }

    @Override
    protected long stableId(Student item) {
        return item.getId();
    }

    public void loadStudents() {
        showLoading();
        executeDatabase(useCase::getStudents, students -> {
            replaceItems(students);
            hideLoading();
        }, () -> {
            hideLoading();
            showError("Lỗi tải danh sách học viên.");
        });
    }

    public StudentInputValidator.Error saveStudent(
            long id,
            String code,
            String name,
            String ageText,
            String scratchLevel
    ) {
        StudentUseCase.PreparedStudent prepared = useCase.prepareStudent(
                id,
                code,
                name,
                ageText,
                scratchLevel
        );
        if (!prepared.isValid()) {
            return prepared.getError();
        }

        executeDatabase(
                () -> useCase.saveStudent(prepared.getStudent()),
                saved -> {
                    saveEvent.setValue(new UiEvent<>(saved));
                    if (saved) {
                        loadStudents();
                    } else {
                        showError("Lưu thất bại. Mã đã tồn tại hoặc có lỗi xảy ra.");
                    }
                },
                () -> {
                    saveEvent.setValue(new UiEvent<>(false));
                    showError("Không thể lưu học viên. Vui lòng thử lại.");
                }
        );
        return StudentInputValidator.Error.NONE;
    }

    public void deleteStudent(long studentId) {
        executeDatabase(() -> useCase.deleteStudent(studentId), deleted -> {
            if (deleted) {
                loadStudents();
            } else {
                showError("Học viên không còn tồn tại.");
            }
        });
    }

    public void deleteSelectedStudents() {
        List<Student> selected = getSelectedItemsList();
        if (selected.isEmpty()) {
            return;
        }

        List<Long> ids = new ArrayList<>();
        for (Student student : selected) {
            ids.add(student.getId());
        }

        executeDatabase(() -> useCase.deleteStudents(ids), deleted -> {
            if (deleted) {
                setSelectionMode(false);
                loadStudents();
            } else {
                showError("Không thể xóa toàn bộ học viên đã chọn.");
            }
        });
    }
}
