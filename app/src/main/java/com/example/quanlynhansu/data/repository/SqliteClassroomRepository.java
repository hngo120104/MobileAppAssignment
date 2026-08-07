package com.example.quanlynhansu.data.repository;

import com.example.quanlynhansu.data.local.ClassroomDatabase;
import com.example.quanlynhansu.data.local.dao.CourseDao;
import com.example.quanlynhansu.data.local.dao.EnrollmentDao;
import com.example.quanlynhansu.data.local.dao.ReportDao;
import com.example.quanlynhansu.data.local.dao.StudentDao;
import com.example.quanlynhansu.domain.model.Course;
import com.example.quanlynhansu.domain.model.CourseStatistic;
import com.example.quanlynhansu.domain.model.CourseSummary;
import com.example.quanlynhansu.domain.model.Enrollment;
import com.example.quanlynhansu.domain.model.Student;
import com.example.quanlynhansu.domain.repository.ClassroomRepository;

import java.util.List;

public final class SqliteClassroomRepository implements ClassroomRepository {
    private final StudentDao studentDao;
    private final CourseDao courseDao;
    private final EnrollmentDao enrollmentDao;
    private final ReportDao reportDao;

    public SqliteClassroomRepository(ClassroomDatabase database) {
        studentDao = new StudentDao(database);
        courseDao = new CourseDao(database);
        enrollmentDao = new EnrollmentDao(database);
        reportDao = new ReportDao(database);
    }

    @Override
    public List<Student> getStudents() {
        return studentDao.getStudents();
    }

    @Override
    public long saveStudent(Student student) {
        return studentDao.saveStudent(student);
    }

    @Override
    public boolean deleteStudent(long studentId) {
        return studentDao.deleteStudent(studentId);
    }

    @Override
    public boolean deleteStudents(List<Long> studentIds) {
        return studentDao.deleteStudents(studentIds);
    }

    @Override
    public List<Course> getCourses() {
        return courseDao.getCourses();
    }

    @Override
    public List<CourseSummary> getCourseSummaries() {
        return enrollmentDao.getCourseSummaries();
    }

    @Override
    public long saveCourse(Course course) {
        return courseDao.saveCourse(course);
    }

    @Override
    public boolean deleteCourse(long courseId) {
        return courseDao.deleteCourse(courseId);
    }

    @Override
    public boolean deleteCourses(List<Long> courseIds) {
        return courseDao.deleteCourses(courseIds);
    }

    @Override
    public boolean enrollStudents(List<Long> studentIds, long courseId) {
        return enrollmentDao.enrollStudents(studentIds, courseId);
    }

    @Override
    public boolean unenrollStudents(List<Long> studentIds, long courseId) {
        return enrollmentDao.unenrollStudents(studentIds, courseId);
    }

    @Override
    public List<Student> getStudentsByCourse(long courseId) {
        return enrollmentDao.getStudentsByCourse(courseId);
    }

    @Override
    public List<Student> getPythonBasicStudentsAged10To12() {
        return reportDao.getPythonBasicStudentsAged10To12();
    }

    @Override
    public List<Student> getUnenrolledStudents() {
        return reportDao.getUnenrolledStudents();
    }

    @Override
    public List<Student> getStudentsByAgeRange(int minimumAge, int maximumAge) {
        return reportDao.getStudentsByAgeRange(minimumAge, maximumAge);
    }

    @Override
    public List<CourseStatistic> getCourseStatisticsByLanguage() {
        return reportDao.getCourseStatisticsByLanguage();
    }

    @Override
    public List<CourseStatistic> getCourseStatisticsByLevel() {
        return reportDao.getCourseStatisticsByLevel();
    }

    @Override
    public List<Enrollment> getEnrollmentsByDateRange(String startDate, String endDate) {
        return reportDao.getEnrollmentsByDateRange(startDate, endDate);
    }
}
