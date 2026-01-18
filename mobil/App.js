import React, { useState, useEffect, useRef } from 'react';
import { StyleSheet, View, StatusBar, BackHandler, ActivityIndicator, Text, Image, TouchableOpacity } from 'react-native';
import { WebView } from 'react-native-webview';
import { useCameraPermissions } from 'expo-camera';

const BASE_URL = 'https://yoklama.yusuferenseyrek.com.tr';

export default function App() {
  const [isLoading, setIsLoading] = useState(true);
  const [permission, requestPermission] = useCameraPermissions();
  const webViewRef = useRef(null);

  // Başlangıçta izin kontrolü ve Splash Screen
  useEffect(() => {
    async function init() {
      // 2 saniye splash bekle
      setTimeout(() => {
        setIsLoading(false);
      }, 2000);

      // İzin durumunu kontrol et, yoksa iste
      if (!permission?.granted) {
        try {
          await requestPermission();
        } catch (e) {
          console.warn('İzin hatası:', e);
        }
      }
    }
    init();
  }, []);

  // Geri tuşu yönetimi
  useEffect(() => {
    const backAction = () => {
      if (webViewRef.current) {
        webViewRef.current.goBack();
        return true;
      }
      return false;
    };
    const backHandler = BackHandler.addEventListener('hardwareBackPress', backAction);
    return () => backHandler.remove();
  }, []);

  // Splash Screen
  if (isLoading) {
    return (
      <View style={styles.splashContainer}>
        <StatusBar barStyle="light-content" backgroundColor="#667eea" />
        <Image source={require('./assets/icon.png')} style={styles.splashLogo} />
        <Text style={styles.splashTitle}>Yoklama Sistemi</Text>
        <Text style={styles.splashSubtitle}>Adıyaman Üniversitesi</Text>
        <ActivityIndicator size="large" color="#fff" style={styles.loader} />
      </View>
    );
  }

  // İzin verilmediyse Uyarı Ekranı
  if (permission && !permission.granted) {
    return (
      <View style={styles.errorContainer}>
        <StatusBar barStyle="light-content" backgroundColor="#667eea" />
        <Text style={styles.errorText}>Kamera İzni Gerekli 📷</Text>
        <Text style={styles.errorSubText}>
          Yüz tanıma ile yoklama alabilmek için kameranıza erişmemiz gerekiyor.
        </Text>
        <TouchableOpacity style={styles.button} onPress={requestPermission}>
          <Text style={styles.buttonText}>İzin Ver</Text>
        </TouchableOpacity>
      </View>
    );
  }

  // Ana Uygulama - WebView
  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#667eea" />
      <WebView
        ref={webViewRef}
        source={{ uri: BASE_URL }}
        style={styles.webview}
        javaScriptEnabled={true}
        domStorageEnabled={true}
        startInLoadingState={true}
        allowsInlineMediaPlayback={true}
        mediaPlaybackRequiresUserAction={false}
        allowsFullscreenVideo={true}
        // İzinler (Android)
        allowFileAccess={true}
        allowFileAccessFromFileURLs={true}
        allowUniversalAccessFromFileURLs={true}
        renderLoading={() => (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="large" color="#667eea" />
            <Text style={styles.loadingText}>Yükleniyor...</Text>
          </View>
        )}
        // Web sitesi kamera istediğinde otomatik onayla
        onPermissionRequest={(req) => {
          req.grant(req.resources);
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#667eea' },
  webview: { flex: 1 },
  splashContainer: { flex: 1, backgroundColor: '#667eea', alignItems: 'center', justifyContent: 'center' },
  splashLogo: { width: 120, height: 120, borderRadius: 25, marginBottom: 24 },
  splashTitle: { fontSize: 28, fontWeight: 'bold', color: '#fff', marginBottom: 8 },
  splashSubtitle: { fontSize: 16, color: 'rgba(255,255,255,0.8)', marginBottom: 40 },
  loader: { marginTop: 20 },
  loadingContainer: { position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, alignItems: 'center', justifyContent: 'center', backgroundColor: '#f5f5f5' },
  loadingText: { marginTop: 16, fontSize: 16, color: '#666' },
  errorContainer: { flex: 1, backgroundColor: '#667eea', alignItems: 'center', justifyContent: 'center', padding: 24 },
  errorText: { fontSize: 24, fontWeight: 'bold', color: '#fff', marginBottom: 16 },
  errorSubText: { fontSize: 16, color: 'rgba(255,255,255,0.9)', textAlign: 'center', marginBottom: 30, lineHeight: 24 },
  button: { backgroundColor: '#fff', paddingVertical: 12, paddingHorizontal: 30, borderRadius: 25 },
  buttonText: { color: '#667eea', fontSize: 18, fontWeight: 'bold' }
});
