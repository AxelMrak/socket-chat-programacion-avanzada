# Socket Chat en Java

- Autor: Axel Sarmiento Mrak
- Legajo: 114759
- Fecha: 15-04-2026
- Asignatura: Programación Avanzada

Implementación de un chat cliente-servidor usando sockets TCP en Java.
El objetivo del trabajo es aplicar conceptos de arquitectura por capas, separación de responsabilidades y diseño orientado a interfaces para lograr un código simple de mantener y extender.

## Estructura general del proyecto

El proyecto está organizado en cuatro capas:

- `app`: puntos de entrada (`ClientMain` y `ServerMain`).
- `domain`: lógica de negocio para procesar mensajes.
- `infrastructure`: red (sockets) y logging por consola.
- `shared`: configuración común (`HOST`, `PORT`, comandos, mensajes).

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
- Allí se crean `ConsoleLogger`, `DefaultMessageProcessor` y `ChatServer`.

Esto evita que `ChatServer` construya sus dependencias internamente (`new` dispersos), reduciendo acoplamiento y facilitando pruebas o reemplazos futuros.

---

### 3. Modelo de concurrencia *Thread-per-Connection*

En el servidor, cada cliente aceptado se procesa en un `Thread` independiente:

- `./src/com/tpsockets/infrastructure/network/ChatServer.java` crea una nueva sesión por conexión.
- `./src/com/tpsockets/infrastructure/network/ClientSessionHandler.java` encapsula el ciclo de vida de cada cliente.


## Principios SOLID aplicados

### S — *Single Responsibility Principle*

Las responsabilidades están separadas:

- `ChatServer`: aceptar conexiones.
- `ClientSessionHandler`: manejar la sesión de un cliente.
- `DefaultMessageProcessor`: interpretar comandos.
- `ConsoleLogger`: registrar eventos.

### O — *Open/Closed Principle*

`MessageProcessor` habilita extender procesamiento con nuevas implementaciones sin modificar la infraestructura que lo consume.

### L — *Liskov Substitution Principle*

Cualquier implementación de `MessageProcessor` debe poder reemplazar a `DefaultMessageProcessor` sin romper el contrato `process(String)`.

### I — *Interface Segregation Principle*

La interfaz `MessageProcessor` es minimalista y enfocada en un único comportamiento (`process`). No obliga a implementar métodos innecesarios.

### D — *Dependency Inversion Principle*

El servidor depende de una abstracción (`MessageProcessor`) y no de una implementación concreta para la lógica de dominio.

> Nota: el logger todavía se inyecta como clase concreta (`ConsoleLogger`). Como mejora, se puede extraer una interfaz `LoggerPort` para reforzar aún más DIP.

## Comandos disponibles en el chat

- `HELLO`
- `TIME`
- `DATE`
- `UPPER <mensaje>`
- `MUNDIAL`
- `HELP`
- `SALIR`

## Cómo ejecutar

### 1) Compilar

```bash
javac -d out $(find src -name "*.java")
```

### 2) Levantar servidor

```bash
java -cp out com.tpsockets.app.server.ServerMain
```

### 3) Levantar cliente

```bash
java -cp out com.tpsockets.app.client.ClientMain
```

## Referencias

- [Strategy Pattern - Refactoring Guru](https://refactoring.guru/es/design-patterns/strategy)
- [SOLID - Robert C. Martin (overview)](https://en.wikipedia.org/wiki/SOLID)
- [Java Sockets - Oracle Docs](https://docs.oracle.com/javase/tutorial/networking/sockets/)

## LINK REPO

- [AxelMrak/socket-chat-programacion-avanzada](https://github.com/AxelMrak/socket-chat-programacion-avanzada)
