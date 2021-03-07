package com.br.ms.communication.controlartemperatura.service.processar;

import java.io.IOException;

import java.util.Arrays;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.br.ms.communication.controlartemperatura.gateway.json.QueryJson;
import com.br.ms.communication.controlartemperatura.gateway.json.SensorJson;
import com.br.ms.communication.controlartemperatura.service.model.MedicaoTemperatura;
import com.fasterxml.jackson.core.JsonParseException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


@Component
@RestController
public class RequestHTTP {
	

	@RequestMapping(value = "/ac/", method = RequestMethod.POST)
	public int PostArCondicionado(@RequestBody SensorJson sensorJson, String url) {

		//ArCondicionadoJson arCondicionadoJson = new ArCondicionadoJson();
		
		HttpHeaders headers = new HttpHeaders();

		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		HttpEntity<SensorJson> entity = new HttpEntity<SensorJson>(sensorJson, headers);

		RestTemplate restTemplate = new RestTemplate();

		try {
			
			System.out.println(restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody());
			return 201;

		} catch (HttpStatusCodeException e) {

			ResponseEntity<String> response = ResponseEntity.status(e.getRawStatusCode())
					.headers(e.getResponseHeaders()).body(e.getResponseBodyAsString());
			
			System.out.println(response.toString());
			
			return e.getStatusCode().value();
		}
	}
	
	@RequestMapping(value = "/_sql/", method = RequestMethod.POST)
	public MedicaoTemperatura PostSensorElastic(@RequestBody QueryJson queryJson, String url) throws JsonParseException, Exception, IOException {

		//ArCondicionadoJson arCondicionadoJson = new ArCondicionadoJson();
		
		MedicaoTemperatura medicaoTemperatura = new MedicaoTemperatura();
		
		HttpHeaders headers = new HttpHeaders();

		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		HttpEntity<QueryJson> entity = new HttpEntity<QueryJson>(queryJson, headers);

		RestTemplate restTemplate = new RestTemplate();

		try {
			
			String response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
			
			JsonParser jsonParser = new JsonParser();
			
			JsonElement element = jsonParser.parse(response);
			
			JsonObject jsonObject = element.getAsJsonObject();
			
			JsonArray jsonArray = jsonObject.get("rows").getAsJsonArray();
			
			
			String row = jsonArray.toString();
			
			row = row.replaceAll("\\[", "");
			row = row.replaceAll("\\]", "");
			
			String rows[] = row.split(",");
			
			String temperaturaMedia = rows[0];
			String umidadeMedia = rows[1];
			
			temperaturaMedia = temperaturaMedia.replace("null", "");
			umidadeMedia = umidadeMedia.replace("null", "");
			
			medicaoTemperatura.setResponseCode(200);
			
			
			try {

			medicaoTemperatura.setTemperaturaMedia(Double.parseDouble(temperaturaMedia));
			medicaoTemperatura.setUmidadeMedia(Double.parseDouble(umidadeMedia));
			
			System.out.println("Response sucesso: " + response.toString());
			System.out.println("Temperatura obtida: " + Double.parseDouble(temperaturaMedia));
			System.out.println("Umidade medida: " + Double.parseDouble(umidadeMedia));
			} catch (NumberFormatException e) {
				System.out.println("ERRO! Não existem valores de temperatura e/ou umidade no período");
				System.out.println("Erro: " + e.toString());
				System.out.println("Response sucesso: " + response.toString());
				System.out.println("Temperatura obtida: " + medicaoTemperatura.getTemperaturaMedia());
				System.out.println("Umidade medida: " + medicaoTemperatura.getUmidadeMedia());
				
			}
			
			
			return medicaoTemperatura;

		} catch (HttpStatusCodeException e) {
			
			ResponseEntity<String> response = ResponseEntity.status(e.getRawStatusCode())
					.headers(e.getResponseHeaders()).body(e.getResponseBodyAsString());
			
			System.out.println("Response erro: " + response.toString());
			
			//medicaoTemperatura.setTemperaturaMedia(0.0);
			//medicaoTemperatura.setUmidadeMedia(0.0);
			
			medicaoTemperatura.setResponseCode(e.getStatusCode().value());
			
			return medicaoTemperatura;
		}
	}
}