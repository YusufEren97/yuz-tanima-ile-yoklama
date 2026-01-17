package com.adiyaman.yoklama.config;

import com.adiyaman.yoklama.entity.Bolum;
import com.adiyaman.yoklama.entity.Kullanici;
import com.adiyaman.yoklama.entity.Rol;
import com.adiyaman.yoklama.entity.Sinif;
import com.adiyaman.yoklama.repository.BolumRepository;
import com.adiyaman.yoklama.repository.KullaniciRepository;
import com.adiyaman.yoklama.repository.SinifRepository;
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
    private final BolumRepository bolumRepository;
    private final SinifRepository sinifRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Admin kullanıcısı oluştur
        if (!kullaniciRepository.existsByEmail("admin@adiyaman.edu.tr")) {
            Kullanici admin = Kullanici.builder()
                    .email("admin@adiyaman.edu.tr")
                    .sifre(passwordEncoder.encode("admin123"))
                    .ad("Admin")
                    .soyad("User")
                    .rol(Rol.ADMIN)
                    .aktif(true)
                    .build();
            kullaniciRepository.save(admin);
            log.info("Admin kullanıcısı oluşturuldu: admin@adiyaman.edu.tr / admin123");
        }

        // Örnek bölüm oluştur
        if (!bolumRepository.existsByKod("BILMUH")) {
            Bolum bolum = Bolum.builder()
                    .ad("Bilgisayar Mühendisliği")
                    .kod("BILMUH")
                    .build();
            bolum = bolumRepository.save(bolum);

            // Örnek sınıflar
            for (int yil = 1; yil <= 4; yil++) {
                Sinif sinif = Sinif.builder()
                        .ad(yil + ". Sınıf")
                        .bolum(bolum)
                        .build();
                sinifRepository.save(sinif);
            }
            log.info("Örnek bölüm ve sınıflar oluşturuldu: Bilgisayar Mühendisliği");
        }
    }
}
