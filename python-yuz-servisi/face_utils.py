"""
Yüz Tanıma Yardımcı Fonksiyonları
Adıyaman Üniversitesi - Yoklama Sistemi
"""
import os
import time
import pickle
import threading
from pathlib import Path

import cv2
import face_recognition
import numpy as np

BASE_DIR = Path(__file__).resolve().parent
ENCODINGS_DIR = BASE_DIR / "encodings"
IMAGES_DIR = BASE_DIR / "yuz_verileri"
ENCODINGS_DIR.mkdir(parents=True, exist_ok=True)
IMAGES_DIR.mkdir(parents=True, exist_ok=True)

# Ayarlar
TOLERANCE = float(os.getenv("FACE_TOLERANCE", "0.5"))
FACE_MODEL = os.getenv("FACE_MODEL", "hog")  # GPU yoksa hog kullan
MIN_FACE_AREA_RATIO = float(os.getenv("FACE_MIN_AREA_RATIO", "0.08"))
MIN_BRIGHTNESS = int(os.getenv("FACE_MIN_BRIGHTNESS", "45"))
MIN_ENROLL_SAMPLES = int(os.getenv("FACE_ENROLL_MIN_SAMPLES", "3"))
VERIFY_CONFIRM_RATIO = float(os.getenv("FACE_VERIFY_CONFIRM_RATIO", "0.6"))

# Encoding dosyası
ENCODINGS_FILE = ENCODINGS_DIR / "encodings.pkl"
encodings_lock = threading.Lock()


def load_encodings():
    """Kayıtlı yüz encoding'lerini yükle."""
    with encodings_lock:
        if not ENCODINGS_FILE.exists():
            return {"names": [], "encodings": [], "user_ids": []}
        try:
            with ENCODINGS_FILE.open("rb") as f:
                data = pickle.load(f)
        except Exception:
            return {"names": [], "encodings": [], "user_ids": []}
        if not isinstance(data, dict):
            return {"names": [], "encodings": [], "user_ids": []}
        data.setdefault("names", [])
        data.setdefault("encodings", [])
        data.setdefault("user_ids", [])
        return data


def save_encodings(data):
    """Encoding'leri güvenli şekilde kaydet."""
    with encodings_lock:
        tmp_path = ENCODINGS_FILE.with_suffix(".tmp")
        with tmp_path.open("wb") as f:
            pickle.dump(data, f)
        tmp_path.replace(ENCODINGS_FILE)


def analyze_face(image_bytes):
    """
    Görüntüden yüz encoding'i ve kalite skoru üret.
    Returns: (encoding, kalite, error_message)
    """
    nparr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    if img is None:
        return None, 0, "Görüntü okunamadı"

    rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    rgb = np.ascontiguousarray(rgb, dtype=np.uint8)
    locations = face_recognition.face_locations(rgb, model=FACE_MODEL)

    if len(locations) == 0:
        return None, 0, "Yüz bulunamadı"
    if len(locations) > 1:
        return None, 0, "Birden fazla yüz tespit edildi"

    top, right, bottom, left = locations[0]
    height, width = rgb.shape[:2]
    face_area = max(0, bottom - top) * max(0, right - left)
    frame_area = max(1, height * width)
    area_ratio = face_area / frame_area
    if area_ratio < MIN_FACE_AREA_RATIO:
        return None, area_ratio, "Yüz çok uzak, kameraya biraz yaklaşın"

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    brightness = float(np.mean(gray))
    if brightness < MIN_BRIGHTNESS:
        return None, area_ratio, "Ortam çok karanlık, daha aydınlık bir yere geçin"

    encodings = face_recognition.face_encodings(rgb, locations)
    if not encodings:
        return None, area_ratio, "Yüz encoding'i alınamadı"

    quality = area_ratio + (brightness / 255.0) * 0.1
    return encodings[0], quality, None


def get_face_encoding_from_image(image_bytes):
    """
    Byte dizisinden yüz encoding'i çıkar.
    Returns: (encoding, error_message)
    """
    encoding, _, error = analyze_face(image_bytes)
    return encoding, error


