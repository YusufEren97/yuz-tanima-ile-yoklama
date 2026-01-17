package com.adiyaman.yoklama.service;

import com.adiyaman.yoklama.entity.*;
import com.adiyaman.yoklama.repository.YoklamaKayitRepository;
import com.adiyaman.yoklama.repository.YoklamaOturumuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class YoklamaService {

    private final YoklamaOturumuRepository oturumRepository;
    private final YoklamaKayitRepository kayitRepository;

    // Oturum İşlemleri
    public YoklamaOturumu oturumBaslat(Ders ders) {
        // Aynı derse ait aktif oturum varsa önce kapat
        oturumRepository.findByDersIdAndAktifTrue(ders.getId())
                .ifPresent(oturum -> {
                    oturum.setAktif(false);
                    oturum.setBitisZamani(LocalDateTime.now());
                    oturumRepository.save(oturum);
                });

        YoklamaOturumu yeniOturum = YoklamaOturumu.builder()
                .ders(ders)
                .baslangicZamani(LocalDateTime.now())
                .aktif(true)
                .build();

        return oturumRepository.save(yeniOturum);
    }

    public YoklamaOturumu oturumBitir(Long oturumId) {
        YoklamaOturumu oturum = oturumRepository.findById(oturumId)
                .orElseThrow(() -> new RuntimeException("Oturum bulunamadı: " + oturumId));

        oturum.setAktif(false);
        oturum.setBitisZamani(LocalDateTime.now());

        return oturumRepository.save(oturum);
    }

    public Optional<YoklamaOturumu> oturumIdIleBul(Long id) {
        return oturumRepository.findById(id);
    }

    public Optional<YoklamaOturumu> dersAktifOturumunuBul(Long dersId) {
        return oturumRepository.findByDersIdAndAktifTrue(dersId);
    }

    public List<YoklamaOturumu> aktifOturumlariGetir() {
        return oturumRepository.findByAktifTrue();
    }

    public List<YoklamaOturumu> dersinOturumlariniGetir(Long dersId) {
        return oturumRepository.findByDersId(dersId);
    }

    public List<YoklamaOturumu> ogretmeninOturumlariniGetir(Long ogretmenId) {
        return oturumRepository.findByDersOgretmenId(ogretmenId);
    }

    // Kayıt İşlemleri
    public YoklamaKayit katilimKaydet(YoklamaOturumu oturum, Kullanici ogrenci, Double guvenSkor) {
        // Öğrenci zaten katılmış mı kontrol et
        Optional<YoklamaKayit> mevcutKayit = kayitRepository
                .findByOturumIdAndOgrenciId(oturum.getId(), ogrenci.getId());

        if (mevcutKayit.isPresent()) {
            return mevcutKayit.get(); // Zaten katılmış
        }

        YoklamaKayit kayit = YoklamaKayit.builder()
                .oturum(oturum)
                .ogrenci(ogrenci)
                .katilimZamani(LocalDateTime.now())
                .katildi(true)
                .guvenSkor(guvenSkor)
                .build();

        return kayitRepository.save(kayit);
    }

    public List<YoklamaKayit> oturumunKayitlariniGetir(Long oturumId) {
        return kayitRepository.findByOturumId(oturumId);
    }

    public List<YoklamaKayit> ogrencininKayitlariniGetir(Long ogrenciId) {
        return kayitRepository.findByOgrenciId(ogrenciId);
    }

    public boolean ogrenciKatildiMi(Long oturumId, Long ogrenciId) {
        return kayitRepository.existsByOturumIdAndOgrenciId(oturumId, ogrenciId);
    }

    public long katilimSayisi(Long oturumId) {
        return kayitRepository.countByOturumIdAndKatildiTrue(oturumId);
    }
}
