# Đối chiếu chức năng và giao diện

| Giao diện | Chức năng nghiệp vụ | Cách sử dụng |
|---|---|---|
| Trang chủ | Điều hướng toàn bộ ứng dụng | Chọn **Học viên**, **Khóa học**, **Ghi danh** hoặc **Báo cáo và truy vấn**. |
| Quản lý học viên | Thêm, sửa, xóa một hoặc nhiều học viên (mã, tên, tuổi, trình độ Scratch) | Chọn **Thêm mới**; chạm dòng hoặc nút **Sửa**; dùng nút **Xóa** cho một dòng. Chọn **Chọn** để đánh dấu nhiều dòng, chọn tất cả nếu cần rồi xóa trong một lần. |
| Quản lý khóa học | Thêm, sửa, xóa một hoặc nhiều khóa (mã, tên, ngôn ngữ, cấp độ, ngày bắt đầu, ngày kết thúc) | Nhập ngày theo `yyyy-MM-dd`; ngày bắt đầu không được sau ngày kết thúc. Chạm dòng để sửa hoặc dùng chế độ **Chọn** để xóa nhiều khóa bằng một transaction. |
| Ghi danh học viên | Gán học viên vào khóa học và hủy nhiều ghi danh | Chọn khóa rồi chọn nhiều học viên để ghi danh. Khi xem lớp, đánh dấu một hoặc nhiều học viên và chọn **Hủy đã chọn**. |
| Báo cáo và truy vấn | Liệt kê học viên theo khóa | Chọn loại báo cáo, tìm/chọn khóa rồi nhấn **Xem báo cáo**. |
| Báo cáo và truy vấn | Liệt kê tất cả khóa học và số học viên | Chọn **Tất cả khóa học và số học viên**, gồm cả khóa có số lượng bằng 0. |
| Báo cáo và truy vấn | Tìm học viên chưa ghi danh | Chọn **Học viên chưa ghi danh**. |
| Báo cáo và truy vấn | Lọc học viên theo độ tuổi | Chọn **Học viên theo khoảng tuổi**, nhập tuổi từ/đến trong khoảng 5–18 rồi chạy báo cáo. |
| Báo cáo và truy vấn | Thống kê theo ngôn ngữ hoặc cấp độ | Chọn loại thống kê tương ứng để xem số khóa và số lượt ghi danh của từng nhóm. |
| Báo cáo và truy vấn | Lọc ghi danh theo thời gian | Chọn **Ghi danh theo khoảng thời gian**, nhập hai ngày `yyyy-MM-dd`; kết quả hiển thị học viên, khóa học và thời điểm ghi danh. |
| Báo cáo và truy vấn | Tìm học viên 10–12 tuổi thuộc khóa Python cấp độ Cơ bản | Chọn truy vấn nhanh tương ứng rồi nhấn **Xem báo cáo**. |

## Dữ liệu

Ứng dụng sử dụng SQLite với ba bảng `students`, `courses`, `enrollments`. `courses` lưu ngày bắt đầu/kết thúc; `enrollments` tự lưu thời điểm ghi danh. Bảng ghi danh có khóa chính ghép và khóa ngoại xóa liên hoàn, bảo đảm không trùng ghi danh và không để lại dữ liệu mồ côi.

## Kiến trúc phân tầng

| Tầng | Thành phần | Trách nhiệm |
|---|---|---|
| Presentation | Các Activity và ViewModel theo từng feature | Activity hiển thị/nhận thao tác; ViewModel giữ state và gọi đúng use case. `ReportActivity` là màn hình báo cáo riêng. |
| Presentation dùng chung | `BaseMvvmListActivity`, `BaseListViewModel`, `FormViews` | Dùng lại danh sách, loading/empty state, selection, system bar và form. |
| Domain | Model, validator, use case, `ClassroomRepository` | Biểu diễn nghiệp vụ, kiểm tra input và định nghĩa cổng dữ liệu độc lập SQLite. |
| Data | `StudentDao`, `CourseDao`, `EnrollmentDao`, `ReportDao` | Mỗi DAO chứa SQL đúng nhóm chức năng; mapper chuyển `Cursor` thành domain model. |
| Data | `SqliteClassroomRepository`, `ClassroomDatabase` | Repository ủy quyền cho DAO; database quản lý schema version 5, migration và seed. |

Luồng phụ thuộc: `Activity → ViewModel → UseCase → ClassroomRepository ← SqliteClassroomRepository → DAO → SQLite`. Activity không gọi repository, không truy cập `Cursor`, tên bảng hoặc câu SQL trực tiếp.
