package com.adiyaman.yoklama.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bolumler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bolum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ad;

    @Column(nullable = false, unique = true)
    private String kod;

    @OneToMany(mappedBy = "bolum", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Sinif> siniflar = new ArrayList<>();

    @OneToMany(mappedBy = "bolum")
    @Builder.Default
    private List<Kullanici> kullanicilar = new ArrayList<>();
}
