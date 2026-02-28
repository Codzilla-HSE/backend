package com.codzilla.backend.controller;

import com.codzilla.backend.repository.CoffeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/coffee")
public class RestApiDemoController {

    private final CoffeeRepository coffeeRepository;
//    private final List<Coffee> coffees = new ArrayList<>();

    public RestApiDemoController(CoffeeRepository coffeeRepository) {
        this.coffeeRepository = coffeeRepository;

        this.coffeeRepository.saveAll(List.of(
                new Coffee("Café Cereza"),
                new Coffee("Café Ganador"),
                new Coffee("Café Lareño"),
                new Coffee("Café Três Pontas")
        ));
    }



    @RequestMapping(value = "", method = RequestMethod.GET)
    Iterable<Coffee> getCoffees() {
        return coffeeRepository.findAll();
    }

    @GetMapping("/{id}")
    Optional<Coffee> getCoffee(@PathVariable String id) {
        return coffeeRepository.findById(id);
    }

    @PostMapping
    Coffee postCoffee(@RequestBody Coffee coffee) {
        return coffeeRepository.save(coffee);
    }

//    @PutMapping("/{id}")
//    Coffee putCoffee(@PathVariable String id, @RequestBody Coffee coffee) {
//        int coffeeIndex = -1;
//        for (Coffee c: coffees) {
//            if (c.getId().equals(id)) {
//                coffeeIndex = coffees.indexOf(c);
//                coffees.set(coffeeIndex, coffee);
//            }
//        }
//        return (coffeeIndex == -1) ? postCoffee(coffee) : coffee;
//    }

//    @PutMapping("/{id}")
//    ResponseEntity<Coffee> putCoffee(@PathVariable String id,
//                                     @RequestBody Coffee coffee) {
//        int coffeeIndex = -1;
//        for (Coffee c: coffees) {
//            if (c.getId().equals(id)) {
//                coffeeIndex = coffees.indexOf(c);
//                coffees.set(coffeeIndex, coffee);
//            }
//        }
//        return (coffeeIndex == -1) ?
//                new ResponseEntity<>(postCoffee(coffee), HttpStatus.CREATED) :
//                new ResponseEntity<>(coffee, HttpStatus.OK);
//    }

    @PutMapping("/{id}")
    ResponseEntity<Coffee> putCoffee(@PathVariable String id , @RequestBody Coffee coffee){
        return (!coffeeRepository.existsById(id)) ?
                new ResponseEntity<>(coffeeRepository.save(coffee) , HttpStatus.CREATED) :
                new ResponseEntity<>(coffeeRepository.save(coffee) , HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    void deleteCoffee(@PathVariable String id) {
        coffeeRepository.deleteById(id);
    }
}
