package com.adiyaman.yoklama.repository;

import com.adiyaman.yoklama.entity.Kullanici;
import com.adiyaman.yoklama.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {

    Optional<Kullanici> findByEmail(String email);

    Optional<Kullanici> findByOgrenciNo(String ogrenciNo);

    List<Kullanici> findByRol(Rol rol);

    List<Kullanici> findByBolumId(Long bolumId);

    List<Kullanici> findBySinifId(Long sinifId);

    List<Kullanici> findByRolAndAktif(Rol rol, Boolean aktif);

    boolean existsByEmail(String email);

    boolean existsByOgrenciNo(String ogrenciNo);
}
