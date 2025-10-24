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

            // Core helper method
            createDoctor("Seed Doctor", "General", "doc@test.com", "0600000000", 33.5731, -7.5898, "Seed St, Casablanca");

            createDoctor("Dr. Sarah Johnson", "Cardiology", "sarah.j@example.com", "0612345678", 33.5800, -7.6000, "123 Heart Center, Casablanca");
            createDoctor("Dr. Ahmed Hassan", "Orthopedic Surgery", "ahmed.h@example.com", "0612345679", 33.5850, -7.6100, "456 Bones Clinic, Casablanca");
            createDoctor("Dr. Maria Lopez", "ENT", "maria.l@example.com", "0612345680", 33.5750, -7.5950, "789 Hearing Center, Casablanca");
            createDoctor("Dr. John Smith", "Neurology", "john.s@example.com", "0612345681", 33.5780, -7.5920, "321 Brain Clinic, Casablanca");
            createDoctor("Dr. Fatima Zahra", "Dermatology", "fatima.z@example.com", "0612345682", 33.5760, -7.5980, "654 Skin Care, Casablanca");
            createDoctor("Dr. Ali Benkirane", "Pediatrics", "ali.b@example.com", "0612345683", 33.5790, -7.6010, "12 Children Hospital, Casablanca");
            createDoctor("Dr. Hanan El Fassi", "Ophthalmology", "hanan.f@example.com", "0612345684", 33.5770, -7.5960, "34 Eye Clinic, Casablanca");
            createDoctor("Dr. Omar El Mansouri", "Psychiatry", "omar.m@example.com", "0612345685", 33.5740, -7.5930, "56 Mind Care, Casablanca");
            createDoctor("Dr. Nadia Ait Ahmed", "Gastroenterology", "nadia.a@example.com", "0612345686", 33.5820, -7.5990, "78 Digestive Clinic, Casablanca");
            createDoctor("Dr. Youssef Chraibi", "Cardiology", "youssef.c@example.com", "0612345687", 33.5830, -7.6020, "90 Heart Center, Casablanca");
            createDoctor("Dr. Samira Bensalem", "Endocrinology", "samira.b@example.com", "0612345688", 33.5840, -7.6030, "22 Hormone Clinic, Casablanca");
            createDoctor("Dr. Khalid El Idrissi", "Orthopedic Surgery", "khalid.i@example.com", "0612345689", 33.5855, -7.6050, "44 Bones Clinic, Casablanca");
            createDoctor("Dr. Leila Bouzid", "ENT", "leila.b@example.com", "0612345690", 33.5860, -7.6070, "66 Hearing Center, Casablanca");
            createDoctor("Dr. Mustapha Benali", "Neurology", "mustapha.b@example.com", "0612345691", 33.5870, -7.6080, "88 Brain Clinic, Casablanca");
            createDoctor("Dr. Sarah El Yacoubi", "Dermatology", "sarah.e@example.com", "0612345692", 33.5880, -7.6090, "11 Skin Center, Casablanca");
            createDoctor("Dr. Hicham El Khatib", "Pediatrics", "hicham.k@example.com", "0612345693", 33.5890, -7.6100, "33 Children Hospital, Casablanca");
            createDoctor("Dr. Amira El Fadil", "Ophthalmology", "amira.f@example.com", "0612345694", 33.5900, -7.6110, "55 Eye Clinic, Casablanca");
            createDoctor("Dr. Mehdi El Aaraj", "Psychiatry", "mehdi.a@example.com", "0612345695", 33.5910, -7.6120, "77 Mind Care, Casablanca");
        }
    }

    private void createDoctor(String name, String specialization, String email, String phone, double lat, double lng, String address) {
        Doctor d = new Doctor();
        d.setName(name);
        d.setSpecialization(specialization);
        d.setEmail(email);
        d.setPassword(encoder.encode("password123"));
        d.setPhoneNumber(phone);

        Location loc = new Location();
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        loc.setAddress(address);
        d.setLocation(loc);

        d.setRating(Math.round((3 + Math.random() * 2) * 10.0) / 10.0); // Random rating 3.0–5.0
        d.setReviewsCount((int)(Math.random() * 300)); // Random review count 0–300

        repo.save(d);
    }
}
