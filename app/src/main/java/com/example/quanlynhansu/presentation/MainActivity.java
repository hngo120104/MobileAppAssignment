package com.example.quanlynhansu.presentation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlynhansu.R;
import com.example.quanlynhansu.presentation.common.SystemBars;
import com.example.quanlynhansu.presentation.course.CourseActivity;
import com.example.quanlynhansu.presentation.enrollment.EnrollmentActivity;
import com.example.quanlynhansu.presentation.report.ReportActivity;
import com.example.quanlynhansu.presentation.student.StudentActivity;

public final class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemBars.applyInsets(findViewById(android.R.id.content));
        open(R.id.btnStudents, StudentActivity.class);
        open(R.id.btnCourses, CourseActivity.class);
        open(R.id.btnEnrollments, EnrollmentActivity.class);
        open(R.id.btnReports, ReportActivity.class);
    }

    private void open(int viewId, Class<?> target) {
        findViewById(viewId).setOnClickListener(
                view -> startActivity(new Intent(this, target))
        );
    }
}
