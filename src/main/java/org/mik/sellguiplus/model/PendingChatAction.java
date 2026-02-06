package org.mik.sellguiplus.model;

public class PendingChatAction {

    public enum Type {
        PRICE,
        SLOT
    }

    private final Type type;
    private final String itemKey;
    private final int page;

    public PendingChatAction(Type type, String itemKey, int page) {
        this.type = type;
        this.itemKey = itemKey;
        this.page = page;
    }

    public Type getType() {
        return type;
    }

    public String getItemKey() {
        return itemKey;
    }

    public int getPage() {
        return page;
    }
}
