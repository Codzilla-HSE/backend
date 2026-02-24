package com.codzilla.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class MyRestController {


//
//    public MyRestController(Teacher teacher) {
//        this.teacher = teacher;
//    }

    @GetMapping("/")
    public String helloWorld() {
        return "hello World!!!!!!!!!!!!!!" ;
    }

    @GetMapping("/teacher")
    public String teacherSay() {
        return "teacher" ;
    }

}
