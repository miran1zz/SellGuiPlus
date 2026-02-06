package org.mik.sellguiplus.model;

public class PendingSignAction {

    public enum Type {
        SEARCH
    }

    private final Type type;
    private final int page;

    public PendingSignAction(Type type, int page) {
        this.type = type;
        this.page = page;
    }

    public Type getType() {
        return type;
    }

    public int getPage() {
        return page;
    }
}
