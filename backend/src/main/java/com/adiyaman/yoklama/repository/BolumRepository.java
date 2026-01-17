package com.adiyaman.yoklama.repository;

import com.adiyaman.yoklama.entity.Bolum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BolumRepository extends JpaRepository<Bolum, Long> {

    Optional<Bolum> findByKod(String kod);

    Optional<Bolum> findByAd(String ad);

    boolean existsByKod(String kod);

    boolean existsByAd(String ad);
}
