package com.adiyaman.yoklama.config;

import com.adiyaman.yoklama.entity.Kullanici;
import com.adiyaman.yoklama.entity.Rol;
import com.adiyaman.yoklama.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Admin kullanicisini olustur
        Kullanici admin = null;
        if (!kullaniciRepository.existsByEmail("admin@adiyaman.edu.tr")) {
            admin = Kullanici.builder()
                    .email("admin@adiyaman.edu.tr")
                    .sifre(passwordEncoder.encode("admin123"))
                    .ad("Admin")
                    .soyad("User")
                    .rol(Rol.ADMIN)
                    .aktif(true)
                    .build();
            kullaniciRepository.save(admin);
            log.info("Admin kullanicisi olusturuldu: admin@adiyaman.edu.tr / admin123");
        } else {
            admin = kullaniciRepository.findByEmail("admin@adiyaman.edu.tr").orElse(null);
        }
    }
}
