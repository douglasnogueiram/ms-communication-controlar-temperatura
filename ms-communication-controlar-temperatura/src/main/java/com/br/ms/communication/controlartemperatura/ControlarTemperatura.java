package com.br.ms.communication.controlartemperatura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.scheduling.annotation.EnableScheduling;


import co.elastic.apm.attach.ElasticApmAttacher;

@SpringBootApplication
@EnableCircuitBreaker
@EnableScheduling
public class ControlarTemperatura {

	public static void main(String[] args) throws InterruptedException {
		ElasticApmAttacher.attach();
		SpringApplication.run(ControlarTemperatura.class, args);

	}

}