package gui;

import com.toedter.calendar.JDateChooser;
import iuh.fit.core.dto.CaLamDTO;
import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.service.CaLamService;
import iuh.fit.core.service.NhanVienService;
import iuh.fit.core.service.PhanCongService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AssignShiftDialog extends JDialog {

    private static final Color COLOR_BUTTON_BLUE = new Color(40, 28, 244);
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_COMPONENT = new Font("Arial", Font.PLAIN, 14);

    private JDateChooser dateChooser;
    private JComboBox<CaLamDTO> cbCaLam;
    private JList<NhanVien> listNhanVien;
    private DefaultListModel<NhanVien> modelNhanVien;
    private JButton btnAssign;
    private JButton btnCancel;

    private final CaLamService caLamService;
    private final NhanVienService nhanVienService;
    private final PhanCongService phanCongService;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public AssignShiftDialog(Frame owner) {
        super(owner, "Phân công ca làm", true);

        this.caLamService = new CaLamService();
        this.nhanVienService = new NhanVienService();
        this.phanCongService = new PhanCongService();

        setSize(450, 550);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        loadCaLam();
        loadNhanVien();

        addEventHandlers();
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblNgay = new JLabel("Chọn ngày:");
        lblNgay.setFont(FONT_LABEL);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        formPanel.add(lblNgay, gbc);

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setFont(FONT_COMPONENT);
        dateChooser.setDate(new Date());

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        formPanel.add(dateChooser, gbc);

        JLabel lblCa = new JLabel("Chọn ca:");
        lblCa.setFont(FONT_LABEL);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        formPanel.add(lblCa, gbc);

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

                    setText(String.format(
                            "%s (%s - %s)",
                            ca.getTenCa(),
                            gioBatDau,
                            gioKetThuc
                    ));
                }

                return this;
            }
        });

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.8;
        formPanel.add(cbCaLam, gbc);

        JLabel lblNV = new JLabel("Chọn nhân viên:");
        lblNV.setFont(FONT_LABEL);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        formPanel.add(lblNV, gbc);

        modelNhanVien = new DefaultListModel<>();
        listNhanVien = new JList<>(modelNhanVien);
        listNhanVien.setFont(FONT_COMPONENT);
        listNhanVien.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listNhanVien.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof NhanVien nv) {
                    String hoTen = nv.getHoten() != null ? nv.getHoten() : "Không rõ";
                    setText(hoTen);
                }

                return this;
            }
        });

        JScrollPane scrollPaneNV = new JScrollPane(listNhanVien);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollPaneNV, gbc);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

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

    private void loadNhanVien() {
        modelNhanVien.clear();

        try {
            List<NhanVien> dsNhanVien = nhanVienService.findAll();

            for (NhanVien nv : dsNhanVien) {
                modelNhanVien.addElement(nv);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải danh sách nhân viên: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void addEventHandlers() {
        btnAssign.addActionListener(e -> assignShifts());
        btnCancel.addActionListener(e -> dispose());
    }

    private void assignShifts() {
        Date selectedUtilDate = dateChooser.getDate();
        CaLamDTO selectedCaLam = (CaLamDTO) cbCaLam.getSelectedItem();
        List<NhanVien> selectedNhanViens = listNhanVien.getSelectedValuesList();

        if (selectedUtilDate == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn ngày.",
                    "Lỗi",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (selectedCaLam == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn ca.",
                    "Lỗi",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (selectedNhanViens == null || selectedNhanViens.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn ít nhất một nhân viên.",
                    "Lỗi",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        LocalDate selectedDate = selectedUtilDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        String maCa = selectedCaLam.getMaCa();

        int successCount = 0;
        int failCount = 0;
        List<String> failedNames = new ArrayList<>();

        for (NhanVien nv : selectedNhanViens) {
            try {
                boolean success = phanCongService.themPhanCong(
                        nv.getManv(),
                        maCa,
                        selectedDate
                );

                if (success) {
                    successCount++;
                } else {
                    failCount++;
                    failedNames.add(nv.getHoten());
                }

            } catch (Exception ex) {
                failCount++;
                failedNames.add(nv.getHoten());
            }
        }

        StringBuilder message = new StringBuilder();

        if (successCount > 0) {
            message.append("Đã phân công thành công cho ")
                    .append(successCount)
                    .append(" nhân viên.\n");
        }

        if (failCount > 0) {
            message.append("Phân công thất bại cho ")
                    .append(failCount)
                    .append(" nhân viên (có thể đã được phân công ca khác):\n");

            for (String name : failedNames) {
                message.append("- ").append(name).append("\n");
            }
        }

        JOptionPane.showMessageDialog(
                this,
                message.toString(),
                "Kết quả phân công",
                failCount == 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
        );

        if (failCount < selectedNhanViens.size()) {
            dispose();
        }
    }
}