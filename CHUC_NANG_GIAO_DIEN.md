# Đối chiếu chức năng và giao diện

| Giao diện | Chức năng nghiệp vụ | Cách sử dụng |
|---|---|---|
| Trang chủ | Điều hướng toàn bộ ứng dụng | Chọn **Học viên**, **Khóa học**, **Ghi danh** hoặc **Báo cáo và truy vấn**. |
| Quản lý học viên | Thêm, sửa, xóa học viên (mã, tên, tuổi, trình độ Scratch) | Chọn **Thêm mới**; chạm một dòng để sửa; nhấn giữ để xóa. Tuổi được kiểm tra trong khoảng 5–18 và mã không được trùng. |
| Quản lý khóa học | Thêm, sửa, xóa khóa (mã, tên, ngôn ngữ Scratch/Python, cấp độ) | Chọn **Thêm mới**; chạm để sửa; nhấn giữ để xóa. |
| Ghi danh học viên | Gán học viên vào khóa học và hủy ghi danh | Chọn **Thêm mới**, chọn học viên và khóa. Nhấn giữ một ghi danh để hủy. Một học viên không thể được gán hai lần vào cùng khóa. |
| Báo cáo và truy vấn | Liệt kê học viên theo khóa | Chọn khóa trong danh sách rồi chọn **Xem học viên theo khóa**. |
| Báo cáo và truy vấn | Tìm học viên 10–12 tuổi thuộc khóa Python cấp độ Cơ bản | Chọn **Học viên 10–12 tuổi • Python cơ bản**. |

## Dữ liệu

Ứng dụng sử dụng SQLite với ba bảng `students`, `courses`, `enrollments`. Bảng ghi danh có khóa chính ghép và khóa ngoại xóa liên hoàn, bảo đảm không trùng ghi danh và không để lại dữ liệu mồ côi khi xóa học viên/khóa học.

## Kiến trúc phân tầng

| Tầng | Thành phần | Trách nhiệm |
|---|---|---|
| Presentation | `presentation.MainActivity` và các Activity trong `presentation.student`, `presentation.course`, `presentation.enrollment`, `presentation.report` | Hiển thị dữ liệu, nhận thao tác người dùng và kiểm tra dữ liệu nhập trên form. |
| Presentation dùng chung | `presentation.common.BaseListActivity`, `FormViews` | Dùng lại cách hiển thị danh sách, xử lý system bar inset và tạo các thành phần form. |
| Domain | `Student`, `Course`, `Enrollment` | Biểu diễn dữ liệu nghiệp vụ đúng với yêu cầu lớp học lập trình. |
| Domain | `ClassroomRepository` | Định nghĩa các thao tác nghiệp vụ mà giao diện được phép sử dụng, không phụ thuộc SQLite. |
| Data | `SqliteClassroomRepository` | Thực thi CRUD, ghi danh và truy vấn báo cáo; chuyển dữ liệu SQLite thành domain model. |
| Data | `ClassroomDatabase` | Khởi tạo, nâng cấp và tạo dữ liệu mẫu cho ba bảng SQLite. |

Luồng phụ thuộc: `presentation → domain ← data`. Activity chỉ làm việc với `ClassroomRepository`, không truy cập `Cursor`, tên bảng hoặc câu SQL trực tiếp.
