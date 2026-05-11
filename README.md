# Socket Chat en Java

- Autor: Axel Sarmiento Mrak
- Legajo: 114759
- Fecha: 15-04-2026
- Asignatura: Programación Avanzada

Implementación de un chat cliente-servidor usando sockets TCP en Java con interfaz gráfica Swing.
El objetivo del trabajo es aplicar conceptos de arquitectura por capas, separación de responsabilidades y diseño orientado a interfaces para lograr un código simple de mantener y extender.

## Estructura general del proyecto

El proyecto está organizado en cuatro capas más la capa de UI:

- `app`: puntos de entrada (consola y Swing, servidor y cliente, plus dashboard).
- `domain`: lógica de negocio para procesar mensajes y sistema de apuestas con ranking.
- `infrastructure`: red (sockets), logging por consola y repositorio de apuestas en archivo.
- `shared`: configuración común (`HOST`, `PORT`, comandos, mensajes) e interfaz de logger.
- `ui`: componentes Swing reutilizables (tema oscuro, logger visual, formateadores) y ventanas de servidor y cliente.

## Patrones de diseño utilizados

### 1. Patrón de comportamiento: *Strategy*

Se usa para el procesamiento de mensajes del servidor.

- `./src/com/tpsockets/domain/MessageProcessor.java` define la estrategia (contrato).
- `./src/com/tpsockets/domain/DefaultMessageProcessor.java` es la estrategia concreta actual.
- `./src/com/tpsockets/infrastructure/network/ChatServer.java` y `ClientSessionHandler.java` consumen la abstracción.

La ventaja principal es que la lógica de procesamiento no queda acoplada a la infraestructura de red. Si mañana se quiere cambiar el comportamiento (por ejemplo, un procesador por idioma o por rol), se agrega otra implementación sin tocar el flujo principal del servidor.

---

### 2. Inyección de dependencias (composición en arranque)

No es un patrón GoF clásico, pero sí un patrón de diseño clave en sistemas mantenibles.

- `./src/com/tpsockets/app/server/ServerMain.java` actúa como *composition root*.
- `./src/com/tpsockets/app/dashboard/Dashboard.java` arma el grafo de objetos para la versión Swing.
- Allí se crean `ConsoleLogger`, `DefaultMessageProcessor`, `ChatServer`, `BetCoordinator`, etc.

Esto evita que `ChatServer` construya sus dependencias internamente (`new` dispersos), reduciendo acoplamiento y facilitando pruebas o reemplazos futuros.

---

### 3. Modelo de concurrencia *Thread-per-Connection*

En el servidor, cada cliente aceptado se procesa en un `Thread` independiente:

- `./src/com/tpsockets/infrastructure/network/ChatServer.java` crea una nueva sesión por conexión.
- `./src/com/tpsockets/infrastructure/network/ClientSessionHandler.java` encapsula el ciclo de vida de cada cliente.

---

### 4. Patrón *Decorator* — ObservableBroadcaster

`ObservableBroadcaster` decora `ClientBroadcaster` agregando notificaciones de cambios en la lista de clientes conectados. Permite que la UI reaccione a conexiones y desconexiones sin modificar la lógica de broadcast original.

---

### 5. Sistema de apuestas con *BetCoordinator*

Flujo de apuestas integrado en el chat:

1. El cliente envía `BET`.
2. El servidor muestra los partidos disponibles (`BetMatchCatalog`).
3. El cliente selecciona un partido por número.
4. El cliente ingresa `<equipo> <monto>`.
5. Se registra la apuesta en `BetLogRepository` (archivo de texto) y se actualiza el ranking.

### 6. Ranking de apuestas — *BetRankingTracker*

Cada apuesta se registra en un tracker que mantiene estadísticas por cliente:

- Total de apuestas, ganadas y perdidas.
- Monto apostado y ganado.
- Win rate, ganancia neta y puntaje de ranking (60% win rate + 40% ratio de ganancia).

Se muestra en tiempo real en la ventana del servidor.

---

## Principios SOLID aplicados

### S — *Single Responsibility Principle*

Las responsabilidades están separadas:

