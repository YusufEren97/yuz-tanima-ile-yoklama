package com.adiyaman.yoklama.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "siniflar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sinif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ad; // Örn: "1-A", "2-B"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bolum_id", nullable = false)
    private Bolum bolum;

    @OneToMany(mappedBy = "sinif")
    @Builder.Default
    private List<Kullanici> ogrenciler = new ArrayList<>();

    @OneToMany(mappedBy = "sinif")
    @Builder.Default
    private List<Ders> dersler = new ArrayList<>();
}
