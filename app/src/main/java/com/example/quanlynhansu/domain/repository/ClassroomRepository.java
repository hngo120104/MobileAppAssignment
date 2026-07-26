package com.example.quanlynhansu.domain.repository;

import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.CourseSummary;
import com.example.quanlynhansu.domain.model.Student;

import java.util.List;

public interface ClassroomRepository {
    List<Student> getStudents();
    long saveStudent(Student student);
    boolean deleteStudent(long studentId);

    List<Course> getCourses();
    List<CourseSummary> getCourseSummaries();
    long saveCourse(Course course);
    boolean deleteCourse(long courseId);

    boolean enrollStudents(List<Long> studentIds, long courseId);
    boolean unenroll(long studentId, long courseId);

    List<Student> getStudentsByCourse(long courseId);
    List<Student> getPythonBasicStudentsAged10To12();
}
