package com.adiyaman.yoklama.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "yoklama_oturumlari")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YoklamaOturumu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ders_id", nullable = false)
    private Ders ders;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime baslangicZamani = LocalDateTime.now();

    private LocalDateTime bitisZamani;

    @Column(nullable = false)
    @Builder.Default
    private Boolean aktif = true;

    @OneToMany(mappedBy = "oturum", cascade = CascadeType.ALL)
    @Builder.Default
    private List<YoklamaKayit> kayitlar = new ArrayList<>();
}
