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
    public String yuzKaydet(@RequestParam("fotograflar") List<MultipartFile> fotograflar,
            Authentication auth,
            RedirectAttributes redirect) {
        Kullanici ogrenci = getKullanici(auth);

        if (fotograflar.size() < 5) {
            redirect.addFlashAttribute("hata", "En az 5 fotoğraf gerekli!");
            return "redirect:/ogrenci/yuz-kayit";
        }

        Map<String, Object> sonuc = yuzTanimaService.yuzKaydet(
                ogrenci.getId(),
                ogrenci.getTamAd(),
                fotograflar);

        if (Boolean.TRUE.equals(sonuc.get("basarili"))) {
            kullaniciService.yuzKayitliOlarakIsaretle(ogrenci.getId());
            redirect.addFlashAttribute("mesaj", "Yüz kaydı başarılı!");
        } else {
            redirect.addFlashAttribute("hata", sonuc.get("mesaj"));
        }

        return "redirect:/ogrenci/yuz-kayit";
    }

    @PostMapping("/yuz-guncelle")
    public String yuzGuncelle(@RequestParam("fotograflar") List<MultipartFile> fotograflar,
            Authentication auth,
            RedirectAttributes redirect) {
        Kullanici ogrenci = getKullanici(auth);

        if (fotograflar.size() < 5) {
            redirect.addFlashAttribute("hata", "En az 5 fotoğraf gerekli!");
            return "redirect:/ogrenci/yuz-kayit";
        }

        Map<String, Object> sonuc = yuzTanimaService.yuzGuncelle(
                ogrenci.getId(),
                ogrenci.getTamAd(),
                fotograflar);

        if (Boolean.TRUE.equals(sonuc.get("basarili"))) {
            redirect.addFlashAttribute("mesaj", "Yüz güncellendi!");
        } else {
            redirect.addFlashAttribute("hata", sonuc.get("mesaj"));
        }

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
    public String yoklamaKatil(@PathVariable Long oturumId,
            @RequestParam("fotograf") MultipartFile fotograf,
            Authentication auth,
            RedirectAttributes redirect) {
        Kullanici ogrenci = getKullanici(auth);
        YoklamaOturumu oturum = yoklamaService.oturumIdIleBul(oturumId).orElse(null);

        if (oturum == null || !oturum.getAktif()) {
            redirect.addFlashAttribute("hata", "Yoklama oturumu aktif değil!");
            return "redirect:/ogrenci";
        }

        if (!ogrenci.getYuzKayitli()) {
            redirect.addFlashAttribute("hata", "Önce yüz kaydı yapmalısınız!");
            return "redirect:/ogrenci/yuz-kayit";
        }

        // Yüz doğrulama
        Map<String, Object> sonuc = yuzTanimaService.yuzDogrula(ogrenci.getId(), fotograf);

        if (Boolean.TRUE.equals(sonuc.get("basarili"))) {
            Double guvenSkor = sonuc.get("guven") != null ? ((Number) sonuc.get("guven")).doubleValue() : null;
            yoklamaService.katilimKaydet(oturum, ogrenci, guvenSkor);
            redirect.addFlashAttribute("mesaj", "Yoklamaya katılım kaydedildi!");
        } else {
            redirect.addFlashAttribute("hata", "Yüz doğrulanamadı: " + sonuc.get("mesaj"));
        }

        return "redirect:/ogrenci";
    }

    @GetMapping("/gecmis")
    public String yoklamaGecmisi(Authentication auth, Model model) {
        Kullanici ogrenci = getKullanici(auth);
        List<YoklamaKayit> kayitlar = yoklamaService.ogrencininKayitlariniGetir(ogrenci.getId());
        model.addAttribute("kayitlar", kayitlar);
        return "ogrenci/gecmis";
    }
}
