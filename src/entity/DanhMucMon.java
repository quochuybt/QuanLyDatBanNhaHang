    /**
     * Lớp DanhMucMon đại diện cho thực thể danh mục món ăn.
     * Class này tuân thủ các yêu cầu nghiệp vụ đã mô tả.
     */
    package entity;
    public class DanhMucMon {

        // 1. Khai báo thuộc tính
        private String madm;  // 1.1: Khóa chính, format DM0XXX
        private String tendm; // 1.2: Không rỗng
        private String mota;  // 1.3: Mô tả chi tiết

        // 3. Viết các constructor

        /**
         * 3.1 Constructor đầy đủ.
         * Khởi tạo tất cả thuộc tính và sử dụng setters để validate dữ liệu.
         *
         * @param madm  Mã danh mục (phải theo format "DM0XXX").
         * @param tendm Tên danh mục (không rỗng).
         * @param mota  Mô tả.
         * @throws IllegalArgumentException nếu madm hoặc tendm không hợp lệ.
         */
        public DanhMucMon(String madm, String tendm, String mota) throws IllegalArgumentException {
            // Sử dụng setters để đảm bảo tính hợp lệ của dữ liệu
            setMadm(madm);
            setTenDM(tendm);
            this.mota = mota;
        }

        /**
         * 3.2 Copy constructor.
         * Sao chép các thuộc tính từ một đối tượng DanhMucMon khác.
         *
         * @param other Đối tượng DanhMucMon cần sao chép.
         */
        public DanhMucMon(DanhMucMon other) {
            this.madm = other.madm;
            this.tendm = other.tendm;
            this.mota = other.mota;
        }

        // Constructor mặc định (không được yêu cầu nhưng thường hữu ích)
        // Bạn có thể thêm vào nếu cần:
        // public DanhMucMon() {
        // }

        // 2. Viết các phương thức getter, setter

        // --- Getters ---

        public String getMadm() {
            return madm;
        }

        public String getTendm() {
            return tendm;
        }

        public String getMota() {
            return mota;
        }

        // --- Setters (với validation) ---

        /**
         * Setter cho madm.
         * Ràng buộc 1.1: Phải có 6 ký tự và theo format "DM0XXX" (với XXX là 3 chữ số).
         * * @param madm Mã danh mục mới.
         * @throws IllegalArgumentException nếu mã không hợp lệ.
         */
        public void setMadm(String madm) throws IllegalArgumentException {
            if (madm == null || !madm.matches("^DM0\\d{3}$")) {
                throw new IllegalArgumentException("Mã danh mục không hợp lệ. Phải có 6 ký tự dạng 'DM0XXX' (XXX là 3 chữ số).");
            }
            this.madm = madm;
        }

        /**
         * 2.1 setTenDM
         * Ràng buộc 1.2: Không được rỗng.
         *
         * @param tendm Tên danh mục mới.
         * @throws IllegalArgumentException "Tên danh mục không được rỗng"
         */
        public void setTenDM(String tendm) throws IllegalArgumentException {
            if (tendm == null || tendm.trim().isEmpty()) {
                // Ném exception theo yêu cầu
                throw new IllegalArgumentException("Tên danh mục không được rỗng");
            }
            this.tendm = tendm;
        }

        /**
         * Setter cho mota.
         * Ràng buộc 1.3: Không có ràng buộc.
         *
         * @param mota Mô tả mới.
         */
        public void setMota(String mota) {
            this.mota = mota;
        }


        /**
         * 4. Viết phương thức toString()
         * Trả về chuỗi đại diện cho đối tượng.
         *
         * @return String theo format "DanhMucMon{madm='...', tendm='...', mota='...'}'"
         */
        @Override
        public String toString() {
            return "DanhMucMon{" +
                    "madm='" + madm + '\'' +
                    ", tendm='" + tendm + '\'' +
                    ", mota='" + mota + '\'' +
                    '}';
        }
    }