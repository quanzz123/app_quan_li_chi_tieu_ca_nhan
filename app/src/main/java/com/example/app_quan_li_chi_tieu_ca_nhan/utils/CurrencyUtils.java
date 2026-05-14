package com.example.app_quan_li_chi_tieu_ca_nhan.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyUtils {
    public static String formatVND(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setMonetaryDecimalSeparator(',');
        
        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
        return decimalFormat.format(amount) + " đ";
    }

    public static String formatTransactionAmount(double amount, boolean isExpense) {
        String prefix = isExpense ? "-" : "+";
        return prefix + formatVND(Math.abs(amount));
    }
}
