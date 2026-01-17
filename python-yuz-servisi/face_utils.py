"""
Yuz Tanima Yardimci Fonksiyonlari
Adiyaman Universitesi - Yoklama Sistemi
"""
import os
import io
import time
import pickle
import threading
import sys
from pathlib import Path

print(f"PYTHON VERSION: {sys.version}")
print(f"EXECUTABLE: {sys.executable}")
try:
    import dlib
    print(f"DLIB VERSION: {dlib.__version__}")
except:
    print("DLIB VERSION: ERROR")

try:
    import face_recognition
    print(f"FACE_RECOGNITION VERSION: {face_recognition.__version__}")
except:
    print("FACE_RECOGNITION VERSION: ERROR")


import cv2
import face_recognition
import numpy as np

print(f"NUMPY VERSION: {np.__version__}")
if np.__version__.startswith("2"):
    print("WARNING: Numpy 2.x detected! This may cause issues with dlib. Please downgrade to numpy<2.")

from PIL import Image

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

# Encoding dosyasi
ENCODINGS_FILE = ENCODINGS_DIR / "encodings.pkl"
encodings_lock = threading.Lock()


def load_encodings():
    """Kayitli yuz encoding'lerini yukle."""
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
    """Encoding'leri guvenli sekilde kaydet."""
    with encodings_lock:
        tmp_path = ENCODINGS_FILE.with_suffix(".tmp")
        with tmp_path.open("wb") as f:
            pickle.dump(data, f)
        tmp_path.replace(ENCODINGS_FILE)


def analyze_face(image_bytes):
    """
    Goruntuden yuz encoding'i ve kalite skoru uret.
    Returns: (encoding, kalite, error_message)
    """
    temp_filename = None
    try:
        # Gecici dosya olustur
        import tempfile
        import uuid
        
        # Temp klasoru
        temp_dir = BASE_DIR / "temp"
        temp_dir.mkdir(exist_ok=True)
        
        # Benzersiz dosya adi
        temp_filename = temp_dir / f"temp_{uuid.uuid4()}.jpg"
        
        # Once PIL ile duzgunce kaydet
        try:
             pil_image = Image.open(io.BytesIO(image_bytes))
             # Convert to RGB to ensure 3 channels
             if pil_image.mode != 'RGB':
                 pil_image = pil_image.convert('RGB')
             pil_image.save(temp_filename, format="JPEG", quality=95)
        except Exception as e:
             print(f"DEBUG: PIL save hatasi: {e}", flush=True)
             with open(temp_filename, "wb") as f:
                 f.write(image_bytes)

        # Dosyadan yukle
        rgb = face_recognition.load_image_file(str(temp_filename))
        
        # CRITICAL: dlib C-contiguous array ister
        if not rgb.flags['C_CONTIGUOUS']:
            rgb = np.ascontiguousarray(rgb)
            
        # CRITICAL: uint8 ister
        if rgb.dtype != np.uint8:
            rgb = rgb.astype(np.uint8)

        print(f"DEBUG: Analyze - Shape: {rgb.shape}, Dtype: {rgb.dtype}", flush=True)

    except Exception as e:
        print(f"DEBUG: Resim yukleme hatasi: {e}", flush=True)
        return None, 0, f"Goruntu islenemedi: {str(e)}"

    # Goruntu boyutunu kontrol et
    if rgb.shape[0] < 10 or rgb.shape[1] < 10:
        if temp_filename and temp_filename.exists():
            try: temp_filename.unlink() 
            except: pass
        return None, 0, "Goruntu cok kucuk"

    # Yuz tespiti yap - Hata yakalamali
    locations = []
    try:
        locations = face_recognition.face_locations(rgb, model=FACE_MODEL)
    except Exception as e:
        print(f"DEBUG: RGB Detection Error: {e}", flush=True)
        # Fallback: Grayscale
        try:
            print("DEBUG: Grayscale fallback...", flush=True)
            # PIL ile tekrar acip grayscale yap
            pil_gray = Image.open(str(temp_filename)).convert('L')
            gray = np.array(pil_gray, dtype=np.uint8)
            locations = face_recognition.face_locations(gray, model=FACE_MODEL)
        except Exception as e2:
            print(f"DEBUG: Grayscale Error: {e2}", flush=True)
            if temp_filename and temp_filename.exists():
                try: temp_filename.unlink() 
                except: pass
            return None, 0, f"Yuz tespiti hatasi (Format sorunu): {str(e)}"

    # Is bittikten sonra temp sil
    if temp_filename and temp_filename.exists():
        try: temp_filename.unlink() 
        except: pass

    if len(locations) == 0:
        return None, 0, "Yuz bulunamadi"
    if len(locations) > 1:
        return None, 0, "Birden fazla yuz tespit edildi"

    # Encoding (RGB kullanilmali)
    try:
        encodings = face_recognition.face_encodings(rgb, locations)
    except Exception as e:
        print(f"DEBUG: Encoding Error: {e}", flush=True)
        return None, 0, f"Encoding hatasi: {str(e)}"

    if not encodings:
        return None, 0, "Yuz encoding'i alinamadi"

    # Brightness
    try:
        brightness = float(np.mean(rgb))
    except:
        brightness = 128.0 # Default

    top, right, bottom, left = locations[0]
    height, width = rgb.shape[:2]
    face_area = max(0, bottom - top) * max(0, right - left)
    frame_area = max(1, height * width)
    area_ratio = face_area / frame_area
    
    if area_ratio < MIN_FACE_AREA_RATIO:
        return None, area_ratio, "Yuz cok uzak, kameraya biraz yaklasin"

    quality = area_ratio + (brightness / 255.0) * 0.1
    return encodings[0], quality, None


