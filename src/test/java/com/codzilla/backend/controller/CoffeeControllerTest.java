package com.codzilla.backend.controller;

import com.codzilla.backend.repository.CoffeeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CoffeeControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CoffeeRepository coffeeRepository;

    @BeforeEach
    void setUp() {
        coffeeRepository.deleteAll();
    }

    @Test
    void getCoffees_shouldReturnList() {
        coffeeRepository.save(new Coffee("Espresso"));
        coffeeRepository.save(new Coffee("Latte"));

        ResponseEntity<Coffee[]> response = restTemplate.getForEntity("/coffee", Coffee[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getCoffee_shouldReturnCoffee_whenExists() {
        Coffee saved = coffeeRepository.save(new Coffee("Cappuccino"));

        ResponseEntity<Coffee> response = restTemplate.getForEntity("/coffee/" + saved.getId(), Coffee.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getName()).isEqualTo("Cappuccino");
    }

    @Test
    void postCoffee_shouldCreateAndReturnCoffee() {
        Coffee coffee = new Coffee("Mocha");

        ResponseEntity<Coffee> response = restTemplate.postForEntity("/coffee", coffee, Coffee.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getName()).isEqualTo("Mocha");
    }

    @Test
    void deleteCoffee_shouldRemoveCoffee() {
        Coffee saved = coffeeRepository.save(new Coffee("ToDelete"));

        restTemplate.delete("/coffee/" + saved.getId());

        assertThat(coffeeRepository.findById(saved.getId())).isEmpty();
    }
}