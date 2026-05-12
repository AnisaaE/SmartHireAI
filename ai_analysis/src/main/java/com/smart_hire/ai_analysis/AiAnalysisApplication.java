package com.smart_hire.ai_analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AiAnalysisApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiAnalysisApplication.class, args);
	}

}
