package it.fabiopetricola.springmvc.appback.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * Classe di configurazione di spring mvc
 * @author Fabio Petricola
 * */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages= {"it.fabiopetricola.springmvc.appback.config","it.fabiopetricola.springmvc.appback.service.impl","it.fabiopetricola.springmvc.appback.api","it.fabiopetricola.springmvc.appback.swagger"})
public class MvcConfig implements WebMvcConfigurer{
	
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/swagger-ui/**")
		.addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/5.11.8/")
		.resourceChain(false)
		.addTransformer(new SwaggerTrasformerResource());
	}
	
	
}
