package de.dustplanet.silkspawnersshopaddon.shop;

import java.util.Arrays;
import java.util.Locale;

import javax.annotation.Nullable;

public enum SilkspawnersShopMode {
    SELL, BUY;

    @Nullable
    public static SilkspawnersShopMode getMode(String stringMode) {
        if (stringMode == null) {
            return null;
        }
        // In case one sends a new syntax like BUY:16
        switch (stringMode.split(":")[0].toUpperCase(Locale.ENGLISH)) {
            case "BUY":
                return BUY;
            case "SELL":
                return SELL;
            default:
                return null;
        }
    }

    public static boolean isValidMode(SilkspawnersShopMode mode) {
        return Arrays.stream(SilkspawnersShopMode.values()).anyMatch(mode::equals);
    }
}
