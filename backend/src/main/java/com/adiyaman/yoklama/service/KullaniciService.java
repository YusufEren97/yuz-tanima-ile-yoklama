package com.adiyaman.yoklama.service;

import com.adiyaman.yoklama.entity.Kullanici;
import com.adiyaman.yoklama.entity.Rol;
import com.adiyaman.yoklama.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class KullaniciService {

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;

    public Kullanici kaydet(Kullanici kullanici) {
        if (kullanici.getId() == null) {
            kullanici.setSifre(passwordEncoder.encode(kullanici.getSifre()));
        }
        return kullaniciRepository.save(kullanici);
    }

    public Optional<Kullanici> emailIleBul(String email) {
        return kullaniciRepository.findByEmail(email);
    }

    public Optional<Kullanici> ogrenciNoIleBul(String ogrenciNo) {
        return kullaniciRepository.findByOgrenciNo(ogrenciNo);
    }

    public Optional<Kullanici> idIleBul(Long id) {
        return kullaniciRepository.findById(id);
    }

    public List<Kullanici> tumKullanicilariGetir() {
        return kullaniciRepository.findAll();
    }

    public List<Kullanici> rolIleGetir(Rol rol) {
        return kullaniciRepository.findByRol(rol);
    }

    public List<Kullanici> ogretmenleriGetir() {
        return kullaniciRepository.findByRolAndAktif(Rol.OGRETMEN, true);
    }

    public List<Kullanici> ogrencileriGetir() {
        return kullaniciRepository.findByRolAndAktif(Rol.OGRENCI, true);
    }

    public boolean emailMevcutMu(String email) {
        return kullaniciRepository.existsByEmail(email);
    }

    public boolean ogrenciNoMevcutMu(String ogrenciNo) {
        return kullaniciRepository.existsByOgrenciNo(ogrenciNo);
    }

    public void sil(Long id) {
        kullaniciRepository.deleteById(id);
    }

    public void yuzKayitliOlarakIsaretle(Long id) {
        kullaniciRepository.findById(id).ifPresent(kullanici -> {
            kullanici.setYuzKayitli(true);
            kullaniciRepository.save(kullanici);
        });
    }

    public boolean sifreDogrula(String email, String sifre) {
        return kullaniciRepository.findByEmail(email)
                .map(k -> passwordEncoder.matches(sifre, k.getSifre()))
                .orElse(false);
    }
}
