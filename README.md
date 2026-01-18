<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Python-3.12-3776AB?style=for-the-badge&logo=python&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/License-Academic-orange?style=for-the-badge" />
</p>

<h1 align="center">🎓 Yüz Tanıma ile Yoklama Sistemi</h1>

<p align="center">
  <strong>Adıyaman Üniversitesi Mühendislik Fakültesi Bitirme Projesi</strong><br>
  Yapay zeka destekli yüz tanıma teknolojisi ile otomatik yoklama sistemi
</p>

<p align="center">
  <a href="https://yoklama.yusuferenseyrek.com.tr">
    <img src="https://img.shields.io/badge/🌐%20CANLI%20DEMO-yoklama.yusuferenseyrek.com.tr-667eea?style=for-the-badge" />
  </a>
</p>

---

## 📸 Ekran Görüntüleri

<p align="center">
  <img src="https://via.placeholder.com/800x400/667eea/ffffff?text=Admin+Panel" width="45%" />
  <img src="https://via.placeholder.com/800x400/764ba2/ffffff?text=Yuz+Kayit+Ekrani" width="45%" />
</p>

---

## ✨ Özellikler

### 👤 Kullanıcı Rolleri

| Rol | Yetkiler |
|-----|----------|
| **🔑 Admin** | Bölüm, Sınıf, Ders, Öğretmen ve Öğrenci yönetimi |
| **👨‍🏫 Öğretmen** | Yoklama başlatma/bitirme, gerçek zamanlı takip, raporlama |
| **👨‍🎓 Öğrenci** | Yüz kaydı (5 fotoğraf), yoklamaya katılım, devamsızlık takibi |

### 🌟 Öne Çıkan Özellikler

- 🤖 **Yapay Zeka Destekli:** dlib & face_recognition ile %95+ doğruluk oranı
- ⚡ **Hızlı Doğrulama:** Ortalama 1 saniyenin altında yüz tanıma
- 🔒 **Güvenli:** Spring Security ile rol bazlı yetkilendirme
- 📱 **Responsive:** Mobil uyumlu modern arayüz (Glassmorphism)
- 🎯 **Kolay Kullanım:** Tek tıkla yoklama başlatma ve katılım

---

## 🏗️ Mimari

```
┌─────────────────────────────────────────────────────────────────┐
│                     KULLANICI ARAYÜZÜ                           │
│         Admin Panel  │  Öğretmen  │  Öğrenci Dashboard          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                 ANA SERVİS (Spring Boot 3.2.1)                  │
│         Controller  │  Service  │  Repository  │  Security     │
└─────────────────────────────────────────────────────────────────┘
          │                                        │
          ▼                                        ▼
┌──────────────────┐                  ┌───────────────────────────┐
│   VERİTABANI     │                  │   YÜZ TANIMA SERVİSİ      │
│     (MySQL)      │                  │    (Python Flask)         │
└──────────────────┘                  │   dlib + face_recognition │
                                      └───────────────────────────┘
```

---

## 🛠️ Teknoloji Yığını

<table>
<tr>
<td width="50%">

### ☕ Backend (Spring Boot)
| Teknoloji | Versiyon |
|-----------|----------|
| Java | 17+ |
| Spring Boot | 3.2.1 |
| Spring Security | 6.x |
| Spring Data JPA | 3.x |
| Thymeleaf | 3.x |
| MySQL | 8.x |
| Maven | 3.x |

</td>
<td width="50%">

### 🐍 Yüz Tanıma (Flask)
| Teknoloji | Versiyon |
|-----------|----------|
| Python | 3.12 |
| Flask | 3.x |
| dlib | 19.24+ |
| face_recognition | 1.3+ |
| OpenCV | 4.9+ |
| NumPy | <2.0 |

</td>
</tr>
</table>

---

## ⚙️ Kurulum

### 📋 Gereksinimler

- ☕ Java 17 veya üzeri
- 🐍 Python 3.12 *(NOT: Python 3.14 veya NumPy 2.x kullanmayın!)*
- 🐬 MySQL Server 8.x

### 🚀 Hızlı Başlangıç

**1️⃣ Veritabanını Oluşturun:**
```sql
CREATE DATABASE yoklama_db;
```

