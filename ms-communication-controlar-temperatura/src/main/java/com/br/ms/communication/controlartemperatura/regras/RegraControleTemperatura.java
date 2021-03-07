package com.br.ms.communication.controlartemperatura.regras;

public class RegraControleTemperatura {
	

	public double converterTemperaturaParaCelsius(double temperaturaFahrenheit) {

		return ((temperaturaFahrenheit - 32.0) * (5.0 / 9.0));

	}
	
	public double converterTemperaturaParaFahrenheit(double temperaturaCelsius) {

		return ((temperaturaCelsius * 9.0 / 5.0) + 32.0);

	}

	public double calcularHeatIndex(double temperaturaFahrenheit, double umidadeRelativa) {
		
		

		// Passo 1: Executa a função simples (Steadman) para definir o Heat Index
		double heatIndexSimplificado = 0.5
				* (temperaturaFahrenheit + 61.0 + ((temperaturaFahrenheit - 68.0) * 1.2) + (umidadeRelativa * 0.094));
		System.out.println("Simplificado: " + heatIndexSimplificado);

		// Passo 2: se o valor obtido for abaixo de 80°F, ele deve ser recalculado
		// utilizando a regressão de Rothfusz

		if (heatIndexSimplificado >= 80.0) {
			double heatIndex = (-42.379 + 2.04901523 * temperaturaFahrenheit + 10.14333127 * umidadeRelativa
					- 0.22475541 * temperaturaFahrenheit * umidadeRelativa
					- 0.00683783 * temperaturaFahrenheit * temperaturaFahrenheit
					- 0.05481717 * umidadeRelativa * umidadeRelativa
					+ 0.00122874 * temperaturaFahrenheit * temperaturaFahrenheit * umidadeRelativa
					+ 0.00085282 * temperaturaFahrenheit * umidadeRelativa * umidadeRelativa
					- 0.00000199 * temperaturaFahrenheit * temperaturaFahrenheit * umidadeRelativa * umidadeRelativa);

			// Passo 2.1: ao realizar o cálculo, é necessário fazer a suavização/ajuste, nas
			// condições:
			// If the RH is less than 13% and the temperature is between 80 and
			// 112 degrees F, then the following adjustment is subtracted from HI:
			if (umidadeRelativa < 0.13 && (temperaturaFahrenheit >= 80.0 && temperaturaFahrenheit <= 112.0)) {
				heatIndex = heatIndex - ((13.0 - umidadeRelativa) / 4)
						* Math.sqrt((17 - Math.abs(temperaturaFahrenheit - 95.0)) / 17.0);
			}
			// On the other hand, if the RH is greater than 85% and the temperature is
			// between 80 and 87 degrees F, then the following adjustment is added to HI:
			if (umidadeRelativa > 0.85 && (temperaturaFahrenheit >= 80.0 && temperaturaFahrenheit <= 87.0)) {
				heatIndex = heatIndex + ((umidadeRelativa - 85.0) / 10.0) * ((87 - temperaturaFahrenheit) / 5);
			}

			return heatIndex;

		} else {
			return heatIndexSimplificado;
		}

	}

}
