package com.pragma.powerup.infrastructure.out.client;

import com.pragma.powerup.domain.exception.DomainException;
import com.pragma.powerup.domain.exception.NotificationUnavailableException;
import com.pragma.powerup.domain.spi.INotificationPort;
import com.pragma.powerup.infrastructure.out.client.dto.SmsClientRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Llama a mensajeria-ms reenviando el token del usuario que hizo la petición original. */
@RequiredArgsConstructor
public class NotificationClientAdapter implements INotificationPort {

    private final RestTemplate restTemplate;
    private final String messagingBaseUrl;

    @Override
    public void sendSms(String phoneNumber, String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            String authorization = currentAuthorizationHeader();
            if (authorization != null) {
                headers.set(HttpHeaders.AUTHORIZATION, authorization);
            }
            restTemplate.postForEntity(messagingBaseUrl + "/api/v1/message/sms",
                    new HttpEntity<>(new SmsClientRequest(phoneNumber, message), headers), Void.class);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new DomainException("El celular del cliente no tiene formato internacional "
                        + "(por ejemplo +573001112233), por eso no se pudo enviar el PIN");
            }
            throw new NotificationUnavailableException();
        } catch (RestClientException ex) {
            throw new NotificationUnavailableException();
        }
    }

    private String currentAuthorizationHeader() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attributes).getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }
}