**2️⃣ Python Bağımlılıklarını Yükleyin:**
```bash
cd python-yuz-servisi
pip install -r requirements.txt
```

**3️⃣ Sistemi Başlatın:**
```bash
# Windows
baslat.bat

# Veya manuel başlatma:
# Terminal 1: cd python-yuz-servisi && python app.py
# Terminal 2: cd backend && mvnw spring-boot:run
```

**4️⃣ Tarayıcıdan Açın:**
```
http://localhost:8080
```

---

## 📂 Proje Yapısı

```
yuz-tanima-ile-yoklama/
│
├── 📁 backend/                    # Java Spring Boot
│   ├── 📁 src/main/java/
│   │   └── 📁 com/adiyaman/yoklama/
│   │       ├── 📁 controller/     # API Endpoints
│   │       ├── 📁 service/        # İş Mantığı
│   │       ├── 📁 repository/     # Veritabanı
│   │       ├── 📁 entity/         # Veri Modelleri
│   │       └── 📁 config/         # Güvenlik, vb.
│   └── 📁 src/main/resources/
│       ├── 📁 templates/          # Thymeleaf HTML
│       └── 📄 application.properties
│
├── 📁 python-yuz-servisi/         # Python Flask API
│   ├── 📄 app.py                  # REST Endpoints
│   ├── 📄 face_utils.py           # Yüz İşleme
│   └── 📁 encodings/              # Kayıtlı Yüz Verileri
│
├── 📁 mobil/                      # Expo React Native
│   └── 📄 App.js                  # WebView Wrapper
│
└── 📄 baslat.bat                  # Tek Tıkla Başlatma
```

---

## 🔌 API Endpoints

### Yüz Tanıma Servisi (Port: 5000)

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| `POST` | `/api/yuz/kaydet` | 5 fotoğraf ile yüz kaydı |
| `POST` | `/api/yuz/dogrula` | Yüz doğrulama (yoklama) |
| `POST` | `/api/yuz/guncelle` | Yüz fotoğraflarını güncelle |
| `DELETE` | `/api/yuz/sil/{id}` | Yüz verisini sil |

---

## � Performans

| Metrik | Değer |
|--------|-------|
| Yüz Tanıma Doğruluğu | **%95+** |
| Ortalama Yanıt Süresi | **<1 saniye** |
| Eş Zamanlı Kullanıcı | **100+** |
| Günlük İşlem Kapasitesi | **10,000+** |

---

## 👥 Geliştiriciler

<table>
<tr>
<td align="center" width="50%">
<a href="https://github.com/YusufEren97">
<img src="https://github.com/YusufEren97.png" width="120" height="120" style="border-radius:50%"><br>
<strong>Yusuf Eren SEYREK</strong><br>
@YusufEren97
</a><br>
<sub>Backend & Yüz Tanıma</sub>
</td>
<td align="center" width="50%">
<a href="https://github.com/Deleny">
<img src="https://github.com/Deleny.png" width="120" height="120" style="border-radius:50%"><br>
<strong>Mehmet DELİN</strong><br>
@Deleny
</a><br>
<sub>Frontend & Mobil</sub>
</td>
</tr>
</table>

---

## 🎓 Proje Bilgileri

| | |
|---|---|
| **Üniversite** | Adıyaman Üniversitesi |
| **Fakülte** | Mühendislik Fakültesi |
| **Bölüm** | Bilgisayar Mühendisliği |
| **Proje Türü** | Lisans Bitirme Projesi |
| **Yıl** | 2025-2026 |

---

## 📄 Lisans

Bu proje **Adıyaman Üniversitesi Mühendislik Fakültesi** Bilgisayar Mühendisliği Bölümü **Lisans Bitirme Projesi** kapsamında geliştirilmiştir.

---

<p align="center">
  <strong>⭐ Projeyi beğendiyseniz yıldız vermeyi unutmayın!</strong>
</p>

<p align="center">
  <a href="https://yoklama.yusuferenseyrek.com.tr">🌐 Demo</a> •
  <a href="#kurulum">📖 Kurulum</a> •
  <a href="#api-endpoints">🔌 API</a> •
  <a href="#geliştiriciler">👥 Geliştiriciler</a>
</p>
