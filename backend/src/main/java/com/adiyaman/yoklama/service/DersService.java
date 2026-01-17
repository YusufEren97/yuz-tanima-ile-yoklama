package com.adiyaman.yoklama.service;

import com.adiyaman.yoklama.entity.Ders;
import com.adiyaman.yoklama.entity.Kullanici;
import com.adiyaman.yoklama.repository.DersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DersService {

    private final DersRepository dersRepository;

    public Ders kaydet(Ders ders) {
        return dersRepository.save(ders);
    }

    public Optional<Ders> idIleBul(Long id) {
        return dersRepository.findById(id);
    }

    public Optional<Ders> kodIleBul(String kod) {
        return dersRepository.findByKod(kod);
    }

    public List<Ders> tumDersleriGetir() {
        return dersRepository.findAll();
    }

    public List<Ders> ogretmeninDersleriniGetir(Long ogretmenId) {
        return dersRepository.findByOgretmenId(ogretmenId);
    }

    public List<Ders> ogrencininDersleriniGetir(Long ogrenciId) {
        return dersRepository.findByOgrenciId(ogrenciId);
    }

    public List<Ders> sinifaDersleriniGetir(Long sinifId) {
        return dersRepository.findBySinifId(sinifId);
    }

    public void sil(Long id) {
        dersRepository.deleteById(id);
    }

    public boolean kodMevcutMu(String kod) {
        return dersRepository.existsByKod(kod);
    }

    public void ogrenciEkle(Long dersId, Kullanici ogrenci) {
        dersRepository.findById(dersId).ifPresent(ders -> {
            ders.getOgrenciler().add(ogrenci);
            dersRepository.save(ders);
        });
    }

    public void ogrenciCikar(Long dersId, Long ogrenciId) {
        dersRepository.findById(dersId).ifPresent(ders -> {
            ders.getOgrenciler().removeIf(o -> o.getId().equals(ogrenciId));
            dersRepository.save(ders);
        });
    }
}
