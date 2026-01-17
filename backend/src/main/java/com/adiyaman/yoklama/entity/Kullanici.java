package com.adiyaman.yoklama.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kullanicilar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String sifre;

    @Column(nullable = false)
    private String ad;

    @Column(nullable = false)
    private String soyad;

    @Column(unique = true)
    private String ogrenciNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bolum_id")
    private Bolum bolum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinif_id")
    private Sinif sinif;

    @Column(nullable = false)
    @Builder.Default
    private Boolean aktif = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean yuzKayitli = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime olusturmaTarihi = LocalDateTime.now();

    public String getTamAd() {
        return ad + " " + soyad;
    }
}
