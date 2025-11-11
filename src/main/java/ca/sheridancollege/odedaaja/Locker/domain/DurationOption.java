package ca.sheridancollege.odedaaja.Locker.domain;

public enum DurationOption {
    QUARTERLY(4),
    TWO_QUARTERS(8),
    YEARLY(12);

    private final int months;
    DurationOption(int months) { this.months = months; }
    public int getMonths() { return months; }
}
