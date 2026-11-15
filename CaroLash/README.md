# Caro Lash (app Android, 100 % sin internet)

App para Caro: agenda de citas, clientas con su diseño de pestañas (muñeca, ardilla u ojo de gato), mapping, recordatorios por WhatsApp y logros.
Todo se guarda en el celular (archivo `caro_lash.json`). La app no pide ningún permiso.

## Sacar el APK con GitHub
1. Sube esta carpeta a un repositorio nuevo de GitHub (rama `main`).
2. Entra a la pestaña **Actions** → **Compilar APK** → si no arrancó sola, **Run workflow**.
3. Cuando termine (unos 5 min), abre la ejecución y descarga el artefacto **CaroLash-apk** (zip con `app-release.apk`).
4. Pasa el APK al celular de Caro e instálalo (hay que permitir "instalar apps desconocidas").

## Abrir en Android Studio
Abre la carpeta, espera el sync y dale Run. Requiere JDK 17.

## Dónde está cada cosa
- `Logic.kt`: guía de ojos (recomendador), mantenimientos sugeridos, logros, mensajes de WhatsApp, datos de ejemplo.
- `Codec.kt` y `Store.kt`: guardado en JSON.
- `MainActivity.kt`: navegación y transiciones (push, volver, pestañas, hojas, fundido).
- `Ui.kt`: colores, letras, íconos y piezas reutilizables.
- Pantallas: `Onboarding.kt`, `HoyScreen.kt`, `AgendaScreen.kt`, `ClientasScreen.kt`, `FichaScreen.kt`, `LogrosScreen.kt`.
- Para probar rápido: Ajustes → "Cargar datos de ejemplo".
