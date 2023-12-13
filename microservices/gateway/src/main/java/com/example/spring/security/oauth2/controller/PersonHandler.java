package com.example.spring.security.oauth2.controller;

import com.example.spring.security.oauth2.model.Person;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class PersonHandler {
    private static List<Person> persons = new ArrayList();
    static {
        Person p1 = new Person("Jack", "Smith");
        Person p2 = new Person("Lucas", "Derrik");
        Person p3 = new Person("Andy", "Miller");
        persons.add(p1);
        persons.add(p2);
        persons.add(p3);
    }

    public Mono<List<Person>> getAllPersons() {
        return Mono.just(persons);
    }
}
