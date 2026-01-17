package com.adiyaman.yoklama.controller;

import com.adiyaman.yoklama.entity.*;
import com.adiyaman.yoklama.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final KullaniciService kullaniciService;
    private final BolumService bolumService;
    private final DersService dersService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("ogretmenSayisi", kullaniciService.rolIleGetir(Rol.OGRETMEN).size());
        model.addAttribute("ogrenciSayisi", kullaniciService.rolIleGetir(Rol.OGRENCI).size());
        model.addAttribute("bolumSayisi", bolumService.tumBolumleriGetir().size());
        model.addAttribute("dersSayisi", dersService.tumDersleriGetir().size());
        return "admin/dashboard";
    }

    // === BÖLÜM YÖNETİMİ ===
    @GetMapping("/bolumler")
    public String bolumler(Model model) {
        model.addAttribute("bolumler", bolumService.tumBolumleriGetir());
        return "admin/bolumler";
    }

    @PostMapping("/bolumler/ekle")
    public String bolumEkle(@RequestParam String ad, @RequestParam String kod,
            @RequestParam(required = false, defaultValue = "0") int sinifSayisi,
            RedirectAttributes redirect) {
        if (bolumService.kodMevcutMu(kod)) {
            redirect.addFlashAttribute("hata", "Bu bölüm kodu zaten mevcut!");
            return "redirect:/admin/bolumler";
        }
        Bolum bolum = Bolum.builder().ad(ad).kod(kod).build();
        bolumService.kaydet(bolum);

        // Otomatik sinif olusturma
        if (sinifSayisi > 0) {
            for (int i = 1; i <= sinifSayisi; i++) {
                Sinif sinif = Sinif.builder()
                        .ad(i + ". Sınıf")
                        .bolum(bolum)
                        .build();
                bolumService.sinifKaydet(sinif);
            }
        }

        redirect.addFlashAttribute("mesaj",
                "Bölüm eklendi: " + ad + (sinifSayisi > 0 ? " (" + sinifSayisi + " sınıf oluşturuldu)" : ""));
        return "redirect:/admin/bolumler";
    }

    @PostMapping("/bolumler/sil/{id}")
    public String bolumSil(@PathVariable Long id, RedirectAttributes redirect) {
        bolumService.sil(id);
        redirect.addFlashAttribute("mesaj", "Bölüm silindi.");
        return "redirect:/admin/bolumler";
    }

    // === SINIF YÖNETİMİ ===
    @PostMapping("/siniflar/ekle")
    public String sinifEkle(@RequestParam String ad, @RequestParam Long bolumId,
            RedirectAttributes redirect) {
        Bolum bolum = bolumService.idIleBul(bolumId).orElse(null);
        if (bolum == null) {
            redirect.addFlashAttribute("hata", "Bölüm bulunamadı!");
            return "redirect:/admin/bolumler";
        }
        Sinif sinif = Sinif.builder().ad(ad).bolum(bolum).build();
        bolumService.sinifKaydet(sinif);
        redirect.addFlashAttribute("mesaj", "Sınıf eklendi: " + ad);
        return "redirect:/admin/bolumler";
    }

    // === DERS YÖNETİMİ ===
    @GetMapping("/dersler")
    public String dersler(Model model) {
        model.addAttribute("dersler", dersService.tumDersleriGetir());
        model.addAttribute("ogretmenler", kullaniciService.ogretmenleriGetir());
        model.addAttribute("siniflar", bolumService.tumSiniflariGetir());
        return "admin/dersler";
    }

    @PostMapping("/dersler/ekle")
    public String dersEkle(@RequestParam String ad, @RequestParam String kod,
            @RequestParam(required = false) Long ogretmenId,
            @RequestParam(required = false) Long sinifId,
            RedirectAttributes redirect) {
        if (dersService.kodMevcutMu(kod)) {
            redirect.addFlashAttribute("hata", "Bu ders kodu zaten mevcut!");
            return "redirect:/admin/dersler";
        }

        Kullanici ogretmen = ogretmenId != null ? kullaniciService.idIleBul(ogretmenId).orElse(null) : null;
        Sinif sinif = sinifId != null ? bolumService.sinifIdIleBul(sinifId).orElse(null) : null;

        Ders ders = Ders.builder()
                .ad(ad)
                .kod(kod)
                .ogretmen(ogretmen)
                .sinif(sinif)
                .build();
        dersService.kaydet(ders);
        redirect.addFlashAttribute("mesaj", "Ders eklendi: " + ad);
        return "redirect:/admin/dersler";
    }

    @PostMapping("/dersler/sil/{id}")
    public String dersSil(@PathVariable Long id, RedirectAttributes redirect) {
        dersService.sil(id);
        redirect.addFlashAttribute("mesaj", "Ders silindi.");
        return "redirect:/admin/dersler";
    }

    // === ÖĞRETMEN YÖNETİMİ ===
    @GetMapping("/ogretmenler")
    public String ogretmenler(Model model) {
        model.addAttribute("ogretmenler", kullaniciService.rolIleGetir(Rol.OGRETMEN));
        return "admin/ogretmenler";
    }

    @PostMapping("/ogretmenler/ekle")
    public String ogretmenEkle(@RequestParam String email, @RequestParam String sifre,
            @RequestParam String ad, @RequestParam String soyad,
            RedirectAttributes redirect) {
        if (kullaniciService.emailMevcutMu(email)) {
            redirect.addFlashAttribute("hata", "Bu email zaten kayıtlı!");
            return "redirect:/admin/ogretmenler";
        }

        Kullanici ogretmen = Kullanici.builder()
                .email(email)
                .sifre(sifre)
                .ad(ad)
                .soyad(soyad)
                .rol(Rol.OGRETMEN)
                .aktif(true)
                .build();
        kullaniciService.kaydet(ogretmen);
        redirect.addFlashAttribute("mesaj", "Öğretmen eklendi: " + ad + " " + soyad);
        return "redirect:/admin/ogretmenler";
    }

    @PostMapping("/ogretmenler/sil/{id}")
    public String ogretmenSil(@PathVariable Long id, RedirectAttributes redirect) {
        kullaniciService.sil(id);
        redirect.addFlashAttribute("mesaj", "Öğretmen silindi.");
        return "redirect:/admin/ogretmenler";
    }

    // === ÖĞRENCİ YÖNETİMİ ===
    @GetMapping("/ogrenciler")
    public String ogrenciler(Model model) {
        model.addAttribute("ogrenciler", kullaniciService.rolIleGetir(Rol.OGRENCI));
        return "admin/ogrenciler";
    }

    // Mevcut ogrencileri siniflarinin derslerine ekle
    @PostMapping("/ogrenciler/senkronize")
    public String ogrencileriSenkronize(RedirectAttributes redirect) {
        var ogrenciler = kullaniciService.rolIleGetir(Rol.OGRENCI);
        int eklenen = 0;

        for (Kullanici ogrenci : ogrenciler) {
            if (ogrenci.getSinif() != null) {
                var dersler = dersService.sinifaDersleriniGetir(ogrenci.getSinif().getId());
                for (Ders ders : dersler) {
                    if (!ders.getOgrenciler().contains(ogrenci)) {
                        dersService.ogrenciEkle(ders.getId(), ogrenci);
                        eklenen++;
                    }
                }
            }
        }

        redirect.addFlashAttribute("mesaj", "Senkronizasyon tamamlandi. " + eklenen + " kayit eklendi.");
        return "redirect:/admin/ogrenciler";
    }
}
