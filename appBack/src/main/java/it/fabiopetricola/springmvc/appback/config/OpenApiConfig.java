package it.fabiopetricola.springmvc.appback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
/**
 * Classe di configurazione che istanzia nel contesto spring l'oggetto OpenAPI
 * @author Fabio Petricola
 * */
@Configuration
public class OpenApiConfig {
	
	
	@Bean
	OpenAPI openApi() {
		return new OpenAPI();
	}
}
