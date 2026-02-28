package com.codzilla.backend.repository;

import com.codzilla.backend.controller.Coffee;
import org.springframework.data.repository.CrudRepository;

public interface CoffeeRepository extends CrudRepository<Coffee , String> {
}
