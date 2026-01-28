# SensorManagement 🌡️📡

**SensorManagement** es una solución integral de IoT diseñada para la monitorización avanzada y el control de dispositivos en tiempo real. El proyecto abarca desde la programación de dispositivos en el "edge" hasta una infraestructura de backend robusta y containerizada.

## 🚀 Características Principales

* **Integración Hardware-Software:** Implementación de lógica para microcontroladores mediante el uso de **ESP32/Arduino**.
* **Backend Escalable:** Aplicación desarrollada en **Java** utilizando el ecosistema de **Maven** (`pom.xml`).
* **Infraestructura Containerizada:** Despliegue simplificado y consistente mediante **Docker Compose**.
* **Seguridad:** Gestión de identidades y cifrado mediante el uso de **Java KeyStore (JCEKS)**.
* **Persistencia y APIs:** Soporte para endpoints integrados con bases de datos **MySQL**.

## 🛠️ Stack Tecnológico

* **Microcontroladores:** ESP32 (Lenguaje Arduino/C++).
* **Lenguajes:** Java, SQL.
* **Gestión de Dependencias:** Maven.
* **DevOps:** Docker, Docker Compose.
* **IDE:** Configuración optimizada para IntelliJ IDEA y Eclipse.

## 📂 Estructura del Repositorio

El repositorio está organizado de la siguiente manera:

* `ESP32Arduino.ino`: Firmware para el microcontrolador ESP32 que gestiona la lectura de sensores y actuadores.
* `src/main/java/`: Código fuente del backend encargado de la lógica de negocio y gestión de datos.
* `static/`: Recursos estáticos (posible interfaz web o documentación técnica).
* `docker-compose.yaml`: Configuración para levantar el entorno completo (Base de datos, Broker, Backend) en segundos.
* `keystore.jceks`: Almacén de claves para asegurar las comunicaciones del sistema.

## ⚙️ Instalación y Despliegue

1. **Requisitos:** Tener instalado **Docker** y **Java JDK**.
2. **Clonar el repositorio:**
```bash
git clone https://github.com/pabloaguayogd/SensorManagement.git
cd SensorManagement

```


3. **Desplegar con Docker:**
```bash
docker-compose up -d

```


4. **Cargar Firmware:** Abre el archivo `ESP32Arduino.ino` en el IDE de Arduino y cárgalo en tu placa ESP32 configurando las credenciales de tu red.

## 📧 Contacto

**Pablo Aguayo**

* **GitHub:** [pabloaguayogd](https://www.google.com/search?q=https://github.com/pabloaguayogd)

---
