package iuh.fit.gui;

import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.entity.Ban;
import iuh.fit.core.entity.TrangThaiBan;
import iuh.fit.core.mapper.JsonMapper;
import iuh.fit.core.service.BanService;
import iuh.fit.core.service.DonDatMonService;
import iuh.fit.core.service.KhachHangService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ManHinhDatBanGUI extends JPanel {

    private final BanService banService = new BanService();
    private final DonDatMonService donDatMonService = new DonDatMonService();
    private final KhachHangService khachHangService = new KhachHangService();

    private JSpinner spSoKhach;
    private JSpinner spNgay;
    private JSpinner spGio;
    private JTextField txtSDT;
    private JTextField txtGhiChu;
    private JTable tblBan;
    private DefaultTableModel model;

    private List<Ban> danhSachLoc;

    public ManHinhDatBanGUI(DanhSachBanGUI parent, MainGUI mainGUI) {
        this();
    }

    public ManHinhDatBanGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 20, 15, 20));
        add(createFilterPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.SOUTH);
        taiDanhSachBan();
    }

    public void refreshData() {
        taiDanhSachBan();
    }

    private JPanel createFilterPanel() {
        JPanel p = new JPanel(new GridLayout(2, 4, 8, 8));
        p.add(new JLabel("Số khách:"));
        p.add(new JLabel("Ngày đến:"));
        p.add(new JLabel("Giờ đến:"));
        p.add(new JLabel("SĐT khách (tùy chọn):"));

        spSoKhach = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        spNgay = new JSpinner(new SpinnerDateModel());
        spNgay.setEditor(new JSpinner.DateEditor(spNgay, "dd/MM/yyyy"));
        spGio = new JSpinner(new SpinnerDateModel());
        spGio.setEditor(new JSpinner.DateEditor(spGio, "HH:mm"));
        txtSDT = new JTextField();
        txtGhiChu = new JTextField();
        JButton btnLoc = new JButton("Lọc bàn trống phù hợp");
        btnLoc.addActionListener(e -> taiDanhSachBan());

        p.add(spSoKhach);
        p.add(spNgay);
        p.add(spGio);
        p.add(txtSDT);
        p.add(new JLabel("Ghi chú:"));
        p.add(txtGhiChu);
        p.add(new JLabel());
        p.add(btnLoc);
        return p;
    }

    private JScrollPane createTablePanel() {
        model = new DefaultTableModel(new String[]{"Mã bàn", "Tên bàn", "Số ghế", "Khu vực", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblBan = new JTable(model);
        tblBan.setRowHeight(30);
        return new JScrollPane(tblBan);
    }

    private JPanel createActionPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDat = new JButton("Đặt bàn");
        btnDat.addActionListener(e -> datBan());
        p.add(btnDat);
        return p;
    }

    private void taiDanhSachBan() {
        int soKhach = (Integer) spSoKhach.getValue();
        LocalDateTime thoiGianDen = layThoiGianDen();

        List<String> maBanDaDat = donDatMonService.getMaBanDaDatTrongKhoang(
                thoiGianDen.minusMinutes(120),
                thoiGianDen.plusMinutes(120)
        );

        danhSachLoc = banService.getAllBan().stream().map(b -> JsonMapper.convert(b, Ban.class))
                .filter(b -> b.getSoGhe() >= soKhach)
                .filter(b -> b.getTrangThai() == TrangThaiBan.TRONG || b.getTrangThai() == TrangThaiBan.DA_DAT_TRUOC)
                .filter(b -> !maBanDaDat.contains(b.getMaBan()))
                .collect(Collectors.toList());

        model.setRowCount(0);
        for (Ban b : danhSachLoc) {
            model.addRow(new Object[]{b.getMaBan(), b.getTenBan(), b.getSoGhe(), b.getKhuVuc(), b.getTrangThai()});
        }
    }

    private LocalDateTime layThoiGianDen() {
        java.util.Date dNgay = (java.util.Date) spNgay.getValue();
        java.util.Date dGio = (java.util.Date) spGio.getValue();
        LocalDate ngay = dNgay.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalTime gio = dGio.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
        return LocalDateTime.of(ngay, gio);
    }

    private void datBan() {
        int row = tblBan.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn cần đặt");
            return;
        }

        String maBan = (String) model.getValueAt(row, 0);
        LocalDateTime thoiGianDen = layThoiGianDen();
        String sdt = txtSDT.getText().trim();
        String maKH = null;
        if (!sdt.isEmpty()) {
            var kh = khachHangService.findBySdt(sdt);
            if (kh != null) maKH = kh.getMaKH();
        }

        String maDon = "DON" + DateTimeFormatter.ofPattern("ddMMyyHHmmss").format(LocalDateTime.now()) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        DonDatMonDTO dto = DonDatMonDTO.builder()
                .maDon(maDon)
                .ngayKhoiTao(LocalDateTime.now())
                .maNV("NV01001")
                .maKH(maKH)
                .maBan(maBan)
                .thoiGianDen(thoiGianDen)
                .trangThai("Chưa thanh toán")
                .ghiChu(txtGhiChu.getText().trim())
                .build();

        try {
            donDatMonService.save(dto);
            JOptionPane.showMessageDialog(this, "Đặt bàn thành công: " + maDon);
            taiDanhSachBan();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đặt bàn thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
