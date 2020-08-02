package com.br.ms.communication.buyprocess.service.processar;

import java.io.IOException;

import com.br.ms.communication.buyprocess.service.bank.PagamentoRetorno;
import com.br.ms.communication.buyprocess.service.bank.BankService;
import com.br.ms.communication.buyprocess.gateway.json.CompraChaveJson;
import com.br.ms.communication.buyprocess.gateway.json.CompraFinalizadaJson;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@Service
public class ListenerService {

	@Autowired
	private BankService bank;

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private Gson gson;

	@Value("${fila.entrada}")
	private String nomeFilaRepublicar;

	@Value("${fila.finalizado}")
	private String nomeFilaFinalizado;

	//@HystrixCommand(fallbackMethod = "republicOnMessage")
	@RabbitListener(queues="${fila.entrada}")
	//@HystrixProperty(name = "hystrix.command.default.execution.timeout.enabled", value = "false")
    public void onMessage(Message message) throws JsonSyntaxException, IOException  {
		
		System.out.println("Chegou mensagem na fila de entrada...");
		String json = new String(message.getBody(), "UTF-8");
		
		System.out.println("Mensagem recebida:"+json);
		

		CompraChaveJson compraChaveJson = gson.fromJson(json, CompraChaveJson.class);

		PagamentoRetorno pg = bank.pagar(compraChaveJson);
		

		CompraFinalizadaJson compraFinalizadaJson = new CompraFinalizadaJson();
		compraFinalizadaJson.setCompraChaveJson(compraChaveJson);
		compraFinalizadaJson.setPagamentoOK(pg.isPagamentoOK());
		compraFinalizadaJson.setMensagem(pg.getMensagem());
		
		
		String jsonFinalizado = gson.toJson(compraFinalizadaJson);
		
		/*
		org.codehaus.jackson.map.ObjectMapper obj = new org.codehaus.jackson.map.ObjectMapper();
		String jsonFinalizado = obj.writeValueAsString(compraFinalizadaJson);
		*/

		rabbitTemplate.convertAndSend(nomeFilaFinalizado, jsonFinalizado);
		System.out.println("Mensagem republicada: " + jsonFinalizado);
    }

	public void republicOnMessage(Message message) throws IOException  {
		System.out.println("Republicando mensagem...");
		rabbitTemplate.convertAndSend(nomeFilaRepublicar, message);
	}
}