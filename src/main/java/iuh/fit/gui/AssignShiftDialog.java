package iuh.fit.gui;

import iuh.fit.core.dto.CaLamDTO;
import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.entity.PhanCong;
import iuh.fit.core.service.CaLamService;
import iuh.fit.core.service.NhanVienService;
import iuh.fit.core.service.PhanCongService;

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

    private final CaLamService caLamService = new CaLamService();
    private final NhanVienService nhanVienService = new NhanVienService();
    private final PhanCongService phanCongService = new PhanCongService();

    private JSpinner dateSpinner;
    private JComboBox<CaLamDTO> cbCaLam;

    private DefaultListModel<NhanVien> modelChuaPhanCong;
    private DefaultListModel<NhanVien> modelDaPhanCong;

    private JList<NhanVien> listChuaPhanCong;
    private JList<NhanVien> listDaPhanCong;

    private JButton btnAdd;
    private JButton btnRemove;
    private JButton btnClose;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public AssignShiftDialog(Frame owner) {
        super(owner, "Phân công ca làm", true);

        setSize(700, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPane.setBackground(Color.WHITE);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        loadCaLam();
        addEventHandlers();
        reloadNhanVienTheoCaVaNgay();
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
        cbCaLam.removeAllItems();

        try {
            List<CaLamDTO> dsCaLam = caLamService.getAllCaLamOrderByGioBatDau();

            for (CaLamDTO ca : dsCaLam) {
                cbCaLam.addItem(ca);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải danh sách ca làm: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void addEventHandlers() {
        btnAdd.addActionListener(e -> themNhanVienVaoCa());
        btnRemove.addActionListener(e -> xoaNhanVienKhoiCa());
        btnClose.addActionListener(e -> dispose());

        cbCaLam.addActionListener(e -> reloadNhanVienTheoCaVaNgay());

        dateSpinner.addChangeListener(e -> reloadNhanVienTheoCaVaNgay());
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

        modelChuaPhanCong.clear();
        modelDaPhanCong.clear();

        CaLamDTO ca = getSelectedCaLam();

        if (ca == null || ca.getMaCa() == null) {
            return;
        }

        LocalDate ngayLam = getSelectedDate();

        try {
            List<NhanVien> dsTatCaNV = nhanVienService.findAll();

            List<PhanCong> dsPhanCongTrongNgay = phanCongService.findByNgayLam(ngayLam);

            Set<String> idDaPhanCongCaNay = dsPhanCongTrongNgay.stream()
                    .filter(pc -> pc.getCaLam() != null)
                    .filter(pc -> ca.getMaCa().equals(pc.getCaLam().getMaCa()))
                    .map(PhanCong::getNhanVien)
                    .filter(Objects::nonNull)
                    .map(NhanVien::getManv)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(HashSet::new));

            for (NhanVien nv : dsTatCaNV) {
                if (nv == null || nv.getManv() == null) {
                    continue;
                }

                if (idDaPhanCongCaNay.contains(nv.getManv())) {
                    modelDaPhanCong.addElement(nv);
                } else {
                    modelChuaPhanCong.addElement(nv);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải danh sách nhân viên theo ca/ngày: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
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

        try {
            boolean success = phanCongService.themPhanCong(
                    nv.getManv(),
                    ca.getMaCa(),
                    getSelectedDate()
            );

            if (success) {
                modelChuaPhanCong.removeElement(nv);
                modelDaPhanCong.addElement(nv);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Lỗi: Không thể thêm nhân viên. Có thể nhân viên đã có ca khác."
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi: " + e.getMessage(),
                    "Không thể thêm nhân viên",
                    JOptionPane.ERROR_MESSAGE
            );
        }
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

        try {
            boolean success = phanCongService.xoaPhanCong(
                    nv.getManv(),
                    ca.getMaCa(),
                    getSelectedDate()
            );

            if (success) {
                modelDaPhanCong.removeElement(nv);
                modelChuaPhanCong.addElement(nv);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Lỗi: Không thể xóa nhân viên."
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi: " + e.getMessage(),
                    "Không thể xóa nhân viên",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}