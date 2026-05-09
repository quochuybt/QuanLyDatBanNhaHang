package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.dto.GiaoCaDTO;
import iuh.fit.core.net.dto.giaoca.GiaoCaDashboardRequest;
import iuh.fit.core.net.dto.giaoca.GiaoCaDashboardResponse;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.GiaoCaService;
import iuh.fit.core.service.HoaDonService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class GiaoCaDashboardLoadHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiaoCaDashboardLoadHandler.class);

    private final GiaoCaService giaoCaService = new GiaoCaService();
    private final HoaDonService hoaDonService = new HoaDonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            GiaoCaDashboardRequest payload = parsePayload(request, GiaoCaDashboardRequest.class);

            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaNV(), "Mã nhân viên không được để trống.");

            String maNV = payload.getMaNV().trim();
            LocalDate today = LocalDate.now();

            GiaoCaDTO caHienTai = giaoCaService.getThongTinCaDangLam(maNV);

            double tongGioTuan = giaoCaService.getTongGioLamTheoTuan(maNV, today);
            double tongGioThang = giaoCaService.getTongGioLamTheoThang(maNV, today);

            LocalDateTime dauNgay = today.atStartOfDay();

            double doanhThuHomNay =
                    hoaDonService.getDoanhThuTheoHinhThuc(maNV, dauNgay, "Tiền mặt")
                            + hoaDonService.getDoanhThuTheoHinhThuc(maNV, dauNgay, "Chuyển khoản")
                            + hoaDonService.getDoanhThuTheoHinhThuc(maNV, dauNgay, "Thẻ");

            double doanhThuCaHienTai = 0;
            double tienMatTrongKet = 0;

            if (caHienTai != null && caHienTai.getThoiGianBatDau() != null) {
                LocalDateTime batDauCa = caHienTai.getThoiGianBatDau();

                double revCash = hoaDonService.getDoanhThuTheoHinhThuc(maNV, batDauCa, "Tiền mặt");
                double revTransfer = hoaDonService.getDoanhThuTheoHinhThuc(maNV, batDauCa, "Chuyển khoản");
                double revCard = hoaDonService.getDoanhThuTheoHinhThuc(maNV, batDauCa, "Thẻ");

                doanhThuCaHienTai = revCash + revTransfer + revCard;
                tienMatTrongKet = caHienTai.getTienDauCa() + revCash;
            }

            Map<String, Double> gioLamTheoNgay = giaoCaService.getGioLamTheoNgay(maNV, 7);
            List<String> cacCaLamSapToi = giaoCaService.getCacCaLamSapToi(maNV);

            String[] caTruocSau = giaoCaService.getThongTinCaTruocSau(maNV, today);

            GiaoCaDashboardResponse response = GiaoCaDashboardResponse.builder()
                    .caHienTai(caHienTai)
                    .tongGioTuan(tongGioTuan)
                    .tongGioThang(tongGioThang)
                    .doanhThuHomNay(doanhThuHomNay)
                    .doanhThuCaHienTai(doanhThuCaHienTai)
                    .tienMatTrongKet(tienMatTrongKet)
                    .gioLamTheoNgay(gioLamTheoNgay)
                    .cacCaLamSapToi(cacCaLamSapToi)
                    .caTruoc(caTruocSau != null && caTruocSau.length > 0 ? caTruocSau[0] : "Không có")
                    .caSau(caTruocSau != null && caTruocSau.length > 1 ? caTruocSau[1] : "Không có")
                    .build();

            LOGGER.info("[SocketServer] GIAOCA_DASHBOARD_LOAD thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", maNV=" + maNV);

            return ok(request, response);
        }, "Lỗi server khi tải dashboard nhân viên.");
    }
}