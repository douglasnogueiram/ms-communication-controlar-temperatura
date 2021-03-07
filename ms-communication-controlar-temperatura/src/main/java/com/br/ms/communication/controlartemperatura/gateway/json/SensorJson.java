package com.br.ms.communication.controlartemperatura.gateway.json;

import java.math.BigDecimal;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorJson {
	
	private BigDecimal umidade;
	
	private BigDecimal temperatura;
	
	private String redeWifi;
	
	private String ip;
	
	private String macAddress;
	
	private String timeStamp;

}
