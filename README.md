# Yüz Tanıma ile Yoklama Sistemi

Adıyaman Üniversitesi için yüz tanıma tabanlı yoklama sistemi.

## Teknolojiler

| Bileşen | Teknoloji |
|---------|-----------|
| Backend | Spring Boot (Java 17) |
| Yüz Tanıma | Python + face_recognition |
| Veritabanı | MySQL |
| Frontend | Thymeleaf + HTML/CSS/JS |

## Proje Yapısı

```
├── backend/          # Spring Boot uygulaması
├── face-service/     # Python yüz tanıma servisi
└── README.md
```

## Kurulum

### 1. Veritabanı
```sql
CREATE DATABASE yoklama_db;
```

### 2. Backend
```bash
cd backend
./mvnw spring-boot:run
```

### 3. Python Servisi
```bash
cd face-service
pip install -r requirements.txt
python app.py
```

## Varsayılan Giriş Bilgileri

| Rol | Email | Şifre |
|-----|-------|-------|
| Admin | admin@adiyaman.edu.tr | admin123 |

## Özellikler

- **Admin**: Bölüm, ders ve öğretmen yönetimi
- **Öğretmen**: Yoklama başlatma/bitirme, katılım takibi
- **Öğrenci**: Yüz kaydı, yoklamaya katılım
