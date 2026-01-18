package com.adiyaman.yoklama.controller;

import com.adiyaman.yoklama.entity.*;
import com.adiyaman.yoklama.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/ogrenci")
@RequiredArgsConstructor
public class OgrenciController {

    private final KullaniciService kullaniciService;
    private final DersService dersService;
    private final YoklamaService yoklamaService;
    private final YuzTanimaService yuzTanimaService;

    private Kullanici getKullanici(Authentication auth) {
        return kullaniciService.emailIleBul(auth.getName()).orElseThrow();
    }

    @GetMapping
    public String dashboard(Authentication auth, Model model) {
        Kullanici ogrenci = getKullanici(auth);
        List<Ders> dersler = dersService.ogrencininDersleriniGetir(ogrenci.getId());

        // Aktif yoklamalar (öğrencinin derslerine ait)
        List<YoklamaOturumu> aktifYoklamalar = yoklamaService.aktifOturumlariGetir()
                .stream()
                .filter(o -> dersler.stream().anyMatch(d -> d.getId().equals(o.getDers().getId())))
                .toList();

        model.addAttribute("ogrenci", ogrenci);
        model.addAttribute("dersler", dersler);
        model.addAttribute("aktifYoklamalar", aktifYoklamalar);
        model.addAttribute("yuzKayitli", ogrenci.getYuzKayitli());

        return "ogrenci/dashboard";
    }

    // === YÜZ KAYIT ===
    @GetMapping("/yuz-kayit")
    public String yuzKayitFormu(Authentication auth, Model model) {
        Kullanici ogrenci = getKullanici(auth);
        model.addAttribute("ogrenci", ogrenci);
        model.addAttribute("yuzKayitli", ogrenci.getYuzKayitli());
        return "ogrenci/yuz-kayit";
    }

    @PostMapping("/yuz-kayit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> yuzKaydet(@RequestParam("fotograflar") List<MultipartFile> fotograflar,
            Authentication auth) {
        Kullanici ogrenci = getKullanici(auth);

        if (fotograflar.size() < 5) {
            return ResponseEntity.badRequest().body(Map.of("basarili", false, "mesaj", "En az 5 fotoğraf gerekli!"));
        }

        Map<String, Object> sonuc = yuzTanimaService.yuzKaydet(
                ogrenci.getId(),
                ogrenci.getOgrenciNo(),
                fotograflar);

        if (Boolean.TRUE.equals(sonuc.get("basarili"))) {
            kullaniciService.yuzKayitliOlarakIsaretle(ogrenci.getId());
            return ResponseEntity.ok(Map.of("basarili", true, "mesaj", "Yüz kaydı başarılı!"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("basarili", false, "mesaj", sonuc.get("mesaj")));
        }
    }

    @PostMapping("/yuz-guncelle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> yuzGuncelle(@RequestParam("fotograflar") List<MultipartFile> fotograflar,
            Authentication auth) {
        Kullanici ogrenci = getKullanici(auth);

        if (fotograflar.size() < 5) {
            return ResponseEntity.badRequest().body(Map.of("basarili", false, "mesaj", "En az 5 fotoğraf gerekli!"));
        }

        Map<String, Object> sonuc = yuzTanimaService.yuzGuncelle(
                ogrenci.getId(),
                ogrenci.getOgrenciNo(),
                fotograflar);

        if (Boolean.TRUE.equals(sonuc.get("basarili"))) {
            return ResponseEntity.ok(Map.of("basarili", true, "mesaj", "Yüz güncellendi!"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("basarili", false, "mesaj", sonuc.get("mesaj")));
        }
    }

    @PostMapping("/yuz-sil")
    public String yuzSil(Authentication auth, RedirectAttributes redirect) {
        Kullanici ogrenci = getKullanici(auth);

        // 1. Python servisinden sil
        yuzTanimaService.yuzSil(ogrenci.getId());

        // 2. Veritabanından flag'i kaldır
        kullaniciService.yuzKaydiniSil(ogrenci.getId());

        redirect.addFlashAttribute("mesaj", "Yüz kaydınız başarıyla silindi ve sıfırlandı.");
        return "redirect:/ogrenci/yuz-kayit";
    }

    // === YOKLAMA ===
    @GetMapping("/yoklama/{oturumId}")
    public String yoklamaKatilFormu(@PathVariable Long oturumId,
            Authentication auth,
            Model model) {
        Kullanici ogrenci = getKullanici(auth);
        YoklamaOturumu oturum = yoklamaService.oturumIdIleBul(oturumId).orElse(null);

        if (oturum == null || !oturum.getAktif()) {
            model.addAttribute("hata", "Yoklama oturumu aktif değil!");
            return "ogrenci/yoklama";
        }

        boolean zadenKatildi = yoklamaService.ogrenciKatildiMi(oturumId, ogrenci.getId());

        model.addAttribute("oturum", oturum);
        model.addAttribute("ogrenci", ogrenci);
        model.addAttribute("zadenKatildi", zadenKatildi);

        return "ogrenci/yoklama";
    }

    @PostMapping("/yoklama/{oturumId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> yoklamaKatil(@PathVariable Long oturumId,
            @RequestParam("fotograf") MultipartFile fotograf,
            Authentication auth) {
        Kullanici ogrenci = getKullanici(auth);
        YoklamaOturumu oturum = yoklamaService.oturumIdIleBul(oturumId).orElse(null);

        if (oturum == null || !oturum.getAktif()) {
            return ResponseEntity.badRequest().body(Map.of("basarili", false, "mesaj", "Yoklama oturumu aktif değil!"));
        }

        if (!ogrenci.getYuzKayitli()) {
            return ResponseEntity.badRequest().body(Map.of("basarili", false, "mesaj", "Önce yüz kaydı yapmalısınız!"));
        }

        // Yüz doğrulama
        Map<String, Object> sonuc = yuzTanimaService.yuzDogrula(ogrenci.getId(), fotograf);

        if (Boolean.TRUE.equals(sonuc.get("basarili"))) {
            Double guvenSkor = sonuc.get("guven") != null ? ((Number) sonuc.get("guven")).doubleValue() : null;
            yoklamaService.katilimKaydet(oturum, ogrenci, guvenSkor);
            return ResponseEntity.ok(Map.of("basarili", true, "mesaj", "Yoklamaya katılım kaydedildi!"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("basarili", false, "mesaj", sonuc.get("mesaj")));
        }
    }

    @GetMapping("/gecmis")
    public String yoklamaGecmisi(Authentication auth, Model model) {
        Kullanici ogrenci = getKullanici(auth);
        List<YoklamaKayit> kayitlar = yoklamaService.ogrencininKayitlariniGetir(ogrenci.getId());
        model.addAttribute("kayitlar", kayitlar);
        return "ogrenci/gecmis";
    }
}
