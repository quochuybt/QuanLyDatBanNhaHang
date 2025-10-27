package gui;

import com.toedter.calendar.JDateChooser; // Cần thư viện JCalendar
import dao.CaLamDAO;
import dao.NhanVienDAO;
import dao.PhanCongDAO;
import entity.CaLam;
import entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date; // Dùng java.util.Date cho JDateChooser
import java.util.List;

public class AssignShiftDialog extends JDialog {

    // Màu sắc và Font (Có thể lấy từ LichLamViecGUI hoặc định nghĩa riêng)
    private static final Color COLOR_BUTTON_BLUE = new Color(40, 28, 244);
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_COMPONENT = new Font("Arial", Font.PLAIN, 14);

    // Components
    private JDateChooser dateChooser;
    private JComboBox<CaLam> cbCaLam;
    private JList<NhanVien> listNhanVien;
    private DefaultListModel<NhanVien> modelNhanVien;
    private JButton btnAssign;
    private JButton btnCancel;

    // DAOs
    private final CaLamDAO caLamDAO;
    private final NhanVienDAO nhanVienDAO;
    private final PhanCongDAO phanCongDAO;

    public AssignShiftDialog(Frame owner) {
        super(owner, "Phân công ca làm", true); // true: modal dialog

        // Khởi tạo DAOs
        this.caLamDAO = new CaLamDAO();
        this.nhanVienDAO = new NhanVienDAO();
        this.phanCongDAO = new PhanCongDAO();

        // --- Cấu hình Dialog ---
        setSize(450, 550);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15)); // Padding

        // --- Tạo giao diện ---
        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        // --- Load dữ liệu ban đầu ---
        loadCaLam();
        loadNhanVien();

        // --- Thêm sự kiện ---
        addEventHandlers();
    }

    /**
     * Tạo panel chứa form nhập liệu (Ngày, Ca, Nhân viên)
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; // Component fill theo chiều ngang
        gbc.insets = new Insets(5, 5, 5, 5); // Khoảng cách
        gbc.anchor = GridBagConstraints.WEST; // Căn lề trái

        // --- Hàng 1: Chọn Ngày ---
        JLabel lblNgay = new JLabel("Chọn ngày:");
        lblNgay.setFont(FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2; // Tỷ lệ chiều rộng label
        formPanel.add(lblNgay, gbc);

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setFont(FONT_COMPONENT);
        dateChooser.setDate(new Date()); // Mặc định là ngày hôm nay
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8; // Tỷ lệ chiều rộng date chooser
        formPanel.add(dateChooser, gbc);

        // --- Hàng 2: Chọn Ca ---
        JLabel lblCa = new JLabel("Chọn ca:");
        lblCa.setFont(FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(lblCa, gbc);

        cbCaLam = new JComboBox<>();
        cbCaLam.setFont(FONT_COMPONENT);
        // Custom renderer để hiển thị tên ca thay vì object.toString()
        cbCaLam.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CaLam) {
                    CaLam ca = (CaLam) value;
                    setText(String.format("%s (%s - %s)",
                            ca.getTenCa(),
                            ca.getGioBatDau().format(DateTimeFormatter.ofPattern("HH:mm")),
                            ca.getGioKetThuc().format(DateTimeFormatter.ofPattern("HH:mm"))));
                }
                return this;
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(cbCaLam, gbc);

        // --- Hàng 3: Chọn Nhân viên ---
        JLabel lblNV = new JLabel("Chọn nhân viên:");
        lblNV.setFont(FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Label chiếm cả 2 cột
        formPanel.add(lblNV, gbc);

        modelNhanVien = new DefaultListModel<>();
        listNhanVien = new JList<>(modelNhanVien);
        listNhanVien.setFont(FONT_COMPONENT);
        listNhanVien.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // Cho phép chọn nhiều
        // Custom renderer để hiển thị tên NV
        listNhanVien.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhanVien) {
                    setText(((NhanVien) value).getHoten());
                }
                return this;
            }
        });

        JScrollPane scrollPaneNV = new JScrollPane(listNhanVien);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // JList chiếm cả 2 cột
        gbc.weighty = 1.0; // Cho phép JList giãn theo chiều dọc
        gbc.fill = GridBagConstraints.BOTH; // Fill cả ngang và dọc
        formPanel.add(scrollPaneNV, gbc);

        return formPanel;
    }

    /**
     * Tạo panel chứa nút "Phân công" và "Hủy"
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Căn phải

        btnAssign = new JButton("Phân công");
        btnAssign.setBackground(COLOR_BUTTON_BLUE);
        btnAssign.setForeground(Color.WHITE);
        btnAssign.setFont(FONT_LABEL);
        btnAssign.setFocusPainted(false);

        btnCancel = new JButton("Hủy");
        btnCancel.setFont(FONT_LABEL);

        buttonPanel.add(btnAssign);
        buttonPanel.add(btnCancel);

        return buttonPanel;
    }

    /**
     * Load danh sách ca làm vào ComboBox
     */
    private void loadCaLam() {
        List<CaLam> dsCaLam = caLamDAO.getAllCaLam();
        for (CaLam ca : dsCaLam) {
            cbCaLam.addItem(ca);
        }
    }

    /**
     * Load danh sách nhân viên vào JList
     */
    private void loadNhanVien() {
        List<NhanVien> dsNhanVien = nhanVienDAO.getAllNhanVien(); // Lấy tất cả NV
        modelNhanVien.addAll(dsNhanVien);
    }

    /**
     * Gắn sự kiện cho các nút
     */
    private void addEventHandlers() {
        // Sự kiện nút "Phân công"
        btnAssign.addActionListener(e -> assignShifts());

        // Sự kiện nút "Hủy"
        btnCancel.addActionListener(e -> dispose()); // Đóng dialog
    }

    /**
     * Xử lý logic khi nhấn nút "Phân công"
     */
    private void assignShifts() {
        // 1. Lấy thông tin đã chọn
        Date selectedUtilDate = dateChooser.getDate();
        CaLam selectedCaLam = (CaLam) cbCaLam.getSelectedItem();
        List<NhanVien> selectedNhanViens = listNhanVien.getSelectedValuesList();

        // 2. Kiểm tra dữ liệu
        if (selectedUtilDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedCaLam == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ca.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedNhanViens.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một nhân viên.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Chuyển java.util.Date sang LocalDate
        LocalDate selectedDate = selectedUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String maCa = selectedCaLam.getMaCa();

        // 3. Thực hiện phân công cho từng nhân viên được chọn
        int successCount = 0;
        int failCount = 0;
        List<String> failedNames = new ArrayList<>();

        for (NhanVien nv : selectedNhanViens) {
            boolean success = phanCongDAO.themPhanCong(nv.getManv(), maCa, selectedDate);
            if (success) {
                successCount++;
            } else {
                failCount++;
                failedNames.add(nv.getHoten());
            }
        }

        // 4. Hiển thị kết quả
        StringBuilder message = new StringBuilder();
        if (successCount > 0) {
            message.append("Đã phân công thành công cho ").append(successCount).append(" nhân viên.\n");
        }
        if (failCount > 0) {
            message.append("Phân công thất bại cho ").append(failCount).append(" nhân viên (có thể đã được phân công ca khác):\n");
            failedNames.forEach(name -> message.append("- ").append(name).append("\n"));
        }

        JOptionPane.showMessageDialog(this, message.toString(), "Kết quả phân công",
                (failCount == 0) ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

        // 5. Đóng dialog nếu không có lỗi nào nghiêm trọng
        if (failCount < selectedNhanViens.size()) { // Nếu có ít nhất 1 thành công
            dispose(); // Đóng dialog
        }
    }
}