Online Auction System

    Hệ thống đấu giá trực tuyến — Bài tập lớn môn Lập trình nâng cao · Học kỳ II, 2025–2026

Giới thiệu

Nền tảng đấu giá trực tuyến theo mô hình eBay: người Seller đăng sản phẩm với giá khởi điểm,các Bidder cạnh tranh đặt giá trong thời gian xác định, hệ thống tự động đóng phiên vàxác định người thắng cuộc. Giao tiếp Client–Server qua Socket, cập nhật giá realtime chotất cả người đang xem phiên.


Tính năng
Bắt buộc

     Đăng ký / đăng nhập, phân vai trò Bidder / Seller / Admin
     CRUD sản phẩm đấu giá (tên, mô tả, giá khởi điểm, thời gian)
     Đặt giá, kiểm tra tính hợp lệ, cập nhật người dẫn đầu
     Tự động đóng phiên khi hết giờ, xác định người thắng
     Chuyển trạng thái phiên: OPEN → RUNNING → FINISHED → PAID/CANCELED
     Xử lý lỗi & ngoại lệ (bid không hợp lệ, phiên đã đóng, lỗi kết nối)
     GUI JavaFX: danh sách phiên, chi tiết sản phẩm, đấu giá realtime, quản lý sản phẩm
     Realtime update mọi client khi có bid mới (Observer + Socket)
     Xử lý đấu giá đồng thời an toàn (tránh lost update, race condition)

Nâng cao

     Auto-Bidding (maxBid + increment, dùng PriorityQueue)
     Anti-sniping — tự gia hạn phiên khi có bid trong giây cuối
     Bid History Visualization — biểu đồ giá realtime (LineChart)

Công nghệ
Thành phần	Công nghệ
Ngôn ngữ	Java 25
GUI	JavaFX + FXML (SceneBuilder)
Build	Maven
Kiến trúc	Client–Server (Socket) + MVC
Lưu trữ dữ liệu	Serialization (File I/O)
Unit test	JUnit 5
CI/CD	GitHub Actions
Convention	Google Java Style Guide
 

Kiến trúc

┌────────────────┐        Socket          ┌─────────────────────┐│     CLIENT     │ ◄──── (message) ─────► │       SERVER        ││ JavaFX + FXML  │                        │ Controller → Model  ││     (MVC)      │                        │    → DAO            │└────────────────┘                        └──────────┬──────────┘                                          Serialization                                          (chỉ server ghi/đọc)



Cấu trúc dự án

src/├── main/java/com/auction/│   ├── common/     # Entity, message, protocol (dùng chung 2 phía)│   ├── server/     # Server, client handler, logic đấu giá, storage│   └── client/     # JavaFX app, controller, network client├── main/resources/ # FXML, CSS, ảnh└── test/java/      # JUnit tests



Cài đặt & chạy

Yêu cầu: JDK 21, Maven 3.8+

# Buildmvn clean package# Chạy testmvn test# Khởi động server (chạy trước)... # Chạy client (mở nhiều terminal = nhiều client)...


Thành viên
Thành viên	Vai trò	Module
[Vũ Việt Hưng] (@mouseyPlus)	Leader	DevOps, CI/CD, QA, SaveData
[Nguyễn Tuấn Khanh] (@tuankhanh-coder07)	Member	Backend Core — logic đấu giá, OOP
[Chu Tuấn Kiệt] (@kiettt2two)	Member	Network — Client/Server, Socket
[Lê Vũ Bằng Linh] (@07dec24)	Member	GUI — JavaFX, UI/UX


Đóng góp

Quy ước branch, commit, pull request: xem CONTRIBUTING.md
