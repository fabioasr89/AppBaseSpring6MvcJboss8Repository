package it.fabiopetricola.springmvc.appback.service;

import it.fabiopetricola.springmvc.commons.exceptions.ConfigException;
/**
 * Interfaccia che espone il servizio di business di esempio
 *  @author Fabio Petricola
 * */
public interface EsempioService {
	
	String defaultMessage() throws ConfigException;
}
