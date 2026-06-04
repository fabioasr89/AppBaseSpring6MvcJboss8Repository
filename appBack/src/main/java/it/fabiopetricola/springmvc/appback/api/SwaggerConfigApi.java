package it.fabiopetricola.springmvc.appback.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
/**
 * Classe di configurazione che espone il modello json dell'oggetto OpenApi
 * @author Fabio Petricola
 * */
@RestController
@RequestMapping("/openapi")
public class SwaggerConfigApi {
	
	@Autowired
	private OpenAPI openApi;
	
	@GetMapping("/v3/api-docs")
	public String getOpenApiDocs() {
		return Json.pretty(openApi);
	}
}
