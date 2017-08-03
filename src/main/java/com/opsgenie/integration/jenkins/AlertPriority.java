package com.opsgenie.integration.jenkins;

public enum  AlertPriority {
    P1("P1", "P1-Critical"),
    P2("P2", "P2-High"),
    P3("P3", "P3-Moderate"),
    P4("P4", "P4-Low"),
    P5("P5", "P5-Informational");

    private String value;
    private String displayName;

    AlertPriority(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AlertPriority fromDisplayName(String value) {
        for (AlertPriority priority : AlertPriority.values()) {
            if (priority.getDisplayName().equals(value)) {
                return priority;
            }
        }

        return P3;
    }

}
