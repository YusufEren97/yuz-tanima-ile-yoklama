package com.adiyaman.yoklama.controller;

import com.adiyaman.yoklama.entity.Kullanici;
import com.adiyaman.yoklama.service.KullaniciService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profil")
@RequiredArgsConstructor
public class ProfilController {

    private final KullaniciService kullaniciService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String profilSayfasi(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/giris";
        }

        String email = principal.getName();
        Kullanici kullanici = kullaniciService.emailIleBul(email).orElseThrow();

        model.addAttribute("kullanici", kullanici);
        return "profil";
    }

    @PostMapping("/sifre-degistir")
    public String sifreDegistir(@RequestParam String mevcutSifre,
            @RequestParam String yeniSifre,
            @RequestParam String yeniSifreTekrar,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/giris";
        }

        if (!yeniSifre.equals(yeniSifreTekrar)) {
            redirectAttributes.addFlashAttribute("hata", "Yeni şifreler eşleşmiyor!");
            return "redirect:/profil";
        }

        String email = principal.getName();
        Kullanici kullanici = kullaniciService.emailIleBul(email).orElseThrow();

        if (!passwordEncoder.matches(mevcutSifre, kullanici.getSifre())) {
            redirectAttributes.addFlashAttribute("hata", "Mevcut şifreniz yanlış!");
            return "redirect:/profil";
        }

        // Yeni sifreyi encode et ve kaydet
        kullanici.setSifre(passwordEncoder.encode(yeniSifre));
        kullaniciService.kaydet(kullanici);

        redirectAttributes.addFlashAttribute("mesaj", "Şifreniz başarıyla güncellendi.");
        return "redirect:/profil";
    }
}
