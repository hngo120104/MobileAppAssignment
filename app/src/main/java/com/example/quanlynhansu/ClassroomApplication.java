package com.example.quanlynhansu;

import android.app.Application;

import com.example.quanlynhansu.data.local.ClassroomDatabase;
import com.example.quanlynhansu.data.repository.SqliteClassroomRepository;
import com.example.quanlynhansu.di.ClassroomViewModelFactory;
import com.example.quanlynhansu.domain.repository.ClassroomRepository;

import androidx.lifecycle.ViewModelProvider;

public final class ClassroomApplication extends Application {
    private ViewModelProvider.Factory viewModelFactory;

    @Override
    public void onCreate() {
        super.onCreate();
        ClassroomRepository repository = new SqliteClassroomRepository(
                new ClassroomDatabase(this)
        );
        viewModelFactory = new ClassroomViewModelFactory(repository);
    }

    public ViewModelProvider.Factory getViewModelFactory() {
        return viewModelFactory;
    }
}
