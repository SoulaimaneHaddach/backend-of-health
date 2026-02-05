package com.rickbot.healing_backend.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rickbot.healing_backend.dto.*;
import com.rickbot.healing_backend.entity.Doctor;
import com.rickbot.healing_backend.mapper.DoctorMapper;
import com.rickbot.healing_backend.repository.DoctorRepo;
import com.rickbot.healing_backend.security.JwtUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);
    private final PasswordEncoder passwordEncoder;
    private final DoctorRepo doctorRepo;
    private final DoctorMapper doctorMapper;
    private final JwtUtil jwtUtil;

    @Value("${app.doctors.max-results:1000}")
    private int maxDoctorResults;

    @Cacheable(value = "doctorsCache", key = "#specialization + '-' + #latitude + '-' + #longitude + '-' + #radiusKm", unless = "#result == null || #result.isEmpty()")
    public List<DoctorResponseDto> findDoctors(String specialization, Double latitude, Double longitude, Double radiusKm) {
        double radiusMeters = (radiusKm != null ? radiusKm : 50d) * 1000d;
        List<Doctor> doctors;
        if (latitude != null && longitude != null) {
            try {
                doctors = doctorRepo.findDoctorsWithinRadius(latitude, longitude, radiusMeters, PageRequest.of(0, Math.max(1, maxDoctorResults)));
            } catch (Exception ex) {
                log.warn("Radius query failed, falling back to in-memory filter: {}", ex.getMessage());
                doctors = doctorRepo.findAll(PageRequest.of(0, Math.max(1, maxDoctorResults)))
                        .stream()
                        .filter(d -> d.getLocation() != null)
                        .filter(d -> distanceMeters(d.getLocation().getLatitude(), d.getLocation().getLongitude(), latitude, longitude) <= radiusMeters)
                        .collect(Collectors.toList());
            }
        } else {
            doctors = doctorRepo.findAll(PageRequest.of(0, Math.max(1, maxDoctorResults)))
                    .stream().filter(d -> d.getLocation() != null).collect(Collectors.toList());
        }

        // If specialization filter provided, apply case-insensitive match
        if (specialization != null && !specialization.isBlank()) {
            String specLower = specialization.trim().toLowerCase();
            doctors = doctors.stream()
                    .filter(d -> d.getSpecialization() != null && d.getSpecialization().toLowerCase().contains(specLower))
                    .collect(Collectors.toList());
        }

        return doctors.stream().map(doctorMapper::toDto).collect(Collectors.toList());
    }

    public List<DoctorMapDto> findDoctorsForMap(Double latitude, Double longitude, Double radiusKm) {
        if (latitude == null || longitude == null) throw new IllegalArgumentException("latitude and longitude required");

        double radiusMeters = Math.min(50d, Math.max(1d, radiusKm != null ? radiusKm : 20d)) * 1000d;
        List<Doctor> doctors;
        try {
            doctors = doctorRepo.findDoctorsWithinRadius(latitude, longitude, radiusMeters, PageRequest.of(0, Math.max(1, maxDoctorResults)));
        } catch (Exception ex) {
            log.warn("Radius native query failed, falling back to in-memory filter for map: {}", ex.getMessage());
            doctors = doctorRepo.findAll(PageRequest.of(0, Math.max(1, maxDoctorResults)))
                    .stream()
                    .filter(d -> d.getLocation() != null)
                    .filter(d -> distanceMeters(d.getLocation().getLatitude(), d.getLocation().getLongitude(), latitude, longitude) <= radiusMeters)
                    .collect(Collectors.toList());
        }

        return doctors.stream()
                .map(doctorMapper::toMapDto)
                .sorted(Comparator.comparingDouble(d -> distanceMeters(d.getLocation().getLatitude(), d.getLocation().getLongitude(), latitude, longitude)))
                .collect(Collectors.toList());
    }

    private double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6_371_000d;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    @Transactional
    public DoctorResponseDto updateDoctorDetails(UUID id, DoctorRequestDto dto) {
        Doctor doctor = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctorMapper.updateEntityFromDto(dto, doctor);
        if (dto.getPassword() != null && !dto.getPassword().isEmpty())
            doctor.setPassword(passwordEncoder.encode(dto.getPassword()));
        return doctorMapper.toDto(doctorRepo.save(doctor));
    }

    public boolean verifyJwtToken(String token) {
        try { return jwtUtil.validateToken(token); }
        catch (Exception e) { return false; }
    }

    public DoctorResponseDto updateDoctor(UUID id, DoctorRequestDto dto) {
        Doctor doctor = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctorMapper.updateDoctorFromDto(dto, doctor);
        if (dto.getPassword() != null && !dto.getPassword().isEmpty())
            doctor.setPassword(passwordEncoder.encode(dto.getPassword()));
        return doctorMapper.toDto(doctorRepo.save(doctor));
    }

    @Transactional
    public AdminDoctorResponse createAdminDoctor(AdminDoctorRequest req) {
        if (doctorRepo.findByEmail(req.getEmail()).isPresent()) throw new IllegalArgumentException("EMAIL_EXISTS");

        Doctor d = new Doctor();
        d.setName(req.getName());
        d.setSpecialization(req.getSpecialty());
        d.setEmail(req.getEmail());
        d.setPhoneNumber(req.getPhone());
        d.setAbout(req.getBio());
        boolean provided = req.getPassword() != null && !req.getPassword().isEmpty();
        d.setPassword(passwordEncoder.encode(provided ? req.getPassword() : UUID.randomUUID().toString()));
        d.setStatus(provided ? "active" : "pending");

        Doctor saved = doctorRepo.save(d);
        AdminDoctorResponse r = new AdminDoctorResponse();
        r.setId(saved.getId().toString());
        r.setName(saved.getName());
        r.setEmail(saved.getEmail());
        r.setSpecialty(saved.getSpecialization());
        r.setStatus(saved.getStatus());
        r.setBookings(saved.getBookings() != null ? saved.getBookings() : 0);
        r.setCreatedAt(saved.getCreatedAt());
        return r;
    }

    public List<AdminDoctorResponse> listAllForAdmin() {
        return doctorRepo.findAll().stream().map(d -> {
            AdminDoctorResponse r = new AdminDoctorResponse();
            r.setId(d.getId().toString());
            r.setName(d.getName());
            r.setEmail(d.getEmail());
            r.setSpecialty(d.getSpecialization());
            r.setStatus(d.getStatus() != null ? d.getStatus() : "pending");
            r.setBookings(d.getBookings() != null ? d.getBookings() : 0);
            r.setCreatedAt(d.getCreatedAt());
            return r;
        }).collect(Collectors.toList());
    }

    public AdminDoctorResponse approveDoctor(UUID id) {
        Doctor d = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
        d.setStatus("active");
        Doctor saved = doctorRepo.save(d);
        AdminDoctorResponse r = new AdminDoctorResponse();
        r.setId(saved.getId().toString());
        r.setName(saved.getName());
        r.setEmail(saved.getEmail());
        r.setSpecialty(saved.getSpecialization());
        r.setStatus(saved.getStatus());
        r.setBookings(saved.getBookings() != null ? saved.getBookings() : 0);
        r.setCreatedAt(saved.getCreatedAt());
        return r;
    }

    public AdminDoctorResponse disableDoctor(UUID id) {
        Doctor d = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
        d.setStatus("inactive");
        Doctor saved = doctorRepo.save(d);
        AdminDoctorResponse r = new AdminDoctorResponse();
        r.setId(saved.getId().toString());
        r.setName(saved.getName());
        r.setEmail(saved.getEmail());
        r.setSpecialty(saved.getSpecialization());
        r.setStatus(saved.getStatus());
        r.setBookings(saved.getBookings() != null ? saved.getBookings() : 0);
        r.setCreatedAt(saved.getCreatedAt());
        return r;
    }

    public AdminDoctorResponse enableDoctor(UUID id) {
        Doctor d = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
        d.setStatus("active");
        Doctor saved = doctorRepo.save(d);
        AdminDoctorResponse r = new AdminDoctorResponse();
        r.setId(saved.getId().toString());
        r.setName(saved.getName());
        r.setEmail(saved.getEmail());
        r.setSpecialty(saved.getSpecialization());
        r.setStatus(saved.getStatus());
        r.setBookings(saved.getBookings() != null ? saved.getBookings() : 0);
        r.setCreatedAt(saved.getCreatedAt());
        return r;
    }

    public AdminDoctorResponse rejectDoctor(UUID id) {
        Doctor d = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
        d.setStatus("rejected");
        Doctor saved = doctorRepo.save(d);
        AdminDoctorResponse r = new AdminDoctorResponse();
        r.setId(saved.getId().toString());
        r.setName(saved.getName());
        r.setEmail(saved.getEmail());
        r.setSpecialty(saved.getSpecialization());
        r.setStatus(saved.getStatus());
        r.setBookings(saved.getBookings() != null ? saved.getBookings() : 0);
        r.setCreatedAt(saved.getCreatedAt());
        return r;
    }

    public AdminDoctorResponse getAdminDoctorById(UUID id) {
        Doctor d = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
        AdminDoctorResponse r = new AdminDoctorResponse();
        r.setId(d.getId().toString());
        r.setName(d.getName());
        r.setEmail(d.getEmail());
        r.setSpecialty(d.getSpecialization());
        r.setStatus(d.getStatus() != null ? d.getStatus() : "pending");
        r.setBookings(d.getBookings() != null ? d.getBookings() : 0);
        r.setCreatedAt(d.getCreatedAt());
        return r;
    }

    public List<Map<String,Object>> migratePlaintextPasswords() {
        // Basic migration: if a doctor's password does not appear to be bcrypt (starts with $2), encode it.
        List<Map<String,Object>> results = new ArrayList<>();
        for (Doctor d : doctorRepo.findAll()) {
            Map<String,Object> item = new HashMap<>();
            item.put("id", d.getId() != null ? d.getId().toString() : null);
            String pw = d.getPassword();
            if (pw != null && !pw.startsWith("$2")) {
                String encoded = passwordEncoder.encode(pw);
                d.setPassword(encoded);
                doctorRepo.save(d);
                item.put("updated", true);
            } else {
                item.put("updated", false);
            }
            results.add(item);
        }
        return results;
    }

    public List<Map<String,Object>> bulkCreate(BulkImportRequest req) {
        List<Map<String,Object>> res = new ArrayList<>();
        if (req == null || req.getDoctors() == null) return res;
        for (AdminDoctorRequest ad : req.getDoctors()) {
            Map<String,Object> entry = new HashMap<>();
            try {
                if (req.getPasswordForAll() != null) ad.setPassword(req.getPasswordForAll());
                AdminDoctorResponse created = createAdminDoctor(ad);
                entry.put("id", created.getId());
                entry.put("email", created.getEmail());
                entry.put("updated", true);
            } catch (Exception ex) {
                entry.put("email", ad.getEmail());
                entry.put("updated", false);
                entry.put("error", ex.getMessage());
            }
            res.add(entry);
        }
        return res;
    }

    @Transactional
    public void deleteDoctor(UUID id) {
        if (!doctorRepo.existsById(id)) throw new RuntimeException("Doctor not found");
        doctorRepo.deleteById(id);
    }

    public DoctorResponseDto getDoctorProfile(UUID id) {
        Doctor doctor = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
        return doctorMapper.toDto(doctor);
    }

    public Doctor findByEmail(String email) {
        return doctorRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    public boolean verifyDoctorPassword(UUID id, String rawPassword) {
        Doctor doctor = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
        return doctor.getPassword() != null && passwordEncoder.matches(rawPassword, doctor.getPassword());
    }
}
