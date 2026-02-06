package org.mik.sellguiplus.model;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class SignInputSession {

    private final Location location;
    private final BlockData originalData;
    private final PendingSignAction action;

    public SignInputSession(Location location, BlockData originalData, PendingSignAction action) {
        this.location = location;
        this.originalData = originalData;
        this.action = action;
    }

    public Location getLocation() {
        return location;
    }

    public BlockData getOriginalData() {
        return originalData;
    }

    public PendingSignAction getAction() {
        return action;
    }
}
