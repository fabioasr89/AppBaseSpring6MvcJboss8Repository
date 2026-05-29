package it.fabiopetricola.springmvc.appback.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
/**
 * Classe di configurazione per il caricamento delle configurazioni esterne all'applicativo
 * 
 * */
@Configuration
@PropertySource(value = {"file:///opt/appWeb/config/esempio.properties"})
public class PropertiesConfig {

}
