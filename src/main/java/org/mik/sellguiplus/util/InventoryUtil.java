package org.mik.sellguiplus.util;

import java.util.HashSet;
import java.util.Set;

public final class InventoryUtil {

    private InventoryUtil() {
    }

    public static Set<Integer> getBorderSlots(int size) {
        Set<Integer> slots = new HashSet<>();
        int rows = size / 9;
        for (int col = 0; col < 9; col++) {
            slots.add(col);
            slots.add((rows - 1) * 9 + col);
        }
        for (int row = 0; row < rows; row++) {
            slots.add(row * 9);
            slots.add(row * 9 + 8);
        }
        return slots;
    }
}
