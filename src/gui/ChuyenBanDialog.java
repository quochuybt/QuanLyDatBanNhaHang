package gui;

import entity.Ban;
import entity.TrangThaiBan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import dao.BanDAO;
import dao.DonDatMonDAO;
import dao.HoaDonDAO;

public class ChuyenBanDialog extends JDialog {

    private List<Ban> allTablesFromDB;
    private Ban selectedSourceTable = null;
    private Ban selectedTargetTable = null;
    private JPanel leftTableContainer;
    private JPanel rightTableContainer;
    private String currentLeftFilter = "Tất cả";
    private String currentRightFilter = "Tất cả";
    private List<BanPanel> leftBanPanelList = new ArrayList<>();
    private List<BanPanel> rightBanPanelList = new ArrayList<>();
    private BanDAO banDAO;
    private DonDatMonDAO donDatMonDAO;
    private HoaDonDAO hoaDonDAO;

    public ChuyenBanDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        this.banDAO = new BanDAO();


        try {
            // Tải danh sách bàn từ CSDL
            this.allTablesFromDB = banDAO.getAllBan();

            // Cập nhật lại bộ đếm static (giống ManHinhBanGUI)
            int maxSoThuTu = banDAO.getSoThuTuBanLonNhat();
            Ban.setSoThuTuBanHienTai(maxSoThuTu);

            System.out.println("Dialog Chuyển Bàn: Tải thành công " + allTablesFromDB.size() + " bàn.");

        } catch (Exception e) {
            e.printStackTrace();
            this.allTablesFromDB = new ArrayList<>();
        }

