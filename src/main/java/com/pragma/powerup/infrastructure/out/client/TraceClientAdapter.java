package com.pragma.powerup.infrastructure.out.client;

import com.pragma.powerup.domain.exception.TraceUnavailableException;
import com.pragma.powerup.domain.model.OrderTraceModel;
import com.pragma.powerup.domain.spi.ITracePort;
import com.pragma.powerup.infrastructure.out.client.dto.TraceClientRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Llama a trazabilidad-ms reenviando el token del usuario que hizo la petición original. */
@RequiredArgsConstructor
public class TraceClientAdapter implements ITracePort {

    private final RestTemplate restTemplate;
    private final String traceBaseUrl;

    @Override
    public void record(OrderTraceModel trace) {
        try {
            HttpHeaders headers = new HttpHeaders();
            String authorization = currentAuthorizationHeader();
            if (authorization != null) {
                headers.set(HttpHeaders.AUTHORIZATION, authorization);
            }
            TraceClientRequest body = new TraceClientRequest(trace.getOrderId(), trace.getClientId(),
                    trace.getClientEmail(), trace.getRestaurantId(), trace.getRestaurantOwnerId(), trace.getPreviousStatus(), trace.getNewStatus(),
                    trace.getEmployeeId(), trace.getEmployeeEmail());
            restTemplate.postForEntity(traceBaseUrl + "/api/v1/trace", new HttpEntity<>(body, headers), Void.class);
        } catch (RestClientException ex) {
            throw new TraceUnavailableException();
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
