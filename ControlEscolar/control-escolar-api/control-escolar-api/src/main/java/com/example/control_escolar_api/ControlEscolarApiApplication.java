package com.example.control_escolar_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.control_escolar_api")
public class ControlEscolarApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ControlEscolarApiApplication.class, args);
	}

}
