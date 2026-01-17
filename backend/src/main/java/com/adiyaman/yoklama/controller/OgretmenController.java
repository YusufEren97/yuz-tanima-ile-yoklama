package com.adiyaman.yoklama.controller;

import com.adiyaman.yoklama.entity.*;
import com.adiyaman.yoklama.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ogretmen")
@RequiredArgsConstructor
public class OgretmenController {

    private final KullaniciService kullaniciService;
    private final DersService dersService;
    private final YoklamaService yoklamaService;

    private Kullanici getKullanici(Authentication auth) {
        return kullaniciService.emailIleBul(auth.getName()).orElseThrow();
    }

    @GetMapping
    public String dashboard(Authentication auth, Model model) {
        Kullanici ogretmen = getKullanici(auth);
        List<Ders> dersler = dersService.ogretmeninDersleriniGetir(ogretmen.getId());

        model.addAttribute("ogretmen", ogretmen);
        model.addAttribute("dersler", dersler);
        model.addAttribute("aktifOturumlar", yoklamaService.aktifOturumlariGetir()
                .stream()
                .filter(o -> dersler.stream().anyMatch(d -> d.getId().equals(o.getDers().getId())))
                .collect(Collectors.toList()));

        return "ogretmen/dashboard";
    }

    @PostMapping("/yoklama/baslat/{dersId}")
    public String yoklamaBaslat(@PathVariable Long dersId,
            Authentication auth,
            RedirectAttributes redirect) {
        Kullanici ogretmen = getKullanici(auth);
        Ders ders = dersService.idIleBul(dersId).orElse(null);

        if (ders == null || !ders.getOgretmen().getId().equals(ogretmen.getId())) {
            redirect.addFlashAttribute("hata", "Bu dersi yönetme yetkiniz yok!");
            return "redirect:/ogretmen";
        }

        YoklamaOturumu oturum = yoklamaService.oturumBaslat(ders);
        redirect.addFlashAttribute("mesaj", "Yoklama başlatıldı: " + ders.getAd());
        return "redirect:/ogretmen/yoklama/" + oturum.getId();
    }

    @GetMapping("/yoklama/{oturumId}")
    public String yoklamaDetay(@PathVariable Long oturumId,
            Authentication auth,
            Model model) {
        YoklamaOturumu oturum = yoklamaService.oturumIdIleBul(oturumId).orElse(null);
        if (oturum == null) {
            return "redirect:/ogretmen";
        }

        Ders ders = oturum.getDers();
        List<YoklamaKayit> kayitlar = yoklamaService.oturumunKayitlariniGetir(oturumId);
        Set<Long> katilanlariIdleri = kayitlar.stream()
                .map(k -> k.getOgrenci().getId())
                .collect(Collectors.toSet());

        List<Kullanici> katilmayanlar = ders.getOgrenciler().stream()
                .filter(o -> !katilanlariIdleri.contains(o.getId()))
                .collect(Collectors.toList());

        model.addAttribute("oturum", oturum);
        model.addAttribute("ders", ders);
        model.addAttribute("kayitlar", kayitlar);
        model.addAttribute("katilmayanlar", katilmayanlar);
        model.addAttribute("toplamOgrenci", ders.getOgrenciler().size());

        return "ogretmen/yoklama";
    }

    @PostMapping("/yoklama/bitir/{oturumId}")
    public String yoklamaBitir(@PathVariable Long oturumId, RedirectAttributes redirect) {
        yoklamaService.oturumBitir(oturumId);
        redirect.addFlashAttribute("mesaj", "Yoklama bitirildi.");
        return "redirect:/ogretmen";
    }

    @GetMapping("/gecmis")
    public String yoklamaGecmisi(Authentication auth, Model model) {
        Kullanici ogretmen = getKullanici(auth);
        List<YoklamaOturumu> oturumlar = yoklamaService.ogretmeninOturumlariniGetir(ogretmen.getId());
        model.addAttribute("oturumlar", oturumlar);
        return "ogretmen/gecmis";
    }
}
