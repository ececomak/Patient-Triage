package com.ece.ertriage.model;

public final class Patient {
    private final String id;
    private final String name;
    private final int age;
    private final Severity severity;
    private final String complaint;
    private final boolean chronic;
    private final boolean pregnant;
    private final long arrivalMillis;
    private final long arrivalOrder;

    public Patient(String id, String name, int age, Severity severity, String complaint,
                   boolean chronic, boolean pregnant, long arrivalMillis, long arrivalOrder) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.severity = severity;
        this.complaint = complaint;
        this.chronic = chronic;
        this.pregnant = pregnant;
        this.arrivalMillis = arrivalMillis;
        this.arrivalOrder = arrivalOrder;
    }

    public String id() { return id; }
    public String name() { return name; }
    public int age() { return age; }
    public Severity severity() { return severity; }
    public String complaint() { return complaint; }
    public boolean chronic() { return chronic; }
    public boolean pregnant() { return pregnant; }
    public long arrivalMillis() { return arrivalMillis; }
    public long arrivalOrder() { return arrivalOrder; }
}