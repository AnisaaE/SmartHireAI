# SmartHire Mobile

Bu modul, mevcut React/Vite arayuzunu mobil odakli bir `JavaFX + Gluon` kabugu icinde calistirir.

## Neden Bu Yapi?

- Mevcut `frontend` akisi yeniden yazilmaz.
- API istekleri mobil kabuk icindeki `/api` proxy uzerinden calisir.
- Ayni kod tabaniyla Android paketi uretilmeye hazirdir.
- Masaustu moduluyle uyumlu bir mimari sunar.

## Gelistirme Modunda Calistirma

1. `frontend` klasorunde statik dosyalari uret:

```powershell
cd frontend
npm run build
```

2. Backend servislerinin ve `dispatcher` servisinin acik oldugundan emin ol.

3. Mobil kabugu yerelde calistir:

```powershell
cd mobile
mvn javafx:run
```

## Android Paketi Hazirlama

Android build icin Android SDK, GraalVM ve Gluon araclari gerekir. Ortam degiskenleri hazir oldugunda:

```powershell
cd mobile
mvn gluonfx:build gluonfx:package
```

Gerekiyorsa hedef cihazi acik secmek icin:

```powershell
mvn -Dgluonfx.target=android gluonfx:build gluonfx:package
```

## Alternatif API Adresi

Varsayilan API adresi `http://localhost:8080` olur. Farkli bir adres icin:

```powershell
$env:SMARTHIRE_API_BASE_URL="http://10.0.2.2:8080"
mvn javafx:run
```

Android emulatorunde backend erisimi icin genellikle `10.0.2.2` kullanilir.
