package com.example.quanlynhansu;

import android.app.Application;

import com.example.quanlynhansu.data.local.ClassroomDatabase;
import com.example.quanlynhansu.data.repository.SqliteClassroomRepository;
import com.example.quanlynhansu.domain.repository.ClassroomRepository;

public final class ClassroomApplication extends Application {
    private ClassroomRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        repository = new SqliteClassroomRepository(new ClassroomDatabase(this));
    }

    public ClassroomRepository getRepository() {
        return repository;
    }
}
