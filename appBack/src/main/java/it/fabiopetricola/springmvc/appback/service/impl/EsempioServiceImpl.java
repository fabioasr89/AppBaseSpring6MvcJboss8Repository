package it.fabiopetricola.springmvc.appback.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import it.fabiopetricola.springmvc.appback.service.EsempioService;
import it.fabiopetricola.springmvc.commons.exceptions.ConfigException;
/**
 * Implementazione del servizio di business di esempio
 * @author Fabio Petricola
 * */

@Service
public class EsempioServiceImpl implements EsempioService{
	@Autowired
	Environment environment;
	
	@Override
	public String defaultMessage() throws ConfigException{
		
		return environment.getProperty("messaggio.default");
	}

}
