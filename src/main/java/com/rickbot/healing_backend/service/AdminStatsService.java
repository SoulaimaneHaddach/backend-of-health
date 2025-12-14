package com.rickbot.healing_backend.service;

import com.rickbot.healing_backend.repository.AdminRepository;
import com.rickbot.healing_backend.repository.BookingRepo;
import com.rickbot.healing_backend.repository.DoctorRepo;
import com.rickbot.healing_backend.repository.SpecialtyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class AdminStatsService {

    private final DoctorRepo doctorRepo;
    private final BookingRepo bookingRepo;
    private final SpecialtyRepository specialtyRepository;
    private final AdminRepository adminRepository;

    public AdminStatsService(DoctorRepo doctorRepo, BookingRepo bookingRepo, SpecialtyRepository specialtyRepository, AdminRepository adminRepository) {
        this.doctorRepo = doctorRepo;
        this.bookingRepo = bookingRepo;
        this.specialtyRepository = specialtyRepository;
        this.adminRepository = adminRepository;
    }

    public Map<String, Object> getStats() {
        Map<String, Object> m = new HashMap<>();

        // indicators — use specialty count as a proxy
        long indicators = specialtyRepository.count();
        m.put("indicators", indicators);

        // doctors count
        long doctors = doctorRepo.count();
        m.put("doctors", doctors);

        // admins count
        long admins = adminRepository.count();
        m.put("admins", admins);

        // patients — distinct emailUser in bookings
        List<com.rickbot.healing_backend.entity.Book> all = bookingRepo.findAll();
        Set<String> users = new HashSet<>();
        for (com.rickbot.healing_backend.entity.Book b : all) {
            if (b.getEmailUser() != null && !b.getEmailUser().isBlank()) users.add(b.getEmailUser().toLowerCase());
            else if (b.getPhoneNumberUser() != null && !b.getPhoneNumberUser().isBlank()) users.add(b.getPhoneNumberUser());
        }
        m.put("patients", users.size());

        // dailyAppointments — bookings for today
        LocalDate today = LocalDate.now();
        LocalDateTime start = LocalDateTime.of(today, LocalTime.MIDNIGHT);
        LocalDateTime end = LocalDateTime.of(today, LocalTime.of(23, 59, 59));
        long daily = bookingRepo.countByAppointmentTimeBetween(start, end);
        m.put("dailyAppointments", daily);

        // favoriteUsers — top N frequent users (count of users with >1 bookings)
        Map<String, Integer> freq = new HashMap<>();
        for (com.rickbot.healing_backend.entity.Book b : all) {
            String key = (b.getEmailUser() != null && !b.getEmailUser().isBlank()) ? b.getEmailUser().toLowerCase() : b.getPhoneNumberUser();
            if (key == null) continue;
            freq.put(key, freq.getOrDefault(key, 0) + 1);
        }
        long fav = freq.values().stream().filter(c -> c > 1).count();
        m.put("favoriteUsers", fav);

        return m;
    }
}
