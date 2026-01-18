package com.adiyaman.yoklama;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class YoklamaApplication {

    @PostConstruct
    public void init() {
        // AWS UTC sorununu çöz - Türkiye saat dilimine ayarla
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Istanbul"));
    }

    public static void main(String[] args) {
        // JVM başlamadan önce de timezone ayarla
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Istanbul"));
        SpringApplication.run(YoklamaApplication.class, args);
    }
}
