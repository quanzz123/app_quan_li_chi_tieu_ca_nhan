package com.example.app_quan_li_chi_tieu_ca_nhan.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Lớp tiện ích hỗ trợ định dạng tiền tệ (VND) cho ứng dụng.
 */
public class CurrencyUtils {
    
    /**
     * Định dạng số tiền kiểu double thành chuỗi hiển thị theo định dạng tiền tệ Việt Nam (VND).
     * Ví dụ: 100000 -> "100.000 đ"
     * 
     * @param amount Số tiền cần định dạng
     * @return Chuỗi số tiền đã được định dạng kèm ký hiệu "đ"
     */
    public static String formatVND(double amount) {
        // Thiết lập ký hiệu định dạng cho tiếng Việt
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.'); // Dấu phân cách hàng nghìn
        symbols.setMonetaryDecimalSeparator(','); // Dấu phân cách thập phân
        
        // Định dạng số không hiển thị phần thập phân lẻ lẻ
        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
        return decimalFormat.format(amount) + " đ";
    }

    /**
     * Định dạng số tiền giao dịch kèm theo tiền tố cộng (+) hoặc trừ (-) tương ứng với thu hay chi.
     * Ví dụ: Chi 50000 -> "-50.000 đ", Thu 20000 -> "+20.000 đ"
     * 
     * @param amount Số tiền giao dịch
     * @param isExpense true nếu là khoản chi tiêu (dấu trừ), false nếu là khoản thu nhập/nạp tiền (dấu cộng)
     * @return Chuỗi số tiền giao dịch có tiền tố (+/-) và đã định dạng VND
     */
    public static String formatTransactionAmount(double amount, boolean isExpense) {
        String prefix = isExpense ? "-" : "+";
        return prefix + formatVND(Math.abs(amount));
    }
}

