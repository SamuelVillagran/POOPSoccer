# ⚽ POOPSoccer (DOPOSoccer) — El *Head Soccer* Académico

![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=openjdk)
![GUI](https://img.shields.io/badge/Interfaz-Java%20Swing%20(60%20FPS)-blue?style=for-the-badge)
![Database](https://img.shields.io/badge/Persistencia-SQLite%20%2B%20Serializaci%C3%B3n%20.dat-003B57?style=for-the-badge&logo=sqlite)
![Testing](https://img.shields.io/badge/Pruebas-JUnit%205-25A162?style=for-the-badge)
![Architecture](https://img.shields.io/badge/Arquitectura-Presentaci%C3%B3n%20%7C%20Dominio%20%7C%20Persistencia-8A2BE2?style=for-the-badge)

> **Proyecto Final 2026-2 — Desarrollo Orientado por Objetos (Escuela Colombiana de Ingeniería Julio Garavito)**
> Adaptación de escritorio de *Head Soccer* en 2D con partidos locales en el mismo teclado, tiros de poder únicos por país, inteligencia artificial, progresión persistente de estadísticas en SQLite y guardado/carga de partidas.

---

## Tabla de Contenidos

1. [¿Qué es POOPSoccer?](#1-qu%C3%A9-es-poopsoccer)
2. [Controles, Personajes, Poderes y Estadísticas](#2-controles-personajes-poderes-y-estad%C3%ADsticas)
3. [Arquitectura y Ejecución](#3-arquitectura-y-ejecuci%C3%B3n)
4. [Matriz de Cobertura de Requisitos Funcionales (RF-01 a RF-13)](#4-matriz-de-cobertura-de-requisitos-funcionales-rf-01-a-rf-13)
5. [Historia y Pruebas de Aceptación: El Especial de Samuel y Juan (*"Pop Soccer"*)](#5-historia-y-pruebas-de-aceptaci%C3%B3n-el-especial-de-samuel-y-juan-pop-soccer)
   - [Acto I — Samuel contra la Máquina: Configuración, Errores, MvM y PvM (Video 1)](#acto-i--samuel-contra-la-m%C3%A1quina-configuraci%C3%B3n-errores-mvm-y-pvm)
   - [Acto II — Juan contra la Máquina (Parte 1): Tornado, Ilusión, Aturdimiento y Pausa (Video 2)](#acto-ii--juan-contra-la-m%C3%A1quina-parte-1-tornado-ilusi%C3%B3n-aturdimiento-y-pausa)
   - [Acto III — Juan contra la Máquina (Parte 2): Persistencia SQLite, Mejoras y Guardar/Abrir (Video 3)](#acto-iii--juan-contra-la-m%C3%A1quina-parte-2-persistencia-sqlite-mejoras-y-guardarabrir)
   - [Acto IV — Samuel vs. Juan: El Encuentro PvP, Partido por Tiempo y Muerte Súbita (Video 4)](#acto-iv--samuel-vs-juan-el-encuentro-pvp-partido-por-tiempo-y-muerte-s%C3%BAbita)
   - [Acto V — Samuel vs. Juan: La Gran Final, Partida Guardada, Premiación y Cierre (Video 5)](#acto-v--samuel-vs-juan-la-gran-final-partida-guardada-premiaci%C3%B3n-y-cierre)

---

## 1. ¿Qué es POOPSoccer?

**POOPSoccer** (también referenciado en la documentación del curso como **DOPOSoccer**, y rebautizado con cariño como *"Pop Soccer"* por quienes lo prueban por primera vez) lleva la frenética esencia arcade de *Head Soccer* (D&D Dream, 2012) del celular al computador:

- **Tres modalidades de juego:** `Jugador vs Jugador (PvP)`, `Jugador vs Máquina (PvM)` y `Máquina vs Máquina (MvM)`.
- **Dos tipos de partido:** `Por goles` (primero en llegar a `3`, `5` o `7` goles) y `Por tiempo` (`1`, `2`, `3` o `5` minutos, con **`¡MUERTE SÚBITA!`** automática en caso de empate al llegar el reloj a `0s`).
- **Tres resoluciones adaptativas:** `Pequeña (1/4 de pantalla)`, `Mediana (1/2 de pantalla)` y `Grande (3/4 de pantalla)`, escalando tanto la cancha (`800×600` lógico) como el HUD superior (`InfoPanel`).
- **Sistema de combate y aturdimiento:** Patadas cuerpo a cuerpo y tiros de poder que reducen la vida del rival; al llegar a `0`, el oponente queda **`¡ATURDIDO!`** por `3` segundos antes de recuperar su vida máxima.
- **Progresión persistente y partidas portables:** Los puntos, niveles de estadísticas e historial (`PJ` y `G`) se guardan en SQLite (`data/database/poopsoccer.db`), mientras que cualquier partido en curso puede guardarse y reabrirse en formato `.dat` (`data/saves/`).

---

## 2. Controles, Personajes, Poderes y Estadísticas

### Controles en el mismo teclado

| Acción | Jugador 1 (Izquierda) | Jugador 2 (Derecha) |
| :--- | :---: | :---: |
| **Mover a la izquierda** | `A` | `←` |
| **Mover a la derecha** | `D` | `→` |
| **Saltar** | `W` | `↑` |
| **Patear** | `S` | `↓` |
| **Activar Poder** | `Q` | `Ctrl` (Derecho) |
| **Pausar Partido** | `Esc` *(compartido)* | `Esc` *(compartido)* |

### Selecciones y Tiros de Poder

La barra de poder se llena en **`10` segundos** (y recibe un impulso instantáneo del **`+25%`** cada vez que el jugador concede un gol). Al presionar la tecla de poder con la barra llena (`100%`), el personaje obtiene un aura dorada/celeste y lanza su tiro especial al tocar el balón:

| País | Tiro de Poder | Comportamiento del Disparo | Efecto al Rival | Duración | Daño Base (`DañoTiro`) |
| :--- | :--- | :--- | :--- | :---: | :---: |
| **Corea del Sur** | `BlueAura` (Aura Azul) | Disparo recto y horizontal a máxima velocidad (`16 px/frame`), sin caída por gravedad. | `Push` — Empuja al rival hacia su propio arco. | `1.5 s` | `40` |
| **Camerún** | `Lightning` (Rayo) | Disparo recto eléctrico que ignora la gravedad. | `Electrocuted` — Impide saltar y reduce velocidad al `50%`. | `3.0 s` | `60` |
| **Nigeria** | `Tornado` | Remolino rasante que rebota rápido contra el césped. | `Unconscious` — Lanza al rival al aire e impide moverse, saltar o patear. | `3.0 s` | `40` |
| **Estados Unidos** | `Illusion` (Ilusión) | Divide el balón en **`8` balones idénticos** (`1` real + `7` señuelos visibles para ambos). | `Confusion` — Confunde al rival sobre cuál es el balón real. | `2.5 s` | `30` |
| **Colombia** *(extra)* | `Illusion` (Ilusión) | Selección adicional habilitada en el selector con el tiro de poder `Illusion`. | `Confusion` — Mismo efecto de múltiples balones. | `2.5 s` | `30` |

> **Reglas clave de poder:**
> - Los efectos no se acumulan: un nuevo impacto reemplaza el efecto anterior.
> - Si el tiro de poder entra al arco sin tocar al rival, el gol cuenta, pero no aplica daño ni efecto.
> - Si el balón con tiro de poder rebota en una pared lateral o en el travesaño, el aura del disparo se limpia para que siga su física natural.

### Estadísticas, Daño y Recompensas

- **Estadísticas mejorables (Nivel `1` a `10`):** `Vel` (*Speed*), `Salto` (*Jump*), `Patada` (*Kick*) y `Vida` (*Survival*).
- **Crecimiento por nivel:** Cada nivel sobre `1` suma `+10%`:
  - `multiplicador = 1 + 0.10 × (Nivel - 1)`
  - `VidaMax = 100 × (1 + 0.10 × (NivelVida - 1))` *(de `100` a `190` puntos de vida)*
  - `DañoPatada = 10 × multiplicadorKick` *(solo hace daño si alcanza al rival y no toca el balón)*
  - `DañoPoder = DañoTiro × multiplicadorKick`
- **Costos de mejora:** Inician en `500` puntos y se duplican en cada nivel (`500`, `1000`, `2000`, ... según `500 × 2^(nivel - 1)`).
- **Puntos por partido:** **`+100` puntos** al ganador y **`+25` puntos** al perdedor.

---

## 3. Arquitectura y Ejecución

```text
POOPSoccer/
├── Docs/                          # Especificación oficial, diagramas (.asta) y documento técnico
├── data/
│   ├── database/poopsoccer.db     # Base de datos SQLite de perfiles, puntos e historial
│   ├── logs/POOPSoccer_log.txt    # Log de excepciones legible para desarrolladores
│   └── saves/*.dat                # Partidas guardadas mediante serialización
└── POOPSoccer/
    ├── lib/
    │   ├── Jugar_POOPSoccer.bat   # Lanzador directo en Windows (Java 17+)
    │   ├── POOPSoccer.jar         # Ejecutable empaquetado
    │   └── sqlite-jdbc-3.54.1.0.jar
    ├── resources/                 # Sprites por país, balón, arcos, estadio, menús y audio WAV
    └── src/
        ├── presentation/          # Swing UI, GamePanel (60 FPS), InfoPanel, PlayerConfig, PauseDialog, MatchSummaryDialog
        ├── domain/                # Fachada POOPSoccer, Player, Ball, Power/Effect, Machine, ProfileManager, Log
        ├── persistence/           # SQLitePlayerDatabase (registrado vía SPI en META-INF/services)
        └── test/                  # Pruebas unitarias JUnit 5 (POOPSoccerTest.java)
```

Para ejecutar directamente en Windows:
- Abrir `POOPSoccer/lib/Jugar_POOPSoccer.bat` (requiere Java 17 o superior) o ejecutar la clase principal `presentation.POOPSoccerGUI`.

---

## 4. Matriz de Cobertura de Requisitos Funcionales (RF-01 a RF-13)

Cada requisito funcional de la especificación del proyecto (`Docs/!DOPO-FIN-2026-02.pdf`) queda cubierto y demostrado en al menos uno de los **5 videos de pruebas de aceptación**:

| Código | Requisito Funcional | Evidencia en Video(s) | Qué se verifica en pantalla |
| :--- | :--- | :---: | :--- |
| **RF-01** | Seleccionar modalidad (`PvP`, `PvM`, `MvM`) y perfil de máquina | Video 1, Video 2, Video 4 | Botones de `MenuPanel`, selector `Perfil de máquina:` (`Torpe` activo; aviso de perfiles en construcción). |
| **RF-02** | Seleccionar resolución de cancha al iniciar | Video 1, Video 4 | Cambio entre `Pequeña (1/4 de pantalla)`, `Mediana (1/2 de pantalla)` y `Grande (3/4 de pantalla)`. |
| **RF-03** | Elegir personaje (país) por jugador | Video 1, Video 2, Video 4 | Combo `País / Poder:` (`Corea del Sur`, `Camerún`, `Nigeria`, `Estados Unidos`, `Colombia`) con sprites y banderas. |
| **RF-04** | Seleccionar tipo de partido (`Por tiempo` / `Por goles`) y su límite | Video 1, Video 4, Video 5 | Selector dinámico entre `Goles para ganar:` (`3`, `5`, `7`) y `Minutos del partido:` (`1`, `2`, `3`, `5`), incluyendo `¡MUERTE SÚBITA!`. |
| **RF-05** | Realizar movimientos: mover, saltar, patear y activar poder | Video 1, Video 2, Video 4 | Controles P1 (`A`, `D`, `W`, `S`, `Q`) y P2 (`←`, `→`, `↑`, `↓`, `Ctrl`), física, daño, aturdimiento de `3 s`, recarga de `10 s` / `+25%` y los 4 poderes únicos. |
| **RF-06** | Ilustrar permanentemente estado del juego e información de jugadores | Video 1, Video 2, Video 4 | `InfoPanel` superior: banderas, nombres, vida (verde/amarillo/rojo), poder, poder activo, niveles (`Vel/Sal/Pat/Vid`), marcador y reloj. |
| **RF-07** | Pausar momentáneamente el juego | Video 2, Video 4 | Tecla `Esc` abre el diálogo modal `JUEGO EN PAUSA` con marcador actual y botones `Continuar`, `Reiniciar` y `Volver al menú`. |
| **RF-08** | Terminar el juego en cualquier momento | Video 1, Video 2, Video 5 | `Volver al menú` desde pausa, `Archivo -> Nuevo` y `Archivo -> Salir` (con diálogo `"¿Desea salir del juego?"`). |
| **RF-09** | Notificar ganador y puntos obtenidos al finalizar | Video 1, Video 5 | Diálogo `FIN DEL PARTIDO` con trofeo dorado, marcador final, ganador (`+100 pts`) y perdedor (`+25 pts`). |
| **RF-10** | Mejorar estadísticas de cada jugador | Video 3 | Botones `Vel Nv.X (+)`, `Salto Nv.X (+)`, `Patada Nv.X (+)`, `Vida Nv.X (+)` descontando `500`, `1000`, `2000` pts. |
| **RF-11** | Salvar y abrir el estado de un partido | Video 3, Video 5 | Menú `Archivo -> Guardar` (`.dat` en `data/saves/`) y `Archivo -> Abrir` restaurando marcador, posiciones, reloj y vida. |
| **RF-12** | Mantener progreso de jugadores (puntos y estadísticas) | Video 3, Video 4, Video 5 | Al escribir el nombre del jugador en `PlayerConfig`, SQLite recarga sus `Puntos`, `PJ`, `G` y niveles guardados. |
| **RF-13** | Presentar mensajes claros y detallados en caso de error | Video 1, Video 3 | Validaciones: nombres `< 3` letras, perfil de máquina en construcción, puntos insuficientes para mejora y error al abrir `.dat` inválido (`POOPSoccerException` + `POOPSoccer_log.txt`). |

---

## 5. Historia y Pruebas de Aceptación: El Especial de Samuel y Juan (*"Pop Soccer"*)

### Prólogo: La serie en el canal de Samuel

Todo empezó un viernes por la noche cuando **Samuel** decidió lanzar en su canal de YouTube una serie especial probando una joya académica en Java: la versión universitaria de *Head Soccer* para PC llamada **POOPSoccer** —aunque su primo **Juan**, convencido de que el nombre era demasiado serio, insistió en llamarlo *"Pop Soccer"* durante toda la aventura.

Para que la prueba fuera justa y cubriera cada rincón del juego, armaron el especial por etapas:
1. **Primero, cada uno por su cuenta contra la máquina:** En el **Acto I**, **Samuel** graba solo explorando los menús, las validaciones de error, las resoluciones, el modo espectador de robots y sus primeros partidos contra la CPU. Luego, en los **Actos II y III**, **Juan** graba su propia sesión individual contra la máquina para poner a prueba los poderes más caóticos, el aturdimiento por golpes, la pausa, el sistema de mejoras y el guardado de partidas.
2. **Después, el duelo frente a frente:** Con todas las funcionalidades individuales dominadas, en los **Actos IV y V** ambos se "encuentran en la misma cancha" —una forma elegante de decir que, aunque el juego es **100% local**, se sientan codo con codo frente al mismo teclado— para disputar el esperado **Jugador vs Jugador (PvP)** para el cierre del canal.

Luz, cámara, Java Swing... ¡comienzan las **Pruebas de Aceptación**!

---

### Acto I — Samuel contra la Máquina: Configuración, Errores, MvM y PvM

> **En escena:** Solamente **Samuel**, grabando desde su escritorio para abrir el especial del canal mientras Juan termina de llegar a su turno.
> **Requisitos cubiertos:** `RF-01`, `RF-02`, `RF-03`, `RF-04`, `RF-05`, `RF-06`, `RF-08`, `RF-09`, `RF-13`.

**Qué se muestra en pantalla durante la grabación:**
1. Se inicia la aplicación mostrando el menú principal (`MenuPanel`) con música de fondo y los tres modos (`PvP`, `PvM`, `MvM`).
2. Samuel entra a **`Jugador vs Máquina (PvM)`** para probar las validaciones de error (`RF-13`):
   - Escribe un nombre corto (`"Sa"`) y pulsa `Iniciar Partido` → aparece el diálogo: `"El nombre de cada jugador debe tener al menos 3 letras"`.
   - Corrige a `"Samuel"`, selecciona un perfil de máquina distinto a `Torpe` (`Ofensiva`, `Defensiva` o `Experta`) y pulsa `Iniciar Partido` → aparece el aviso: `"Tipo de máquina en construcción, elija máquina torpe"`.
3. Prueba el selector **`Resolución de la cancha:`** (`RF-02`) iniciando brevemente en `Pequeña (1/4 de pantalla)` y saliendo con `Archivo -> Nuevo` o `Esc -> Volver al menú` (`RF-08`), y luego entra a **`Máquina vs Máquina (MvM)`** en resolución `Grande (3/4 de pantalla)` dejando jugar unos segundos a dos `CPU Torpe` antes de volver al menú.
4. Regresa a **`Jugador vs Máquina (PvM)`** en resolución `Mediana (1/2 de pantalla)`, selecciona **`Por goles`** (`3` goles), país **`Corea del Sur`** contra `CPU Torpe` (`Camerún`), y demuestra:
   - El **HUD permanente (`InfoPanel`)** con banderas, `"Samuel (Corea del Sur)"`, vida verde, barra de poder cargando en `10` segundos (y el bono de **`+25%`** al recibir un gol) y resumen `Vel:1 Sal:1 Pat:1 Vid:1`.
   - Movimientos (`A`, `D`, `W`, `S`), activación con `Q` (`"Poder activo: BlueAura"`) y el disparo **`Aura Azul`** empujando (`Push`) a la máquina hacia su arco (`1.5 s`, `40` de daño).
   - Una muestra rápida con **`Camerún`** lanzando el **`Rayo`** (`Lightning`) para dejar a la CPU con `Electrocuted` (sin saltar y al `50%` de velocidad por `3 s`, `60` de daño).
5. Completa los `3` goles para mostrar el diálogo modal **`FIN DEL PARTIDO`** con el trofeo dorado, el marcador, `"¡Ganador: Samuel (+100 pts | Total: ... pts)!"` y `"Perdedor: CPU Torpe (+25 pts | Total: ... pts)"`.

**Guion / Diálogo (Solo Samuel):**
> **Samuel:** ¡Qué pasa, gente! Bienvenidos a un nuevo video del canal. Hoy me toca arrancar en solitario probando **POOPSoccer**, la versión académica en Java de *Head Soccer* —aunque mi primo Juan insista por chat en llamarlo *"Pop Soccer"*—. Antes de que él venga a retarme, voy a testear todo el juego por mi cuenta contra la máquina.
>
> Entro primero a **Jugador vs Máquina** y vamos a ver si aguanta pruebas de usuario curioso: pongo solo `"Sa"` en mi nombre y le doy **Iniciar Partido**... ¡ajá! *"El nombre de cada jugador debe tener al menos 3 letras"*. Ahora pongo `"Samuel"` y trato de retar a la máquina **Experta**... *"Tipo de máquina en construcción, elija máquina torpe"*. Perfecto, mensajes claros y sin romperse.
>
> Probemos también las resoluciones: aquí ven la **Pequeña (1/4)** —ideal para jugar escondido en clase—, salgo en cualquier momento con **Archivo → Nuevo**, y en **Máquina vs Máquina** con pantalla **Grande (3/4)** dejo que dos **CPU Torpe** se agarren a patadas solitas mientras preparo mi debut.
>
> Ahora sí, me toca a mí en **Jugador vs Máquina (Mediana)** a **3 goles** con **Corea del Sur**. Miren arriba el HUD completo: bandera, barras de vida y poder —que recarga en **10 segundos**, o sube **+25%** si me meten gol por consolación— y mis niveles iniciales. Me muevo con `A` y `D`, salto con `W`, pateo con `S` y apenas se pone dorada la barra presiono `Q`... ¡toma **Aura Azul**! Empujón directo contra su propio arco. Y miren con **Camerún**: ¡**Rayo** directo y la CPU queda **Electrocuted**, sin poder saltar y caminando en cámara lenta!
>
> ¡Tercer gol y victoria! Trofeo en **FIN DEL PARTIDO**, **+100 puntos** para Samuel y **+25** para la máquina. Le dejo la cancha libre a Juan para que pruebe su parte antes de nuestro gran Versus.

**Video 1:**
(Aquí va el link)

---

### Acto II — Juan contra la Máquina (Parte 1): Tornado, Ilusión, Aturdimiento y Pausa

> **En escena:** Solamente **Juan**, grabando aparte su primera sesión individual contra la máquina.
> **Requisitos cubiertos:** `RF-01`, `RF-03`, `RF-05`, `RF-06`, `RF-07`, `RF-08`.

**Qué se muestra en pantalla durante la grabación:**
1. En `Jugador vs Máquina (PvM)`, Juan escribe `"Juan"` como Jugador 1 y elige **`Nigeria`** contra la `CPU Torpe` (`Estados Unidos`).
2. Activa su barra con `Q` y dispara el **`Tornado`** de Nigeria: el remolino rebota a ras de césped y lanza a la máquina por los aires con la etiqueta **`Unconscious`** durante `3 s` (`40` de daño), impidiéndole moverse, saltar o patear.
3. En un segundo intento elige **`Estados Unidos`** (o `Colombia`) para disparar **`Illusion`**: en la cancha aparecen **`8` balones idénticos** (`1` real + `7` señuelos) durante `2.5 s` (`Confusion`), desapareciendo los falsos al terminar el efecto.
4. Juan acorrala a la máquina y conecta **patadas cuerpo a cuerpo (`S`)** (sin tocar el balón) hasta bajar su barra de vida de verde → amarillo → rojo → `0`, mostrando el tinte rojo sobre la CPU y la etiqueta **`¡ATURDIDO!`** durante `3` segundos hasta que recupera el `100%` de vida.
5. En pleno partido presiona **`Esc`** para abrir **`JUEGO EN PAUSA`**, muestra el marcador congelado y prueba las opciones `Continuar`, `Reiniciar` y finalmente `Volver al menú` (`RF-08`).

**Guion / Diálogo (Solo Juan):**
> **Juan:** ¡Hola gente del canal de Samuel! Aquí habla Juan, el verdadero pro de la familia. Ya vi que mi primo se creyó comentarista deportivo en el video anterior, así que ahora estoy solo frente al computador probando mi propia cuenta en este *"Pop Soccer"* —sí, Samuel, le voy a seguir diciendo así—.
>
> Entro en **Jugador vs Máquina** como `"Juan"` y me pido a **Nigeria**. Cargo la barra de poder, presiono `Q`, le pego al balón y... ¡JAJAJA, miren ese **Tornado** rebotando por el piso! Mandó a volar a la máquina como costal y la dejó **Unconscious** tres segundos en el aire.
>
> Ahora vamos con **Estados Unidos**, que me dijeron que es puro engaño visual. Disparo **Ilusión** y... ¡¿PERO QUÉ ES ESTO?! ¡OCHO BALONES EN LA CANCHA AL MISMO TIEMPO! Ni yo sé cuál patear, aunque me duró 2.5 segundos la fiesta antes de que desaparecieran los siete falsos.
>
> Esperen, que descubrí otro truco: si me acerco sin balón y le doy patadas normales con `S` a la pobre CPU... miren esa barra de vida bajando de verde a amarillo, rojo y... ¡KNOCKOUT! Le salió **`¡ATURDIDO!`** en rojo encima. Tres segundos tiesa sin poder moverse y después revive con toda la vida.
>
> Y por si me llaman a cenar en mitad de la remontada: presiono **Esc**, aparece **JUEGO EN PAUSA** con el marcador congelado. Puedo dar **Continuar**, **Reiniciar** si voy perdiendo por goleada, o **Volver al menú** cuando quiera. De lujo, ahora sigo en otro video con las mejoras y el guardado para llegar mamadísimo contra Samuel.

**Video 2:**
(Aquí va el link)

---

### Acto III — Juan contra la Máquina (Parte 2): Persistencia SQLite, Mejoras y Guardar/Abrir

> **En escena:** Solamente **Juan**, cerrando la etapa de pruebas individuales contra la máquina y dejando su perfil listo antes del clásico contra Samuel.
> **Requisitos cubiertos:** `RF-10`, `RF-11`, `RF-12`, `RF-13`.

**Qué se muestra en pantalla durante la grabación:**
1. **Persistencia automática en SQLite (`RF-12`):** En `PlayerConfig`, al escribir `"Juan"`, la píldora superior muestra sus puntos acumulados, `PJ` (Partidos Jugados) y `G` (Ganados) guardados en `data/database/poopsoccer.db`. Cambia temporalmente a `"NuevoJugador"` para confirmar que inicia en `0` pts y nivel `1`, y al volver a escribir `"Juan"` recupera su progreso intacto.
2. **Validación y compra de mejoras (`RF-10`, `RF-13`):**
   - Si todavía tiene menos de `500` puntos, pulsa un botón de mejora (ej. `Patada Nv.1 (+)`) y muestra el mensaje claro: `"Necesitás 500 puntos (o ya está al máximo nivel 10)."`.
   - Ya con `≥ 500` puntos acumulados en sus partidas contra la máquina, compra una mejora (ej. `Patada Nv.1 (+)` → `Patada Nv.2 (+)` o `Vel Nv.1 (+)` → `Vel Nv.2 (+)`), mostrando el descuento de `500` pts, cómo el siguiente nivel sube de costo a `1000` pts y cómo el HUD del partido muestra el nivel actualizado (`Pat:2` o `Vel:2`).
3. **Guardar, abrir y manejo de errores de archivo (`RF-11`, `RF-13`):**
   - Durante un partido contra la máquina con marcador en curso (ej. `1 - 0` o `2 - 1`), abre **`Archivo -> Guardar`**, guarda `entrenamiento_juan.dat` en `data/saves/` y verifica el aviso `"Partida guardada en: entrenamiento_juan.dat"`.
   - Inicia o reinicia otro partido en `0 - 0`, luego abre **`Archivo -> Abrir`**, selecciona `entrenamiento_juan.dat` y comprueba que la cancha regresa al marcador y posiciones guardadas.
   - Finalmente, intenta abrir un archivo inválido/corrupto desde `Archivo -> Abrir` para mostrar el cuadro de error (`POOPSoccerException`) y mencionar su registro automático en `data/logs/POOPSoccer_log.txt`.

**Guion / Diálogo (Solo Juan):**
> **Juan:** Última sesión de entrenamiento individual antes de verle la cara a Samuel. Miren esta maravilla en la pantalla de configuración: apenas escribo `"Juan"`, la base de datos SQLite me carga automáticamente mis puntos, partidos jugados y victorias de las palizas que le di a la CPU. Si pongo `"NuevoJugador"`, arranca en cero; vuelvo a escribir `"Juan"` y todo regresa intacto.
>
> Ahora vamos a la tienda de mejoras. Cuando tenía menos de **500 puntos** e intenté subir **Patada**, el juego me frenó en seco: *"Necesitás 500 puntos (o ya está al máximo nivel 10)"*. Pero como ya junté los puntos jugando contra la máquina... ¡clic en **Patada Nv.1 (+)**! Descuento de **500**, ya soy nivel 2 y el siguiente nivel cuesta **1000** —el doble, pura inflación gamer—. Y al entrar al partido, arriba en el HUD ya presume mi `Pat:2`.
>
> También probé qué pasa si toca cerrar el juego a mitad de un partidazo: menú **`Archivo → Guardar`**, lo guardo como `entrenamiento_juan.dat` y listo. Reinicio otro partido en `0 - 0` para comprobar que no es truco... voy a **`Archivo → Abrir`**, elijo `entrenamiento_juan.dat` y ¡zas!, aparece exactamente donde lo dejé. Y si algún despistado intenta abrir un archivo que no sirve, sale el error detallado de `POOPSoccerException` y queda guardado el reporte en `POOPSoccer_log.txt`.
>
> Con todo probado contra la máquina, ahora sí: **Samuel, prepara el teclado que llegó la hora del Versus.**

**Video 3:**
(Aquí va el link)

---

### Acto IV — Samuel vs. Juan: El Encuentro PvP, Partido por Tiempo y Muerte Súbita

> **En escena:** **Samuel y Juan juntos** frente al mismo computador, iniciando la fase **Jugador vs Jugador (PvP)** después de haber completado sus pruebas individuales contra la máquina.
> **Requisitos cubiertos:** `RF-01`, `RF-02`, `RF-03`, `RF-04`, `RF-05`, `RF-06`, `RF-07`, `RF-12`.

**Qué se muestra en pantalla durante la grabación:**
1. En `MenuPanel`, seleccionan **`Jugador vs Jugador (PvP)`**.
2. Configuran ambos perfiles humanos en `PlayerConfig`: **Samuel** (Jugador 1, izquierda, `WASD + Q`, ej. `Camerún` o `Corea del Sur`) y **Juan** (Jugador 2, derecha, `Flechas + Ctrl Derecho`, ej. `Nigeria` o `Estados Unidos`), comprobando que SQLite carga el historial y mejoras de los dos (`RF-12`).
3. Seleccionan `Tipo de partido:` = **`Por tiempo`**, `Minutos del partido:` = **`1`**, y `Resolución de la cancha:` = **`Grande (3/4 de pantalla)`**.
4. Durante el minuto reglamentario demuestran a ambos jugadores actuando simultáneamente en el mismo teclado: saltos, patadas, choques sin atravesarse, reemplazo de efectos cuando impactan poderes seguidos, un disparo de poder que entra directo sin tocar al rival (cuenta el gol sin daño ni efecto) y una pausa rápida con `Esc`.
5. Mantienen el marcador empatado (ej. `1 - 1` o `2 - 2`) hasta que el cronómetro marca `0s` → el centro del HUD muestra **`¡MUERTE SÚBITA!`** en rojo y continúan jugando hasta el gol de oro ganador.

**Guion / Diálogo (Samuel vs. Juan):**
> **Samuel:** ¡Ahora sí, gente! Después de que cada uno entrenó y probó todo por su lado contra la máquina, llegó el duelo que pedía el canal: **Samuel vs. Juan** en **Jugador vs Jugador**.
>
> **Juan:** *(riéndose junto al teclado)* Hay que contarle la verdad a la audiencia: como este juegazo es local, "encontrarnos en partida" significa que estamos apretados hombro con hombro en el **mismo teclado**. Tú vas por la izquierda con **`WASD` y `Q`**, y yo defiendo la derecha con las **Flechas y `Ctrl`**. ¡Cuidado me sacas un dedo de lugar!
>
> **Samuel:** Mira cómo apenas escribimos `"Samuel"` y `"Juan"`, SQLite nos cargó a ambos nuestros puntos y niveles reales. Vamos a ponerlo **Por tiempo** a **1 minuto** en resolución **Grande**. ¡Empieza el clásico familiar!
>
> **Juan:** ¡Toma **Tornado**! ¡Uy, qué atajada! ¡Ey, no vale pegarme con **Rayo** encima mientras sigo mareado, mira cómo me reemplazó el efecto! ¡Aguanta que empatamos `1 - 1` a diez segundos del final!
>
> **Samuel:** Pausa táctica de emergencia... no mentira, era solo para tomar aire con `Esc` y darle **Continuar**. `3... 2... 1...` ¡CERO SEGUNDOS! ¡Miren arriba en rojo: **`¡MUERTE SÚBITA!`**! El primero que la meta gana... ¡disparo cruzado, rebote... Y GOOOOL!

**Video 4:**
(Aquí va el link)

---

### Acto V — Samuel vs. Juan: La Gran Final, Partida Guardada, Premiación y Cierre

> **En escena:** **Samuel y Juan juntos**, jugando la revancha definitiva del especial y cerrando la aplicación.
> **Requisitos cubiertos:** `RF-04`, `RF-08`, `RF-09`, `RF-11`, `RF-12`.

**Qué se muestra en pantalla durante la grabación:**
1. Retoman una partida PvP decisiva guardada en `.dat` mediante **`Archivo -> Abrir`** (o guardan un instante tenso de la final con `Archivo -> Guardar` y lo reabren para demostrar portabilidad en `PvP`).
2. Definen el partido final hasta que aparece el diálogo modal **`FIN DEL PARTIDO`**, verificando en pantalla el trofeo dorado, el marcador final, el ganador con **`+100 pts`** y su total actualizado, y el perdedor con **`+25 pts`** y su total actualizado (`RF-09`).
3. Regresan al menú y entran un momento a `PlayerConfig` para confirmar que SQLite guardó el nuevo puntaje, `PJ` y `G` de `"Samuel"` y `"Juan"` (`RF-12`).
4. Cierran la aplicación desde **`Archivo -> Salir`**, mostrando el cuadro de confirmación `"¿Desea salir del juego?"` (`RF-08`).

**Guion / Diálogo (Samuel vs. Juan):**
> **Juan:** ¡Eso en muerte súbita fue pura suerte de principiante, primo! Menos mal guardamos la revancha del clásico en un `.dat`. Voy a **`Archivo → Abrir`**, cargamos nuestra partida decisiva y aquí se define quién manda en la casa.
>
> **Samuel:** ¡Dale, sin piedad! Balón dividido en el centro, salto al límite, patada al ángulo... ¡y adentro!
>
> **Juan:** Ahí está el cartelazo de **FIN DEL PARTIDO**: trofeo dorado en pantalla, **+100 puntos** para el campeón del duelo y **+25 puntos** para el que al menos dio espectáculo.
>
> **Samuel:** Entramos un segundo a configuración y confirmamos que la base de datos SQLite ya sumó el partido jugado, la victoria y los puntos nuevos para los dos. ¡Con esto quedan probados todos los requisitos de **POOPSoccer** de principio a fin!
>
> **Juan:** Si les gustó la serie —y sobre todo el bautizo oficial como *"Pop Soccer"*—, dejen su like y suscríbanse al canal de Samuel. Me voy a **`Archivo → Salir`**, acepto `"¿Desea salir del juego?"` y... ¡corte!

**Video 5:**
(Aquí va el link)

---

*Este README fue elaborado por Antigravity (IA).*
