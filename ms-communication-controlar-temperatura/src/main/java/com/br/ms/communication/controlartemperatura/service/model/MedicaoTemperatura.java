package com.br.ms.communication.controlartemperatura.service.model;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicaoTemperatura {
	
	private int responseCode;
	private double temperaturaMedia;
	private double umidadeMedia;

}
