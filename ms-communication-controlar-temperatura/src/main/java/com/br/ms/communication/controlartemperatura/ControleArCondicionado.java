package com.br.ms.communication.controlartemperatura;

import java.io.IOException;

import java.time.LocalDateTime;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.br.ms.communication.controlartemperatura.gateway.json.QueryJson;
import com.br.ms.communication.controlartemperatura.gateway.json.SensorJson;
import com.br.ms.communication.controlartemperatura.regras.RegraControleTemperatura;
import com.br.ms.communication.controlartemperatura.service.model.MedicaoTemperatura;
import com.br.ms.communication.controlartemperatura.service.processar.RequestHTTP;
import com.fasterxml.jackson.core.JsonParseException;

@Component
public class ControleArCondicionado {

	@Value("${ligar-ac}")
	private String urlLigarAc;
	
	@Value("${temperatura-ac}")
	private String urlTemperaturaAc;

	@Value("${elastic-query}")
	private String urlElasticQuery;

	@Value("${tempo-busca-minutos}")
	private int tempoBuscaMinutos;
	
	@Value("${tempo-execucao}")
	private int tempoExecucao;

	RequestHTTP requestHttp = new RequestHTTP();
	
	LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("GMT-03:00"));

	DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'-03:00'")
			.withZone(ZoneId.of("GMT-03:00"));


	
	@Scheduled(fixedDelayString = "${tempo-execucao}")
	public void controlarTemperaturaAmbiente() throws JsonParseException, IOException, Exception {
		
		String dataHoraInicioProcesso = localDateTime.format(dateTimeFormatter);
		
		System.out.println("Iniciando execução--------------------------------------------------------------------------");
		System.out.println("Iniciado em: " + dataHoraInicioProcesso);
		// Etapa 1: buscar a medição de temperatura e umidade atuais, com uma média dos
		// últimos x minutos (a parametrização é feita via properties

		MedicaoTemperatura medicaoTemperaturaAtual = buscarDadosSensoresElastic();
		System.out.println("Etapa 1 concluída...");
		System.out.println("Response code: " + medicaoTemperaturaAtual.getResponseCode());
		System.out.println("Temperatura média medida: " + medicaoTemperaturaAtual.getTemperaturaMedia());
		System.out.println("Umidade média medida: " + medicaoTemperaturaAtual.getUmidadeMedia());

		// Etapa 2: se existir uma medicao válida, calcular o Heat Index atual e qual o
		// Heat Index a alcançar de acordo com a umidade relativa

		int responseCode = medicaoTemperaturaAtual.getResponseCode();
		double temperaturaMediaAtual = medicaoTemperaturaAtual.getTemperaturaMedia();
		double umidadeMediaAtual = medicaoTemperaturaAtual.getUmidadeMedia();

		if ((temperaturaMediaAtual != 0.0 && umidadeMediaAtual != 0.0) || responseCode != 200) {

			System.out.println("Existe medição válida");
			RegraControleTemperatura regraControleTemperatura = new RegraControleTemperatura();

			// Temperatura ideal em 24°C/75,2°F
			double heatIndexIdeal = regraControleTemperatura.calcularHeatIndex(75.2, umidadeMediaAtual);
			double heatIndexReal = regraControleTemperatura.calcularHeatIndex(
					regraControleTemperatura.converterTemperaturaParaFahrenheit(temperaturaMediaAtual),
					umidadeMediaAtual);
			System.out.println("Heat index ideal: " + heatIndexIdeal);
			System.out.println("Heat index real: " + heatIndexReal);
			

			// Se houver uma diferença entre o Heat Index ideal e o Heat Index atual,
			// acionar o AC na temperatura calculada pelo Heat Index ideal
			
			int temperaturaIdeal = (int) Math.round(heatIndexIdeal);
			int temperaturaReal = (int)  Math.round(heatIndexReal);
			System.out.println("Temperatura ideal: " + temperaturaIdeal);
			System.out.println("Temperatura real: " + temperaturaReal);
			
			if((temperaturaIdeal - temperaturaReal) != 0) {
				ligarArCondicionado();
				
				int temperaturaIdealCelsius = (int) Math.round(regraControleTemperatura.converterTemperaturaParaCelsius(heatIndexIdeal));
				
				ajustarTemperaturaArCondicionado(temperaturaIdealCelsius);
				System.out.println("Ajuste de temperatura");
				System.out.println("Temperatura de ajuste: " + temperaturaIdealCelsius);
				
			} else {
				System.out.println("Temperatura ideal e temperatura real são iguais");
			}

		} else { //Se não tiver medição, define temperatura padrão
			
			System.out.println("Não existe medição válida");
			ligarArCondicionado();
			ajustarTemperaturaArCondicionado(24);
			System.out.println("Ajuste de temperatura para padrão");
			
		}
		
		long proximaExecucao = tempoExecucao / 1000;
		
		String dataHoraFimProcesso = localDateTime.format(dateTimeFormatter);
		String dataHoraInicioProximaExecucao = localDateTime.plusSeconds(proximaExecucao).format(dateTimeFormatter);
		
		System.out.println("Finalizando execução--------------------------------------------------------------------------");
		System.out.println("Finalizado em: " + dataHoraFimProcesso);
		System.out.println("Proxima execução em: " + dataHoraInicioProximaExecucao);

	}

	public void ligarArCondicionado() {

		SensorJson sensorJson = new SensorJson();

		int responseCode = requestHttp.PostArCondicionado(sensorJson, urlLigarAc);

		System.out.println("Ligando AC, no endpoint " + urlLigarAc);
		System.out.println("Response code: " + responseCode);

	}
	
	public void ajustarTemperaturaArCondicionado(int temperatura) {

		SensorJson sensorJson = new SensorJson();

		int responseCode = requestHttp.PostArCondicionado(sensorJson, urlTemperaturaAc + temperatura);

		System.out.println("Ajuste de temperatura para " + temperatura + "°C, no endpoint " + urlTemperaturaAc + temperatura);
		System.out.println("Response code: " + responseCode);

	}
	



	public MedicaoTemperatura buscarDadosSensoresElastic() throws JsonParseException, IOException, Exception {


		String dataHoraInicio = localDateTime.minusMinutes(tempoBuscaMinutos).format(dateTimeFormatter);

		System.out.println("Data e hora iniciais: " + dataHoraInicio);

		String dataHoraAtual = localDateTime.format(dateTimeFormatter);

		System.out.println("Data e hora atuais: " + dataHoraAtual);

		QueryJson queryJson = new QueryJson();

		queryJson.setQuery("SELECT avg(temperatura), avg(umidade) FROM sensores WHERE timeStamp between '"
				+ dataHoraInicio + "' and '" + dataHoraAtual + "'");
		
		System.out.println("Query Elasticsearch: " + queryJson.getQuery());

		return requestHttp.PostSensorElastic(queryJson, urlElasticQuery);

	}

}
