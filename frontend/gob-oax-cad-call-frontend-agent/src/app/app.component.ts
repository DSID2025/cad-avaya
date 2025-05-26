import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  title = 'gob-oax-cad-call-frontend-agent';
  messages: string[] = [];

  private client: Client | null = null;
  private subscription: StompSubscription | null = null;

  extension: string = '';
  isConnected: boolean = false;
  inCall: boolean = false;
  isOnHold: boolean = false;
  currentCallId: string | null = null;
  callerNumber: string | null = null;

  readonly baseUrl = 'ws://localhost:8080/gob-oax-cad-call-backend/ws';
  private topic = '';

  connect() {
    if (!this.extension) {
      this.messages.push('⚠️ Debes ingresar tu extensión antes de conectarte');
      return;
    }

    this.topic = `/topic/agent/${this.extension}`;
    this.client = new Client({
      webSocketFactory: () => new WebSocket(this.baseUrl),
      reconnectDelay: 5000,
      connectHeaders: {
        'agent-id': this.extension,
      },
    });

    this.client.onConnect = () => {
      this.isConnected = true;
      this.messages.push(`🟢 Conectado como agente ${this.extension}`);

      this.subscription = this.client!.subscribe(
        this.topic,
        (message: IMessage) => {
          const event = JSON.parse(message.body);

          console.log(`Evento recibido: ${message.body}`);

          if (event.state === 'RINGING') {
            this.currentCallId = event.callId;
            this.callerNumber = event.from;
            this.inCall = false;
            this.messages.push(
              `📥 Llamada entrante de ${event.from} (callId=${event.callId})`
            );
          } else if (event.state === 'CONNECTED') {
            this.inCall = true;
            this.callerNumber = event.from;
            this.messages.push(`🔗 Llamada conectada (callId=${event.callId})`);
          } else if (event.state === 'DISCONNECTED') {
            this.inCall = false;
            this.isOnHold = false;
            this.currentCallId = null;
            this.callerNumber = null;
            this.messages.push(
              `📴 Llamada finalizada (callId=${event.callId})`
            );
          } else {
            this.messages.push(
              `📥 Evento recibido: [${event.state || event.eventType}]`
            );
          }
        }
      );
    };

    this.client.onStompError = (frame) => {
      this.messages.push(`❌ Error STOMP: ${frame.headers['message']}`);
    };

    this.client.activate();
  }

  disconnect() {
    if (this.client && this.isConnected) {
      this.client.deactivate();
      this.client = null;
      this.subscription = null;
      this.isConnected = false;
      this.inCall = false;
      this.isOnHold = false;
      this.currentCallId = null;
      this.callerNumber = null;
      this.messages.push(`🔌 Desconectado`);
    }
  }

  sendAction(action: string) {
    if (!this.client || !this.isConnected || !this.currentCallId) {
      this.messages.push(`⚠️ Acción ignorada. Falta conexión o callId.`);
      return;
    }

    const payload = {
      action,
      callId: this.currentCallId,
      timestamp: new Date().toISOString(),
    };

    this.client.publish({
      destination: `/app/agent/${this.extension}/action`,
      body: JSON.stringify(payload),
    });

    this.messages.push(
      `📤 Acción enviada: ${action} (callId=${this.currentCallId})`
    );
  }

  toggleHold() {
    if (!this.inCall) return;
    this.isOnHold = !this.isOnHold;
    this.sendAction(this.isOnHold ? 'HOLD' : 'UNHOLD');
  }

  answerCall() {
    this.sendAction('ANSWER');
  }

  hangUp() {
    this.sendAction('HANGUP');
  }

  transferCall() {
    this.sendAction('TRANSFER');
  }

  lookupLocation(): void {
    if (!this.callerNumber) {
      this.messages.push(
        '⚠️ No hay número de origen disponible para consultar ubicación.'
      );
      return;
    }

    fetch(
      'http://localhost:8080/gob-oax-cad-call-backend/api/location/lookup',
      {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          phoneNumber: this.callerNumber,
        }),
      }
    )
      .then(async (response) => {
        if (response.ok) {
          const data = await response.json();
          if (data.location) {
            this.messages.push(
              `📍 Ubicación estimada del número ${this.callerNumber}: ${data.location} (confianza: ${data.confidence})`
            );
          } else {
            this.messages.push(
              `⚠️ No se encontró ubicación para el número ${this.callerNumber}`
            );
          }
        } else if (response.status === 404) {
          this.messages.push(
            `⚠️ El backend no encontró ubicación para el número ${this.callerNumber}`
          );
        } else {
          this.messages.push(
            `❌ Error al consultar ubicación: código ${response.status}`
          );
        }
      })
      .catch((error) => {
        console.error('Error al consultar ubicación:', error);
        this.messages.push(
          `❌ Error al consultar ubicación para el número ${this.callerNumber}`
        );
      });
  }
}
