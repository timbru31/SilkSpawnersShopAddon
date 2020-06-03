package de.dustplanet.silkspawnersshopaddon.shop;

import java.util.Arrays;
import java.util.Locale;

import javax.annotation.Nullable;

/**
 * Enum for possible shop operation modes.
 *
 * @author timbru31
 */
public enum SilkspawnersShopMode {
    SELL, BUY;

    /**
     * Helper method to retrieve the shop mode from a string.
     *
     * @param stringMode the shop mode as a string value to resolve
     * @return the shop mode if found, null otherwise
     */
    @Nullable
    @SuppressWarnings("checkstyle:ReturnCount")
    public static SilkspawnersShopMode getMode(final String stringMode) {
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

    /**
     * Checks whether a given shop mode is valid or not.
     *
     * @param mode the shop mode to check
     * @return the result - true or false
     */
    public static boolean isValidMode(final SilkspawnersShopMode mode) {
        return Arrays.stream(SilkspawnersShopMode.values()).anyMatch(mode::equals);
    }
}
