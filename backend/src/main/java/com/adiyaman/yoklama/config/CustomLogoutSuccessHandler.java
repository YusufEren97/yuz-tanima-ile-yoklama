package com.adiyaman.yoklama.config;

import com.adiyaman.yoklama.entity.Rol;
import com.adiyaman.yoklama.service.KullaniciService;
import com.adiyaman.yoklama.service.YoklamaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final KullaniciService kullaniciService;
    private final YoklamaService yoklamaService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (authentication != null && authentication.getName() != null) {
            kullaniciService.emailIleBul(authentication.getName())
                    .ifPresent(kullanici -> {
                        // Öğretmen çıkış yapıyorsa aktif yoklamalarını kapat
                        if (kullanici.getRol() == Rol.OGRETMEN) {
                            yoklamaService.ogretmeninAktifOturumlariniKapat(kullanici.getId());
                        }
                    });
        }

        response.sendRedirect("/giris?cikis=true");
    }
}
