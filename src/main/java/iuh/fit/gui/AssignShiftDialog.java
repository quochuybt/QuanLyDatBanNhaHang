package iuh.fit.gui;

import iuh.fit.core.dto.CaLamDTO;
import iuh.fit.core.dto.PhanCongDTO;
import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.net.client.CaLamRemoteService;
import iuh.fit.core.net.client.ClientEventListener;
import iuh.fit.core.net.client.NhanVienRemoteService;
import iuh.fit.core.net.client.PhanCongRemoteService;
import iuh.fit.core.net.client.SocketClientConnection;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AssignShiftDialog extends JDialog {

    private static final Color COLOR_BUTTON_BLUE = new Color(40, 28, 244);
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_COMPONENT = new Font("Arial", Font.PLAIN, 14);

    private final CaLamRemoteService caLamRemoteService;
    private final NhanVienRemoteService nhanVienRemoteService;
    private final PhanCongRemoteService phanCongRemoteService;

    private JSpinner dateSpinner;
    private JComboBox<CaLamDTO> cbCaLam;

    private DefaultListModel<NhanVien> modelChuaPhanCong;
    private DefaultListModel<NhanVien> modelDaPhanCong;

    private JList<NhanVien> listChuaPhanCong;
    private JList<NhanVien> listDaPhanCong;

    private JButton btnAdd;
    private JButton btnRemove;
    private JButton btnClose;

    private boolean suppressReload = false;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public AssignShiftDialog(Frame owner, SocketClientConnection socketConnection) {
        super(owner, "Phân công ca làm", true);

        Objects.requireNonNull(socketConnection, "SocketClientConnection không được null.");
        this.phanCongRemoteService = new PhanCongRemoteService(socketConnection);
        this.caLamRemoteService = new CaLamRemoteService(socketConnection);
        this.nhanVienRemoteService = new NhanVienRemoteService(socketConnection);

        socketConnection.addEventListener(new ClientEventListener() {
            @Override
            public void onEvent(MessageEnvelope event) {
                if (event == null || event.getName() == null) return;
                if (EventType.SHIFT_UPDATED.name().equals(event.getName())) {
                    SwingUtilities.invokeLater(AssignShiftDialog.this::reloadNhanVienTheoCaVaNgay);
                }
            }
        });

        setSize(700, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPane.setBackground(Color.WHITE);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        addEventHandlers();
        loadCaLam();
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblNgay = new JLabel("Chọn ngày:");
        lblNgay.setFont(FONT_LABEL);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.15;
        topPanel.add(lblNgay, gbc);

        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        dateSpinner.setFont(FONT_COMPONENT);
        dateSpinner.setValue(new Date());

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.85;
        topPanel.add(dateSpinner, gbc);

        JLabel lblCa = new JLabel("Chọn ca:");
        lblCa.setFont(FONT_LABEL);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.15;
        topPanel.add(lblCa, gbc);

        cbCaLam = new JComboBox<>();
        cbCaLam.setFont(FONT_COMPONENT);
        cbCaLam.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof CaLamDTO ca) {
                    String gioBatDau = ca.getGioBatDau() != null
                            ? ca.getGioBatDau().format(timeFormatter)
                            : "--:--";

                    String gioKetThuc = ca.getGioKetThuc() != null
                            ? ca.getGioKetThuc().format(timeFormatter)
                            : "--:--";

                    setText(String.format("%s (%s - %s)", ca.getTenCa(), gioBatDau, gioKetThuc));
                }

                return this;
            }
        });

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.85;
        topPanel.add(cbCaLam, gbc);

        return topPanel;
    }

    private JPanel createCenterPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 10, 5, 10);

        JLabel lblChuaPhanCong = new JLabel("Nhân viên chưa phân công:");
        lblChuaPhanCong.setFont(FONT_LABEL);

        JLabel lblDaPhanCong = new JLabel("Nhân viên đã phân công:");
        lblDaPhanCong.setFont(FONT_LABEL);

        modelChuaPhanCong = new DefaultListModel<>();
        modelDaPhanCong = new DefaultListModel<>();

        listChuaPhanCong = new JList<>(modelChuaPhanCong);
        listDaPhanCong = new JList<>(modelDaPhanCong);

        listChuaPhanCong.setFont(FONT_COMPONENT);
        listDaPhanCong.setFont(FONT_COMPONENT);

        listChuaPhanCong.setCellRenderer(createNhanVienRenderer());
        listDaPhanCong.setCellRenderer(createNhanVienRenderer());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.45;
        gbc.weighty = 0;
        mainPanel.add(lblChuaPhanCong, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1;
        mainPanel.add(new JScrollPane(listChuaPhanCong), gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 15));
        buttonPanel.setBackground(Color.WHITE);

        btnAdd = new JButton(">>");
        btnRemove = new JButton("<<");

        btnAdd.setFont(FONT_LABEL);
        btnRemove.setFont(FONT_LABEL);

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnRemove);
        buttonPanel.add(Box.createVerticalGlue());

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        gbc.weighty = 1;
        mainPanel.add(buttonPanel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.45;
        gbc.weighty = 0;
        mainPanel.add(lblDaPhanCong, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1;
        mainPanel.add(new JScrollPane(listDaPhanCong), gbc);

        return mainPanel;
    }

    private JPanel createBottomPanel() {
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(Color.WHITE);

        btnClose = new JButton("Đóng");
        btnClose.setFont(FONT_LABEL);
        btnClose.setFocusPainted(false);

        southPanel.add(btnClose);

        return southPanel;
    }

    private ListCellRenderer<NhanVien> createNhanVienRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String tenNV = value.getHoten() != null ? value.getHoten() : "Không rõ";
            String maNV = value.getManv() != null ? value.getManv() : "N/A";

            JLabel label = new JLabel(tenNV + " (" + maNV + ")");
            label.setOpaque(true);
            label.setFont(FONT_COMPONENT);
            label.setBorder(new EmptyBorder(6, 8, 6, 8));

            if (isSelected) {
                label.setBackground(new Color(255, 255, 200));
                label.setForeground(Color.BLACK);
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }

            return label;
        };
    }

    private void loadCaLam() {
        setBusy(true);

        new SwingWorker<List<CaLamDTO>, Void>() {
            @Override
            protected List<CaLamDTO> doInBackground() {
                return caLamRemoteService.getAllCaLamOrderByGioBatDau();
            }

            @Override
            protected void done() {
                try {
                    List<CaLamDTO> dsCaLam = get();

                    suppressReload = true;
                    cbCaLam.removeAllItems();

                    if (dsCaLam != null) {
                        for (CaLamDTO ca : dsCaLam) {
                            cbCaLam.addItem(ca);
                        }
                    }

                    if (cbCaLam.getItemCount() > 0) {
                        cbCaLam.setSelectedIndex(0);
                    }

                    suppressReload = false;
                    reloadNhanVienTheoCaVaNgay();

                } catch (Exception e) {
                    suppressReload = false;
                    showError("Lỗi tải danh sách ca làm: " + getRootMessage(e), "Lỗi");
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void addEventHandlers() {
        btnAdd.addActionListener(e -> themNhanVienVaoCa());
        btnRemove.addActionListener(e -> xoaNhanVienKhoiCa());
        btnClose.addActionListener(e -> dispose());

        cbCaLam.addActionListener(e -> {
            if (!suppressReload) {
                reloadNhanVienTheoCaVaNgay();
            }
        });

        dateSpinner.addChangeListener(e -> {
            if (!suppressReload) {
                reloadNhanVienTheoCaVaNgay();
            }
        });
    }

    private LocalDate getSelectedDate() {
        Date date = (Date) dateSpinner.getValue();

        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private CaLamDTO getSelectedCaLam() {
        return (CaLamDTO) cbCaLam.getSelectedItem();
    }

    private void reloadNhanVienTheoCaVaNgay() {
        if (modelChuaPhanCong == null || modelDaPhanCong == null) {
            return;
        }

        CaLamDTO ca = getSelectedCaLam();

        if (ca == null || ca.getMaCa() == null) {
            modelChuaPhanCong.clear();
            modelDaPhanCong.clear();
            setBusy(false);
            return;
        }

        LocalDate ngayLam = getSelectedDate();
        String maCa = ca.getMaCa();

        setBusy(true);

        new SwingWorker<ReloadResult, Void>() {
            @Override
            protected ReloadResult doInBackground() {
                List<NhanVien> dsTatCaNV = nhanVienRemoteService.findAll().stream()
                        .map(iuh.fit.core.dto.NhanVienDTO::toEntity)
                        .toList();

                /*
                 * Gọi socket: PHANCONG_LIST_BY_DATE
                 */
                List<PhanCongDTO> dsPhanCongTrongNgay = phanCongRemoteService.findByNgayLam(ngayLam);

                Set<String> idDaPhanCongCaNay = dsPhanCongTrongNgay.stream()
                        .filter(Objects::nonNull)
                        .filter(pc -> maCa.equals(pc.getMaCa()))
                        .map(PhanCongDTO::getMaNV)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(HashSet::new));

                ReloadResult result = new ReloadResult();

                if (dsTatCaNV != null) {
                    for (NhanVien nv : dsTatCaNV) {
                        if (nv == null || nv.getManv() == null) {
                            continue;
                        }

                        if (idDaPhanCongCaNay.contains(nv.getManv())) {
                            result.daPhanCong.add(nv);
                        } else {
                            result.chuaPhanCong.add(nv);
                        }
                    }
                }

                return result;
            }

            @Override
            protected void done() {
                try {
                    ReloadResult result = get();

                    modelChuaPhanCong.clear();
                    modelDaPhanCong.clear();

                    for (NhanVien nv : result.chuaPhanCong) {
                        modelChuaPhanCong.addElement(nv);
                    }

                    for (NhanVien nv : result.daPhanCong) {
                        modelDaPhanCong.addElement(nv);
                    }

                } catch (Exception e) {
                    showError(
                            "Lỗi tải danh sách nhân viên theo ca/ngày: " + getRootMessage(e),
                            "Lỗi"
                    );
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void themNhanVienVaoCa() {
        NhanVien nv = listChuaPhanCong.getSelectedValue();
        CaLamDTO ca = getSelectedCaLam();

        if (nv == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần thêm.");
            return;
        }

        if (ca == null || ca.getMaCa() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ca làm.");
            return;
        }

        String maNV = nv.getManv();
        String maCa = ca.getMaCa();
        LocalDate ngayLam = getSelectedDate();

        if (maNV == null || maNV.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã nhân viên không hợp lệ.");
            return;
        }

        setBusy(true);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                /*
                 * Gọi socket: PHANCONG_ADD
                 */
                return phanCongRemoteService.themPhanCong(maNV, maCa, ngayLam);
            }

            @Override
            protected void done() {
                try {
                    boolean success = Boolean.TRUE.equals(get());

                    if (success) {
                        JOptionPane.showMessageDialog(
                                AssignShiftDialog.this,
                                "Thêm phân công thành công.",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        reloadNhanVienTheoCaVaNgay();
                    } else {
                        showError(
                                "Không thể thêm nhân viên. Có thể nhân viên đã có ca khác.",
                                "Không thể thêm nhân viên"
                        );
                        setBusy(false);
                    }

                } catch (Exception e) {
                    showError(
                            "Lỗi: " + getRootMessage(e),
                            "Không thể thêm nhân viên"
                    );
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void xoaNhanVienKhoiCa() {
        NhanVien nv = listDaPhanCong.getSelectedValue();
        CaLamDTO ca = getSelectedCaLam();

        if (nv == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa.");
            return;
        }

        if (ca == null || ca.getMaCa() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ca làm.");
            return;
        }

        String maNV = nv.getManv();
        String maCa = ca.getMaCa();
        LocalDate ngayLam = getSelectedDate();

        if (maNV == null || maNV.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã nhân viên không hợp lệ.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn hủy phân công nhân viên này khỏi ca đã chọn?",
                "Xác nhận hủy phân công",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setBusy(true);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                /*
                 * Gọi socket: PHANCONG_REMOVE
                 */
                return phanCongRemoteService.xoaPhanCong(maNV, maCa, ngayLam);
            }

            @Override
            protected void done() {
                try {
                    boolean success = Boolean.TRUE.equals(get());

                    if (success) {
                        JOptionPane.showMessageDialog(
                                AssignShiftDialog.this,
                                "Hủy phân công thành công.",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        reloadNhanVienTheoCaVaNgay();
                    } else {
                        showError("Không thể xóa nhân viên khỏi ca.", "Không thể xóa nhân viên");
                        setBusy(false);
                    }

                } catch (Exception e) {
                    showError(
                            "Lỗi: " + getRootMessage(e),
                            "Không thể xóa nhân viên"
                    );
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void setBusy(boolean busy) {
        if (btnAdd != null) {
            btnAdd.setEnabled(!busy);
        }

        if (btnRemove != null) {
            btnRemove.setEnabled(!busy);
        }

        if (btnClose != null) {
            btnClose.setEnabled(!busy);
        }

        if (cbCaLam != null) {
            cbCaLam.setEnabled(!busy);
        }

        if (dateSpinner != null) {
            dateSpinner.setEnabled(!busy);
        }

        setCursor(busy
                ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor()
        );
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

    private static class ReloadResult {
        private final java.util.List<NhanVien> chuaPhanCong = new java.util.ArrayList<>();
        private final java.util.List<NhanVien> daPhanCong = new java.util.ArrayList<>();
    }
}
