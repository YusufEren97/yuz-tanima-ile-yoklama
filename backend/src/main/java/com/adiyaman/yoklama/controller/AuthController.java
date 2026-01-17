package com.adiyaman.yoklama.controller;

import com.adiyaman.yoklama.entity.*;
import com.adiyaman.yoklama.service.BolumService;
import com.adiyaman.yoklama.service.DersService;
import com.adiyaman.yoklama.service.KullaniciService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final KullaniciService kullaniciService;
    private final BolumService bolumService;
    private final DersService dersService;

    @GetMapping("/")
    public String anasayfaYonlendir() {
        return "redirect:/giris";
    }

    @GetMapping("/giris")
    public String girisFormu(@RequestParam(required = false) String hata,
            @RequestParam(required = false) String cikis,
            Model model) {
        if (hata != null) {
            model.addAttribute("hata", "Email veya sifre hatali!");
        }
        if (cikis != null) {
            model.addAttribute("mesaj", "Basariyla cikis yaptiniz.");
        }
        return "giris";
    }

    @GetMapping("/kayit")
    public String kayitFormu(Model model) {
        model.addAttribute("bolumler", bolumService.tumBolumleriGetir());
        model.addAttribute("siniflar", bolumService.tumSiniflariGetir());
        return "kayit";
    }

    @PostMapping("/kayit")
    public String kayitOl(@RequestParam String email,
            @RequestParam String sifre,
            @RequestParam String ad,
            @RequestParam String soyad,
            @RequestParam String ogrenciNo,
            @RequestParam Long bolumId,
            @RequestParam Long sinifId,
            RedirectAttributes redirect) {

        if (kullaniciService.emailMevcutMu(email)) {
            redirect.addFlashAttribute("hata", "Bu email zaten kayitli!");
            return "redirect:/kayit";
        }

        if (kullaniciService.ogrenciNoMevcutMu(ogrenciNo)) {
            redirect.addFlashAttribute("hata", "Bu ogrenci numarasi zaten kayitli!");
            return "redirect:/kayit";
        }

        Bolum bolum = bolumService.idIleBul(bolumId).orElse(null);
        Sinif sinif = bolumService.sinifIdIleBul(sinifId).orElse(null);

        Kullanici yeniKullanici = Kullanici.builder()
                .email(email)
                .sifre(sifre)
                .ad(ad)
                .soyad(soyad)
                .ogrenciNo(ogrenciNo)
                .rol(Rol.OGRENCI)
                .bolum(bolum)
                .sinif(sinif)
                .aktif(true)
                .build();

        Kullanici kaydedilen = kullaniciService.kaydet(yeniKullanici);

        // Ogrenciyi sinifin derslerine otomatik ekle
        if (sinif != null) {
            List<Ders> sinifDersleri = dersService.sinifaDersleriniGetir(sinif.getId());
            for (Ders ders : sinifDersleri) {
                dersService.ogrenciEkle(ders.getId(), kaydedilen);
            }
        }

        redirect.addFlashAttribute("mesaj", "Kayit basarili! Simdi giris yapabilirsiniz.");
        return "redirect:/giris";
    }

    @GetMapping("/anasayfa")
    public String anasayfa(Authentication auth) {
        if (auth != null) {
            String rol = auth.getAuthorities().iterator().next().getAuthority();
            if (rol.equals("ROLE_ADMIN")) {
                return "redirect:/admin";
            } else if (rol.equals("ROLE_OGRETMEN")) {
                return "redirect:/ogretmen";
            } else if (rol.equals("ROLE_OGRENCI")) {
                return "redirect:/ogrenci";
            }
        }
        return "redirect:/giris";
    }
}
