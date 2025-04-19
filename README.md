# Todo List Microservices Application

Đây là một ứng dụng danh sách công việc (Todo List) được xây dựng theo kiến trúc microservices sử dụng Spring Boot và Spring Cloud, với frontend được viết bằng Flutter Web.

## Giới thiệu

Ứng dụng này bao gồm các thành phần chính:

*   **Eureka Server:** Dịch vụ khám phá (Service Discovery) để các microservice khác có thể tìm thấy nhau.
*   **API Gateway:** Cổng vào duy nhất cho frontend, xử lý định tuyến (routing), cân bằng tải (load balancing), CORS và các vấn đề liên quan đến API chung.
*   **User Service:** Microservice quản lý thông tin người dùng, đăng ký và đăng nhập (sử dụng JWT).
*   **Todo Service:** Microservice quản lý các công việc (todo items), bao gồm các thao tác CRUD (Thêm, Sửa, Xóa, Lấy danh sách) cho từng người dùng.
*   **Frontend:** Giao diện người dùng web được xây dựng bằng Flutter, tương tác với backend thông qua API Gateway.

## Kiến trúc

```
+-----------------+      +-----------------+      +-----------------+
| Frontend (Web)  |----->|  API Gateway    |----->|  User Service   |
| Flutter         |<-----| (Spring Cloud GW)|<-----| (Spring Boot)   |
+-----------------+      | Port: 8080      |      | Port: 8081      |
                       +-----------------+      +-----------------+
                                |                      |
                                |                      |
                                v                      v
                       +-----------------+      +-----------------+
                       |  Todo Service   |<-----|  Eureka Server  |
                       | (Spring Boot)   |----->| (Spring Cloud)  |
                       | Port: 8082      |      | Port: 8761      |
                       +-----------------+      +-----------------+
```

*   Tất cả các request từ Frontend đều đi qua API Gateway.
*   API Gateway sử dụng Eureka Server để tìm địa chỉ của User Service và Todo Service.
*   User Service và Todo Service sử dụng MongoDB để lưu trữ dữ liệu.
*   Xác thực được xử lý bằng JWT, token được tạo bởi User Service và gửi kèm theo các request đến các service cần bảo vệ (ở đây là Todo Service) thông qua API Gateway.

## Điều kiện tiên quyết

Để chạy ứng dụng này, bạn cần cài đặt:

*   **Java Development Kit (JDK):** Phiên bản 17 trở lên được khuyến nghị.
*   **Apache Maven:** Để build các project Spring Boot.
*   **Flutter SDK:** Phiên bản 3.x trở lên (đã bật hỗ trợ web).
*   **MongoDB:** Đảm bảo MongoDB server đang chạy (mặc định trên cổng 27017).
*   **IDE:** IntelliJ IDEA, VS Code hoặc IDE khác hỗ trợ Java/Spring và Flutter.
*   **Git:** Để clone repository.

## Cài đặt

1.  **Clone Repository:**
    ```bash
    git clone <url-repository-cua-ban>
    cd todolist
    ```

2.  **Cấu hình Backend:**
    *   **MongoDB:** Nếu MongoDB của bạn yêu cầu xác thực, hãy cập nhật username và password trong các file cấu hình:
        *   `user-service/src/main/resources/application.yml`
        *   `todo-service/src/main/resources/application.yml`
        Tìm đến phần `spring.data.mongodb` và sửa các dòng `username`, `password` (và `authentication-database` nếu cần).
    *   **JWT Secret:** (Tùy chọn) Bạn có thể thay đổi `jwt.secret` trong các file `application.yml` trên. Nên sử dụng biến môi trường (`JWT_SECRET`) cho môi trường production.

3.  **Build Backend Services:**
    Mở terminal và chạy lệnh sau trong *từng* thư mục con của backend (`eureka-server`, `user-service`, `todo-service`, `api-gateway`):
    ```bash
    mvn clean install
    ```
    *(Lệnh này chỉ cần chạy một lần hoặc khi có thay đổi về dependency)*

4.  **Cài đặt Frontend Dependencies:**
    ```bash
    cd frontend
    flutter pub get
    ```
    *(Nếu thư mục `web` chưa tồn tại, bạn có thể cần chạy `flutter create .` trong thư mục `frontend` trước)*

## Chạy ứng dụng

Việc khởi động các service backend theo đúng thứ tự là rất quan trọng.

1.  **Khởi động Eureka Server:**
    ```bash
    cd eureka-server
    mvn spring-boot:run
    ```
    Chờ đến khi thấy log báo khởi động thành công (thường chạy trên port 8761).

2.  **Khởi động User Service:**
    Mở một terminal mới:
    ```bash
    cd user-service
    mvn spring-boot:run
    ```
    Chờ khởi động thành công (thường chạy trên port 8081).

3.  **Khởi động Todo Service:**
    Mở một terminal mới:
    ```bash
    cd todo-service
    mvn spring-boot:run
    ```
    Chờ khởi động thành công (thường chạy trên port 8082).

4.  **Khởi động API Gateway:**
    Mở một terminal mới:
    ```bash
    cd api-gateway
    mvn spring-boot:run
    ```
    Chờ khởi động thành công (thường chạy trên port 8080).

5.  **Kiểm tra Eureka Dashboard:**
    Mở trình duyệt và truy cập `http://localhost:8761`. Bạn sẽ thấy `USER-SERVICE`, `TODO-SERVICE`, và `API-GATEWAY` đã đăng ký và có trạng thái `UP`.

6.  **Chạy Frontend:**
    Mở một terminal mới:
    ```bash
    cd frontend
    flutter run -d chrome
    ```
    *(Nếu gặp lỗi font tiếng Việt, thử chạy với:* `flutter run -d chrome --web-renderer canvaskit` *)*

    Ứng dụng Flutter sẽ mở trong trình duyệt Chrome.

## Tính năng chính

*   Đăng ký tài khoản người dùng mới.
*   Đăng nhập bằng username và password (nhận về JWT).
*   Xem danh sách công việc cá nhân.
*   Thêm công việc mới.
*   Đánh dấu công việc là hoàn thành/chưa hoàn thành.
*   Sửa tiêu đề công việc.
*   Xóa công việc (vuốt sang trái).
*   Lưu trạng thái đăng nhập sau khi làm mới trang.

## Công nghệ sử dụng

*   **Backend:**
    *   Java (JDK 17+)
    *   Spring Boot
    *   Spring Cloud (Eureka Discovery, Gateway)
    *   Spring Data MongoDB
    *   Spring Security
    *   JSON Web Token (JJWT)
    *   Maven
    *   Lombok
*   **Frontend:**
    *   Flutter
    *   Dart
    *   Provider (State Management)
    *   google_fonts
    *   intl
    *   shared_preferences
*   **Database:**
    *   MongoDB

## Đóng góp

Nếu bạn muốn đóng góp, vui lòng fork repository và tạo Pull Request.

## License

MIT License