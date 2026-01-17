package com.adiyaman.yoklama.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "yoklama_kayitlari")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YoklamaKayit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oturum_id", nullable = false)
    private YoklamaOturumu oturum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ogrenci_id", nullable = false)
    private Kullanici ogrenci;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime katilimZamani = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean katildi = true;

    private Double guvenSkor; // Yüz doğrulama güven skoru
}
