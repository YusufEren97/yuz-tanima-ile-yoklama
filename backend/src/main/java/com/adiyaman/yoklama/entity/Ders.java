package com.adiyaman.yoklama.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "dersler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ad;

    @Column(nullable = false, unique = true)
    private String kod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ogretmen_id")
    private Kullanici ogretmen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinif_id")
    private Sinif sinif;

    @ManyToMany
    @JoinTable(name = "ders_ogrenci", joinColumns = @JoinColumn(name = "ders_id"), inverseJoinColumns = @JoinColumn(name = "ogrenci_id"))
    @Builder.Default
    private Set<Kullanici> ogrenciler = new HashSet<>();
}
