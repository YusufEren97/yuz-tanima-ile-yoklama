package com.adiyaman.yoklama.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class YuzTanimaService {

    @Value("${face.service.url}")
    private String faceServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Python yüz tanıma servisine 5 fotoğraf göndererek yüz kaydı yapar.
     */
    public Map<String, Object> yuzKaydet(Long kullaniciId, String ad, List<MultipartFile> fotograflar) {
        try {
            String url = faceServiceUrl + "/api/yuz/kaydet";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("kullanici_id", kullaniciId.toString());
            body.add("ad", ad);

            for (MultipartFile foto : fotograflar) {
                body.add("fotograflar", foto.getResource());
            }

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Yüz kayıt hatası: {}", e.getMessage());
            return Map.of("basarili", false, "mesaj", "Yüz tanıma servisine bağlanılamadı: " + e.getMessage());
        }
    }

    /**
     * Yüz fotoğraflarını günceller.
     */
    public Map<String, Object> yuzGuncelle(Long kullaniciId, String ad, List<MultipartFile> fotograflar) {
        try {
            String url = faceServiceUrl + "/api/yuz/guncelle";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("kullanici_id", kullaniciId.toString());
            body.add("ad", ad);

            for (MultipartFile foto : fotograflar) {
                body.add("fotograflar", foto.getResource());
            }

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Yüz güncelleme hatası: {}", e.getMessage());
            return Map.of("basarili", false, "mesaj", "Yüz tanıma servisine bağlanılamadı: " + e.getMessage());
        }
    }

    /**
     * Yüz doğrulaması yapar (yoklama için).
     */
    public Map<String, Object> yuzDogrula(Long kullaniciId, MultipartFile fotograf) {
        try {
            String url = faceServiceUrl + "/api/yuz/dogrula";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("kullanici_id", kullaniciId.toString());
            body.add("fotograf", fotograf.getResource());

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Yüz doğrulama hatası: {}", e.getMessage());
            return Map.of("basarili", false, "mesaj", "Yüz tanıma servisine bağlanılamadı: " + e.getMessage());
        }
    }

    /**
     * Yüz verisini siler.
     */
    public Map<String, Object> yuzSil(Long kullaniciId) {
        try {
            String url = faceServiceUrl + "/api/yuz/sil/" + kullaniciId;
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.DELETE, null, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Yüz silme hatası: {}", e.getMessage());
            return Map.of("basarili", false, "mesaj", "Yüz tanıma servisine bağlanılamadı: " + e.getMessage());
        }
    }
}