        // --- Thiết lập cho JDialog (thay vì JPanel) ---
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 100)); // Hiệu ứng mờ
        setLayout(new GridBagLayout()); // Để căn giữa

        // Panel nội dung chính
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setPreferredSize(new Dimension(1000, 600));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        contentPanel.add(createTitleBar(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        JPanel leftPane = createListPanel("Danh sách bàn trống", true);
        JPanel rightPane = createListPanel("Danh sách bàn đã đặt/ phục vụ", false);
        splitPane.setLeftComponent(rightPane);
        splitPane.setRightComponent(leftPane);

        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.add(createBottomBar(), BorderLayout.SOUTH);

        // Thêm panel nội dung vào JDialog
        add(contentPanel);
        setSize(parent.getSize());
        setLocationRelativeTo(parent);

        // Populate
        populateLeftPanel(currentLeftFilter);
        populateRightPanel(currentRightFilter);
    }


    private JPanel createTitleBar() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Chuyển bàn");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(new EmptyBorder(5, 10, 5, 10));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose()); // <-- Sửa: dispose()
        titlePanel.add(closeButton, BorderLayout.EAST);
        return titlePanel;
    }

    private JPanel createBottomBar() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnHuyBo = new JButton("Hủy bỏ");
        styleDefaultButton(btnHuyBo);
        btnHuyBo.addActionListener(e -> dispose());

        JButton btnChuyen = new JButton("Chuyển");
        stylePrimaryButton(btnChuyen);

        btnChuyen.addActionListener(e -> xuLyChuyenBan());

        buttonPanel.add(btnHuyBo);
        buttonPanel.add(btnChuyen);
        return buttonPanel;
    }

    private JPanel createListPanel(String title, boolean isLeftPanel) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 5, 0, 5));
        JPanel headerPanel = new JPanel(new BorderLayout(0, 5));
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(createFilterPanel(isLeftPanel), BorderLayout.CENTER);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Sử dụng class công khai
        JPanel tableContainer = new VerticallyWrappingFlowPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new EmptyBorder(5, 5, 5, 5));
        if (isLeftPanel) {
            this.leftTableContainer = tableContainer;
        } else {
            this.rightTableContainer = tableContainer;
        }
        JScrollPane scrollPane = new JScrollPane(tableContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFilterPanel(boolean isLeftPanel) {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filterPanel.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        String[] filters = {"Tất cả", "Tầng trệt", "Tầng 1"};
        ActionListener filterListener = e -> {
            String selectedFilter = e.getActionCommand();
            if (isLeftPanel) {
                currentLeftFilter = selectedFilter;
                populateLeftPanel(currentLeftFilter);
            } else {
                currentRightFilter = selectedFilter;
                populateRightPanel(currentRightFilter);
            }
        };
        for (String filter : filters) {
            JToggleButton button = createFilterButton(filter, filter.equals("Tất cả"));
            button.setActionCommand(filter);
            button.addActionListener(filterListener);
            group.add(button);
            filterPanel.add(button);
        }
        return filterPanel;
    }

    private JToggleButton createFilterButton(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBorder(new EmptyBorder(5, 15, 5, 15));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.addChangeListener(e -> {
            if (button.isSelected()) {
                // Sử dụng hằng số từ BanPanel
                button.setBackground(BanPanel.COLOR_ACCENT_BLUE);
                button.setForeground(Color.WHITE);
                button.setBorder(new EmptyBorder(5, 15, 5, 15));
            } else {
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                        new EmptyBorder(4, 14, 4, 14)
                ));
            }
        });
        button.setSelected(selected);
        return button;
    }

    private void populateLeftPanel(String khuVucFilter) {
        leftTableContainer.removeAll();
        leftBanPanelList.clear();

        boolean targetStillVisible = false;

        for (Ban ban : allTablesFromDB) {
            boolean khuVucMatch = khuVucFilter.equals("Tất cả") || ban.getKhuVuc().equals(khuVucFilter);
            boolean statusMatch = (ban.getTrangThai() == TrangThaiBan.TRONG);

            if (khuVucMatch && statusMatch) {
                BanPanel banPanel = new BanPanel(ban);

                if (selectedTargetTable != null && selectedTargetTable.equals(ban)) {
                    banPanel.setSelected(true);
                    targetStillVisible = true;
                }

                banPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            handleSelectTarget(ban, banPanel); // Hàm xử lý chọn đơn
                        }
                    }
                });
                leftTableContainer.add(banPanel);
                leftBanPanelList.add(banPanel);
            }
        }
        if (!targetStillVisible) selectedTargetTable = null;

        leftTableContainer.revalidate();
        leftTableContainer.repaint();
    }

    private void populateRightPanel(String khuVucFilter) {
        rightTableContainer.removeAll();
        rightBanPanelList.clear();

        boolean sourceStillVisible = false;

        for (Ban ban : allTablesFromDB) {
            boolean khuVucMatch = khuVucFilter.equals("Tất cả") || ban.getKhuVuc().equals(khuVucFilter);
            TrangThaiBan status = ban.getTrangThai();
            boolean statusMatch = (status == TrangThaiBan.DANG_PHUC_VU || status == TrangThaiBan.DA_DAT_TRUOC);
            if (khuVucMatch && statusMatch) {
                BanPanel banPanel = new BanPanel(ban);

                if (selectedSourceTable != null && selectedSourceTable.equals(ban)) {
                    banPanel.setSelected(true);
                    sourceStillVisible = true;
                }

                banPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            handleSelectSource(ban, banPanel); // Hàm xử lý chọn đơn
                        }
                    }
                });
                rightTableContainer.add(banPanel);
                rightBanPanelList.add(banPanel);
            }
        }
        if (!sourceStillVisible) selectedSourceTable = null;

        rightTableContainer.revalidate();
        rightTableContainer.repaint();
    }

    private void handleSelectSource(Ban ban, BanPanel clickedPanel) {
        // 1. Nếu click vào bàn đang chọn -> Bỏ chọn
        if (selectedSourceTable != null && selectedSourceTable.equals(ban)) {
            selectedSourceTable = null;
            clickedPanel.setSelected(false);
            return;
        }

        // 2. Chọn bàn mới -> Bỏ chọn tất cả bàn cũ trong panel phải
        selectedSourceTable = ban;
        for (BanPanel p : rightBanPanelList) {
            p.setSelected(false); // Reset hết
        }
        clickedPanel.setSelected(true); // Highlight bàn mới
    }

    private void handleSelectTarget(Ban ban, BanPanel clickedPanel) {
        // 1. Nếu click vào bàn đang chọn -> Bỏ chọn
        if (selectedTargetTable != null && selectedTargetTable.equals(ban)) {
            selectedTargetTable = null;
            clickedPanel.setSelected(false);
            return;
        }

        // 2. Chọn bàn mới -> Bỏ chọn tất cả bàn cũ trong panel trái
        selectedTargetTable = ban;
        for (BanPanel p : leftBanPanelList) {
            p.setSelected(false); // Reset hết
        }
        clickedPanel.setSelected(true); // Highlight bàn mới
    }

    private void xuLyChuyenBan() {
        // 1. Validate
        if (selectedSourceTable == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn cần chuyển (Bên trái)!", "Chưa chọn bàn nguồn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedTargetTable == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn mới để đến (Bên phải)!", "Chưa chọn bàn đích", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Xác nhận
        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Xác nhận chuyển toàn bộ đơn từ bàn [%s] sang bàn [%s]?",
                        selectedSourceTable.getTenBan(), selectedTargetTable.getTenBan()),
                "Xác nhận chuyển bàn",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // 3. Thực hiện chuyển trong CSDL
        boolean ketQua = thucHienChuyenBanTrongDB(selectedSourceTable, selectedTargetTable);

        if (ketQua) {
            JOptionPane.showMessageDialog(this, "Chuyển bàn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

            // Cập nhật lại giao diện cha (ManHinhBanGUI)
            if (getParent() instanceof RootPaneContainer) { // Hoặc cách nào đó để gọi refreshManHinhBan
                // Tạm thời dispose, ManHinhBanGUI cần tự refresh khi dialog đóng
            }
            dispose(); // Đóng dialog
        } else {
            JOptionPane.showMessageDialog(this, "Chuyển bàn thất bại! Vui lòng kiểm tra lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean thucHienChuyenBanTrongDB(Ban banCu, Ban banMoi) {
        // Logic chuyển bàn:
        // 1. Tìm đơn đặt món (DonDatMon) đang hoạt động của banCu
        // 2. Update maBan của đơn đó thành banMoi.getMaBan()
        // 3. Update trạng thái banCu thành TRONG
        // 4. Update trạng thái banMoi thành trạng thái cũ của banCu (DANG_PHUC_VU hoặc DA_DAT_TRUOC)
        // 5. Update gioMoBan của banMoi = gioMoBan của banCu, banCu = null

        // Bạn cần viết hàm này trong BanDAO hoặc DonDatMonDAO
        // Ví dụ gọi: return banDAO.chuyenBan(banCu.getMaBan(), banMoi.getMaBan());

        System.out.println("Đang thực hiện chuyển DB: " + banCu.getMaBan() + " -> " + banMoi.getMaBan());

        // --- CHỖ NÀY CẦN GỌI DAO THỰC SỰ ---
        return banDAO.chuyenBan(banCu, banMoi);
        // -----------------------------------
    }

    private void stylePrimaryButton(JButton b) {
        b.setBackground(BanPanel.COLOR_ACCENT_BLUE);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 20, 8, 20));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleDefaultButton(JButton b) {
        b.setBackground(new Color(230, 230, 230));
        b.setForeground(new Color(51, 51, 51));
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 20, 8, 20));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}