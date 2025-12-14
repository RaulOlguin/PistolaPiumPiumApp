# Pistola Pium Pium 2

¡Bienvenido al repositorio de Pistola Pium Pium 2! Esta es la versión reconstruida y mejorada de la aplicación original, desarrollada siguiendo las mejores prácticas y tecnologías modernas de Android.

## Descripción

Pistola Pium Pium 2 es una aplicación de simulación de pistola de juguete que ofrece una experiencia interactiva y divertida. No solo simula el disparo con efectos de sonido, vibración y linterna, sino que también incluye una comunidad en línea a través de un foro integrado, donde los usuarios pueden iniciar sesión y compartir mensajes.

## ✨ Características Principales

-   **Simulador Interactivo:** Mantén presionado para disparar y observa cómo se consume la munición en tiempo real.
-   **Recarga por Movimiento:** ¡Sacude tu teléfono para recargar la pistola!
-   **Ajustes Personalizables:** Configura la intensidad de la vibración, el volumen del sonido y si deseas que el flash de la cámara se active al disparar.
-   **Persistencia Local:** La configuración del usuario se guarda directamente en el dispositivo gracias a Room.
-   **Autenticación Segura:** Inicio de sesión rápido y seguro utilizando **Google Sign-In**.
-   **Foro Comunitario:** Un foro en tiempo real donde los usuarios autenticados pueden leer y crear nuevos temas de discusión.

## 🛠️ Tecnologías y Arquitectura

Este proyecto fue desarrollado desde cero con un enfoque en la calidad, mantenibilidad y el uso de un stack tecnológico moderno.

### Arquitectura

-   **MVVM (Model-View-ViewModel):** Patrón arquitectónico recomendado por Google que separa la lógica de negocio de la interfaz de usuario, facilitando las pruebas y el mantenimiento.
-   **Package by Feature:** El código está organizado en paquetes por funcionalidad (`data`, `network`, `ui`, `viewmodel`) para una navegación y comprensión más sencillas.
-   **Single-Activity Architecture:** Toda la aplicación se renderiza dentro de una única `MainActivity` que gestiona la navegación entre diferentes pantallas de Compose.

### Stack Tecnológico

-   **Lenguaje:** [Kotlin](https://kotlinlang.org/) (100% Kotlin)
-   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) para una interfaz de usuario declarativa y moderna.
-   **Asincronía:** [Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html) para manejar operaciones asíncronas de forma eficiente.
-   **Persistencia Local:** [Room](https://developer.android.com/training/data-storage/room) para guardar la configuración del usuario.
-   **Networking:** [Retrofit](https://square.github.io/retrofit/) para realizar llamadas a la API de forma declarativa.
-   **Backend & Base de Datos Remota:**
    -   [Firebase Cloud Functions](https://firebase.google.com/docs/functions) como nuestro backend serverless.
    -   [Firebase Firestore](https://firebase.google.com/docs/firestore) como nuestra base de datos NoSQL en tiempo real para el foro.
-   **Autenticación:** [Firebase Authentication](https://firebase.google.com/docs/auth) con Google Sign-In.
-   **Procesamiento de Anotaciones:** [KSP (Kotlin Symbol Processing)](https://kotlinlang.org/docs/ksp-overview.html) para el procesamiento de anotaciones de Room.
-   **Pruebas Unitarias:**
    -   [JUnit](https://junit.org/junit5/)
    -   [Mockito-Kotlin](https://github.com/mockito/mockito-kotlin) para crear mocks de dependencias.
    -   [Turbine](https://github.com/cashapp/turbine) para probar los `StateFlow` del ViewModel.

## 🚀 Configuración del Proyecto

Para clonar y ejecutar este proyecto en tu propia máquina, sigue estos pasos:

1.  **Clona el repositorio:**
    ```bash
    git clone [URL_DEL_REPOSITORIO]
    ```

2.  **Obtén el archivo `google-services.json`:**
    -   Este archivo es **esencial** para que la aplicación se conecte con los servicios de Firebase (Autenticación, Firestore, Cloud Functions).
    -   **No se incluye en el repositorio por seguridad.**
    -   Debes obtenerlo desde la **Consola de Firebase** del proyecto, en `Configuración del proyecto > General > Tus apps`.
    -   Una vez descargado, coloca el archivo `google-services.json` en la carpeta `app/` de tu proyecto.

3.  **Añade tu Huella Digital SHA-1 (para Debug):**
    -   Para que el **Login con Google** funcione en tu computadora de desarrollo, debes añadir la huella digital SHA-1 de tu clave de `debug` a Firebase.
    -   Ejecuta el siguiente comando en la terminal de Android Studio:
        ```bash
        ./gradlew app:signingReport
        ```
    -   Copia la clave **SHA-1** de la variante `debug`.
    -   Ve a la **Consola de Firebase** > `Configuración del proyecto > General > Tus apps > Huellas digitales del certificado SHA`.
    -   Haz clic en **"Añadir huella digital"** y pega tu clave.

4.  **Abre y Sincroniza:**
    -   Abre el proyecto en Android Studio.
    -   Espera a que Gradle se sincronice y descargue todas las dependencias.

¡Y listo! Ya puedes ejecutar la aplicación en un emulador o en un dispositivo físico.
