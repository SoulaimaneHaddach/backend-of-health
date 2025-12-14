package com.rickbot.healing_backend.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rickbot.healing_backend.dto.DoctorRequestDto;
import com.rickbot.healing_backend.dto.DoctorResponseDto;
import com.rickbot.healing_backend.entity.Doctor;
import com.rickbot.healing_backend.mapper.DoctorMapper;
import com.rickbot.healing_backend.repository.DoctorRepo;
import com.rickbot.healing_backend.security.JwtUtil;

import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@Data
@RequiredArgsConstructor
public class DoctorService {

    private final PasswordEncoder passwordEncoder;
        private static final Logger log = LoggerFactory.getLogger(DoctorService.class);
        private final DoctorRepo doctorRepo;
        private final DoctorMapper doctorMapper;
        private final JwtUtil jwtUtil;

        @Transactional
        public List<DoctorResponseDto> findDoctors(String specialization, Double latitude, Double longitude, Double radiusKm) {
        final double effectiveRadiusMeters = (radiusKm != null ? radiusKm : 50d) * 1000d; // default 50 km
        return doctorRepo.findAll().stream()
                // CRITICAL: Filter out doctors with null location FIRST
                .filter(d -> d.getLocation() != null)
                // Filter by specialization if provided
                .filter(d -> specialization == null
                        || specialization.isEmpty()
                        || specialization.equalsIgnoreCase("All")
                        || (d.getSpecialization() != null
                                && d.getSpecialization().equalsIgnoreCase(specialization)))
                // Only include doctors with active status
                .filter(d -> d.getStatus() == null || "active".equalsIgnoreCase(d.getStatus()))
                // Keep only doctors near the provided latitude/longitude when coordinates are provided
                .filter(d -> {
                        if (latitude == null || longitude == null) {
                                return true; // no geo filter if coords not provided
                        }
                        return isWithinRadius(
                                        d.getLocation().getLatitude(),
                                        d.getLocation().getLongitude(),
                                        latitude,
                                        longitude,
                                        effectiveRadiusMeters
                                );
                })
                // Map to DTO
                .map(doctorMapper::toDto)
                .collect(Collectors.toList());
        }

            public List<com.rickbot.healing_backend.dto.DoctorMapDto> findDoctorsForMap(
                            String specialization,
                            Double latitude,
                            Double longitude,
                            Double radiusKm) {
                    if (latitude == null || longitude == null) {
                            throw new IllegalArgumentException("latitude and longitude required");
                    }
                    // default 20 km, clamp to [1,50]
                    double requested = (radiusKm == null ? 20d : radiusKm);
                    double clampedKm = Math.min(50d, Math.max(1d, requested));
                    final double effectiveRadiusMeters = clampedKm * 1000d;

                    return doctorRepo.findAll().stream()
                                    .filter(d -> specialization == null
                                                    || specialization.isEmpty()
                                                    || specialization.equalsIgnoreCase("All")
                                                    || (d.getSpecialization() != null
                                                                    && d.getSpecialization().equalsIgnoreCase(specialization)))
                                    .filter(d -> d.getLocation() != null
                                                    && isWithinRadius(
                                                                    d.getLocation().getLatitude(),
                                                                    d.getLocation().getLongitude(),
                                                                    latitude,
                                                                    longitude,
                                                                    effectiveRadiusMeters))
                                    // Only include doctors with active status on the map
                                    .filter(d -> d.getStatus() == null || "active".equalsIgnoreCase(d.getStatus()))
                                    // sort by distance ascending
                                    .sorted(Comparator.comparingDouble(d -> distanceMeters(
                                                    d.getLocation().getLatitude(),
                                                    d.getLocation().getLongitude(),
                                                    latitude,
                                                    longitude)))
                                    .map(doctorMapper::toMapDto)
                                    .collect(Collectors.toList());
            }
        





