package iuh.fit.gui;

import iuh.fit.core.net.client.SocketClientConnection;
import iuh.fit.core.net.client.NetClientContext;
import iuh.fit.core.net.client.discovery.DiscoveredServer;
import iuh.fit.core.net.client.discovery.LanServerDiscoveryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ServerConnectionGUI extends JFrame {
    private final DefaultListModel<DiscoveredServer> serverListModel = new DefaultListModel<>();
    private final JList<DiscoveredServer> listServers = new JList<>(serverListModel);
    private final JLabel lblStatus = new JLabel("Trạng thái: Chưa quét server");
    private final JButton btnScan = new JButton("Quét server");
    private final JButton btnConnect = new JButton("Kết nối");
    private final JTextField txtManualHost = new JTextField("192.168.1.", 15);
    private final JTextField txtManualPort = new JTextField("9090", 5);
    private final JButton btnManualConnect = new JButton("Kết nối thủ công");

    public ServerConnectionGUI() {
        setTitle("Kết nối Server - StarGuardian Restaurant");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 420);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(root);

        JLabel title = new JLabel("Chọn server trong mạng LAN");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        root.add(title, BorderLayout.NORTH);

        listServers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listServers.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String source = value.getDiscoverySource() != null ? value.getDiscoverySource() : "N/A";
            JLabel label = new JLabel(value.getServiceName() + " - " + value.getHost() + ":" + value.getPort() + " [" + source + "]");
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(6, 8, 6, 8));
            if (isSelected) {
                label.setBackground(new Color(220, 235, 255));
            } else {
                label.setBackground(Color.WHITE);
            }
            return label;
        });
        root.add(new JScrollPane(listServers), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(10, 10));
        lblStatus.setFont(new Font("Arial", Font.ITALIC, 12));
        bottom.add(lblStatus, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnScan);
        buttons.add(btnConnect);
        bottom.add(buttons, BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);

        btnScan.addActionListener(e -> scanServers());
        btnConnect.addActionListener(e -> connectSelectedServer());
        btnManualConnect.addActionListener(e -> connectManual());

        // Panel nhập IP thủ công
        JPanel manualPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        manualPanel.setBorder(BorderFactory.createTitledBorder("Kết nối thủ công (nếu không tìm thấy server)"));
        manualPanel.add(new JLabel("IP:"));
        manualPanel.add(txtManualHost);
        manualPanel.add(new JLabel("Port:"));
        manualPanel.add(txtManualPort);
        manualPanel.add(btnManualConnect);
        bottom.add(manualPanel, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::scanServers);
    }

    private void scanServers() {
        btnScan.setEnabled(false);
        serverListModel.clear();
        lblStatus.setForeground(Color.BLACK);

        new SwingWorker<List<DiscoveredServer>, String>() {
            @Override
            protected List<DiscoveredServer> doInBackground() {
                publish("Đang quét tìm server qua mDNS...");
                LanServerDiscoveryService discoveryService = new LanServerDiscoveryService(9091, 9090);
                Map<String, List<DiscoveredServer>> byStrategy = discoveryService.discoverByStrategy(2500, 1500);

                List<DiscoveredServer> mdns = byStrategy.getOrDefault("mDNS", List.of());
                if (!mdns.isEmpty()) {
                    publish("Tìm thấy server qua mDNS");
                    return mdns;
                }

                List<DiscoveredServer> udp = byStrategy.getOrDefault("UDP", List.of());
                if (!udp.isEmpty()) {
                    publish("Tìm thấy server qua UDP");
                    return udp;
                }

                publish("Không thấy qua mDNS/UDP, đang quét subnet TCP (có thể mất 10 giây)...");
                List<DiscoveredServer> tcp = byStrategy.getOrDefault("TCP Scan", List.of());
                if (!tcp.isEmpty()) {
                    publish("Tìm thấy server qua TCP scan");
                }
                return tcp;
            }

            @Override
            protected void process(List<String> chunks) {
                lblStatus.setText("Trạng thái: " + chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                btnScan.setEnabled(true);
                try {
                    List<DiscoveredServer> servers = get();
                    for (DiscoveredServer s : servers) {
                        serverListModel.addElement(s);
                    }
                    if (servers.isEmpty()) {
                        lblStatus.setText("Trạng thái: Không tìm thấy server — thử nhập IP thủ công");
                        lblStatus.setForeground(Color.RED);
                    } else {
                        listServers.setSelectedIndex(0);
                        lblStatus.setText("Trạng thái: Tìm thấy " + servers.size() + " server");
                        lblStatus.setForeground(new Color(0, 100, 0));
                    }
                } catch (Exception ex) {
                    lblStatus.setText("Trạng thái: Lỗi quét server");
                    lblStatus.setForeground(Color.RED);
                }
            }
        }.execute();
    }

    private void connectManual() {
        String host = txtManualHost.getText().trim();
        String portStr = txtManualPort.getText().trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập địa chỉ IP server.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Port không hợp lệ.");
            return;
        }

        btnManualConnect.setEnabled(false);
        lblStatus.setText("Trạng thái: Đang kết nối " + host + ":" + port + "...");

        final int finalPort = port;
        new SwingWorker<SocketClientConnection, Void>() {
            @Override
            protected SocketClientConnection doInBackground() {
                SocketClientConnection connection = new SocketClientConnection(host, finalPort, 5000, 15000);
                connection.connect();
                return connection;
            }

            @Override
            protected void done() {
                btnManualConnect.setEnabled(true);
                try {
                    SocketClientConnection connection = get();
                    DiscoveredServer server = DiscoveredServer.builder()
                            .serviceName("Manual")
                            .host(host)
                            .port(finalPort)
                            .version("N/A")
                            .discoverySource("Manual")
                            .build();
                    NetClientContext.set(connection, server);
                    lblStatus.setText("Trạng thái: Đã kết nối " + host + ":" + finalPort);
                    dispose();
                    SwingUtilities.invokeLater(() -> new TaiKhoanGUI(connection, server).setVisible(true));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ServerConnectionGUI.this,
                            "Không thể kết nối " + host + ":" + finalPort + "\nChi tiết: " + ex.getMessage(),
                            "Lỗi kết nối",
                            JOptionPane.ERROR_MESSAGE
                    );
                    lblStatus.setText("Trạng thái: Kết nối thất bại");
                }
            }
        }.execute();
    }

    private void connectSelectedServer() {
        DiscoveredServer selected = listServers.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 server trước khi kết nối.");
            return;
        }

        btnConnect.setEnabled(false);
        lblStatus.setText("Trạng thái: Đang kết nối " + selected.getHost() + "...");

        new SwingWorker<SocketClientConnection, Void>() {
            @Override
            protected SocketClientConnection doInBackground() {
                // Kết nối TCP persistent dùng cho toàn bộ command/event sau login
                SocketClientConnection connection = new SocketClientConnection(
                        selected.getHost(),
                        selected.getPort(),
                        5000,
                        15000
                );
                connection.connect();
                return connection;
            }

            @Override
            protected void done() {
                btnConnect.setEnabled(true);
                try {
                    SocketClientConnection connection = get();
                    NetClientContext.set(connection, selected);
                    lblStatus.setText("Trạng thái: Đã kết nối server");
                    dispose();
                    SwingUtilities.invokeLater(() -> new TaiKhoanGUI(connection, selected).setVisible(true));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ServerConnectionGUI.this,
                            "Không thể kết nối server đã chọn.\nChi tiết: " + ex.getMessage(),
                            "Lỗi kết nối",
                            JOptionPane.ERROR_MESSAGE
                    );
                    lblStatus.setText("Trạng thái: Kết nối thất bại");
                }
            }
        }.execute();
    }
}
