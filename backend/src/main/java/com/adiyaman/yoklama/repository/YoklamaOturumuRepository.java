package com.adiyaman.yoklama.repository;

import com.adiyaman.yoklama.entity.YoklamaOturumu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface YoklamaOturumuRepository extends JpaRepository<YoklamaOturumu, Long> {

    List<YoklamaOturumu> findByDersId(Long dersId);

    List<YoklamaOturumu> findByDersIdAndAktif(Long dersId, Boolean aktif);

    Optional<YoklamaOturumu> findByDersIdAndAktifTrue(Long dersId);

    List<YoklamaOturumu> findByAktifTrue();

    List<YoklamaOturumu> findByDersOgretmenIdOrderByBaslangicZamaniDesc(Long ogretmenId);
}
