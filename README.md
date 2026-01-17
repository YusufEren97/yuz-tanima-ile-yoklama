# 🎓 Yüz Tanıma ile Yoklama Sistemi (Graduation Project)

Bu proje, Adıyaman Üniversitesi için geliştirilmiş, **Yüz Tanıma Teknolojisi** kullanan modern bir yoklama takip sistemidir.

![Project Banner](https://via.placeholder.com/1200x400?text=Yuz+Tanima+Ile+Yoklama+Sistemi)

## 🚀 Özellikler

### 👤 Hesap Türleri ve Yetkiler
*   **Admin:** Bölüm, Ders, Öğretmen ve Öğrenci yönetimi.
*   **Öğretmen:** Derslerini görme, yoklama başlatma/bitirme, geçmiş yoklamaları raporlama.
*   **Öğrenci:** Yüz kaydı oluşturma (5 fotoğraf ile), aktif yoklamalara kamera ile katılma, devamsızlık takibi.

### 🌟 Öne Çıkan Özellikler
*   **Yüz Tanıma Entegrasyonu:** Python (dlib & face_recognition) tabanlı yüksek doğruluklu yüz doğrulama.
*   **Canlı Yoklama:** Öğretmen yoklamayı başlattığı an öğrenciler saniyeler içinde yüzlerini okutarak derse katılabilir.
*   **Security:** Spring Security ile güvenli kimlik doğrulama ve rol bazlı yetkilendirme.
*   **Responsive UI:** Mobil uyumlu, modern ve kullanıcı dostu arayüz (Glassmorphism & Indigo Theme).
*   **Yüz Kayıt Kontrolü:** Kaliteli veri seti için öğrencilerden 5 farklı açıdan fotoğraf istenir.

---

## 🛠️ Teknoloji Yığını (Tech Stack)

### Backend (Ana Servis)
*   **Dil:** Java 17+
*   **Framework:** Spring Boot 3.x
*   **Veritabanı:** MySQL
*   **ORM:** Hibernate / Spring Data JPA
*   **Güvenlik:** Spring Security
*   **Build Tool:** Maven

### Microservice (Yüz Tanıma)
*   **Dil:** Python 3.12
*   **Framework:** Flask
*   **Kütüphaneler:** face_recognition, dlib, numpy, opencv-python

### Frontend
*   **Template Engine:** Thymeleaf
*   **Stil:** CSS3 (Custom Premium Design), FontAwesome
*   **JS:** Vanilla JS (Camera API & AJAX)

---

## ⚙️ Kurulum ve Başlatma

### Gereksinimler
*   Java 17 veya üzeri
*   Python 3.12 (NOT: Python 3.14 veya Numpy 2.x kullanmayın, dlib ile uyumsuzluk yapabilir!)
*   MySQL Server

### Adım Adım Çalıştırma

**1. Veritabanını Hazırlayın:**
MySQL'de `yoklama_sistemi` adında bir veritabanı oluşturun.
(Kullanıcı adı: `root`, Şifre: `root` varsayılan olarak ayarlıdır. `application.properties` dosyasından değiştirebilirsiniz.)

**2. Python Bağımlılıklarını Yükleyin:**
```bash
cd python-yuz-servisi
pip install -r requirements.txt
```
*(Öneri: Sanal ortam (venv) kullanmanız tavsiye edilir.)*

**3. Sistemi Başlatın:**
Ana dizindeki `baslat.bat` dosyasına çift tıklayın.
Bu script şunları yapar:
*   Python Flask servisini başlatır (Port: 5000)
*   Spring Boot uygulamasını başlatır (Port: 8080)
*   Tarayıcıyı otomatik açar.

---

## 📂 Proje Yapısı

```
yuz-tanima-ile-yoklama/
├── backend/                  # Java Spring Boot Uygulaması
│   ├── src/main/java/        # Controller, Service, Entity, Repository
│   └── src/main/resources/   # HTML (Thymeleaf), CSS, App Config
├── python-yuz-servisi/       # Python Yüz Tanıma API
│   ├── app.py                # Flask Server
│   ├── face_utils.py         # Yüz işleme fonksiyonları
│   └── yuz_verileri/         # (Otomatik oluşur) Kaydedilen yüz dataları
└── baslat.bat                # Tek tıkla başlatma scripti
```

## 👥 Katkıda Bulunanlar
*   **Yusuf Eren Seyrek** - *Full Stack Developer*

## 📄 Lisans
Bu proje Adıyaman Üniversitesi Bitirme Projesi kapsamında geliştirilmiştir.
