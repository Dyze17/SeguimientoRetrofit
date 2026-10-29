# Pokédex · Consumo de APIs REST en Android

Aplicación Android en Jetpack Compose que consume [PokeAPI](https://pokeapi.co/) con Retrofit. Muestra la lista de
Pokémon con scroll infinito, permite buscarlos por nombre o número y filtrarlos por tipo y por generación, y abre un
detalle con descripción, datos, habilidades y estadísticas base. Un indicador en la barra superior muestra si el
teléfono está en línea o sin conexión.

## Funcionalidades

| Requisito de la actividad | Cómo se cumple |
|---|---|
| Consumir al menos cuatro endpoints con Retrofit | Siete endpoints de PokeAPI en `PokeApiService` (tabla de abajo) |
| Mapear los datos a modelos en Kotlin | DTO con `kotlinx.serialization` (`data/remote/dto`) → modelos del dominio (`domain/model`) con `PokemonMappers` |
| Lista con información básica | Cuadrícula con número, nombre e imagen oficial de cada Pokémon |
| Buscar o filtrar con al menos dos campos | **Tipo** (`/type/{name}`), **generación** (`/generation/{id}`) y **nombre o número** (índice de `/pokemon`). Se pueden combinar |
| Pantalla de detalle con otro endpoint | `/pokemon/{id}` y `/pokemon-species/{id}` |
| Indicador de conexión (online/offline) | Chip «En línea / Sin conexión» en las dos pantallas y franja de aviso sin conexión (`ConnectivityManager`) |
| Paginación con scroll infinito | Paging 3: `PokemonPagingSource` (offset/limit en la API) y `FilteredPokemonPagingSource` (resultados filtrados, de 20 en 20) |
| Manejo de errores de red | Errores traducidos a mensajes claros, botón «Reintentar», reintento automático al volver la conexión |

## API: PokeAPI v2

PokeAPI es una API REST pública y gratuita con la información de los juegos de Pokémon. No pide registro ni clave.
Todas las peticiones son **GET** sobre `https://pokeapi.co/api/v2/` y responden **JSON**. Los listados devuelven
`count`, `next`, `previous` y `results` (pares `name` + `url`, donde la URL termina en el id del recurso).

| Endpoint | Parámetros | Respuesta | Uso en la app |
|---|---|---|---|
| `GET /pokemon` | `offset`, `limit` (query) | Lista paginada de Pokémon | Scroll infinito sin filtros; con `limit` alto, índice para buscar por nombre |
| `GET /pokemon/{id}` | `id` (path) | Tipos, estadísticas, habilidades, altura, peso, imágenes | Pantalla de detalle |
| `GET /pokemon-species/{id}` | `id` (path) | Descripción, categoría, hábitat, generación, ratio de captura | Pantalla de detalle (textos en español) |
| `GET /type` | `limit` (query) | Lista de tipos | Opciones del filtro por tipo |
| `GET /type/{name}` | `name` (path) | Los Pokémon de ese tipo | Filtro por tipo |
| `GET /generation` | `limit` (query) | Lista de generaciones | Opciones del filtro por generación |
| `GET /generation/{id}` | `id` (path) | Las especies de esa generación | Filtro por generación |

Las imágenes vienen del repositorio de sprites de PokeAPI y se cargan con Coil.

## Arquitectura

Arquitectura en capas con inyección de dependencias con Hilt:

```
app/src/main/java/co/edu/uniquindio/pokeapi/
├── ui/                   UI con Jetpack Compose
│   ├── list/             Lista, búsqueda, filtros y su ViewModel
│   ├── detail/           Detalle y su ViewModel
│   ├── components/       Indicador de conexión, estados de carga y error, imágenes, nombres en español
│   ├── navigation/       Rutas con tipos (Navigation Compose)
│   └── theme/            Colores de Material 3
├── domain/               Modelos y contratos, sin dependencias de Android
│   ├── model/            PokemonSummary, PokemonDetail, PokemonFilter, AppError…
│   └── repository/       PokemonRepository, NetworkMonitor
├── data/                 Implementación del acceso a datos
│   ├── remote/           PokeApiService (Retrofit), DTO y traducción de errores
│   ├── paging/           PagingSource de la lista y de los filtros
│   ├── mapper/           DTO → modelos del dominio
│   ├── repository/       PokemonRepositoryImpl
│   └── network/          ConnectivityNetworkMonitor
└── di/                   Módulos de Hilt: Retrofit/OkHttp y enlaces de interfaces
```

- **UI → ViewModel:** las pantallas solo leen `StateFlow` y llaman funciones del ViewModel.
- **ViewModel → Repository:** el ViewModel arma el filtro (con espera de 350 ms al escribir) y pide las páginas al
  repositorio. No sabe nada de Retrofit.
- **Repository → API:** el repositorio decide qué endpoint usar, cruza tipo y generación por número, guarda en
  memoria las listas que ya pidió y convierte las excepciones de red y HTTP en `AppError`.

## Tecnologías

Kotlin 2.4 · Jetpack Compose (Material 3) · Navigation Compose · Hilt · Retrofit 3 + kotlinx.serialization ·
OkHttp · Paging 3 · Coil 3 · Coroutines y Flow.

## Cómo ejecutarla

1. Abrir la carpeta en Android Studio (2026.1 o posterior) y esperar la sincronización de Gradle.
2. Ejecutar la configuración `app` en un emulador o teléfono con Android 9 (API 28) o posterior.

Desde la terminal:

```bash
./gradlew assembleDebug        # APK en app/build/outputs/apk/debug/
./gradlew testDebugUnitTest    # pruebas unitarias
```

Las pruebas unitarias cubren el cruce de filtros, la búsqueda, la paginación de las dos fuentes, el mapeo de los
textos en español y la traducción de errores.

## Recursos de terceros

Los iconos de `res/drawable/ic_*.xml` (salvo `ic_launcher_foreground`) son
[Material Symbols Rounded](https://github.com/google/material-design-icons) de Google, con licencia Apache 2.0.
Los datos y las imágenes de los Pokémon vienen de PokeAPI.
