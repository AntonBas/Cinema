package ua.lviv.bas.cinema.audit.service;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AuditDetails {

    private final Map<String, Object> values = new LinkedHashMap<>();

    private AuditDetails() {
    }

    public static AuditDetails of() {
        return new AuditDetails();
    }

    public AuditDetails put(String key, Object value) {
        values.put(key, value);
        return this;
    }

    public Map<String, Object> build() {
        return values;
    }
}
