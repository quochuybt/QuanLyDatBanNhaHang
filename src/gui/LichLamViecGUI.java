package gui;

import dao.PhanCongDAO;
import dao.NhanVienDAO;
import entity.CaLam;
import entity.NhanVien;
import entity.PhanCong;
import entity.VaiTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class LichLamViecGUI extends JPanel {

    private static final Color COLOR_MAIN_BACKGROUND = Color.WHITE;
    private static final Color COLOR_DAY_HEADER_BG = new Color(220, 222, 226);
    private static final Color COLOR_COLUMN_BG = new Color(244, 245, 247);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_DAY_HEADER_TEXT = new Color(50, 50, 50);
    private static final Color COLOR_NOTE_BG_BLUE = new Color(230, 245, 255);
    private static final Color COLOR_NOTE_FG_BLUE = new Color(20, 70, 200);
    private static final Color COLOR_BUTTON_BLUE = new Color(40, 28, 244);

    private static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 28);
    private static final Font FONT_DAY_HEADER = new Font("Arial", Font.BOLD, 15);
    private static final Font FONT_CARD_TIME = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_CARD_BODY = new Font("Arial", Font.PLAIN, 14);
    private static final Font FONT_CARD_NOTE = new Font("Arial", Font.PLAIN, 12);
    private static final Font FONT_CARD_BODY_BOLD = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_NAVIGATION = new Font("Arial", Font.BOLD, 16); // [SỬA] Font cho điều hướng

    // DAOs và Vai trò
    private final PhanCongDAO phanCongDAO;
    private final NhanVienDAO nhanVienDAO;
    private final VaiTro currentUserRole;

    // Trạng thái cho tuần hiện tại
    private LocalDate currentWeekStartDate; // Ngày Thứ Hai của tuần đang hiển thị
    private final DateTimeFormatter weekRangeFormatter = DateTimeFormatter.ofPattern("dd/MM");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM"); // Cho hiển thị ngày

    //  Thành phần UI cho Điều hướng
    private JLabel lblWeekRange;
    private JButton btnPrevWeek;
    private JButton btnNextWeek;
    private JPanel weekDisplayPanel; // Panel chứa lịch tuần (CENTER)


    public LichLamViecGUI(VaiTro role) {
        this.phanCongDAO = new PhanCongDAO();
        this.nhanVienDAO = new NhanVienDAO();
        this.currentUserRole = role;

        // Khởi tạo tuần hiện tại là Thứ Hai của tuần này
        this.currentWeekStartDate = LocalDate.now().with(DayOfWeek.MONDAY);

        this.setLayout(new BorderLayout(0, 15)); // Tăng khoảng cách dọc
        this.setBackground(COLOR_MAIN_BACKGROUND);
        this.setBorder(new EmptyBorder(20, 25, 20, 25));

        // --- Chỉnh sửa Panel Trên cùng ---
        JPanel topPanel = createTopPanel(); // Tạo panel trên cùng bằng phương thức hỗ trợ
        this.add(topPanel, BorderLayout.NORTH);

        // [SỬA] Tạo một panel riêng cho hiển thị tuần
        weekDisplayPanel = new JPanel(new BorderLayout()); // Dùng BorderLayout để dễ dàng thay thế nội dung
        weekDisplayPanel.setOpaque(false);
        this.add(weekDisplayPanel, BorderLayout.CENTER);

        // Tải lịch ban đầu
        refreshWeekPanel(); // Tải dữ liệu cho tuần hiện tại ban đầu
    }

    /**
     *  Phương thức hỗ trợ tạo panel trên cùng (Tiêu đề, Điều hướng, Nút Giao việc)
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0)); // Thêm khoảng cách ngang
        topPanel.setOpaque(false);

        // Tiêu đề (Bên trái)
        JLabel lblTitle = new JLabel("CA LÀM");
        lblTitle.setFont(FONT_TITLE);
        topPanel.add(lblTitle, BorderLayout.WEST);

        // Panel Giữa cho Điều hướng (CENTER)
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        navigationPanel.setOpaque(false);

        btnPrevWeek = new JButton("<");
        styleNavigationButton(btnPrevWeek);

        lblWeekRange = new JLabel(); // Text sẽ được đặt bởi updateWeekRangeLabel
        lblWeekRange.setFont(FONT_NAVIGATION);
        lblWeekRange.setHorizontalAlignment(SwingConstants.CENTER);

        btnNextWeek = new JButton(">");
        styleNavigationButton(btnNextWeek);

        navigationPanel.add(btnPrevWeek);
        navigationPanel.add(lblWeekRange);
        navigationPanel.add(btnNextWeek);
        topPanel.add(navigationPanel, BorderLayout.CENTER);

        // Panel Nút Giao việc (Bên phải) - Chỉ khi là QUANLY
        if (this.currentUserRole == VaiTro.QUANLY) {
            JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            buttonContainer.setOpaque(false);
            JButton btnPhanCa = new JButton("Phân ca");
            btnPhanCa.setFont(new Font("Arial", Font.BOLD, 14));
            btnPhanCa.setBackground(COLOR_BUTTON_BLUE);
            btnPhanCa.setForeground(Color.WHITE);
            btnPhanCa.setFocusPainted(false);
            btnPhanCa.setBorder(new EmptyBorder(8, 15, 8, 15));
            btnPhanCa.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnPhanCa.addActionListener(e -> showAssignShiftDialog());
            buttonContainer.add(btnPhanCa);
            topPanel.add(buttonContainer, BorderLayout.EAST);
        }

        // Trình lắng nghe Sự kiện cho Nút Điều hướng
        btnPrevWeek.addActionListener(e -> navigateWeek(-1));
        btnNextWeek.addActionListener(e -> navigateWeek(1));

        return topPanel;
    }

    /**
     *Phương thức hỗ trợ định dạng nút điều hướng
     */
    private void styleNavigationButton(JButton button) {
        button.setFont(FONT_NAVIGATION);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(40, 35)); // Điều chỉnh kích thước nếu cần
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     *  Logic xử lý điều hướng tuần
     */
    private void navigateWeek(int weeksToAdd) {
        currentWeekStartDate = currentWeekStartDate.plusWeeks(weeksToAdd);
        refreshWeekPanel(); // Tải lại lịch cho tuần mới
    }

    /**
     * Cập nhật nhãn hiển thị phạm vi tuần hiện tại
     */
    private void updateWeekRangeLabel() {
        LocalDate weekEndDate = currentWeekStartDate.plusDays(6);
        lblWeekRange.setText(String.format("Tuần: %s - %s",
                currentWeekStartDate.format(weekRangeFormatter),
                weekEndDate.format(weekRangeFormatter)));
    }


    /**
     * Tạo panel tuần chính dựa trên currentWeekStartDate
     */
    private JPanel createWeekPanel() {
        JPanel weekPanel = new JPanel(new GridLayout(1, 7, 15, 15));
        weekPanel.setOpaque(false);

        // Dùng currentWeekStartDate
        LocalDate dauTuan = currentWeekStartDate;
        LocalDate cuoiTuan = currentWeekStartDate.plusDays(6);

        List<PhanCong> dsPhanCong = phanCongDAO.getPhanCongChiTiet(dauTuan, cuoiTuan);

        Map<LocalDate, List<PhanCong>> mapTheoNgay = dsPhanCong.stream()
                .collect(Collectors.groupingBy(PhanCong::getNgayLam));

        for (int i = 0; i < 7; i++) {
            LocalDate ngayTrongTuan = dauTuan.plusDays(i);
            List<PhanCong> phanCongCuaNgay = mapTheoNgay.get(ngayTrongTuan);
            Map<CaLam, List<NhanVien>> caLamTheoNgay = null;
            if (phanCongCuaNgay != null) {
                caLamTheoNgay = phanCongCuaNgay.stream()
                        .collect(Collectors.groupingBy(
                                PhanCong::getCaLam,
                                Collectors.mapping(PhanCong::getNhanVien, Collectors.toList())
                        ));
            }

            // Lấy tên ngày (không cần thay đổi ở đây)
            Locale vn = new Locale("vi", "VN");
            String tenThu = ngayTrongTuan.getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, vn);
            tenThu = tenThu.substring(0, 1).toUpperCase() + tenThu.substring(1);

            // Tạo cột với ngày
            weekPanel.add(createDayColumn(tenThu, caLamTheoNgay, ngayTrongTuan));
        }
        return weekPanel;
    }

    /**
     *  tiêu đề ngày để bao gồm ngày
     */
    private JPanel createDayColumn(String dayTitle, Map<CaLam, List<NhanVien>> caLamTheoNgay, LocalDate ngayLam) {
        JPanel columnPanel = new JPanel(new BorderLayout(0, 10));
        columnPanel.setOpaque(false);

        // --- Panel Tiêu đề (Bo góc, chứa Tên Ngày và Ngày) ---
        RoundedPanel dayHeaderPanel = new RoundedPanel(12, COLOR_DAY_HEADER_BG);
        dayHeaderPanel.setLayout(new BoxLayout(dayHeaderPanel, BoxLayout.Y_AXIS)); // Xếp chồng theo chiều dọc
        dayHeaderPanel.setBorder(new EmptyBorder(8, 10, 8, 10)); // Điều chỉnh padding

        // Nhãn Tên Ngày
        JLabel lblDayName = new JLabel(dayTitle);
        lblDayName.setFont(FONT_DAY_HEADER);
        lblDayName.setForeground(COLOR_DAY_HEADER_TEXT);
        lblDayName.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa theo chiều ngang
        lblDayName.setOpaque(false);

        // Nhãn Ngày
        JLabel lblDate = new JLabel(ngayLam.format(dateFormatter)); // Định dạng dd/MM
        lblDate.setFont(FONT_CARD_BODY); // Dùng font nhỏ hơn một chút
        lblDate.setForeground(COLOR_DAY_HEADER_TEXT);
        lblDate.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa theo chiều ngang
        lblDate.setOpaque(false);

        dayHeaderPanel.add(lblDayName);
        dayHeaderPanel.add(Box.createRigidArea(new Dimension(0, 2))); // Khoảng cách nhỏ
        dayHeaderPanel.add(lblDate);
        // --- Kết thúc Panel Tiêu đề ---

        columnPanel.add(dayHeaderPanel, BorderLayout.NORTH); // Thêm tiêu đề kết hợp

        // Panel cho thẻ ca làm
        RoundedPanel pnlCardsContainer = new RoundedPanel(12, COLOR_COLUMN_BG);
        pnlCardsContainer.setLayout(new BoxLayout(pnlCardsContainer, BoxLayout.Y_AXIS));
        pnlCardsContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Logic thêm thẻ ca làm
        if (caLamTheoNgay != null && !caLamTheoNgay.isEmpty()) {
            List<CaLam> caLamSorted = caLamTheoNgay.keySet().stream()
                    .sorted(Comparator.comparing(CaLam::getGioBatDau))
                    .collect(Collectors.toList());
            for (CaLam ca : caLamSorted) {
                List<NhanVien> nhanViens = caLamTheoNgay.get(ca);
                String[] tenNhanViens = nhanViens.stream()
                        .map(NhanVien::getHoten)
                        .toArray(String[]::new);
                String thoiGian = String.format("%s - %s",
                        ca.getGioBatDau().format(DateTimeFormatter.ofPattern("HH:mm")),
                        ca.getGioKetThuc().format(DateTimeFormatter.ofPattern("HH:mm")));
                pnlCardsContainer.add(createShiftCard(thoiGian, tenNhanViens, ca, nhanViens, ngayLam));
                pnlCardsContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        pnlCardsContainer.add(Box.createVerticalGlue());

        // ScrollPane cho thẻ (giữ nguyên)
        JScrollPane scrollPaneCards = new JScrollPane(pnlCardsContainer);
        scrollPaneCards.setBorder(null);
        scrollPaneCards.setOpaque(false);
        scrollPaneCards.getViewport().setOpaque(false);
        scrollPaneCards.getVerticalScrollBar().setUnitIncrement(16);
        columnPanel.add(scrollPaneCards, BorderLayout.CENTER);

        return columnPanel;
    }

    // --- (createShiftCard, showPhanCongDialog ) ---
    private JPanel createShiftCard(String time, String[] employees,
                                   CaLam ca, List<NhanVien> nhanVienList, LocalDate ngayLam) {

        RoundedPanel card = new RoundedPanel(12, COLOR_CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 15, 12, 15));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        JLabel lblTime = new JLabel(time);
        lblTime.setFont(FONT_CARD_TIME);
        card.add(lblTime);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        JLabel lblNhanVienTitle = new JLabel("Nhân viên:");
        lblNhanVienTitle.setFont(FONT_CARD_BODY_BOLD);
        card.add(lblNhanVienTitle);
        card.add(Box.createRigidArea(new Dimension(0, 3)));
        for (String emp : employees) {
            JLabel lblEmp = new JLabel(emp);
            lblEmp.setFont(FONT_CARD_BODY);
            card.add(lblEmp);
        }
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        if (this.currentUserRole == VaiTro.QUANLY) {
            String noteText = "Thêm nhân viên vào ca";
            RoundedPanel pnlNote = new RoundedPanel(8, COLOR_NOTE_BG_BLUE);
            pnlNote.setLayout(new BoxLayout(pnlNote, BoxLayout.X_AXIS));
            pnlNote.setCursor(new Cursor(Cursor.HAND_CURSOR));
            pnlNote.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlNote.setBorder(new EmptyBorder(5, 8, 5, 8));
            JLabel lblNoteIcon = new JLabel();
            lblNoteIcon.setFont(FONT_CARD_NOTE);
            lblNoteIcon.setForeground(COLOR_NOTE_FG_BLUE);
            JLabel lblNoteText = new JLabel("<html><body style='width: 130px;'>"
                    + noteText
                    + "</body></html>");
            lblNoteText.setFont(FONT_CARD_NOTE);
            lblNoteText.setForeground(COLOR_NOTE_FG_BLUE);
            pnlNote.add(lblNoteIcon);
            pnlNote.add(Box.createRigidArea(new Dimension(5, 0)));
            pnlNote.add(lblNoteText);
            pnlNote.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showPhanCongDialog(ca, ngayLam, new ArrayList<>(nhanVienList));
                }
            });
            card.add(pnlNote);
        }
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }
    private void showPhanCongDialog(CaLam ca, LocalDate ngayLam, List<NhanVien> dsDaPhanCong) {

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Phân công nhân viên", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        String title = String.format("Ca: %s (%s - %s) - Ngày: %s",
                ca.getTenCa(),
                ca.getGioBatDau().format(DateTimeFormatter.ofPattern("HH:mm")),
                ca.getGioKetThuc().format(DateTimeFormatter.ofPattern("HH:mm")),
                ngayLam.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(FONT_DAY_HEADER);
        lblTitle.setBorder(new EmptyBorder(10, 10, 0, 10));
        dialog.add(lblTitle, BorderLayout.NORTH);
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.weightx = 0.45;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Nhân viên chưa phân công:"), gbc);
        DefaultListModel<NhanVien> modelChuaPhanCong = new DefaultListModel<>();
        JList<NhanVien> listChuaPhanCong = new JList<>(modelChuaPhanCong);
        Set<String> idDaPhanCong = dsDaPhanCong.stream()
                .map(NhanVien::getManv)
                .collect(Collectors.toSet());
        List<NhanVien> dsTatCaNV = nhanVienDAO.getAllNhanVien();
        List<NhanVien> dsChuaPhanCong = dsTatCaNV.stream()
                .filter(nv -> !idDaPhanCong.contains(nv.getManv()))
                .collect(Collectors.toList());
        modelChuaPhanCong.addAll(dsChuaPhanCong);
        gbc.gridy = 1;
        mainPanel.add(new JScrollPane(listChuaPhanCong), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 15));
        JButton btnAdd = new JButton(">>");
        JButton btnRemove = new JButton("<<");
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnRemove);
        buttonPanel.add(Box.createVerticalGlue());
        mainPanel.add(buttonPanel, gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.45;
        mainPanel.add(new JLabel("Nhân viên đã phân công:"), gbc);
        DefaultListModel<NhanVien> modelDaPhanCong = new DefaultListModel<>();
        modelDaPhanCong.addAll(dsDaPhanCong);
        JList<NhanVien> listDaPhanCong = new JList<>(modelDaPhanCong);
        gbc.gridy = 1;
        mainPanel.add(new JScrollPane(listDaPhanCong), gbc);
        dialog.add(mainPanel, BorderLayout.CENTER);
        JButton btnClose = new JButton("Đóng");
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(btnClose);
        dialog.add(southPanel, BorderLayout.SOUTH);
        btnAdd.addActionListener(e -> {
            NhanVien nv = listChuaPhanCong.getSelectedValue();
            if (nv != null) {
                boolean success = phanCongDAO.themPhanCong(nv.getManv(), ca.getMaCa(), ngayLam);
                if (success) {
                    modelChuaPhanCong.removeElement(nv);
                    modelDaPhanCong.addElement(nv);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lỗi: Không thể thêm nhân viên. (Có thể NV đã có ca khác?)");
                }
            }
        });
        btnRemove.addActionListener(e -> {
            NhanVien nv = listDaPhanCong.getSelectedValue();
            if (nv != null) {
                boolean success = phanCongDAO.xoaPhanCong(nv.getManv(), ca.getMaCa(), ngayLam);
                if (success) {
                    modelDaPhanCong.removeElement(nv);
                    modelChuaPhanCong.addElement(nv);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lỗi: Không thể xóa nhân viên.");
                }
            }
        });
        btnClose.addActionListener(e -> {
            dialog.dispose();
            refreshWeekPanel();
        });
        ListCellRenderer<NhanVien> renderer = (list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value.getHoten());
            label.setOpaque(true);
            if (isSelected) {
                label.setBackground(new Color(255, 255, 200)); // Nền vàng nhạt
                label.setForeground(Color.BLACK);
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }
            label.setBorder(new EmptyBorder(5,5,5,5));
            return label;
        };
        listChuaPhanCong.setCellRenderer(renderer);
        listDaPhanCong.setCellRenderer(renderer);
        dialog.setVisible(true);
    }

    /**
     * [SỬA] Viết lại refreshWeekPanel: Xóa và thêm panel tuần mới
     */
    private void refreshWeekPanel() {
        // Cập nhật nhãn phạm vi tuần trước
        updateWeekRangeLabel();

        // Xóa nội dung panel tuần cũ nếu tồn tại
        weekDisplayPanel.removeAll();

        // Tạo panel tuần mới dựa trên currentWeekStartDate
        JPanel newWeekPanel = createWeekPanel();
        weekDisplayPanel.add(newWeekPanel, BorderLayout.CENTER);

        // Xác thực lại và vẽ lại panel chứa để hiển thị thay đổi
        weekDisplayPanel.revalidate();
        weekDisplayPanel.repaint();
    }

    //  phương thức mới cho nút phân ca
    /**
     * Hiển thị Dialog Phân công ca làm (Chọn ngày, ca, nhân viên).
     * Hiện tại chỉ hiển thị thông báo placeholder.
     */
    private void showAssignShiftDialog() {
        // Tạo và hiển thị dialog mới
        AssignShiftDialog assignDialog = new AssignShiftDialog((Frame) SwingUtilities.getWindowAncestor(this));
        assignDialog.setVisible(true); // Hiển thị dialog

        // Sau khi dialog đóng (người dùng nhấn "Phân công" hoặc "Hủy"),
        // tải lại lịch làm việc để cập nhật thay đổi (nếu có)
        refreshWeekPanel();
    }



    // --- (RoundedPanel, RoundedLabel ) ---
    class RoundedPanel extends JPanel {
        private Color backgroundColor;
        private int cornerRadius;
        public RoundedPanel(int radius, Color bgColor) {
            super();
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            int height = getHeight();
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
        }
    }
    class RoundedLabel extends JLabel {
        private Color backgroundColor;
        private int cornerRadius;
        public RoundedLabel(String text, int radius, Color bgColor) {
            super(text);
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            super.paintComponent(g2);
            g2.dispose();
        }
    }



}