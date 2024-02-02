package com.spring.petDoctor.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GPTConf {

    @Value("${openai.api-key}")
    private String apiKey;


    public String getApiKey(){
        return apiKey;
    }
}