def register_face(user_id, name, image_bytes_list):
    """
    Kullanıcı yüzünü kaydet (5 fotoğraf ile).
    Returns: (success, message)
    """
    if not image_bytes_list:
        return False, "Fotoğraf alınamadı"

    encodings = []
    best_sample = None
    best_quality = -1
    last_error = None

    for sample in image_bytes_list:
        encoding, quality, error = analyze_face(sample)
        if error:
            last_error = error
            continue
        encodings.append(encoding)
        if quality > best_quality:
            best_quality = quality
            best_sample = sample

    if len(encodings) < MIN_ENROLL_SAMPLES:
        return False, last_error or f"Yeterli sayıda net yüz görüntüsü alınamadı (minimum {MIN_ENROLL_SAMPLES})"

    mean_encoding = np.mean(encodings, axis=0)
    norm = np.linalg.norm(mean_encoding)
    if norm > 0:
        mean_encoding = mean_encoding / norm

    data = load_encodings()

    # Kullanıcı daha önce kayıtlıysa güncelle
    str_user_id = str(user_id)
    if str_user_id in data.get("user_ids", []):
        idx = data["user_ids"].index(str_user_id)
        data["encodings"][idx] = mean_encoding
        data["names"][idx] = name
    else:
        data["names"].append(name)
        data["encodings"].append(mean_encoding)
        data["user_ids"].append(str_user_id)

    save_encodings(data)

    # Fotoğrafları kaydet
    if best_sample is None:
        best_sample = image_bytes_list[0]
    
    safe_name = "".join(c for c in name if c.isalnum() or c in "_ ").strip()
    if not safe_name:
        safe_name = "kullanici"
    person_dir = IMAGES_DIR / f"{safe_name}_{user_id}"
    person_dir.mkdir(parents=True, exist_ok=True)

    # Tüm fotoğrafları kaydet
    for i, sample in enumerate(image_bytes_list):
        try:
            nparr = np.frombuffer(sample, np.uint8)
            img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
            if img is not None:
                timestamp = time.strftime("%Y%m%d_%H%M%S")
                cv2.imwrite(str(person_dir / f"{timestamp}_{i}.jpg"), img)
        except Exception:
            pass

    return True, f"Yüz başarıyla kaydedildi ({len(encodings)} fotoğraf işlendi)"


def update_face(user_id, name, image_bytes_list):
    """
    Kullanıcı yüzünü güncelle.
    Returns: (success, message)
    """
    # Eski fotoğrafları sil
    str_user_id = str(user_id)
    safe_name = "".join(c for c in name if c.isalnum() or c in "_ ").strip()
    if not safe_name:
        safe_name = "kullanici"
    person_dir = IMAGES_DIR / f"{safe_name}_{user_id}"
    
    if person_dir.exists():
        for f in person_dir.glob("*.jpg"):
            try:
                f.unlink()
            except Exception:
                pass

    # Yeni kayıt yap
    return register_face(user_id, name, image_bytes_list)


def verify_face(user_id, image_bytes):
    """
    Kullanıcının yüzünü doğrula.
    Returns: (success, message, confidence)
    """
    data = load_encodings()
    str_user_id = str(user_id)

    if str_user_id not in data.get("user_ids", []):
        return False, "Kayıtlı yüzünüz bulunamadı", 0

    idx = data["user_ids"].index(str_user_id)
    known_encoding = data["encodings"][idx]

    samples = image_bytes if isinstance(image_bytes, (list, tuple)) else [image_bytes]
    if not samples:
        return False, "Görüntü alınamadı", 0

    encodings = []
    last_error = None

    for sample in samples:
        encoding, _, error = analyze_face(sample)
        if error:
            last_error = error
            continue
        encodings.append(encoding)

    if not encodings:
        return False, last_error or "Yüz bulunamadı", 0

    distances = [face_recognition.face_distance([known_encoding], encoding)[0] for encoding in encodings]
    matches = [distance <= TOLERANCE for distance in distances]
    match_ratio = sum(matches) / len(matches)
    best_distance = min(distances)
    confidence = 1 - best_distance

    if match_ratio >= VERIFY_CONFIRM_RATIO:
        return True, f"Yüz doğrulandı (güven: {confidence:.2f})", confidence
    return False, "Yüz eşleşmedi", confidence


def recognize_face(image_bytes):
    """
    Resimdeki yüzü tanı.
    Returns: (user_id, name, confidence) veya (None, None, 0)
    """
    data = load_encodings()

    if not data.get("encodings"):
        return None, None, 0

    encoding, error = get_face_encoding_from_image(image_bytes)
    if error:
        return None, None, 0

    distances = face_recognition.face_distance(data["encodings"], encoding)
    best_idx = int(np.argmin(distances))
    best_distance = distances[best_idx]

    if best_distance <= TOLERANCE:
        return (
            data["user_ids"][best_idx],
            data["names"][best_idx],
            1 - best_distance,
        )

    return None, None, 0


def delete_face(user_id):
    """
    Kullanıcının yüz verisini sil.
    Returns: (success, message)
    """
    data = load_encodings()
    str_user_id = str(user_id)

    if str_user_id not in data.get("user_ids", []):
        return False, "Kullanıcı bulunamadı"

    idx = data["user_ids"].index(str_user_id)
    del data["names"][idx]
    del data["encodings"][idx]
    del data["user_ids"][idx]

    save_encodings(data)

    # Fotoğrafları sil
    for person_dir in IMAGES_DIR.glob(f"*_{user_id}"):
        if person_dir.is_dir():
            for f in person_dir.glob("*"):
                try:
                    f.unlink()
                except Exception:
                    pass
            try:
                person_dir.rmdir()
            except Exception:
                pass

    return True, "Yüz verisi silindi"
