package com.adiyaman.yoklama.repository;

import com.adiyaman.yoklama.entity.Sinif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SinifRepository extends JpaRepository<Sinif, Long> {

    List<Sinif> findByBolumId(Long bolumId);

    boolean existsByAdAndBolumId(String ad, Long bolumId);
}