def get_face_encoding_from_image(image_bytes):
    """
    Byte dizisinden yuz encoding'i cikar.
    Returns: (encoding, error_message)
    """
    encoding, _, error = analyze_face(image_bytes)
    return encoding, error


def register_face(user_id, name, image_bytes_list):
    """
    Kullanici yuzunu kaydet (5 fotograf ile).
    Returns: (success, message)
    """
    if not image_bytes_list:
        return False, "Fotograf alinamadi"

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
        return False, last_error or f"Yeterli sayida net yuz goruntusu alinamadi (minimum {MIN_ENROLL_SAMPLES})"

    mean_encoding = np.mean(encodings, axis=0)
    norm = np.linalg.norm(mean_encoding)
    if norm > 0:
        mean_encoding = mean_encoding / norm

    data = load_encodings()

    # Kullanici daha once kayitliysa guncelle
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

    # Fotograflari kaydet
    if best_sample is None:
        best_sample = image_bytes_list[0]
    
    safe_name = "".join(c for c in name if c.isalnum() or c in "_ ").strip()
    if not safe_name:
        safe_name = "kullanici"
    person_dir = IMAGES_DIR / f"{safe_name}_{user_id}"
    person_dir.mkdir(parents=True, exist_ok=True)

    # Tum fotograflari kaydet
    for i, sample in enumerate(image_bytes_list):
        try:
            pil_img = Image.open(io.BytesIO(sample))
            timestamp = time.strftime("%Y%m%d_%H%M%S")
            pil_img.save(person_dir / f"{timestamp}_{i}.jpg")
        except Exception:
            pass

    return True, f"Yuz basariyla kaydedildi ({len(encodings)} fotograf islendi)"


def update_face(user_id, name, image_bytes_list):
    """
    Kullanici yuzunu guncelle.
    Returns: (success, message)
    """
    # Eski fotograflari sil
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

    # Yeni kayit yap
    return register_face(user_id, name, image_bytes_list)


def verify_face(user_id, image_bytes):
    """
    Kullanicinin yuzunu dogrula.
    Returns: (success, message, confidence)
    """
    data = load_encodings()
    str_user_id = str(user_id)

    if str_user_id not in data.get("user_ids", []):
        return False, "Kayitli yuzunuz bulunamadi", 0

    idx = data["user_ids"].index(str_user_id)
    known_encoding = data["encodings"][idx]

    samples = image_bytes if isinstance(image_bytes, (list, tuple)) else [image_bytes]
    if not samples:
        return False, "Goruntu alinamadi", 0

    encodings = []
    last_error = None

    for sample in samples:
        encoding, _, error = analyze_face(sample)
        if error:
            last_error = error
            continue
        encodings.append(encoding)

    if not encodings:
        return False, last_error or "Yuz bulunamadi", 0

    distances = [face_recognition.face_distance([known_encoding], encoding)[0] for encoding in encodings]
    matches = [distance <= TOLERANCE for distance in distances]
    match_ratio = sum(matches) / len(matches)
    best_distance = min(distances)
    confidence = 1 - best_distance

    if match_ratio >= VERIFY_CONFIRM_RATIO:
        return True, f"Yuz dogrulandi (guven: {confidence:.2f})", confidence
    return False, "Yuz eslesmedi", confidence


def recognize_face(image_bytes):
    """
    Resimdeki yuzu tani.
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
    Kullanicinin yuz verisini sil.
    Returns: (success, message)
    """
    data = load_encodings()
    str_user_id = str(user_id)

    if str_user_id not in data.get("user_ids", []):
        return False, "Kullanici bulunamadi"

    idx = data["user_ids"].index(str_user_id)
    del data["names"][idx]
    del data["encodings"][idx]
    del data["user_ids"][idx]

    save_encodings(data)

    # Fotograflari sil
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

    return True, "Yuz verisi silindi"
