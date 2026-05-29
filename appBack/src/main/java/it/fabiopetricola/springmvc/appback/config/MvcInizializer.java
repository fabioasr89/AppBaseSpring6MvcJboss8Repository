package it.fabiopetricola.springmvc.appback.config;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
/**
 * Classe di configurazione per la definizione della DispatcherServlet di spring senza
 * web.xml e l'abilitazione delle annotation per la gestione delle scansioni spring
 * 
 * **/
public class MvcInizializer implements WebApplicationInitializer{

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		
		AnnotationConfigWebApplicationContext context=new AnnotationConfigWebApplicationContext();
		context.register(MvcConfig.class);
		DispatcherServlet dispatcher=new DispatcherServlet(context);
		ServletRegistration.Dynamic registration=servletContext.addServlet("dispatcher", dispatcher);
		registration.addMapping("/");
		registration.setLoadOnStartup(1);
	}

}
