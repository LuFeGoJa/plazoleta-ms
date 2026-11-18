package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.api.IRestaurantEmployeeServicePort;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.spi.ICategoryPersistencePort;
import com.pragma.powerup.domain.spi.ICurrentUserPort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.INotificationPort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantEmployeePersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ISecurityPinPort;
import com.pragma.powerup.domain.spi.ITracePort;
import com.pragma.powerup.domain.spi.IUserClientPort;
import com.pragma.powerup.domain.usecase.DishUseCase;
import com.pragma.powerup.domain.usecase.OrderUseCase;
import com.pragma.powerup.domain.usecase.RestaurantEmployeeUseCase;
import com.pragma.powerup.domain.usecase.RestaurantUseCase;
import com.pragma.powerup.infrastructure.out.client.NotificationClientAdapter;
import com.pragma.powerup.infrastructure.out.client.TraceClientAdapter;
import com.pragma.powerup.infrastructure.out.client.UserClientAdapter;
import com.pragma.powerup.infrastructure.out.jpa.adapter.CategoryJpaAdapter;
import com.pragma.powerup.infrastructure.out.jpa.adapter.DishJpaAdapter;
import com.pragma.powerup.infrastructure.out.jpa.adapter.OrderJpaAdapter;
import com.pragma.powerup.infrastructure.out.jpa.adapter.RestaurantEmployeeJpaAdapter;
import com.pragma.powerup.infrastructure.out.jpa.adapter.RestaurantJpaAdapter;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.ICategoryRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantEmployeeRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantRepository;
import com.pragma.powerup.infrastructure.out.security.SecureRandomPinAdapter;
import com.pragma.powerup.infrastructure.out.security.SecurityContextCurrentUserAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {
    private final IRestaurantRepository restaurantRepository;
    private final IRestaurantEntityMapper restaurantEntityMapper;
    private final IDishRepository dishRepository;
    private final IDishEntityMapper dishEntityMapper;
    private final ICategoryRepository categoryRepository;
    private final IOrderRepository orderRepository;
    private final IRestaurantEmployeeRepository restaurantEmployeeRepository;

    @Value("${services.usuarios.base-url}")
    private String usuariosBaseUrl;
    @Value("${services.mensajeria.base-url}")
    private String mensajeriaBaseUrl;
    @Value("${services.trazabilidad.base-url}")
    private String trazabilidadBaseUrl;
    @Value("${services.usuarios.timeout-seconds}")
    private long usuariosTimeoutSeconds;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(usuariosTimeoutSeconds))
                .setReadTimeout(Duration.ofSeconds(usuariosTimeoutSeconds))
                .build();
    }

    @Bean
    public IUserClientPort userClientPort(RestTemplate restTemplate) {
        return new UserClientAdapter(restTemplate, usuariosBaseUrl);
    }

    @Bean
    public IRestaurantPersistencePort restaurantPersistencePort() {
        return new RestaurantJpaAdapter(restaurantRepository, restaurantEntityMapper);
    }

    @Bean
    public IRestaurantServicePort restaurantServicePort(IUserClientPort userClientPort) {
        return new RestaurantUseCase(restaurantPersistencePort(), userClientPort);
    }

    @Bean
    public ICurrentUserPort currentUserPort() {
        return new SecurityContextCurrentUserAdapter();
    }

    @Bean
    public IDishPersistencePort dishPersistencePort() {
        return new DishJpaAdapter(dishRepository, dishEntityMapper);
    }

    @Bean
    public ICategoryPersistencePort categoryPersistencePort() {
        return new CategoryJpaAdapter(categoryRepository);
    }

    @Bean
    public IDishServicePort dishServicePort() {
        return new DishUseCase(dishPersistencePort(), restaurantPersistencePort(),
                categoryPersistencePort(), currentUserPort());
    }

    @Bean
    public IOrderPersistencePort orderPersistencePort() {
        return new OrderJpaAdapter(orderRepository);
    }

    @Bean
    public INotificationPort notificationPort(RestTemplate restTemplate) {
        return new NotificationClientAdapter(restTemplate, mensajeriaBaseUrl);
    }

    @Bean
    public ISecurityPinPort securityPinPort() {
        return new SecureRandomPinAdapter();
    }

    @Bean
    public ITracePort tracePort(RestTemplate restTemplate) {
        return new TraceClientAdapter(restTemplate, trazabilidadBaseUrl);
    }

    @Bean
    public IOrderServicePort orderServicePort(IUserClientPort userClientPort, INotificationPort notificationPort,
                                              ITracePort tracePort) {
        return new OrderUseCase(orderPersistencePort(), restaurantPersistencePort(),
                dishPersistencePort(), currentUserPort(), restaurantEmployeePersistencePort(),
                userClientPort, notificationPort, securityPinPort(), tracePort);
    }

    @Bean
    public IRestaurantEmployeePersistencePort restaurantEmployeePersistencePort() {
        return new RestaurantEmployeeJpaAdapter(restaurantEmployeeRepository);
    }

    @Bean
    public IRestaurantEmployeeServicePort restaurantEmployeeServicePort(IUserClientPort userClientPort) {
        return new RestaurantEmployeeUseCase(restaurantPersistencePort(), restaurantEmployeePersistencePort(),
                userClientPort, currentUserPort());
    }
}
