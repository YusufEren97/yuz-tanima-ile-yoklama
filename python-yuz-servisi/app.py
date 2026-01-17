"""
Yüz Tanıma REST API
Adıyaman Üniversitesi - Yoklama Sistemi
Flask ile yüz tanıma mikro servisi
"""
import os
from flask import Flask, request, jsonify
from flask_cors import CORS
from dotenv import load_dotenv

import face_utils

load_dotenv()

app = Flask(__name__)
CORS(app)

# Ayarlar
PORT = int(os.getenv("PORT", 5000))
DEBUG = os.getenv("DEBUG", "false").lower() == "true"


@app.route("/", methods=["GET"])
def index():
    """API durumu kontrolü."""
    return jsonify({
        "durum": "aktif",
        "servis": "Yüz Tanıma API",
        "versiyon": "1.0.0"
    })


@app.route("/api/yuz/kaydet", methods=["POST"])
def yuz_kaydet():
    """
    5 fotoğraf ile yüz kaydı yapar.
    Form data: kullanici_id, ad, fotograflar (MultipartFile[])
    """
    try:
        kullanici_id = request.form.get("kullanici_id")
        ad = request.form.get("ad")

        if not kullanici_id or not ad:
            return jsonify({
                "basarili": False,
                "mesaj": "kullanici_id ve ad zorunludur"
            }), 400

        fotograflar = request.files.getlist("fotograflar")
        
        if len(fotograflar) < 5:
            return jsonify({
                "basarili": False,
                "mesaj": f"En az 5 fotoğraf gerekli, {len(fotograflar)} tane gönderildi"
            }), 400

        # Fotoğrafları byte dizisine çevir
        image_bytes_list = []
        for foto in fotograflar:
            try:
                image_bytes_list.append(foto.read())
            except Exception as e:
                print(f"Fotoğraf okuma hatası: {e}")

        if len(image_bytes_list) < 5:
            return jsonify({
                "basarili": False,
                "mesaj": "Fotoğraflar okunamadı"
            }), 400

        success, message = face_utils.register_face(kullanici_id, ad, image_bytes_list)

        return jsonify({
            "basarili": success,
            "mesaj": message
        }), 200 if success else 400

    except Exception as e:
        print(f"Yüz kayıt hatası: {e}")
        return jsonify({
            "basarili": False,
            "mesaj": f"Sunucu hatası: {str(e)}"
        }), 500


@app.route("/api/yuz/guncelle", methods=["POST"])
def yuz_guncelle():
    """
    Yüz fotoğraflarını günceller.
    Form data: kullanici_id, ad, fotograflar (MultipartFile[])
    """
    try:
        kullanici_id = request.form.get("kullanici_id")
        ad = request.form.get("ad")

        if not kullanici_id or not ad:
            return jsonify({
                "basarili": False,
                "mesaj": "kullanici_id ve ad zorunludur"
            }), 400

        fotograflar = request.files.getlist("fotograflar")
        
        if len(fotograflar) < 5:
            return jsonify({
                "basarili": False,
                "mesaj": f"En az 5 fotoğraf gerekli, {len(fotograflar)} tane gönderildi"
            }), 400

        image_bytes_list = []
        for foto in fotograflar:
            try:
                image_bytes_list.append(foto.read())
            except Exception as e:
                print(f"Fotoğraf okuma hatası: {e}")

        success, message = face_utils.update_face(kullanici_id, ad, image_bytes_list)

        return jsonify({
            "basarili": success,
            "mesaj": message
        }), 200 if success else 400

    except Exception as e:
        print(f"Yüz güncelleme hatası: {e}")
        return jsonify({
            "basarili": False,
            "mesaj": f"Sunucu hatası: {str(e)}"
        }), 500


@app.route("/api/yuz/dogrula", methods=["POST"])
def yuz_dogrula():
    """
    Yüz doğrulaması yapar (yoklama için).
    Form data: kullanici_id, fotograf (MultipartFile)
    """
    try:
        kullanici_id = request.form.get("kullanici_id")

        if not kullanici_id:
            return jsonify({
                "basarili": False,
                "mesaj": "kullanici_id zorunludur"
            }), 400

        fotograf = request.files.get("fotograf")
        
        if not fotograf:
            return jsonify({
                "basarili": False,
                "mesaj": "fotograf zorunludur"
            }), 400

        try:
            image_bytes = fotograf.read()
        except Exception as e:
            return jsonify({
                "basarili": False,
                "mesaj": f"Fotoğraf okunamadı: {str(e)}"
            }), 400

        success, message, confidence = face_utils.verify_face(kullanici_id, image_bytes)

        return jsonify({
            "basarili": success,
            "mesaj": message,
            "guven": confidence
        }), 200 if success else 400

    except Exception as e:
        print(f"Yüz doğrulama hatası: {e}")
        return jsonify({
            "basarili": False,
            "mesaj": f"Sunucu hatası: {str(e)}"
        }), 500


@app.route("/api/yuz/tani", methods=["POST"])
def yuz_tani():
    """
    Resimdeki yüzü tanır.
    Form data: fotograf (MultipartFile)
    """
    try:
        fotograf = request.files.get("fotograf")
        
        if not fotograf:
            return jsonify({
                "basarili": False,
                "mesaj": "fotograf zorunludur"
            }), 400

        try:
            image_bytes = fotograf.read()
        except Exception as e:
            return jsonify({
                "basarili": False,
                "mesaj": f"Fotoğraf okunamadı: {str(e)}"
            }), 400

        user_id, name, confidence = face_utils.recognize_face(image_bytes)

        if user_id:
            return jsonify({
                "basarili": True,
                "kullanici_id": user_id,
                "ad": name,
                "guven": confidence
            })
        else:
            return jsonify({
                "basarili": False,
                "mesaj": "Yüz tanınamadı"
            }), 404

    except Exception as e:
        print(f"Yüz tanıma hatası: {e}")
        return jsonify({
            "basarili": False,
            "mesaj": f"Sunucu hatası: {str(e)}"
        }), 500


@app.route("/api/yuz/sil/<kullanici_id>", methods=["DELETE"])
def yuz_sil(kullanici_id):
    """
    Yüz verisini siler.
    Path param: kullanici_id
    """
    try:
        success, message = face_utils.delete_face(kullanici_id)

        return jsonify({
            "basarili": success,
            "mesaj": message
        }), 200 if success else 404

    except Exception as e:
        print(f"Yüz silme hatası: {e}")
        return jsonify({
            "basarili": False,
            "mesaj": f"Sunucu hatası: {str(e)}"
        }), 500


if __name__ == "__main__":
    print(f"Yüz Tanıma API başlatılıyor - Port: {PORT}")
    app.run(host="0.0.0.0", port=PORT, debug=DEBUG)