        private boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radiusMeters) {
                // Haversine formula with meters for higher precision
                final double EARTH_RADIUS_METERS = 6_371_000.0;
                double dLat = Math.toRadians(lat2 - lat1);
                double dLon = Math.toRadians(lon2 - lon1);

                double sinDLat = Math.sin(dLat / 2);
                double sinDLon = Math.sin(dLon / 2);

                double a = sinDLat * sinDLat
                                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                                * sinDLon * sinDLon;

                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                double distanceMeters = EARTH_RADIUS_METERS * c;

                return distanceMeters <= radiusMeters;
        }

                private double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
                                final double EARTH_RADIUS_METERS = 6_371_000.0;
                                double dLat = Math.toRadians(lat2 - lat1);
                                double dLon = Math.toRadians(lon2 - lon1);

                                double sinDLat = Math.sin(dLat / 2);
                                double sinDLon = Math.sin(dLon / 2);

                                double a = sinDLat * sinDLat
                                                                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                                                                                                * sinDLon * sinDLon;

                                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                                return EARTH_RADIUS_METERS * c;
                }





        //update doctor details
        @Transactional
        public DoctorResponseDto updateDoctorDetails(UUID id, DoctorRequestDto doctorDto) {
            Doctor doctor = doctorRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
                doctorMapper.updateEntityFromDto(doctorDto, doctor);
                if (doctorDto.getPassword() != null && !doctorDto.getPassword().isEmpty()) {
                        doctor.setPassword(passwordEncoder.encode(doctorDto.getPassword()));
                }
                Doctor updatedDoctor = doctorRepo.save(doctor);
                return doctorMapper.toDto(updatedDoctor);
        }

        public boolean verifyJwtToken(String token) {
                try {
                        return jwtUtil.validateToken(token);
                } catch (Exception e) {
                        return false;
                }
        }
        
        public DoctorResponseDto updateDoctor(UUID id, DoctorRequestDto dto) {
        Doctor doctor = doctorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctorMapper.updateDoctorFromDto(dto, doctor);  // mapper handles all fields
                // If password was provided in the DTO, encode it before saving
                if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                        doctor.setPassword(passwordEncoder.encode(dto.getPassword()));
                }
                doctor = doctorRepo.save(doctor);

        return doctorMapper.toDto(doctor);
    }

        // Admin: create new doctor (used by admin UI)
        @Transactional
        public com.rickbot.healing_backend.dto.AdminDoctorResponse createAdminDoctor(com.rickbot.healing_backend.dto.AdminDoctorRequest req) {
                // check unique email
                if (doctorRepo.findByEmail(req.getEmail()).isPresent()) {
                        throw new IllegalArgumentException("EMAIL_EXISTS");
                }

                Doctor d = new Doctor();
                d.setName(req.getName());
                d.setSpecialization(req.getSpecialty());
                d.setEmail(req.getEmail());
                d.setPhoneNumber(req.getPhone());
                d.setAbout(req.getBio());
                        // Debug: log whether password was provided
                        boolean provided = req.getPassword() != null && !req.getPassword().isEmpty();
                        log.debug("Admin create doctor request for email='{}' passwordProvided={}", req.getEmail(), provided);

                        // If admin provided a password use it (encode), otherwise set a random password
                        if (provided) {
                                d.setPassword(passwordEncoder.encode(req.getPassword()));
                                // when admin explicitly sets a password, create account as active
                                d.setStatus("active");
                        } else {
                                d.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                                d.setStatus("pending");
                        }

                // Save
                Doctor saved = doctorRepo.save(d);
                // Log saved info for debugging (do NOT log full password hash)
                String pw = saved.getPassword() == null ? "" : saved.getPassword();
                String pwPrefix = pw.length() > 10 ? pw.substring(0, 10) : pw;
                log.debug("Saved doctor id={} email={} status={} passwordPrefix={} pwLen={}", saved.getId(), saved.getEmail(), saved.getStatus(), pwPrefix, pw.length());

                com.rickbot.healing_backend.dto.AdminDoctorResponse resp = new com.rickbot.healing_backend.dto.AdminDoctorResponse();
                resp.setId(saved.getId() != null ? saved.getId().toString() : null);
                resp.setName(saved.getName());
                resp.setEmail(saved.getEmail());
                resp.setSpecialty(saved.getSpecialization());
                resp.setStatus(saved.getStatus() != null ? saved.getStatus() : "pending");
                resp.setBookings(saved.getBookings() != null ? saved.getBookings() : 0);
                resp.setCreatedAt(saved.getCreatedAt());
                return resp;
        }

        public java.util.List<com.rickbot.healing_backend.dto.AdminDoctorResponse> listAllForAdmin() {
                return doctorRepo.findAll().stream().map(d -> {
                        com.rickbot.healing_backend.dto.AdminDoctorResponse r = new com.rickbot.healing_backend.dto.AdminDoctorResponse();
                        r.setId(d.getId() != null ? d.getId().toString() : null);
                        r.setName(d.getName());
                        r.setEmail(d.getEmail());
                        r.setSpecialty(d.getSpecialization());
                        r.setStatus(d.getStatus() != null ? d.getStatus() : "pending");
                        r.setBookings(d.getBookings() != null ? d.getBookings() : 0);
                        r.setCreatedAt(d.getCreatedAt());
                        return r;
                }).collect(java.util.stream.Collectors.toList());
        }

        @Transactional
        public com.rickbot.healing_backend.dto.AdminDoctorResponse approveDoctor(java.util.UUID id) {
                Doctor d = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
                d.setStatus("active");
                Doctor saved = doctorRepo.save(d);

                com.rickbot.healing_backend.dto.AdminDoctorResponse r = new com.rickbot.healing_backend.dto.AdminDoctorResponse();
                r.setId(saved.getId() != null ? saved.getId().toString() : null);
                r.setName(saved.getName());
                r.setEmail(saved.getEmail());
                r.setSpecialty(saved.getSpecialization());
                r.setStatus(saved.getStatus() != null ? saved.getStatus() : "pending");
                r.setBookings(saved.getBookings() != null ? saved.getBookings() : 0);
                r.setCreatedAt(saved.getCreatedAt());
                return r;
        }

        @Transactional
        public com.rickbot.healing_backend.dto.AdminDoctorResponse rejectDoctor(java.util.UUID id) {
                Doctor d = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
                d.setStatus("rejected");
                Doctor saved = doctorRepo.save(d);

                com.rickbot.healing_backend.dto.AdminDoctorResponse r = new com.rickbot.healing_backend.dto.AdminDoctorResponse();
                r.setId(saved.getId() != null ? saved.getId().toString() : null);
                r.setName(saved.getName());
                r.setEmail(saved.getEmail());
                r.setSpecialty(saved.getSpecialization());
                r.setStatus(saved.getStatus() != null ? saved.getStatus() : "pending");
                r.setBookings(saved.getBookings() != null ? saved.getBookings() : 0);
                r.setCreatedAt(saved.getCreatedAt());
                return r;
        }

        public com.rickbot.healing_backend.dto.AdminDoctorResponse getAdminDoctorById(java.util.UUID id) {
                Doctor d = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
                com.rickbot.healing_backend.dto.AdminDoctorResponse r = new com.rickbot.healing_backend.dto.AdminDoctorResponse();
                r.setId(d.getId() != null ? d.getId().toString() : null);
                r.setName(d.getName());
                r.setEmail(d.getEmail());
                r.setSpecialty(d.getSpecialization());
                r.setStatus(d.getStatus() != null ? d.getStatus() : "pending");
                r.setBookings(d.getBookings() != null ? d.getBookings() : 0);
                r.setCreatedAt(d.getCreatedAt());
                return r;
        }

        @Transactional
        public void deleteDoctor(UUID id) {
                if (!doctorRepo.existsById(id)) {
                        throw new RuntimeException("Doctor not found");
                }
                doctorRepo.deleteById(id);
        }

    public DoctorResponseDto getDoctorProfile(UUID id) {
        Doctor doctor = doctorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return doctorMapper.toDto(doctor);
    }





    public Doctor findByEmail(String email) {
        Doctor doctor = doctorRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found with email: " + email));
        return doctor;
    }

        // Admin-only helper: verify a raw password against stored hash for a doctor id
        public boolean verifyDoctorPassword(java.util.UUID id, String rawPassword) {
                Doctor doctor = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
                String stored = doctor.getPassword();
                if (stored == null || stored.isEmpty()) return false;
                return passwordEncoder.matches(rawPassword, stored);
        }

                // Bulk create/import doctors. If transactional=true (default) the whole operation will
                // run in a single transaction and will be rolled back on any error. If transactional=false
                // each row is processed independently and per-row status is returned.
                @Transactional
                public java.util.List<java.util.Map<String,Object>> bulkCreate(com.rickbot.healing_backend.dto.BulkImportRequest req) {
                        java.util.List<java.util.Map<String,Object>> results = new java.util.ArrayList<>();
                        boolean transactional = req.getTransactional() == null ? true : req.getTransactional();
                        java.util.List<com.rickbot.healing_backend.dto.AdminDoctorRequest> docs = req.getDoctors() == null ? java.util.Collections.emptyList() : req.getDoctors();

                        if (transactional) {
                                // perform in single transaction; any failure -> throw and rollback
                                for (com.rickbot.healing_backend.dto.AdminDoctorRequest r : docs) {
                                        if (doctorRepo.findByEmail(r.getEmail()).isPresent()) {
                                                throw new IllegalArgumentException("Email already exists: " + r.getEmail());
                                        }
                                }

                                for (com.rickbot.healing_backend.dto.AdminDoctorRequest r : docs) {
                                        com.rickbot.healing_backend.dto.AdminDoctorRequest local = r;
                                        Doctor d = new Doctor();
                                        d.setName(local.getName());
                                        d.setSpecialization(local.getSpecialty());
                                        d.setEmail(local.getEmail());
                                        d.setPhoneNumber(local.getPhone());
                                        d.setAbout(local.getBio());
                                        String pw = (req.getPasswordForAll() != null && !req.getPasswordForAll().isEmpty()) ? req.getPasswordForAll() : local.getPassword();
                                        boolean provided = pw != null && !pw.isEmpty();
                                        if (provided) {
                                                d.setPassword(passwordEncoder.encode(pw));
                                                d.setStatus("active");
                                        } else {
                                                d.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                                                d.setStatus("pending");
                                        }
                                        Doctor saved = doctorRepo.save(d);
                                        java.util.Map<String,Object> map = new java.util.HashMap<>();
                                        map.put("email", saved.getEmail());
                                        map.put("id", saved.getId());
                                        map.put("updated", true);
                                        results.add(map);
                                }
                                return results;
                        } else {
                                // non-transactional: attempt each row and record per-row success/failure
                                for (com.rickbot.healing_backend.dto.AdminDoctorRequest r : docs) {
                                        java.util.Map<String,Object> map = new java.util.HashMap<>();
                                        map.put("email", r.getEmail());
                                        try {
                                                if (doctorRepo.findByEmail(r.getEmail()).isPresent()) {
                                                        map.put("updated", false);
                                                        map.put("message", "Email exists");
                                                } else {
                                                        Doctor d = new Doctor();
                                                        d.setName(r.getName());
                                                        d.setSpecialization(r.getSpecialty());
                                                        d.setEmail(r.getEmail());
                                                        d.setPhoneNumber(r.getPhone());
                                                        d.setAbout(r.getBio());
                                                        String pw = (req.getPasswordForAll() != null && !req.getPasswordForAll().isEmpty()) ? req.getPasswordForAll() : r.getPassword();
                                                        boolean provided = pw != null && !pw.isEmpty();
                                                        if (provided) {
                                                                d.setPassword(passwordEncoder.encode(pw));
                                                                d.setStatus("active");
                                                        } else {
                                                                d.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                                                                d.setStatus("pending");
                                                        }
                                                        Doctor saved = doctorRepo.save(d);
                                                        map.put("id", saved.getId());
                                                        map.put("updated", true);
                                                }
                                        } catch (Exception ex) {
                                                map.put("updated", false);
                                                map.put("message", ex.getMessage());
                                        }
                                        results.add(map);
                                }
                                return results;
                        }
                }

        @Transactional
        public java.util.List<java.util.Map<String,Object>> migratePlaintextPasswords() {
                java.util.List<java.util.Map<String,Object>> results = new java.util.ArrayList<>();
                for (Doctor d : doctorRepo.findAll()) {
                        String pw = d.getPassword();
                        if (pw == null || pw.isEmpty()) continue;
                        boolean isHashed = pw.startsWith("$2");
                        java.util.Map<String,Object> r = new java.util.HashMap<>();
                        r.put("id", d.getId());
                        r.put("email", d.getEmail());
                        r.put("wasHashed", isHashed);
                        if (!isHashed) {
                                String old = pw;
                                d.setPassword(passwordEncoder.encode(old));
                                doctorRepo.save(d);
                                r.put("updated", true);
                                String newpw = d.getPassword() == null ? "" : d.getPassword();
                                String newPrefix = newpw.length() > 10 ? newpw.substring(0,10) : newpw;
                                r.put("newPrefix", newPrefix);
                                r.put("newLen", newpw.length());
                        } else {
                                r.put("updated", false);
                        }
                        results.add(r);
                }
                return results;
        }

}
