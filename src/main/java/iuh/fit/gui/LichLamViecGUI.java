package iuh.fit.gui;

import iuh.fit.core.entity.CaLam;
import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.entity.PhanCong;
import iuh.fit.core.entity.VaiTro;
import iuh.fit.core.net.client.NhanVienRemoteService;
import iuh.fit.core.net.client.SocketClientConnection;
import iuh.fit.core.service.PhanCongService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private static final Font FONT_NAVIGATION = new Font("Arial", Font.BOLD, 16);

    private final PhanCongService phanCongService = new PhanCongService();
    private final NhanVienRemoteService nhanVienRemoteService;

    private final VaiTro currentUserRole;
    private final SocketClientConnection connection;

    private LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);

    private JLabel lblWeek;
    private JButton btnPrevWeek;
    private JButton btnNextWeek;
    private JPanel weekDisplayPanel;

    private final DateTimeFormatter weekRangeFormatter = DateTimeFormatter.ofPattern("dd/MM");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
    private final DateTimeFormatter fullDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public LichLamViecGUI(VaiTro role) {
        this(role, null);
    }

    public LichLamViecGUI(VaiTro role, SocketClientConnection connection) {
        this.currentUserRole = role;
        this.connection = connection;
        if (connection == null) {
            throw new IllegalStateException("Không có kết nối remote cho màn lịch làm việc.");
        }
        this.nhanVienRemoteService = new NhanVienRemoteService(connection);

        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        setBackground(COLOR_MAIN_BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);

        weekDisplayPanel = new JPanel(new BorderLayout());
        weekDisplayPanel.setOpaque(false);
        add(weekDisplayPanel, BorderLayout.CENTER);

        reloadData();
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);

        JLabel title = new JLabel("CA LÀM");
        title.setFont(FONT_TITLE);
        p.add(title, BorderLayout.WEST);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        center.setOpaque(false);

        btnPrevWeek = new JButton("<");
        styleNavigationButton(btnPrevWeek);

        lblWeek = new JLabel();
        lblWeek.setFont(FONT_NAVIGATION);
        lblWeek.setHorizontalAlignment(SwingConstants.CENTER);

        btnNextWeek = new JButton(">");
        styleNavigationButton(btnNextWeek);

        center.add(btnPrevWeek);
        center.add(lblWeek);
        center.add(btnNextWeek);

        btnPrevWeek.addActionListener(e -> {
            weekStart = weekStart.minusWeeks(1);
            reloadData();
        });

        btnNextWeek.addActionListener(e -> {
            weekStart = weekStart.plusWeeks(1);
            reloadData();
        });

        p.add(center, BorderLayout.CENTER);

        if (currentUserRole == VaiTro.QUANLY) {
            JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            buttonContainer.setOpaque(false);

            JButton assign = new JButton("Phân ca");
            styleAssignButton(assign);

            assign.addActionListener(e -> {
                if (connection == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Không tìm thấy kết nối socket để mở màn hình phân ca.",
                            "Lỗi kết nối",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                AssignShiftDialog d = new AssignShiftDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this),
                        connection
                );
                d.setVisible(true);
                reloadData();
            });

            buttonContainer.add(assign);
            p.add(buttonContainer, BorderLayout.EAST);
        }

        return p;
    }

    private void styleNavigationButton(JButton button) {
        button.setFont(FONT_NAVIGATION);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(40, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleAssignButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(COLOR_BUTTON_BLUE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void reloadData() {
        try {
            LocalDate end = weekStart.plusDays(6);

            lblWeek.setText(String.format(
                    "Tuần: %s - %s",
                    weekStart.format(weekRangeFormatter),
                    end.format(weekRangeFormatter)
            ));

            List<PhanCong> data = phanCongService.getPhanCongChiTiet(weekStart, end);

            weekDisplayPanel.removeAll();
            weekDisplayPanel.add(createWeekPanel(data), BorderLayout.CENTER);
            weekDisplayPanel.revalidate();
            weekDisplayPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi tải lịch làm việc: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JPanel createWeekPanel(List<PhanCong> data) {
        JPanel weekPanel = new JPanel(new GridLayout(1, 7, 15, 15));
        weekPanel.setOpaque(false);

        Map<LocalDate, List<PhanCong>> mapTheoNgay = data.stream()
                .filter(pc -> pc.getNgayLam() != null)
                .collect(Collectors.groupingBy(PhanCong::getNgayLam));

        Locale vi = new Locale("vi", "VN");

        for (int i = 0; i < 7; i++) {
            LocalDate ngayTrongTuan = weekStart.plusDays(i);

            String tenThu = ngayTrongTuan.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL_STANDALONE, vi);

            tenThu = tenThu.substring(0, 1).toUpperCase() + tenThu.substring(1);

            List<PhanCong> phanCongCuaNgay = mapTheoNgay.get(ngayTrongTuan);

            weekPanel.add(createDayColumn(tenThu, ngayTrongTuan, phanCongCuaNgay));
        }

        return weekPanel;
    }

    private JPanel createDayColumn(String dayTitle, LocalDate ngayLam, List<PhanCong> phanCongCuaNgay) {
        JPanel columnPanel = new JPanel(new BorderLayout(0, 10));
        columnPanel.setOpaque(false);

        RoundedPanel dayHeaderPanel = new RoundedPanel(12, COLOR_DAY_HEADER_BG);
        dayHeaderPanel.setLayout(new BoxLayout(dayHeaderPanel, BoxLayout.Y_AXIS));
        dayHeaderPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel lblDayName = new JLabel(dayTitle);
        lblDayName.setFont(FONT_DAY_HEADER);
        lblDayName.setForeground(COLOR_DAY_HEADER_TEXT);
        lblDayName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDayName.setOpaque(false);

        JLabel lblDate = new JLabel(ngayLam.format(dateFormatter));
        lblDate.setFont(FONT_CARD_BODY);
        lblDate.setForeground(COLOR_DAY_HEADER_TEXT);
        lblDate.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDate.setOpaque(false);

        dayHeaderPanel.add(lblDayName);
        dayHeaderPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        dayHeaderPanel.add(lblDate);

        columnPanel.add(dayHeaderPanel, BorderLayout.NORTH);

        RoundedPanel cardsContainer = new RoundedPanel(12, COLOR_COLUMN_BG);
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        if (phanCongCuaNgay != null && !phanCongCuaNgay.isEmpty()) {
            Map<CaLam, List<PhanCong>> mapTheoCa = phanCongCuaNgay.stream()
                    .filter(pc -> pc.getCaLam() != null)
                    .collect(Collectors.groupingBy(PhanCong::getCaLam));

            List<CaLam> caLamSorted = mapTheoCa.keySet().stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(CaLam::getGioBatDau))
                    .collect(Collectors.toList());

            for (CaLam ca : caLamSorted) {
                List<PhanCong> dsPhanCongTrongCa = mapTheoCa.get(ca);

                cardsContainer.add(createShiftCard(ca, dsPhanCongTrongCa, ngayLam));
                cardsContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } else {
            cardsContainer.add(createEmptyCard());
        }

        cardsContainer.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(cardsContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        columnPanel.add(scrollPane, BorderLayout.CENTER);

        return columnPanel;
    }

    private JPanel createShiftCard(CaLam ca, List<PhanCong> dsPhanCongTrongCa, LocalDate ngayLam) {
        RoundedPanel card = new RoundedPanel(12, COLOR_CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 15, 12, 15));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        String thoiGian = String.format(
                "%s - %s",
                ca.getGioBatDau() != null ? ca.getGioBatDau().format(timeFormatter) : "N/A",
                ca.getGioKetThuc() != null ? ca.getGioKetThuc().format(timeFormatter) : "N/A"
        );

        JLabel lblTime = new JLabel(thoiGian);
        lblTime.setFont(FONT_CARD_TIME);
        card.add(lblTime);

        card.add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel lblNhanVienTitle = new JLabel("Nhân viên:");
        lblNhanVienTitle.setFont(FONT_CARD_BODY_BOLD);
        card.add(lblNhanVienTitle);

        card.add(Box.createRigidArea(new Dimension(0, 3)));

        if (dsPhanCongTrongCa != null && !dsPhanCongTrongCa.isEmpty()) {
            for (PhanCong pc : dsPhanCongTrongCa) {
                NhanVien nv = pc.getNhanVien();

                String tenNV = "Không rõ";
                String maNV = "";

                if (nv != null) {
                    tenNV = nv.getHoten() != null ? nv.getHoten() : "Không rõ";
                    maNV = nv.getManv() != null ? " - " + nv.getManv() : "";
                }

                JLabel lblEmp = new JLabel(tenNV + maNV);
                lblEmp.setFont(FONT_CARD_BODY);
                card.add(lblEmp);
            }
        } else {
            JLabel lblEmpty = new JLabel("Chưa có nhân viên");
            lblEmpty.setFont(FONT_CARD_BODY);
            lblEmpty.setForeground(Color.GRAY);
            card.add(lblEmpty);
        }

        if (currentUserRole == VaiTro.QUANLY) {
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            card.add(createAddEmployeeButton(ca, ngayLam, dsPhanCongTrongCa));
        }

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));

        return card;
    }

    private JPanel createAddEmployeeButton(CaLam ca, LocalDate ngayLam, List<PhanCong> dsPhanCongTrongCa) {
        RoundedPanel pnlAdd = new RoundedPanel(8, COLOR_NOTE_BG_BLUE);
        pnlAdd.setLayout(new BoxLayout(pnlAdd, BoxLayout.X_AXIS));
        pnlAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnlAdd.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlAdd.setBorder(new EmptyBorder(5, 8, 5, 8));

        JLabel lblText = new JLabel("Thêm nhân viên vào ca");
        lblText.setFont(FONT_CARD_NOTE);
        lblText.setForeground(COLOR_NOTE_FG_BLUE);

        pnlAdd.add(lblText);

        pnlAdd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                List<NhanVien> dsDaPhanCong = getNhanVienDaPhanCong(dsPhanCongTrongCa);
                showPhanCongDialog(ca, ngayLam, dsDaPhanCong);
            }
        });

        return pnlAdd;
    }

    private List<NhanVien> getNhanVienDaPhanCong(List<PhanCong> dsPhanCongTrongCa) {
        if (dsPhanCongTrongCa == null) {
            return new ArrayList<>();
        }

        return dsPhanCongTrongCa.stream()
                .map(PhanCong::getNhanVien)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void showPhanCongDialog(CaLam ca, LocalDate ngayLam, List<NhanVien> dsDaPhanCong) {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Phân công nhân viên",
                true
        );

        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        String title = String.format(
                "Ca: %s (%s - %s) - Ngày: %s",
                ca.getTenCa(),
                ca.getGioBatDau() != null ? ca.getGioBatDau().format(timeFormatter) : "N/A",
                ca.getGioKetThuc() != null ? ca.getGioKetThuc().format(timeFormatter) : "N/A",
                ngayLam.format(fullDateFormatter)
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

        JLabel lblChuaPhanCong = new JLabel("Nhân viên chưa phân công:");
        JLabel lblDaPhanCong = new JLabel("Nhân viên đã phân công:");

        DefaultListModel<NhanVien> modelChuaPhanCong = new DefaultListModel<>();
        DefaultListModel<NhanVien> modelDaPhanCong = new DefaultListModel<>();

        JList<NhanVien> listChuaPhanCong = new JList<>(modelChuaPhanCong);
        JList<NhanVien> listDaPhanCong = new JList<>(modelDaPhanCong);

        Set<String> idDaPhanCong = dsDaPhanCong.stream()
                .map(NhanVien::getManv)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        List<NhanVien> dsTatCaNV;
        if (nhanVienRemoteService != null) {
            dsTatCaNV = nhanVienRemoteService.findAll().stream()
                    .map(iuh.fit.core.dto.NhanVienDTO::toEntity)
                    .toList();
        } else {
            dsTatCaNV = new ArrayList<>();
        }

        List<NhanVien> dsChuaPhanCong = dsTatCaNV.stream()
                .filter(nv -> nv.getManv() != null)
                .filter(nv -> !idDaPhanCong.contains(nv.getManv()))
                .collect(Collectors.toList());

        for (NhanVien nv : dsChuaPhanCong) {
            modelChuaPhanCong.addElement(nv);
        }

        for (NhanVien nv : dsDaPhanCong) {
            modelDaPhanCong.addElement(nv);
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.45;
        gbc.weighty = 0;
        mainPanel.add(lblChuaPhanCong, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
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
        gbc.weighty = 0;
        mainPanel.add(lblDaPhanCong, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        mainPanel.add(new JScrollPane(listDaPhanCong), gbc);

        dialog.add(mainPanel, BorderLayout.CENTER);

        JButton btnClose = new JButton("Đóng");
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(btnClose);
        dialog.add(southPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> {
            NhanVien nv = listChuaPhanCong.getSelectedValue();

            if (nv == null) {
                return;
            }

            try {
                boolean success = phanCongService.themPhanCong(
                        nv.getManv(),
                        ca.getMaCa(),
                        ngayLam
                );

                if (success) {
                    modelChuaPhanCong.removeElement(nv);
                    modelDaPhanCong.addElement(nv);
                } else {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Lỗi: Không thể thêm nhân viên. (Có thể NV đã có ca khác?)"
                    );
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        dialog,
                        "Lỗi: " + ex.getMessage(),
                        "Không thể thêm nhân viên",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        btnRemove.addActionListener(e -> {
            NhanVien nv = listDaPhanCong.getSelectedValue();

            if (nv == null) {
                return;
            }

            try {
                boolean success = phanCongService.xoaPhanCong(
                        nv.getManv(),
                        ca.getMaCa(),
                        ngayLam
                );

                if (success) {
                    modelDaPhanCong.removeElement(nv);
                    modelChuaPhanCong.addElement(nv);
                } else {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Lỗi: Không thể xóa nhân viên."
                    );
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        dialog,
                        "Lỗi: " + ex.getMessage(),
                        "Không thể xóa nhân viên",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        btnClose.addActionListener(e -> {
            dialog.dispose();
            reloadData();
        });

        ListCellRenderer<NhanVien> renderer = (list, value, index, isSelected, cellHasFocus) -> {
            String tenNV = value.getHoten() != null ? value.getHoten() : "Không rõ";

            JLabel label = new JLabel(tenNV);
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(5, 5, 5, 5));

            if (isSelected) {
                label.setBackground(new Color(255, 255, 200));
                label.setForeground(Color.BLACK);
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }

            return label;
        };

        listChuaPhanCong.setCellRenderer(renderer);
        listDaPhanCong.setCellRenderer(renderer);

        dialog.setVisible(true);
    }

    private JPanel createEmptyCard() {
        RoundedPanel card = new RoundedPanel(12, COLOR_CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 10, 15, 10));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblEmpty = new JLabel("<html><center>Chưa có<br>ca làm</center></html>", SwingConstants.CENTER);
        lblEmpty.setFont(FONT_CARD_BODY);
        lblEmpty.setForeground(Color.GRAY);

        card.add(lblEmpty, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        return card;
    }

    private static class RoundedPanel extends JPanel {
        private final Color backgroundColor;
        private final int cornerRadius;

        public RoundedPanel(int radius, Color bgColor) {
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

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);

            g2.dispose();
        }
    }
}
