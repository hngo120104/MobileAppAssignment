package com.example.quanlynhansu.di;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.quanlynhansu.domain.repository.ClassroomRepository;
import com.example.quanlynhansu.domain.usecase.CourseUseCase;
import com.example.quanlynhansu.domain.usecase.EnrollmentUseCase;
import com.example.quanlynhansu.domain.usecase.ReportUseCase;
import com.example.quanlynhansu.domain.usecase.StudentUseCase;
import com.example.quanlynhansu.domain.validation.CourseInputValidator;
import com.example.quanlynhansu.domain.validation.ReportFilterValidator;
import com.example.quanlynhansu.domain.validation.StudentInputValidator;
import com.example.quanlynhansu.presentation.course.CourseViewModel;
import com.example.quanlynhansu.presentation.enrollment.EnrollmentViewModel;
import com.example.quanlynhansu.presentation.report.ReportViewModel;
import com.example.quanlynhansu.presentation.student.StudentViewModel;

public final class ClassroomViewModelFactory implements ViewModelProvider.Factory {
    private final ClassroomRepository repository;

    public ClassroomViewModelFactory(ClassroomRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass == StudentViewModel.class) {
            return (T) new StudentViewModel(new StudentUseCase(
                    repository,
                    new StudentInputValidator()
            ));
        }
        if (modelClass == CourseViewModel.class) {
            return (T) new CourseViewModel(new CourseUseCase(
                    repository,
                    new CourseInputValidator()
            ));
        }
        if (modelClass == EnrollmentViewModel.class) {
            return (T) new EnrollmentViewModel(new EnrollmentUseCase(repository));
        }
        if (modelClass == ReportViewModel.class) {
            return (T) new ReportViewModel(new ReportUseCase(
                    repository,
                    new ReportFilterValidator()
            ));
        }
        throw new IllegalArgumentException("Unsupported ViewModel: " + modelClass.getName());
    }
}
