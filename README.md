# 📞 Sistema de Atención de Llamadas de Emergencia

Este monorepo contiene todos los componentes del sistema de atención de llamadas 911, incluyendo el backend, adapters de integración y la interfaz del agente.

---

## 📁 Estructura del Monorepo

```
cad/
├── adapters/
│   ├── gob-oax-cad-call-adapter-avaya/           # Adapter para escuchar eventos JTAPI desde Avaya
│   └── gob-oax-cad-location-adapter-rapidsos/    # Adapter para consumir la API externa de RapidSOS
│
├── backend/
│   └── gob-oax-cad-call-backend/                 # Backend principal que enruta llamadas y coordina entre adapters y agentes
│
├── frontend/
│   └── gob-oax-cad-call-frontend-agent/          # Aplicación Angular para agentes que reciben y gestionan llamadas WebRTC
├── deployment/
│   └── wiremock/              # Mappings simulados para el API de RapidSOS
│       ├── mappings/          # Respuestas mock definidas por escenario
│       └── __files/           # Cuerpos de respuesta externos (opcional)
│
└── README.md
```

---

## 🧩 Componentes

### 🔌 Adapters

- **gob-oax-cad-call-adapter-avaya**  
  Microservicio que escucha eventos del sistema Avaya (AES) usando JTAPI. Transforma los eventos de llamadas en eventos internos para el backend.

- **gob-oax-cad-location-adapter-rapidsos**  
  Microservicio cliente de la API de RapidSOS. Permite consultar ubicación de llamadas usando el número telefónico.

### ⚙️ Backend

- **gob-oax-cad-call-backend**  
  Servicio central del sistema. Gestiona:
  - Asignación de llamadas a agentes disponibles.
  - Comunicación bidireccional con adapters.
  - Emisión de eventos a la interfaz del agente por WebSocket.
  - Enrutamiento de llamadas hacia extensiones SIP/WebRTC.

### 🖥️ Frontend

- **gob-oax-cad-call-frontend-agent**  
  Interfaz web Angular para agentes que reciben llamadas.  
  Funcionalidades:
  - Registro de sesión.
  - Recepción de llamadas y eventos (entrante, conectada, finalizada).
  - Consulta de ubicación de quien llama (opcional).
  - Comunicación vía WebRTC con gateway SIP.

---

## 🛠️ Tecnologías utilizadas

- **Java 17 + Spring Boot** – Para todos los servicios backend.
- **Angular 19** – Para el frontend de agentes.
- **JTAPI (Avaya AES)** – Para integración con la central telefónica.
- **RapidSOS API** – Para obtención de datos de localización.
- **WebSocket + WebRTC** – Para comunicación y audio en tiempo real.

---

## 🧬 Diagrama de Componentes

```mermaid
graph TD

subgraph Adaptadores
  AVAYA[Adapter Avaya<br>gob-oax-cad-call-adapter-avaya]
  RAPIDSOS[Adapter RapidSOS<br>gob-oax-cad-location-adapter-rapidsos]
end

subgraph Backend
  BACKEND[Backend de llamadas<br>gob-oax-cad-call-backend]
end

subgraph Frontends
  AGENTE[Frontend Agente Web<br>gob-oax-cad-call-frontend-agent]
end

AVAYA -->|Evento JTAPI| BACKEND
RAPIDSOS -->|Consulta ubicación| BACKEND
BACKEND -->|WebSocket / REST| AGENTE

classDef adapter fill:#e3f2fd,stroke:#2196f3;
classDef backend fill:#fff3e0,stroke:#fb8c00;
classDef frontend fill:#ede7f6,stroke:#673ab7;

class AVAYA,RAPIDSOS adapter;
class BACKEND backend;
class AGENTE frontend;

```

---

### 🎯 Resumen del flujo
Este diagrama describe el proceso de atención de una llamada de emergencia desde una persona que marca al 911, pasando por el sistema Avaya, un backend de control y un gateway SIP↔WebRTC, hasta que finalmente la llamada es atendida por un agente desde su navegador.

```mermaid

sequenceDiagram
    participant Persona as Persona (llama al 911)
    participant Avaya as Avaya AES (JTAPI)
    participant Adapter as JTAPI Adapter
    participant Backend as Backend Router
    participant Agente as Agente (Navegador WebRTC)
    participant Gateway as SIP ↔ WebRTC Gateway

    Persona->>Avaya: Llamada entrante al 911
    Avaya->>Adapter: CallEvent (ALERTING)
    Adapter->>Backend: Emite CallStreamEvent (RINGING, sin 'to')
    Backend->>Backend: Busca agente disponible
    Backend->>Agente: Evento WebSocket `RINGING` (callId, número, etc.)
    
    Note over Agente: El agente ve la notificación<br>y decide contestar la llamada

    Agente->>Backend: POST /api/calls/accept (callId)
    Backend->>Adapter: POST /api/calls/route (callId, agente=WebRTC)
    Adapter->>Avaya: call.connect(911, gatewayExtension)
    Avaya->>Gateway: SIP INVITE (llamada hacia Gateway)
    Gateway->>Agente: WebRTC INVITE (via signaling)
    Agente-->>Gateway: Contesta vía navegador
    Gateway-->>Avaya: SIP 200 OK

    Avaya-->>Adapter: CallEvent (CONNECTED)
    Adapter->>Backend: Emite CallStreamEvent (CONNECTED)

    Note over Persona,Agente: Comunicación en tiempo real (RTP/WebRTC)

    Agente-->>Gateway: Cuelga desde navegador
    Gateway-->>Avaya: SIP BYE
    Avaya-->>Adapter: CallEvent (DISCONNECTED)
    Adapter->>Backend: Emite CallStreamEvent (DISCONNECTED)

```

---

## 🧪 Ejecución de los Componentes

### 🔧 1. Backend y Adapters (Spring Boot)

Cada servicio Spring Boot puede ejecutarse con Maven:

```bash
# JTAPI Adapter
cd adapters/gob-oax-cad-call-adapter-avaya
mvn clean spring-boot:run

# RapidSOS Adapter
cd adapters/gob-oax-cad-location-adapter-rapidsos
mvn clean spring-boot:run

# Router Backend
cd backend/gob-oax-cad-call-backend
mvn clean spring-boot:run
```

### 🌐 2. Frontend (Angular)

Ejecuta el panel del agente con Angular CLI:

```bash
cd frontend/gob-oax-cad-call-frontend-agent
npm install
ng serve
```

Esto abrirá la aplicación en http://localhost:4200.

### 🧪 3. Simulación de RapidSOS con WireMock

Puedes usar WireMock como mock server para simular respuestas de la API de RapidSOS. Requiere Java 17+:

```bash
cd /ruta/externa/a/wiremock/
java -jar wiremock-standalone.jar   --port 8090   --root-dir /ruta/al/monorepo/cad-avaya/deployment/wiremock/
```

#### Directorio de Mappings

El directorio `deployment/wiremock/` dentro del monorepo contiene:

- `mappings/`: archivos `.json` que definen los endpoints simulados y sus respuestas.
- `__files/`: archivos opcionales con cuerpos de respuesta externos (ej. JSON, imágenes, etc).


---

## ⚠️ Notas
- Todos los servicios corren por defecto en los siguientes puertos:
  - JTAPI Adapter: 8081
  - RapidSOS Adapter: 8082
  - Router Backend: 8080
  - Angular (dev): 4200

- Los componentes se comunican entre sí mediante HTTP y WebSocket.
