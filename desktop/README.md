# SmartHire Desktop

Bu modul, mevcut React/Vite arayuzunu degistirmeden JavaFX icinde calistiran masaustu kabugudur.

## Neden Bu Yapi?

- Kullanici akisi ayni kalir.
- `frontend/src/App.jsx` altindaki route mantigi degismez.
- `AuthContext`, `axios`, `localStorage` ve mevcut API cagrilari aynen calisir.
- Swing/JavaFX tarafinda custom graphics kullanilarak masaustu sunumu guclenir.

## Calistirma

1. Mevcut backend servislerini ve `frontend` uygulamasini calistir.
2. Varsayilan frontend adresi `http://localhost:5173` oldugu icin Vite bu adreste acik olmali.
3. Masaustu uygulamayi baslat:

```powershell
cd desktop
mvn javafx:run
```

## Alternatif Frontend Adresi

Farkli bir adres kullanmak istersen:

```powershell
cd desktop
mvn -Dsmarthire.frontend.url=http://localhost:4173 javafx:run
```

veya

```powershell
$env:SMARTHIRE_FRONTEND_URL="http://localhost:4173"
mvn javafx:run
```

## Notlar

- Bu kabuk, web uygulamasini `WebView` icinde gosterdigi icin kullanim davranisi web surumuyle aynidir.
- Ust baslik ve yukleme alani JavaFX canvas cizimleriyle olusturulmustur.
