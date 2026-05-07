package iuh.fit.gui;

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
import java.util.Date;
import java.util.List;

public class AssignShiftDialog extends JDialog {

    private final CaLamService caLamService = new CaLamService();
    private final NhanVienService nhanVienService = new NhanVienService();
    private final PhanCongService phanCongService = new PhanCongService();

    private JSpinner dateSpinner;
    private JComboBox<CaLamDTO> cbCaLam;
    private JList<NhanVien> listNhanVien;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public AssignShiftDialog(Frame owner) {
        super(owner, "Phân công ca làm", true);

        setSize(460, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        loadData();
    }

    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Ngày:"), gbc);

        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));

        gbc.gridx = 1;
        form.add(dateSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Ca làm:"), gbc);

        cbCaLam = new JComboBox<>();
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
        form.add(cbCaLam, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        form.add(new JLabel("Nhân viên:"), gbc);

        DefaultListModel<NhanVien> model = new DefaultListModel<>();
        listNhanVien = new JList<>(model);
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
                    setText(nv.getHoten() + " (" + nv.getManv() + ")");
                }

                return this;
            }
        });

        JScrollPane sp = new JScrollPane(listNhanVien);

        gbc.gridy = 3;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        form.add(sp, gbc);

        return form;
    }

    private JPanel createButtonPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton save = new JButton("Phân công");
        JButton cancel = new JButton("Hủy");

        save.addActionListener(e -> assign());
        cancel.addActionListener(e -> dispose());

        p.add(save);
        p.add(cancel);

        return p;
    }

    private void loadData() {
        cbCaLam.removeAllItems();

        for (CaLamDTO ca : caLamService.getAllCaLamOrderByGioBatDau()) {
            cbCaLam.addItem(ca);
        }

        DefaultListModel<NhanVien> model =
                (DefaultListModel<NhanVien>) listNhanVien.getModel();

        model.clear();

        for (NhanVien nv : nhanVienService.findAll()) {
            model.addElement(nv);
        }
    }

    private void assign() {
        Date date = (Date) dateSpinner.getValue();
        CaLamDTO ca = (CaLamDTO) cbCaLam.getSelectedItem();
        List<NhanVien> nvs = listNhanVien.getSelectedValuesList();

        if (ca == null || nvs.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn ca và ít nhất 1 nhân viên"
            );
            return;
        }

        LocalDate ngay = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        int ok = 0;
        int fail = 0;

        for (NhanVien nv : nvs) {
            try {
                phanCongService.phanCong(nv.getManv(), ca.getMaCa(), ngay);
                ok++;
            } catch (Exception ex) {
                fail++;
            }
        }

        JOptionPane.showMessageDialog(
                this,
                "Thành công: " + ok + ", thất bại: " + fail
        );

        if (ok > 0) {
            dispose();
        }
    }
}