package util;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

public class AppResource {

    // Biến static để lưu ảnh, chỉ load 1 lần duy nhất vào bộ nhớ
    private static Image APP_ICON = null;

    public static Image getAppIcon() {
        if (APP_ICON != null) {
            return APP_ICON;
        }

        // Logic load ảnh chuẩn (như bạn vừa làm thành công)
        try {
            // Thay đường dẫn này bằng đường dẫn thật của bạn
            URL url = AppResource.class.getResource("/img/DangNhap+Logo/Logo.jpg");

            if (url != null) {
                APP_ICON = Toolkit.getDefaultToolkit().getImage(url);
            } else {
                System.err.println("Không tìm thấy icon!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return APP_ICON;
    }
}