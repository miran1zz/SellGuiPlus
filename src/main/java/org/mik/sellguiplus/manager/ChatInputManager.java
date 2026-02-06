package org.mik.sellguiplus.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.mik.sellguiplus.model.PendingChatAction;

public class ChatInputManager {

    private final Map<UUID, PendingChatAction> pending = new ConcurrentHashMap<>();

    public void setPending(UUID uuid, PendingChatAction action) {
        pending.put(uuid, action);
    }

    public PendingChatAction getPending(UUID uuid) {
        return pending.get(uuid);
    }

    public void clear(UUID uuid) {
        pending.remove(uuid);
    }
}
