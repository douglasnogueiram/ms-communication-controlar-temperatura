package com.br.ms.communication.controlartemperatura.gateway.json;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArCondicionadoJson {
	
	  private String model;
	  private String power;
	  private String modeFunc;
	  private int temperature;
	  private String fan;
	  private String clean;
	  private String filter;
	  private String swing;
	  private String command;
	  private String quiet;

}
