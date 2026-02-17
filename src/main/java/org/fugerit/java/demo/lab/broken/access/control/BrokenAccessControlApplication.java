package org.fugerit.java.demo.lab.broken.access.control;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Broken Access Control Lab - Spring Boot Application
 *
 * Un laboratorio educativo per testare e comprendere le vulnerabilità 
 * Broken Access Control nelle applicazioni Java.
 *
 * Convertito dal progetto quarkus https://github.com/lab-sca/lab-broken-access-control-quarkus
 *
 * @author Matteo Franci a.k.a. Fugerit
 * @version 1.0.0
 */
@SpringBootApplication
public class BrokenAccessControlApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrokenAccessControlApplication.class, args);
    }

}
