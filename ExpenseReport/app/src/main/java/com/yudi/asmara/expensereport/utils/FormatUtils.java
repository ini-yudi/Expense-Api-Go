package com.yudi.asmara.expensereport.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtils {

    public static String rupiah(double amount) {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
        fmt.setMinimumFractionDigits(0);
        fmt.setMaximumFractionDigits(0);
        return "Rp " + fmt.format(amount).replace(",", ".");
    }

    public static String getEmojiForIcon(String icon) {
        if (icon == null) return "📦";
        switch (icon.toLowerCase()) {
            case "restaurant": return "🍽";
            case "local_cafe": return "☕";
            case "cookie": return "🍪";
            case "directions_bus": return "🚌";
            case "shopping_cart": return "🛒";
            case "movie": return "🎬";
            case "receipt": return "🧾";
            case "local_hospital": return "🏥";
            case "school": return "🎓";
            case "category": return "📦";
            default: return "📦";
        }
    }
}
