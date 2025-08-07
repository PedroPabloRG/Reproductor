# Reproductor de Música en Java

Un reproductor de música moderno desarrollado en Java utilizando JavaFX para la interfaz gráfica.

## Características

- 🎵 Reproducción de múltiples formatos de audio (MP3, WAV, FLAC, OGG, M4A, AAC)
- 🎨 Interfaz gráfica moderna y responsive
- 📚 Gestión de biblioteca musical con metadatos
- 📝 Creación y gestión de listas de reproducción
- 🎤 Editor de letras con almacenamiento en base de datos
- 🔊 Controles de volumen y progreso
- 🔀 Modo aleatorio y repetición
- 🔍 Búsqueda y filtrado de canciones
- 💾 Persistencia de datos con SQLite

## Requerimientos del Sistema

- **Java**: JDK 17 o superior
- **Maven**: 3.6 o superior
- **Sistema Operativo**: Windows, macOS, Linux

## Instalación y Ejecución

### 1. Clonar el repositorio
```bash
git clone https://github.com/PedroPabloRG/Reproductor.git
cd Reproductor
```

### 2. Compilar el proyecto
```bash
mvn clean compile
```

### 3. Ejecutar la aplicación
```bash
mvn javafx:run
```

### 4. Ejecutar tests
```bash
mvn test
```

### 5. Generar JAR ejecutable
```bash
mvn clean package
```

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/
│   │   └── com/reproductormusica/
│   │       ├── Main.java                 # Clase principal
│   │       ├── model/                    # Modelos de datos
│   │       │   ├── Song.java
│   │       │   ├── Playlist.java
│   │       │   ├── PlaybackState.java
│   │       │   └── RepeatMode.java
│   │       ├── controller/               # Lógica de negocio
│   │       │   └── MainController.java
│   │       ├── view/                     # Interfaz gráfica
│   │       │   └── MainWindow.java
│   │       ├── audio/                    # Manejo de audio
│   │       │   └── AudioPlayer.java
│   │       └── utils/                    # Utilidades
│   │           ├── DatabaseManager.java
│   │           └── MetadataExtractor.java
│   └── resources/
│       ├── styles/
│       │   └── main.css                  # Estilos CSS
│       ├── application.properties        # Configuración
│       └── log4j2.properties            # Configuración de logging
└── test/
    └── java/                            # Tests unitarios
        └── com/reproductormusica/
            ├── model/
            └── controller/
```

## Tecnologías Utilizadas

### Core
- **Java 17**: Lenguaje de programación principal
- **JavaFX 17**: Framework para la interfaz gráfica
- **Maven**: Sistema de gestión de dependencias

### Audio
- **JavaFX Media API**: Reproducción nativa de audio
- **JLayer**: Soporte específico para MP3
- **JAudioTagger**: Extracción y manipulación de metadatos

### Persistencia
- **SQLite JDBC**: Base de datos embebida para almacenamiento

### Testing
- **JUnit 5**: Framework de testing unitario

### Logging
- **SLF4J + Logback**: Sistema de logging

## Uso de la Aplicación

### 1. Importar Música
- Ve a `Archivo > Importar archivos...` o `Archivo > Importar carpeta...`
- Selecciona los archivos de audio o carpetas que deseas agregar
- Los metadatos se extraerán automáticamente

### 2. Reproducir Música
- Haz doble clic en cualquier canción de la biblioteca
- Usa los controles de reproducción en la parte inferior
- Ajusta el volumen con el deslizador lateral

### 3. Gestionar Listas de Reproducción
- Ve a `Lista de reproducción > Nueva lista...`
- Arrastra canciones desde la biblioteca a las listas
- Gestiona el orden dentro de las listas

### 4. Búsqueda
- Usa el campo de búsqueda en la pestaña "Biblioteca"
- Filtra por título, artista, álbum o género

## Configuración

La aplicación utiliza el archivo `application.properties` para la configuración:

```properties
# Configuración de audio
audio.default.volume=0.5
audio.fade.duration=500

# Configuración de la biblioteca
library.scan.subdirectories=true
library.supported.formats=mp3,wav,flac,ogg,m4a,aac

# Configuración de interfaz
ui.theme=default
ui.language=es
```

## Desarrollo

### Agregar Nuevas Características

1. **Nuevos formatos de audio**: Agregar soporte en `MetadataExtractor.java`
2. **Nuevos temas**: Crear archivos CSS en `src/main/resources/styles/`
3. **Nuevas funcionalidades**: Extender `MainController.java` y `MainWindow.java`

### Estructura de Base de Datos

La aplicación utiliza SQLite con las siguientes tablas:

- `songs`: Información de canciones y metadatos
- `playlists`: Información de listas de reproducción
- `playlist_songs`: Relación muchos-a-muchos entre listas y canciones

## Contribución

1. Fork el proyecto
2. Crea una rama para tu característica (`git checkout -b feature/nueva-caracteristica`)
3. Commit tus cambios (`git commit -am 'Agregar nueva característica'`)
4. Push a la rama (`git push origin feature/nueva-caracteristica`)
5. Crea un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## Problemas Conocidos

- La reproducción de algunos formatos puede requerir codecs adicionales
- El análisis de metadatos puede ser lento para bibliotecas muy grandes
- JavaFX Media no soporta todos los formatos de audio en todas las plataformas

## Soporte

Si encuentras algún problema o tienes sugerencias:

1. Revisa los [issues existentes](../../issues)
2. Crea un nuevo issue con detalles del problema
3. Incluye logs y información del sistema

## Roadmap

- [ ] Soporte para streaming online
- [ ] Ecualizador gráfico
- [ ] Sincronización con servicios en la nube
- [ ] Plugins y extensiones
- [ ] Modo oscuro
- [ ] Soporte para múltiples idiomas
