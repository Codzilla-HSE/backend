package com.codzilla.backend.controller;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class Coffee {


    @Id
    private String id ;
    private String name;
    public void setId(String id) {
        this.id = id;
    }


    @JsonCreator
    public Coffee(@JsonProperty("id") String id, @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }

    public Coffee() {
        this.id = UUID.randomUUID().toString();
        this.name = "DEFAULT";

    }

    public Coffee(String name) {
        this(UUID.randomUUID().toString() , name) ;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
