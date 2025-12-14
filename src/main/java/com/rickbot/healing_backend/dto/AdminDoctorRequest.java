package com.rickbot.healing_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminDoctorRequest {

    @NotBlank
    @Size(min = 2, max = 255)
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 2, max = 255)
    private String specialty;

    private String phone;
    private String bio;
    @Size(min = 6)
    private String password;

    public AdminDoctorRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