- `ChatServer`: aceptar conexiones.
- `ClientSessionHandler`: manejar la sesión de un cliente.
- `DefaultMessageProcessor`: interpretar comandos.
- `ConsoleLogger` / `SwingLogger`: registrar eventos.
- `BetCoordinator`: orquestar el flujo de apuestas.
- `BetRankingTracker`: mantener estadísticas de apuestas.

### O — *Open/Closed Principle*

`MessageProcessor` habilita extender procesamiento con nuevas implementaciones sin modificar la infraestructura que lo consume.

### L — *Liskov Substitution Principle*

Cualquier implementación de `MessageProcessor` debe poder reemplazar a `DefaultMessageProcessor` sin romper el contrato `process(String)`.

### I — *Interface Segregation Principle*

La interfaz `MessageProcessor` es minimalista y enfocada en un único comportamiento (`process`). No obliga a implementar métodos innecesarios.

### D — *Dependency Inversion Principle*

El servidor depende de una abstracción (`MessageProcessor`) y no de una implementación concreta para la lógica de dominio.

> Nota: el logger se inyecta como interfaz (`AppLogger`), con dos implementaciones: `ConsoleLogger` para consola y `SwingLogger` para UI.

## Comandos disponibles en el chat

- `HELLO`
- `TIME`
- `DATE`
- `UPPER <mensaje>`
- `MUNDIAL`
- `HELP`
- `SALIR`

### Comando de operador (consola del servidor / panel del dashboard)

- `BROADCAST <mensaje>` → envía a todos (modo por defecto).
- `BROADCAST ALL <mensaje>` → envía a todos (modo explícito).
- `BROADCAST <CLIENT_ID> <mensaje>` → envía sólo al cliente indicado.

### Comando de apuestas

- `BET` → inicia el flujo de apuestas.

### Registro de nombre de cliente al conectar

- Al iniciar un cliente, el servidor solicita un nombre (`CLIENT_ID`).
- Reglas: 3 a 20 caracteres, sólo letras, números, `_` o `-`.
- El servidor rechaza nombres inválidos o repetidos y vuelve a pedir otro.

## Cómo ejecutar

### 1) Compilar

```bash
javac -d out $(find src -name "*.java")
```

### 2) Dashboard (recomendado)

Una sola ventana para gestionar servidor y clientes:

```bash
java -cp out com.tpsockets.app.dashboard.DashboardMain
```

Desde el dashboard podés:
- Iniciar y detener el servidor.
- Agregar ventanas de cliente con un clic.
- Ver el registro de actividad en tiempo real.

### 3) Servidor Swing (independiente)

```bash
java -cp out com.tpsockets.app.server.ServerMainSwing
```

Muestra la ventana del servidor con lista de clientes, log, consola de operador y ranking de apuestas.

### 4) Cliente Swing (independiente)

```bash
java -cp out com.tpsockets.app.client.ClientMainSwing
```

Abre una ventana de chat con diálogo de conexión, handshake y soporte para apuestas.

### 5) Modo consola (original)

```bash
# Servidor
java -cp out com.tpsockets.app.server.ServerMain

# Cliente
java -cp out com.tpsockets.app.client.ClientMain
```

## Interfaz gráfica

Todas las ventanas usan un tema oscuro con fondo negro:

- **Dashboard**: panel central para iniciar/parar el servidor, agregar clientes y ver actividad.
- **ServerWindow**: lista de clientes conectados, log del servidor, consola de operador para broadcasts, y tabla de ranking de apuestas en tiempo real.
- **ClientWindow**: área de chat, campo de entrada, botón de enviar, botón de apostar, y diálogos para handshake y flujo de apuestas.

## Referencias

- [Strategy Pattern - Refactoring Guru](https://refactoring.guru/es/design-patterns/strategy)
- [SOLID - Robert C. Martin (overview)](https://en.wikipedia.org/wiki/SOLID)
- [Java Sockets - Oracle Docs](https://docs.oracle.com/javase/tutorial/networking/sockets/)

## LINK REPO

- [AxelMrak/socket-chat-programacion-avanzada](https://github.com/AxelMrak/socket-chat-programacion-avanzada)
