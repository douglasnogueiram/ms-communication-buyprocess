package com.br.ms.communication.buyprocess.service.bank;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.br.ms.communication.buyprocess.gateway.json.CompraChaveJson;

@Configuration
public class CompraChaveBean {

    @Bean
    public CompraChaveJson compraChaveJson() {
        return new CompraChaveJson();
    }
}

