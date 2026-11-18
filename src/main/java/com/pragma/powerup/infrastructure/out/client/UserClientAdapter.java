package com.pragma.powerup.infrastructure.out.client;

import com.pragma.powerup.domain.exception.UserServiceUnavailableException;
import com.pragma.powerup.domain.model.UserInfoModel;
import com.pragma.powerup.domain.spi.IUserClientPort;
import com.pragma.powerup.infrastructure.out.client.dto.UserClientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/** Consulta usuarios-ms reenviando el token del usuario que hizo la petición original. */
@RequiredArgsConstructor
public class UserClientAdapter implements IUserClientPort {

    private final RestTemplate restTemplate;
    private final String usersBaseUrl;

    @Override
    public Optional<UserInfoModel> findUserById(Long id) {
        try {
            HttpHeaders headers = new HttpHeaders();
            String authorization = currentAuthorizationHeader();
            if (authorization != null) {
                headers.set(HttpHeaders.AUTHORIZATION, authorization);
            }
            UserClientResponse body = restTemplate.exchange(usersBaseUrl + "/api/v1/user/{id}",
                    HttpMethod.GET, new HttpEntity<>(headers), UserClientResponse.class, id).getBody();
            if (body == null) {
                return Optional.empty();
            }
            return Optional.of(new UserInfoModel(body.getId(), body.getRole(), body.getPhone(), body.getEmail()));
        } catch (HttpClientErrorException.NotFound notFound) {
            return Optional.empty();
        } catch (RestClientException ex) {
            throw new UserServiceUnavailableException();
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
