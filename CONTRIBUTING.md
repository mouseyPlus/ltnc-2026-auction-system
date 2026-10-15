Quy ước đóng góp

Quy tắc làm việc chung của nhóm. Đọc kỹ trước khi commit lần đầu — mọi PR không tuân thủ sẽ bị yêu cầu sửa trước khi merge.

    Yêu cầu môn học: commit thường xuyên lên GitHub (không chấp nhận 1 commit duy nhất cuối kỳ), và mọi thành viên phải giải thích được toàn bộ mã nguồn — nếu không, cả nhóm 0 điểm.

Phân công module từng người: xem bảng Thành viên trong README.
1. Branch

main         ← chỉ nhận PR từ dev (đã bảo vệ, cần 1 approval)dev          ← nhánh tích hợp hàng ngày — mọi PR merge vào đâyfeature/...  ← tạo từ dev, code ở đây

Tên branch: feature/<ten> · fix/<ten> · test/<ten>Ví dụ: feature/gui-dang-nhap, fix/lost-update-bid
Quy trình chuẩn

git checkout dev && git pull origin dev      # 1. lấy code mới nhấtgit checkout -b feature/ten-tinh-nang        # 2. tạo branch riêng# ... code, commit thường xuyên ...git push -u origin feature/ten-tinh-nang     # 3. push lên# 4. Tạo PR vào dev trên GitHub → CI xanh + 1 approval → merge

LƯU Ý: Không push trực tiếp vào dev hoặc main.
2. Commit message — Conventional Commits

<type>(<scope>): <mô tả ngắn>

Type	Dùng khi
feat	Tính năng mới
fix	Sửa bug
test	Thêm/sửa unit test
docs	README, diagram, tài liệu
refactor	Sửa code, không đổi hành vi
style	Chỉ format, không đổi logic
chore	Config, CI, dependencies

Scope: auth user item auction bid gui server network storage ci

Ví dụ:

    feat(auth): them man hinh dang ky
    fix(auction): chap nhan bid thap hon gia hien tai
    test(storage): test save/load khi server restart

Nguyên tắc:

    1 commit = 1 thay đổi logic, mô tả đọc lại 1 tháng sau vẫn hiểu
    Commit mỗi khi xong một phần việc — không gom dồn
    CẤM: update, fix bug, aaa, lam not phan cuoi

3. Pull Request

    PR từ feature/... → dev

    Tiêu đề theo format commit: feat(gui): man hinh danh sach phien dau gia

    Mô tả: làm gì — làm thế nào — ảnh hưởng module nào

    Điều kiện merge (đủ cả 3):
         CI (build + test) chạy xanh
         Có ít nhất 1 approval (mặc định leader review)
         Không conflict với dev

    Merge xong → xóa branch feature

    CI trên dev đỏ → dừng việc khác, fix ngay trước khi làm tiếp

4. Coding convention

    Google Java Style Guide (Checkstyle bật từ tuần 9 — viết đúng ngay từ đầu, khỏi sửa lại hàng trăm dòng sau)
    Class PascalCase · method/biến camelCase · hằng số UPPER_SNAKE_CASE
    Không giữ code chết / code bị comment — git đã lưu history rồi
    Code trùng lặp → tách method/class (DRY)

5. Kiểm thử

    Logic quan trọng (bid hợp lệ, chuyển trạng thái phiên, concurrency, save/load) phải có JUnit test
    PR đổi logic → kèm test tương ứng
    Mục tiêu coverage ≥ 60% (deadline tuần 10)

6. Khi gặp vấn đề

    Bị block → tạo Issue, gán người phụ trách, mô tả rõ đang kẹt ở đâu
    Conflict / lỗi build trên dev → báo nhóm ngay, tuyệt đối không force push

Phụ lục: gợi ý format khi gõ commit (khuyến khích)

Tạo file .gitmessage ở gốc repo:

<type>(<scope>): <mo ta ngan># type: feat | fix | test | docs | refactor | style | chore# scope: auth | user | item | auction | bid | gui | server | network | storage | ci

Chạy 1 lần duy nhất trên máy mình:

git config commit.template .gitmessage

→ Mỗi lần git commit, gợi ý hiện sẵn (dòng # không bị đưa vào message).
