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
   - [Acto I — Samuel toma el mando: Configuración, Errores, MvM y PvM (Videos 1 y 2)](#acto-i--samuel-toma-el-mando-configuraci%C3%B3n-errores-mvm-y-pvm)
   - [Acto II — El turno de Juan: Poderes caóticos, Aturdimiento, Pausa, Mejoras y Guardado (Videos 3 y 4)](#acto-ii--el-turno-de-juan-poderes-ca%C3%B3ticos-aturdimiento-pausa-mejoras-y-guardado)
   - [Acto III — El Clásico Familiar: Samuel vs. Juan en el mismo teclado (Videos 5 y 6)](#acto-iii--el-cl%C3%A1sico-familiar-samuel-vs-juan-en-el-mismo-teclado)

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

Cada requisito funcional de la especificación del proyecto (`Docs/!DOPO-FIN-2026-02.pdf`) queda cubierto y demostrado en al menos uno de los **6 videos de pruebas de aceptación**:

| Código | Requisito Funcional | Evidencia en Video(s) | Qué se verifica en pantalla |
| :--- | :--- | :---: | :--- |
| **RF-01** | Seleccionar modalidad (`PvP`, `PvM`, `MvM`) y perfil de máquina | Video 1, Video 2, Video 5 | Botones de `MenuPanel`, selector `Perfil de máquina:` (`Torpe` activo; aviso de perfiles en construcción). |
| **RF-02** | Seleccionar resolución de cancha al iniciar | Video 1, Video 5 | Cambio entre `Pequeña (1/4 de pantalla)`, `Mediana (1/2 de pantalla)` y `Grande (3/4 de pantalla)`. |
| **RF-03** | Elegir personaje (país) por jugador | Video 1, Video 2, Video 3, Video 5 | Combo `País / Poder:` (`Corea del Sur`, `Camerún`, `Nigeria`, `Estados Unidos`, `Colombia`) con sprites y banderas. |
| **RF-04** | Seleccionar tipo de partido (`Por tiempo` / `Por goles`) y su límite | Video 1, Video 2, Video 5 | Selector dinámico entre `Goles para ganar:` (`3`, `5`, `7`) y `Minutos del partido:` (`1`, `2`, `3`, `5`), incluyendo `¡MUERTE SÚBITA!`. |
| **RF-05** | Realizar movimientos: mover, saltar, patear y activar poder | Video 2, Video 3, Video 5 | Controles P1 (`A`, `D`, `W`, `S`, `Q`) y P2 (`←`, `→`, `↑`, `↓`, `Ctrl`), física, daño, aturdimiento de `3 s`, recarga de `10 s` / `+25%` y los 4 poderes únicos. |
| **RF-06** | Ilustrar permanentemente estado del juego e información de jugadores | Video 2, Video 3, Video 5 | `InfoPanel` superior: banderas, nombres, vida (verde/amarillo/rojo), poder, poder activo, niveles (`Vel/Sal/Pat/Vid`), marcador y reloj. |
| **RF-07** | Pausar momentáneamente el juego | Video 3, Video 5 | Tecla `Esc` abre el diálogo modal `JUEGO EN PAUSA` con marcador actual y botones `Continuar`, `Reiniciar` y `Volver al menú`. |
| **RF-08** | Terminar el juego en cualquier momento | Video 1, Video 6 | `Volver al menú` desde pausa, `Archivo -> Nuevo` y `Archivo -> Salir` (con diálogo `"¿Desea salir del juego?"`). |
| **RF-09** | Notificar ganador y puntos obtenidos al finalizar | Video 2, Video 6 | Diálogo `FIN DEL PARTIDO` con trofeo dorado, marcador final, ganador (`+100 pts`) y perdedor (`+25 pts`). |
| **RF-10** | Mejorar estadísticas de cada jugador | Video 4 | Botones `Vel Nv.X (+)`, `Salto Nv.X (+)`, `Patada Nv.X (+)`, `Vida Nv.X (+)` descontando `500`, `1000`, `2000` pts. |
| **RF-11** | Salvar y abrir el estado de un partido | Video 4, Video 6 | Menú `Archivo -> Guardar` (`.dat` en `data/saves/`) y `Archivo -> Abrir` restaurando marcador, posiciones, reloj y vida. |
| **RF-12** | Mantener progreso de jugadores (puntos y estadísticas) | Video 2, Video 4, Video 6 | Al escribir el nombre del jugador en `PlayerConfig`, SQLite recarga sus `Puntos`, `PJ`, `G` y niveles guardados. |
| **RF-13** | Presentar mensajes claros y detallados en caso de error | Video 1, Video 4 | Validaciones: nombres `< 3` letras, perfil de máquina en construcción, puntos insuficientes para mejora y error al abrir `.dat` inválido (`POOPSoccerException` + `POOPSoccer_log.txt`). |

---

## 5. Historia y Pruebas de Aceptación: El Especial de Samuel y Juan (*"Pop Soccer"*)

### Prólogo: La noche del estreno en el canal de Samuel

Todo empezó un viernes por la noche cuando **Samuel** decidió revivir su canal de YouTube con una joya oculta de internet: la adaptación universitaria de *Head Soccer* para PC llamada **POOPSoccer** —aunque su primo **Juan**, convencido de que leyó rápido el título, insistió toda la noche en llamarlo *"Pop Soccer"*.

> *"Primo, si los de Sistemas hicieron este juego para aprobar Desarrollo Orientado por Objetos, nosotros tenemos que hacerle el control de calidad más épico que haya visto YouTube"* —sentenció Samuel mientras ajustaba el micrófono.

El plan de grabación era simple pero ambicioso:
1. **Primero, cada uno por su cuenta:** Samuel probaría los menús, las validaciones, las resoluciones, el modo espectador de máquinas y los primeros partidos contra la CPU; luego Juan tomaría el teclado para poner a prueba los poderes más caóticos, el combate cuerpo a cuerpo hasta el aturdimiento, la pausa, las mejoras en la tienda de estadísticas y el sistema de guardado.
2. **Después, el reencuentro inevitable:** Aunque el juego es **100% local**, ambos prometieron que en el clímax del especial se "encontrarían en el servidor" —eufemismo elegante para apretarse hombro con hombro frente al mismo computador y compartir un solo teclado— en el esperado **Jugador vs Jugador (PvP)** definitivo.

Luz, cámara, Java Swing... ¡rueda la serie de **Pruebas de Aceptación**!

---

### Acto I — Samuel toma el mando: Configuración, Errores, MvM y PvM

#### Caso de Prueba 1: El bautizo de "Pop Soccer", errores a prueba de curiosos y el derby de robots (`RF-01`, `RF-02`, `RF-03`, `RF-04`, `RF-08`, `RF-13`)

**Qué se muestra en pantalla durante la grabación:**
1. Se inicia la aplicación mostrando el menú principal (`MenuPanel`) con música de fondo y los tres modos: `Jugador vs Jugador (PvP)`, `Jugador vs Máquina (PvM)` y `Máquina vs Máquina (MvM)`.
2. Samuel entra primero a `Jugador vs Máquina (PvM)` para probar las validaciones (`RF-13`):
   - Borra el nombre del Jugador 1 dejando 2 letras (ej. `"Sa"`) y pulsa `Iniciar Partido` → aparece el diálogo claro: `"El nombre de cada jugador debe tener al menos 3 letras"`.
   - Corrige el nombre a `"Samuel"`, cambia el `Perfil de máquina:` a `Ofensiva` (o `Defensiva`/`Experta`) y pulsa `Iniciar Partido` → aparece el aviso: `"Tipo de máquina en construcción, elija máquina torpe"`.
3. Cambia la `Resolución de la cancha:` probando la diferencia de tamaño de ventana al iniciar con `Pequeña (1/4 de pantalla)` y luego con `Grande (3/4 de pantalla)` (`RF-02`), usando la barra superior `Archivo -> Nuevo` o `Esc -> Volver al menú` para salir en cualquier momento (`RF-08`).
4. Regresa al menú, selecciona **`Máquina vs Máquina (MvM)`**, deja a `CPU Torpe` en ambos lados con dos países distintos (ej. `Corea del Sur` vs `Camerún`) y `Por goles` (`3`), inicia unos segundos para demostrar cómo ambas IA juegan solas y termina la prueba volviendo al menú.

**Guion / Diálogo:**
> **Samuel:** ¡Qué pasa, gente! Bienvenidos de nuevo al canal. Hoy tenemos video especial con mi primo Juan porque vamos a testear un juegazo académico hecho en Java: **POOPSoccer**...
>
> **Juan:** *(interrumpiendo desde el sillón con unas papas)* ¿*"Pop Soccer"*? Suena a que los balones explotan como crispetas, primo.
>
> **Samuel:** Dice *POOP*, de Programación Orientada a Objetos... pero bueno, quédate con *Pop Soccer* si te hace feliz. Mira, antes de jugar en serio voy a ver si esto aguanta mis pruebas de periodista investigador. Entro a **Jugador vs Máquina**, borro mi nombre, pongo solo `"Sa"` y le doy a **Iniciar Partido**... ¡ajá! *"El nombre de cada jugador debe tener al menos 3 letras"*. Bien jugado, no se rompe.
>
> **Juan:** A ver, pon la máquina en modo **Experta**, que quiero verte sufrir.
>
> **Samuel:** Dale, pongo **Experta**... ¡ja! *"Tipo de máquina en construcción, elija máquina torpe"*. O sea que por hoy la CPU viene humilde, modo **Torpe** obligatorio. Aprovechemos y probemos las tres resoluciones de cancha: primero **Pequeña** —mírala ahí, tamaño mini portátil—, salgo con **Archivo → Nuevo**, y ahora **Máquina vs Máquina** en resolución **Grande** para que los dos bots se den patadas mientras yo me tomo un tinto.
>
> **Juan:** ¡Ey, juegan mejor solos que tú en educación física! Sácalos antes de que aprendan demasiado y volvamos al menú.
>
> **Samuel:** Presiono **Esc**, clic en **Volver al menú**, y primer check de aceptación aprobado.

**Video 1:**
(Aquí va el link)

---

#### Caso de Prueba 2: Samuel vs. CPU Torpe — HUD, movimientos y poderes de Corea del Sur y Camerún (`RF-01`, `RF-03`, `RF-04`, `RF-05`, `RF-06`, `RF-09`, `RF-12`)

**Qué se muestra en pantalla durante la grabación:**
1. En `Jugador vs Máquina (PvM)`, Samuel escribe `"Samuel"` como Jugador 1, elige `Corea del Sur`, deja a la máquina en `Torpe` con `Camerún`, selecciona `Por goles` a `3` goles y resolución `Mediana (1/2 de pantalla)`.
2. En cancha, muestra el HUD superior (`InfoPanel`): bandera, `"Samuel (Corea del Sur)"`, barra de vida verde, barra de poder azul cargando hasta dorarse a los `10` segundos (`"Poder activo: Ninguno (¡Barra llena!)"`), resumen de niveles (`Vel:1 Sal:1 Pat:1 Vid:1`) y marcador `0 - 0`.
3. Demuestra los controles del Jugador 1 (`A` izquierda, `D` derecha, `W` salto, `S` patada, choque sólido contra el rival para no atravesarlo) y recibe un gol a propósito o muestra cómo al conceder gol la barra de poder sube **`+25%`** de golpe.
4. Activa el poder con `Q` (se ilumina el aura celeste/dorada alrededor del personaje y el HUD indica `"Poder activo: BlueAura"`), toca el balón y lanza el **`Aura Azul`** horizontal que empuja (`Push`) a la máquina hacia su arco (`1.5 s`, `40` de daño).
5. Juega un segundo partido rápido eligiendo `Camerún` para mostrar el **`Rayo`** (`Lightning`), impactando a la CPU para mostrar la etiqueta `Electrocuted` y cómo se mueve al `50%` sin poder saltar durante `3 s` (`60` de daño).
6. Anota el gol definitivo para completar la meta (`3` goles), mostrando la ventana modal **`FIN DEL PARTIDO`** con el trofeo dorado, el marcador final, `"¡Ganador: Samuel (+100 pts | Total: ... pts)!"` y `"Perdedor: CPU Torpe (+25 pts | Total: ... pts)"`.

**Guion / Diálogo:**
> **Samuel:** Ahora sí, me toca defender el honor de la familia contra la `CPU Torpe`. Me pongo `"Samuel"`, elijo **Corea del Sur** y arrancamos un partido **Por goles** a 3 tantos en tamaño **Mediano**.
>
> **Juan:** Ojo ahí arriba esa barra, ¿eso azul qué es?
>
> **Samuel:** Ese es el HUD completo, Juan: vida en verde, barra de poder que se llena cada **10 segundos** —o si me meten un gol me regalan un **25%** de consolación por lástima—, mi resumen de estadísticas todo en nivel 1 y el marcador. Mira cómo me muevo con `A`, `D`, salto con `W` y pateo con `S`. Espérate que ahí se llenó la barra y se puso dorada... ¡presiono `Q`!
>
> **Juan:** ¡Te prendiste como árbol de Navidad!
>
> **Samuel:** ¡Y ahí va el **Aura Azul**! Recto como un misil, le pegó a la máquina y la mandó volando contra su propia portería por el empuje. Y mira este segundo partido con **Camerún**: ¡toma **Rayo**! Quedó **Electrocuted**, caminando en cámara lenta y sin poder saltar tres segundos. ¡Golazo!
>
> **Juan:** ¡Opaaa! Salió el trofeo de **FIN DEL PARTIDO**: **+100 puntos para Samuel** y **+25 para la CPU Torpe** por el esfuerzo. Mi turno, pásame ese teclado que te voy a enseñar cómo se juega *Pop Soccer* de verdad.

**Video 2:**
(Aquí va el link)

---

### Acto II — El turno de Juan: Poderes caóticos, Aturdimiento, Pausa, Mejoras y Guardado

#### Caso de Prueba 3: Juan desata el Tornado de Nigeria, los 8 balones de EE. UU., el Aturdimiento de 3s y la Pausa (`RF-03`, `RF-05`, `RF-06`, `RF-07`)

**Qué se muestra en pantalla durante la grabación:**
1. En `Jugador vs Máquina (PvM)`, Juan escribe `"Juan"` como Jugador 1 y elige **`Nigeria`** contra la `CPU Torpe` (`Estados Unidos`).
2. Muestra en cámara el disparo especial **`Tornado`** de Nigeria: el balón rebota a ras de suelo y al impactar a la máquina la lanza por los aires con la etiqueta `Unconscious` durante `3 s` (`40` de daño), dejándola sin moverse, saltar ni patear.
3. Cambia de lado o repite con **`Estados Unidos`** (o muestra a la máquina / a Juan usando EE. UU./Colombia) para lanzar **`Illusion`**: en cancha aparecen **`8` balones idénticos** (`1` real + `7` señuelos) durante `2.5 s`, desapareciendo automáticamente al expirar el efecto (`Confusion`).
4. Juan se acerca a la CPU y le conecta **patadas cuerpo a cuerpo (`S`)** (sin el balón entre ambos) hasta bajar la barra de vida de la CPU de verde → amarillo → rojo → `0`, mostrando el tinte rojo sobre el rival con la etiqueta **`¡ATURDIDO!`** durante `3` segundos y verificando cómo tras esos `3 s` su barra de vida se reinicia al `100%`.
5. En medio del partido presiona **`Esc`** para abrir **`JUEGO EN PAUSA`**, verifica que el marcador y el reloj quedan congelados, prueba `Continuar` y luego vuelve a pausar para probar `Reiniciar`.

**Guion / Diálogo:**
> **Juan:** Quítate, primo, que llegó el estratega. Escribo `"Juan"` y me pido **Nigeria** para probar ese famoso **Tornado**. A ver... cargo barra, presiono `Q`, le pego al balón y... ¡JAJAJA, mira cómo bota por el piso y mandó a volar a la máquina con cara de **Unconscious**!
>
> **Samuel:** ¡Eso parece más lucha libre que fútbol! Ahora prueba **Estados Unidos**, que ese es el verdadero caos visual.
>
> **Juan:** Listo, **Ilusión** activada... ¡¿QUÉ ES ESTO?! ¡OCHO BALONES EN LA CANCHA! ¿Cuál pateo, primo, cuál es el de verdad?! ¡Me engañaron mis propios ojos durante dos segundos y medio!
>
> **Samuel:** Y espérate: acércate sin balón y pégale patadas normales con `S`, a ver qué pasa con la barra de vida de la CPU.
>
> **Juan:** Uno, dos, tres patadones... mira cómo pasa de verde a amarillo, luego a rojo y... ¡KNOCKOUT! Le salió **`¡ATURDIDO!`** encima durante **3 segundos**, no se puede ni mover, y cuando despierta vuelve con toda la vida llena. Espera que me está sonando el celular: presiono **Esc**, sale **JUEGO EN PAUSA** con nuestro marcador congelado. Puedo darle **Continuar** si vuelvo, o **Reiniciar** si la cosa iba fea. ¡Aprobado!

**Video 3:**
(Aquí va el link)

---

#### Caso de Prueba 4: Persistencia en SQLite, tienda de mejoras y Guardar/Abrir partida `.dat` (`RF-10`, `RF-11`, `RF-12`, `RF-13`)

**Qué se muestra en pantalla durante la grabación:**
1. **Persistencia de perfiles SQLite (`RF-12`):** En `PlayerConfig`, al escribir `"Samuel"` y `"Juan"`, la píldora de perfil muestra automáticamente sus `Puntos`, `PJ` (Partidos Jugados) y `G` (Ganados) recuperados de `data/database/poopsoccer.db`. Se cierra o cambia a un nombre nuevo (ej. `"Invitado"`) para ver que arranca en `0` puntos y niveles en `1`, y al volver a escribir `"Samuel"` reaparece su progreso intacto.
2. **Validación y compra de mejoras (`RF-10`, `RF-13`):**
   - Juan (con menos de `500` puntos) intenta pulsar `Patada Nv.1 (+)` → el sistema muestra el mensaje claro: `"Necesitás 500 puntos (o ya está al máximo nivel 10)."`.
   - Samuel (tras acumular `≥ 500` puntos en partidas o usando un perfil con saldo disponible) compra una mejora (por ejemplo `Patada Nv.1 (+)` → `Patada Nv.2 (+)` o `Vel Nv.1 (+)` → `Vel Nv.2 (+)`), mostrando el descuento de `500` pts y que el tooltip/siguiente costo sube a `1000` pts; al entrar al partido, el HUD refleja el nuevo nivel (ej. `Pat:2`).
3. **Guardar y abrir partida + manejo de errores (`RF-11`, `RF-13`):**
   - Durante un partido con marcador distinto de `0 - 0` (ej. `1 - 0` o `2 - 1`), van al menú superior **`Archivo -> Guardar`**, guardan un archivo (ej. `clasico_pausa.dat` en `data/saves/`) y aparece `"Partida guardada en: clasico_pausa.dat"`.
   - Reinician o inician otra partida en `0 - 0`, luego abren **`Archivo -> Abrir`**, cargan `clasico_pausa.dat` y verifican que vuelve exactamente al instante guardado (marcador, posiciones y estado).
   - Para demostrar `RF-13`, intentan abrir un archivo inválido o corrupto desde `Archivo -> Abrir`, mostrando el cuadro `"Error al abrir archivo:"` (lanzado por `POOPSoccerException` y registrado en `data/logs/POOPSoccer_log.txt`).

**Guion / Diálogo:**
> **Samuel:** Ojo a esta parte, gente, que aquí es donde se nota la ingeniería. Mira la pantalla de configuración: apenas escribo `"Samuel"`, la base de datos SQLite me trae mis puntos acumulados, partidos jugados y victorias. Si pongo `"Invitado"`, todo vuelve a cero.
>
> **Juan:** A ver, yo tengo como 100 punticos apenas... le doy clic a mejorar **Patada** y... *"Necesitás 500 puntos (o ya está al máximo nivel 10)"*. Me tocó trabajar duro, pero tú que ya juntaste los **500**, mejórate **Velocidad** o **Patada**, primo.
>
> **Samuel:** ¡Comprado! **500 puntos** menos, subí a nivel 2 y ahora el siguiente nivel ya cuesta **1000** —el doble, nada barato el gimnasio del juego—. Y mira el HUD cuando entramos a la cancha: ya marca nivel 2.
>
> **Juan:** Espérate, ¿y si se va la luz en medio de un partido bueno?
>
> **Samuel:** Para eso está **`Archivo → Guardar`**. Guardo este partido que va `1 - 0` como `clasico_pausa.dat`. Ahora me salgo o arranco otro juego en `0 - 0`... voy a **`Archivo → Abrir`**, selecciono `clasico_pausa.dat` y ¡BOOM!, volvemos exactamente al segundo y marcador donde lo dejamos. Y si alguien intenta abrir un archivo dañado o que no es del juego, salta el diálogo de error de `POOPSoccerException` y queda el chisme anotado en `POOPSoccer_log.txt` para el programador.

**Video 4:**
(Aquí va el link)

---

### Acto III — El Clásico Familiar: Samuel vs. Juan en el mismo teclado

#### Caso de Prueba 5: El reencuentro local — PvP por Tiempo y ¡MUERTE SÚBITA! (`RF-01`, `RF-03`, `RF-04`, `RF-05`, `RF-06`, `RF-07`)

**Qué se muestra en pantalla durante la grabación:**
1. Desde el menú principal eligen **`Jugador vs Jugador (PvP)`**.
2. Configuran a **Samuel** (Jugador 1, izquierda, controles `WASD + Q`, ej. `Corea del Sur` o `Camerún`) vs **Juan** (Jugador 2, derecha, controles `Flechas + Ctrl Derecho`, ej. `Nigeria` o `Estados Unidos`).
3. Configuran `Tipo de partido:` = **`Por tiempo`**, `Minutos del partido:` = **`1`**, y `Resolución de la cancha:` = **`Grande (3/4 de pantalla)`**.
4. Durante el minuto reglamentario demuestran ambos jugadores humanos actuando simultáneamente en el mismo teclado: saltos, choques, intercambio de tiros de poder (mostrando cómo un efecto nuevo reemplaza al anterior sin acumularse y cómo un disparo que entra limpio al arco suma gol sin herir al rival), además de una pausa táctica con `Esc`.
5. Mantienen el marcador empatado (ej. `1 - 1` o `2 - 2`) hasta que el temporizador llega a `0s` → el centro del HUD cambia al texto rojo **`¡MUERTE SÚBITA!`** y el partido continúa hasta que uno de los dos anota el gol de oro decisivo.

**Guion / Diálogo:**
> **Samuel:** Señoras y señores, llegó el momento que todos esperaban en el canal. Después de entrenar cada uno por su lado contra la máquina, Juan y yo por fin nos encontramos en la misma cancha para el **Jugador vs Jugador**...
>
> **Juan:** *(riéndose)* Aclara también que "encontrarnos" significa que estamos pegados codo con codo apretando el **mismo teclado** como en 2008. Yo voy por la derecha con las **Flechas y Ctrl**, tú a la izquierda con **WASD y Q**. ¡No me empujes el brazo!
>
> **Samuel:** Pongo el partido **Por tiempo** a **1 minuto** en pantalla **Grande**. ¡Suena el silbato y arranca el clásico familiar!
>
> **Juan:** ¡Toma **Tornado**! ¡Uy, qué atajada! ¡No, me metiste **Rayo** encima y ya no puedo saltar! ¡Oye, que nos empatamos `1 - 1` faltando diez segundos!
>
> **Samuel:** ¡Aguanta, aguanta! `3... 2... 1...` ¡CERO SEGUNDOS! Mira el marcador allá arriba en rojo: **`¡MUERTE SÚBITA!`** El primero que meta gol se lleva la gloria del canal... ¡pateo, rebota en el palo... Y GOOOOL!

**Video 5:**
(Aquí va el link)

---

#### Caso de Prueba 6: Revancha decisiva abriendo partida guardada, premiación final, verificación SQLite y cierre (`RF-08`, `RF-09`, `RF-11`, `RF-12`)

**Qué se muestra en pantalla durante la grabación:**
1. Para la revancha final, muestran cómo retoman una partida guardada previamente (`Archivo -> Abrir` con un `.dat` de su duelo PvP empatado o cerrado) y definen el partido final hasta que salta **`FIN DEL PARTIDO`**.
2. En el diálogo modal **`FIN DEL PARTIDO`** se verifica en pantalla el trofeo dorado, el marcador final, el ganador acreditado con **`+100 pts`** y su nuevo total, y el perdedor con **`+25 pts`** y su total actualizado (`RF-09`).
3. Al volver al menú, entran brevemente a `Jugador vs Máquina` o `Jugador vs Jugador` (`PlayerConfig`) solo para confirmar que los perfiles de `"Samuel"` y `"Juan"` actualizaron sus contadores de `Puntos`, `PJ` y `G` en la base de datos SQLite (`RF-12`).
4. Despiden el especial haciendo clic en **`Archivo -> Salir`**, mostrando el cuadro `"¿Desea salir del juego?"` y cerrando limpiamente la aplicación (`RF-08`).

**Guion / Diálogo:**
> **Juan:** ¡Eso fue robo en muerte súbita, primo! Menos mal habíamos guardado la revancha antes de que se me congelara la mano. Voy a **`Archivo → Abrir`**, cargamos el `.dat` de la serie definitiva y ahora sí, sin excusas.
>
> **Samuel:** ¡Dale pues, último punto de oro en juego! ¡Patada voladora, rebote en el travesaño, despeje... y ahí está el gol definitivo!
>
> **Juan:** Míralo ahí: **FIN DEL PARTIDO**, trofeo dorado en pantalla, **+100 puntos** para el campeón y **+25 puntos** de consolación para el subcampeón del hogar.
>
> **Samuel:** Y para cerrar con broche de oro, entramos un segundito a configuración y comprobamos que tanto **Samuel** como **Juan** ya tienen actualizados sus puntos, partidos jugados y victorias en la base de datos. ¡Todo verificado de principio a fin!
>
> **Juan:** Bueno gente, si les gustó este especial de *"Pop Soccer"* —perdón, **POOPSoccer**— denle like y suscríbanse al canal de Samuel. Voy a **`Archivo → Salir`**, confirmo `"¿Desea salir del juego?"` en **Sí**, y nos vemos en el próximo video. ¡Chao!

**Video 6:**
(Aquí va el link)
