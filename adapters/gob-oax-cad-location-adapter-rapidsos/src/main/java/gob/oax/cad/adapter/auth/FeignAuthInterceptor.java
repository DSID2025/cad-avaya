package gob.oax.cad.adapter.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeignAuthInterceptor implements RequestInterceptor {

    private final LocationAuthService authService;

    @Override
    public void apply(RequestTemplate template) {
        try {
            String token = authService.getToken();
            template.header("Authorization", "Bearer " + token);

            log.debug("Se agregó encabezado Authorization con token Bearer a la solicitud Feign.");
        } catch (Exception ex) {
            log.error("No se pudo obtener el token para la solicitud Feign: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}