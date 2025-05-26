package gob.oax.cad.adapter.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import gob.oax.cad.adapter.config.RapidSosClientProperties;
import gob.oax.cad.adapter.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationAuthService {

    private final RapidSosClientProperties properties;
    private final ObjectMapper objectMapper;

    private String cachedToken;
    private long expirationTime = 0;

    public String getToken() {
        if (cachedToken != null && Instant.now().toEpochMilli() < expirationTime) {
            log.debug("Token válido encontrado en caché. Reutilizando.");
            return cachedToken;
        }

        try {
            log.info("Solicitando nuevo token de autenticación a RapidSOS...");

            HttpClient client = HttpClient.newHttpClient();

            String body = "grant_type=client_credentials"
                    + "&client_id=" + URLEncoder.encode(properties.getClientId(), StandardCharsets.UTF_8)
                    + "&client_secret=" + URLEncoder.encode(properties.getClientSecret(), StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getAuthUrl()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Respuesta no exitosa al solicitar token. Código: {}, cuerpo: {}", response.statusCode(), response.body());
                throw new RuntimeException("No se pudo obtener el token desde RapidSOS");
            }

            TokenResponse tokenResponse = objectMapper.readValue(response.body(), TokenResponse.class);

            cachedToken = tokenResponse.getAccessToken();
            expirationTime = Instant.now().toEpochMilli() + (tokenResponse.getExpiresIn() * 1000L) - 5000;

            log.info("Token de autenticación obtenido correctamente. Expira en {} segundos.", tokenResponse.getExpiresIn());

            return cachedToken;

        } catch (Exception e) {
            log.error("Error al obtener el token de RapidSOS: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener el token de RapidSOS", e);
        }
    }
}
