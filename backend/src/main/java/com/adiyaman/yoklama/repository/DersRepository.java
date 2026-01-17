package com.adiyaman.yoklama.repository;

import com.adiyaman.yoklama.entity.Ders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DersRepository extends JpaRepository<Ders, Long> {

    Optional<Ders> findByKod(String kod);

    List<Ders> findByOgretmenId(Long ogretmenId);

    List<Ders> findBySinifId(Long sinifId);

    @Query("SELECT d FROM Ders d JOIN d.ogrenciler o WHERE o.id = :ogrenciId")
    List<Ders> findByOgrenciId(@Param("ogrenciId") Long ogrenciId);

    boolean existsByKod(String kod);
}
