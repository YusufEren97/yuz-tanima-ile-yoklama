package com.adiyaman.yoklama.service;

import com.adiyaman.yoklama.entity.Bolum;
import com.adiyaman.yoklama.entity.Sinif;
import com.adiyaman.yoklama.repository.BolumRepository;
import com.adiyaman.yoklama.repository.SinifRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BolumService {

    private final BolumRepository bolumRepository;
    private final SinifRepository sinifRepository;

    public Bolum kaydet(Bolum bolum) {
        return bolumRepository.save(bolum);
    }

    public Optional<Bolum> idIleBul(Long id) {
        return bolumRepository.findById(id);
    }

    public List<Bolum> tumBolumleriGetir() {
        return bolumRepository.findAll();
    }

    public void sil(Long id) {
        bolumRepository.deleteById(id);
    }

    public boolean kodMevcutMu(String kod) {
        return bolumRepository.existsByKod(kod);
    }

    // Sınıf İşlemleri
    public Sinif sinifKaydet(Sinif sinif) {
        return sinifRepository.save(sinif);
    }

    public Optional<Sinif> sinifIdIleBul(Long id) {
        return sinifRepository.findById(id);
    }

    public List<Sinif> bolumunSiniflariniGetir(Long bolumId) {
        return sinifRepository.findByBolumId(bolumId);
    }

    public List<Sinif> tumSiniflariGetir() {
        return sinifRepository.findAll();
    }

    public void sinifSil(Long id) {
        sinifRepository.deleteById(id);
    }
}
