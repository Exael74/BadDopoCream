# Bad Dopo Cream

Juego de estrategia 2D en grid con temática de helados, desarrollado en Java con Swing.

## Requisitos

- Java 23+
- Librerías incluidas en `libraries/` (json-20230227.jar)

## Cómo compilar y ejecutar

```bash
# Compilar
javac -cp "libraries/json-20230227.jar" -d out/production -sourcepath src src/presentation/Main.java

# Ejecutar
java -cp "libraries/json-20230227.jar;out/production" presentation.Main
```

## Controles

| Acción | Jugador 1 | Jugador 2 |
|--------|-----------|-----------|
| Moverse | W/A/S/D | Flechas |
| Acción (estornudar/patada) | SPACE | M |

## Modos de juego

- **1 Jugador** — Un jugador humano contra enemigos IA
- **2 Jugadores** — Dos jugadores humanos compitiendo
- **Machine vs Machine** — Dos jugadores IA compitiendo
- **P1 vs CPU** — Un humano contra una IA

## Tipos de IA

- **EXPERT** — Pathfinding con evasión de enemigos
- **HUNGRY** — Prioriza recolectar frutas
- **FEARFUL** — Prioriza huir de enemigos

## Créditos

Proyecto universitario
