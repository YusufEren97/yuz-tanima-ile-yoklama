package com.adiyaman.yoklama.repository;

import com.adiyaman.yoklama.entity.YoklamaKayit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface YoklamaKayitRepository extends JpaRepository<YoklamaKayit, Long> {

    List<YoklamaKayit> findByOturumId(Long oturumId);

    List<YoklamaKayit> findByOgrenciId(Long ogrenciId);

    Optional<YoklamaKayit> findByOturumIdAndOgrenciId(Long oturumId, Long ogrenciId);

    boolean existsByOturumIdAndOgrenciId(Long oturumId, Long ogrenciId);

    long countByOturumIdAndKatildiTrue(Long oturumId);
}
