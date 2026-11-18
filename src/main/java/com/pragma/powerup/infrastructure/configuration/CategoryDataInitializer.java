package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.infrastructure.out.jpa.entity.CategoryEntity;
import com.pragma.powerup.infrastructure.out.jpa.repository.ICategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/** Crea las categorías base de platos si todavía no existen (ids 1 a 5 en una BD nueva). */
@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements CommandLineRunner {

    private final ICategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        Map<String, String> categories = new LinkedHashMap<>();
        categories.put("Entradas", "Platos para empezar");
        categories.put("Platos fuertes", "Platos principales");
        categories.put("Postres", "Dulces y postres");
        categories.put("Bebidas", "Bebidas frías y calientes");
        categories.put("Acompañamientos", "Adicionales y guarniciones");

        categories.forEach((name, description) -> {
            if (!categoryRepository.existsByName(name)) {
                categoryRepository.save(new CategoryEntity(null, name, description));
            }
        });
    }
}
