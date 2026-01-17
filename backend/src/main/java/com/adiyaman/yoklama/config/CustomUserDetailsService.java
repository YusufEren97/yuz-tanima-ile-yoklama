package com.adiyaman.yoklama.config;

import com.adiyaman.yoklama.entity.Kullanici;
import com.adiyaman.yoklama.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final KullaniciRepository kullaniciRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Kullanici kullanici = kullaniciRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + email));

        if (!kullanici.getAktif()) {
            throw new UsernameNotFoundException("Hesap aktif değil: " + email);
        }

        return new User(
                kullanici.getEmail(),
                kullanici.getSifre(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + kullanici.getRol().name())));
    }
}
