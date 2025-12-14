package com.rickbot.healing_backend.dto;

import java.util.List;

public class BulkImportRequest {
    private List<AdminDoctorRequest> doctors;
    // optional password to apply to all doctors (overrides per-item password when present)
    private String passwordForAll;
    // transactional: if true (default) will run in a single transaction and roll back on any error
    private Boolean transactional;

    public BulkImportRequest() {}

    public List<AdminDoctorRequest> getDoctors() { return doctors; }
    public void setDoctors(List<AdminDoctorRequest> doctors) { this.doctors = doctors; }
    public String getPasswordForAll() { return passwordForAll; }
    public void setPasswordForAll(String passwordForAll) { this.passwordForAll = passwordForAll; }
    public Boolean getTransactional() { return transactional; }
    public void setTransactional(Boolean transactional) { this.transactional = transactional; }
}
