package com.example.spring.security.oauth2.controller;

import com.example.spring.security.oauth2.model.Person;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/persons")
public class PersonController {
    private static List<Person> persons = new ArrayList();
    static {
        Person p1 = new Person("Jack", "Smith");
        Person p2 = new Person("Lucas", "Derrik");
        Person p3 = new Person("Andy", "Miller");
        persons.add(p1);
        persons.add(p2);
        persons.add(p3);
    }
    @GetMapping
    public List<Person> getAllPersons() {
        return persons;
    }
}
