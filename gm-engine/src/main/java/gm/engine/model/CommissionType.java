package gm.engine.model;

public enum CommissionType {
    ON_CLOSE("on-close"),
    ON_PURCHASE("on-purchase");

    private final String xmlValue;

    CommissionType(String xmlValue) {
        this.xmlValue = xmlValue;
    }

    public String getXmlValue() {
        return xmlValue;
    }

    public static CommissionType fromXmlValue(String value) {
        for (CommissionType type : values()) {
            if (type.xmlValue.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown commission type: " + value);
    }
}
