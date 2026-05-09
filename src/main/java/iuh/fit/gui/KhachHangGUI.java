package iuh.fit.gui;

import com.toedter.calendar.JDateChooser;
import iuh.fit.core.dto.KhachHangDTO;
import iuh.fit.core.entity.HangThanhVien;
import iuh.fit.core.net.client.KhachHangRemoteService;
import iuh.fit.core.net.client.SocketClientConnection;
import iuh.fit.core.net.protocol.EventType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class KhachHangGUI extends BaseEventAwarePanel {

    private static KhachHangGUI instance;

    private static final Color COLOR_BACKGROUND = new Color(244, 247, 252);
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);
    private static final Color COLOR_BUTTON_BLUE = new Color(40, 28, 244);
    private static final Color COLOR_TEXT_WHITE = Color.WHITE;
    private static final Color COLOR_TABLE_GRID = new Color(220, 220, 220);

    private JTextField txtMaKH;
    private JTextField txtTenKH;
    private JTextField txtSDT;
    private JTextField txtEmail;
    private JTextField txtDiaChi;
    private JTextField txtTongChiTieu;
    private JComboBox<String> cbGioiTinh;
    private JComboBox<String> cbHangTV;
    private JTextField txtNgayThamGia;
    private JDateChooser txtNgaySinh;

    private JButton btnThem;
    private JButton btnSua;
    private JButton btnTimKiem;
    private JButton btnLamMoiForm;

    private JTable tblKhachHang;
    private DefaultTableModel modelKhachHang;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0' VND'");

    private final KhachHangRemoteService khachHangRemoteService;

    private List<KhachHangDTO> dsKhachHang;
    private KhachHangDTO khachHangDangChon = null;

    public KhachHangGUI(SocketClientConnection socketConnection) {
        super(socketConnection);
        this.khachHangRemoteService = new KhachHangRemoteService(socketConnection);

        instance = this;

        setLayout(new BorderLayout(10, 15));
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        addEventListeners();
        refreshKhachHangTable();
        lamMoiForm();
    }

    @Override
    protected void onBusinessEvent(EventType eventType) {
        if (eventType == EventType.KHACHHANG_UPDATED) {
            SwingUtilities.invokeLater(this::refreshKhachHangTable);
        }
    }

    public static void reloadKhachHangTableIfAvailable() {
        if (instance != null) {
            SwingUtilities.invokeLater(instance::refreshKhachHangTable);
        }
    }

    public void refreshKhachHangTable() {
        setBusy(true);

        new SwingWorker<List<KhachHangDTO>, Void>() {
            @Override
            protected List<KhachHangDTO> doInBackground() {
                return khachHangRemoteService.findAll();
            }

            @Override
            protected void done() {
                try {
                    loadDataToTable(get());
                    revalidate();
                    repaint();
                } catch (Exception e) {
                    showError(
                            "Lỗi khi làm mới danh sách khách hàng: " + getRootMessage(e),
                            "Lỗi"
                    );
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void loadDataToTable(List<KhachHangDTO> listKH) {
        modelKhachHang.setRowCount(0);
        dsKhachHang = listKH;

        if (dsKhachHang == null || dsKhachHang.isEmpty()) {
            return;
        }

        int stt = 1;

        for (KhachHangDTO kh : dsKhachHang) {
            if (kh == null) {
                continue;
            }

            HangThanhVien hang = kh.getHangThanhVien() != null
                    ? kh.getHangThanhVien()
                    : HangThanhVien.MEMBER;

            modelKhachHang.addRow(new Object[]{
                    stt++,
                    kh.getMaKH(),
                    kh.getTenKH(),
                    kh.getGioiTinh(),
                    kh.getSdt(),
                    kh.getEmail(),
                    currencyFormat.format(kh.getTongChiTieu()),
                    hang.toString()
            });
        }
    }

    private class HangThanhVienRenderer extends DefaultTableCellRenderer {
        private Color getBackgroundColor(HangThanhVien hang) {
            return switch (hang) {
                case DIAMOND -> new Color(255, 240, 255);
                case GOLD -> new Color(255, 245, 204);
                case SILVER -> new Color(240, 240, 245);
                case BRONZE -> new Color(250, 240, 230);
                case MEMBER -> new Color(230, 240, 255);
                case NONE -> new Color(255, 255, 255);
            };
        }

        private Color getForegroundColor(HangThanhVien hang) {
            return switch (hang) {
                case DIAMOND -> new Color(180, 0, 180);
                case GOLD -> new Color(200, 160, 0);
                case SILVER -> new Color(100, 100, 150);
                case BRONZE -> new Color(150, 100, 50);
                case MEMBER -> new Color(40, 100, 180);
                case NONE -> Color.GRAY.darker();
            };
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JPanel panel = new JPanel(new GridBagLayout());

            HangThanhVien hang;
            try {
                hang = value != null ? HangThanhVien.valueOf(value.toString()) : HangThanhVien.NONE;
            } catch (Exception e) {
                hang = HangThanhVien.NONE;
            }

            JLabel label = new JLabel(hang.toString());
            label.setOpaque(true);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(5, 15, 5, 15));
            label.setBackground(getBackgroundColor(hang));
            label.setForeground(getForegroundColor(hang));

            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            panel.add(label);

            return panel;
        }
    }

    private void addEventListeners() {
        btnLamMoiForm.addActionListener(e -> {
            refreshKhachHangTable();
            lamMoiForm();
        });

        btnThem.addActionListener(e -> themKhachHang());
        btnSua.addActionListener(e -> suaKhachHang());
        btnTimKiem.addActionListener(e -> timKhachHang());

        tblKhachHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblKhachHang.getSelectedRow();

                if (row == -1 && tblKhachHang.getRowCount() > 0) {
                    row = tblKhachHang.rowAtPoint(e.getPoint());
                }

                if (row == -1 || dsKhachHang == null) {
                    return;
                }

                int modelRow = tblKhachHang.convertRowIndexToModel(row);
                String maKHTuBang = (String) modelKhachHang.getValueAt(modelRow, 1);

                khachHangDangChon = dsKhachHang.stream()
                        .filter(kh -> kh != null && Objects.equals(kh.getMaKH(), maKHTuBang))
                        .findFirst()
                        .orElse(null);

                hienThiChiTiet(khachHangDangChon);
            }
        });
    }

    private KhachHangDTO getKhachHangTuForm(boolean isNew) throws Exception {
        String ma = txtMaKH.getText().trim();
        String ten = txtTenKH.getText().trim();
        String gioiTinh = (String) cbGioiTinh.getSelectedItem();
        String sdt = txtSDT.getText().trim();
        String email = txtEmail.getText().trim();
        String diaChi = txtDiaChi.getText().trim();
        String ngayTGStr = txtNgayThamGia.getText().trim();

        if (ten.isEmpty()) {
            throw new Exception("Tên khách hàng không được rỗng!");
        }

        if (sdt.isEmpty() || !sdt.matches("\\d{10}")) {
            throw new Exception("Số điện thoại không hợp lệ. SĐT phải gồm 10 chữ số!");
        }

        Date selectedDate = txtNgaySinh.getDate();

        if (selectedDate == null) {
            throw new Exception("Ngày sinh không được rỗng hoặc sai định dạng!");
        }

        LocalDate ngaySinh = selectedDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate ngayThamGia = ngayTGStr.isEmpty()
                ? LocalDate.now()
                : LocalDate.parse(ngayTGStr, dtf);

        if (isNew) {
            return KhachHangDTO.builder()
                    .maKH(null)
                    .tenKH(ten)
                    .gioiTinh(gioiTinh)
                    .sdt(sdt)
                    .ngaySinh(ngaySinh)
                    .diaChi(diaChi)
                    .email(email)
                    .ngayThamGia(ngayThamGia)
                    .tongChiTieu(0f)
                    .hangThanhVien(HangThanhVien.MEMBER)
                    .build();
        }

        if (khachHangDangChon == null || khachHangDangChon.getMaKH() == null) {
            throw new Exception("Vui lòng chọn khách hàng cần sửa!");
        }

        float tongChiTieu = khachHangDangChon.getTongChiTieu();
        HangThanhVien hangTV = khachHangDangChon.getHangThanhVien() != null
                ? khachHangDangChon.getHangThanhVien()
                : HangThanhVien.MEMBER;

        return KhachHangDTO.builder()
                .maKH(ma)
                .tenKH(ten)
                .gioiTinh(gioiTinh)
                .sdt(sdt)
                .ngaySinh(ngaySinh)
                .diaChi(diaChi)
                .email(email)
                .ngayThamGia(ngayThamGia)
                .tongChiTieu(tongChiTieu)
                .hangThanhVien(hangTV)
                .build();
    }

    private void themKhachHang() {
        KhachHangDTO khMoi;

        try {
            khMoi = getKhachHangTuForm(true);
        } catch (Exception ex) {
            showError("Lỗi: " + ex.getMessage(), "Lỗi");
            return;
        }

        setBusy(true);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return khachHangRemoteService.addKhachHang(khMoi);
            }

            @Override
            protected void done() {
                try {
                    boolean success = Boolean.TRUE.equals(get());

                    if (success) {
                        JOptionPane.showMessageDialog(
                                KhachHangGUI.this,
                                "Thêm khách hàng thành công!",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        refreshKhachHangTable();
                        lamMoiForm();
                    } else {
                        showError("Không thể thêm khách hàng.", "Lỗi");
                        setBusy(false);
                    }
                } catch (Exception ex) {
                    showError("Lỗi thêm khách hàng: " + getRootMessage(ex), "Lỗi");
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void suaKhachHang() {
        if (khachHangDangChon == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn khách hàng cần sửa!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        KhachHangDTO khCapNhat;

        try {
            khCapNhat = getKhachHangTuForm(false);
        } catch (Exception ex) {
            showError("Lỗi: " + ex.getMessage(), "Lỗi");
            return;
        }

        setBusy(true);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return khachHangRemoteService.update(khCapNhat);
            }

            @Override
            protected void done() {
                try {
                    boolean success = Boolean.TRUE.equals(get());

                    if (success) {
                        JOptionPane.showMessageDialog(
                                KhachHangGUI.this,
                                "Cập nhật khách hàng thành công!",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        lamMoiForm();
                    } else {
                        showError("Không thể cập nhật khách hàng.", "Lỗi");
                        setBusy(false);
                    }
                } catch (Exception ex) {
                    showError("Lỗi cập nhật khách hàng: " + getRootMessage(ex), "Lỗi");
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void timKhachHang() {
        String tuKhoa = JOptionPane.showInputDialog(
                this,
                "Nhập Tên hoặc Số điện thoại để tìm kiếm:",
                "Tìm kiếm khách hàng",
                JOptionPane.PLAIN_MESSAGE
        );

        if (tuKhoa == null) {
            return;
        }

        String keyword = tuKhoa.trim();

        setBusy(true);

        new SwingWorker<List<KhachHangDTO>, Void>() {
            @Override
            protected List<KhachHangDTO> doInBackground() {
                return khachHangRemoteService.search(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<KhachHangDTO> ketQua = get();

                    if (ketQua == null || ketQua.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                KhachHangGUI.this,
                                "Không tìm thấy khách hàng nào phù hợp.",
                                "Kết quả tìm kiếm",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }

                    loadDataToTable(ketQua);
                    lamMoiForm();
                } catch (Exception ex) {
                    showError("Lỗi tìm kiếm khách hàng: " + getRootMessage(ex), "Lỗi");
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.add(createFormDetailPanel(), BorderLayout.CENTER);
        headerPanel.add(createButtonGroupPanel(), BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createFormDetailPanel() {
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_TABLE_GRID),
                new EmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        int row = 0;
        final double WEIGHT_LABEL = 0.01;
        final double WEIGHT_INPUT = 1.0;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Mã khách hàng:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_INPUT;
        txtMaKH = new JTextField(20);
        txtMaKH.setEditable(false);
        formContainer.add(txtMaKH, gbc);

        gbc.gridx = 2;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Ngày sinh (dd/MM/yyyy):"), gbc);

        gbc.gridx = 3;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_INPUT;
        txtNgaySinh = new JDateChooser();
        txtNgaySinh.setDateFormatString("dd/MM/yyyy");
        formContainer.add(txtNgaySinh, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Tên khách hàng:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_INPUT;
        txtTenKH = new JTextField();
        formContainer.add(txtTenKH, gbc);

        gbc.gridx = 2;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Địa chỉ:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_INPUT;
        txtDiaChi = new JTextField();
        formContainer.add(txtDiaChi, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Giới tính:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_INPUT;
        cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        cbGioiTinh.setPreferredSize(new Dimension(0, 30));
        formContainer.add(cbGioiTinh, gbc);

        gbc.gridx = 2;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Ngày tham gia (dd/MM/yyyy):"), gbc);

        gbc.gridx = 3;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_INPUT;
        txtNgayThamGia = new JTextField();
        txtNgayThamGia.setEditable(false);
        formContainer.add(txtNgayThamGia, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Số điện thoại:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_INPUT;
        txtSDT = new JTextField();
        formContainer.add(txtSDT, gbc);

        gbc.gridx = 2;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Tổng chi tiêu:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_INPUT;
        txtTongChiTieu = new JTextField();
        txtTongChiTieu.setEditable(false);
        formContainer.add(txtTongChiTieu, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_INPUT;
        txtEmail = new JTextField();
        formContainer.add(txtEmail, gbc);

        gbc.gridx = 2;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Hạng thành viên:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = row;
        gbc.weightx = WEIGHT_INPUT;
        cbHangTV = new JComboBox<>(getHangThanhVienOptions());
        cbHangTV.setEnabled(false);
        cbHangTV.setPreferredSize(new Dimension(0, 30));
        formContainer.add(cbHangTV, gbc);

        return formContainer;
    }

    private String[] getHangThanhVienOptions() {
        HangThanhVien[] values = HangThanhVien.values();
        String[] options = new String[values.length];

        for (int i = 0; i < values.length; i++) {
            options[i] = values[i].toString();
        }

        return options;
    }

    private JPanel createButtonGroupPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

        btnLamMoiForm = createStyledButton("🔄 Làm mới", COLOR_ACCENT_BLUE.brighter());
        btnThem = createStyledButton(" Thêm", new Color(0, 150, 50));
        btnSua = createStyledButton(" Sửa", COLOR_BUTTON_BLUE);
        btnTimKiem = createStyledButton(" Tìm kiếm", Color.LIGHT_GRAY.darker());

        Dimension buttonSize = new Dimension(150, 40);

        btnLamMoiForm.setMaximumSize(buttonSize);
        btnThem.setMaximumSize(buttonSize);
        btnSua.setMaximumSize(buttonSize);
        btnTimKiem.setMaximumSize(buttonSize);

        btnLamMoiForm.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnThem.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSua.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTimKiem.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(btnLamMoiForm);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnThem);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnSua);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnTimKiem);

        return buttonPanel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(COLOR_TEXT_WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {
                "STT",
                "Mã khách hàng",
                "Tên khách hàng",
                "Giới tính",
                "Số điện thoại",
                "Email",
                "Tổng chi tiêu",
                "Hạng thành viên"
        };

        modelKhachHang = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblKhachHang = new JTable(modelKhachHang);
        tblKhachHang.setRowHeight(35);
        tblKhachHang.setFont(new Font("Arial", Font.PLAIN, 14));
        tblKhachHang.setGridColor(COLOR_TABLE_GRID);
        tblKhachHang.setShowGrid(true);
        tblKhachHang.setIntercellSpacing(new Dimension(0, 0));

        tblKhachHang.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tblKhachHang.getTableHeader().setOpaque(false);
        tblKhachHang.getTableHeader().setBackground(new Color(235, 240, 247));
        tblKhachHang.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tblKhachHang.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        tblKhachHang.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblKhachHang.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblKhachHang.getColumnModel().getColumn(7).setCellRenderer(new HangThanhVienRenderer());

        tblKhachHang.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblKhachHang.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblKhachHang.getColumnModel().getColumn(7).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(tblKhachHang);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID));

        return scrollPane;
    }

    private void hienThiChiTiet(KhachHangDTO kh) {
        if (kh == null) {
            return;
        }

        txtMaKH.setText(kh.getMaKH());
        txtTenKH.setText(kh.getTenKH());
        cbGioiTinh.setSelectedItem(kh.getGioiTinh());
        txtSDT.setText(kh.getSdt());
        txtEmail.setText(kh.getEmail());
        txtDiaChi.setText(kh.getDiaChi());

        if (kh.getNgaySinh() != null) {
            Date date = Date.from(
                    kh.getNgaySinh()
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
            );
            txtNgaySinh.setDate(date);
        } else {
            txtNgaySinh.setDate(null);
        }

        txtNgayThamGia.setText(
                kh.getNgayThamGia() != null ? kh.getNgayThamGia().format(dtf) : ""
        );

        txtTongChiTieu.setText(currencyFormat.format(kh.getTongChiTieu()));

        HangThanhVien hang = kh.getHangThanhVien() != null
                ? kh.getHangThanhVien()
                : HangThanhVien.MEMBER;

        cbHangTV.setSelectedItem(hang.toString());
    }

    private void lamMoiForm() {
        khachHangDangChon = null;

        txtMaKH.setText("(Tự động)");
        txtTenKH.setText("");
        cbGioiTinh.setSelectedItem("Nam");
        txtSDT.setText("");
        txtEmail.setText("");
        txtDiaChi.setText("");
        txtNgaySinh.setDate(null);
        txtNgayThamGia.setText(LocalDate.now().format(dtf));
        txtTongChiTieu.setText(currencyFormat.format(0.0f));
        cbHangTV.setSelectedItem(HangThanhVien.MEMBER.toString());

        if (tblKhachHang != null) {
            tblKhachHang.clearSelection();
        }
    }

    private void setBusy(boolean busy) {
        setCursor(busy
                ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor()
        );

        if (btnThem != null) {
            btnThem.setEnabled(!busy);
        }

        if (btnSua != null) {
            btnSua.setEnabled(!busy);
        }

        if (btnTimKiem != null) {
            btnTimKiem.setEnabled(!busy);
        }

        if (btnLamMoiForm != null) {
            btnLamMoiForm.setEnabled(!busy);
        }
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }

    private String getRootMessage(Exception e) {
        Throwable t = e;

        while (t.getCause() != null) {
            t = t.getCause();
        }

        return t.getMessage() != null ? t.getMessage() : e.getMessage();
    }
}