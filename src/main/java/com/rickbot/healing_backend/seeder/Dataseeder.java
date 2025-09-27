package com.rickbot.healing_backend.seeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.rickbot.healing_backend.entity.Doctor;
import com.rickbot.healing_backend.entity.Location;
import com.rickbot.healing_backend.repository.DoctorRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Dataseeder implements CommandLineRunner {

    private final DoctorRepo repo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            Doctor d = new Doctor();
            d.setName("Seed Doctor");
            d.setSpecialization("General");
            d.setEmail("doc@test.com");
            d.setPassword(encoder.encode("password123"));
            d.setPhoneNumber("1234567890");
            Location loc = new Location();
            loc.setLatitude(33.59);
            loc.setLongitude(-7.6);
            loc.setAddress("Seed St");
            d.setLocation(loc);
            repo.save(d);
        }
    }
}

