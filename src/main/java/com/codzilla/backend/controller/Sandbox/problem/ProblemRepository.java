package com.codzilla.backend.controller.Sandbox.problem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
}

/*
    JpaRepository уже даёт :
     save(problem)        — сохранить
     findById(id)         — найти по id
     findAll()            — все задачи
     deleteById(id)       — удалить
*/


