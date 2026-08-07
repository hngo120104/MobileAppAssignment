package com.example.quanlynhansu.domain.repository;

import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.CourseStatistic;
import com.example.quanlynhansu.domain.model.CourseSummary;
import com.example.quanlynhansu.domain.model.Enrollment;
import com.example.quanlynhansu.domain.model.Student;

import java.util.List;

public interface ClassroomRepository {
    List<Student> getStudents();
    long saveStudent(Student student);
    boolean deleteStudent(long studentId);
    boolean deleteStudents(List<Long> studentIds);

    List<Course> getCourses();
    List<CourseSummary> getCourseSummaries();
    long saveCourse(Course course);
    boolean deleteCourse(long courseId);
    boolean deleteCourses(List<Long> courseIds);

    boolean enrollStudents(List<Long> studentIds, long courseId);
    boolean unenrollStudents(List<Long> studentIds, long courseId);

    List<Student> getStudentsByCourse(long courseId);
    List<Student> getPythonBasicStudentsAged10To12();
    List<Student> getUnenrolledStudents();
    List<Student> getStudentsByAgeRange(int minimumAge, int maximumAge);
    List<CourseStatistic> getCourseStatisticsByLanguage();
    List<CourseStatistic> getCourseStatisticsByLevel();
    List<Enrollment> getEnrollmentsByDateRange(String startDate, String endDate);
}
