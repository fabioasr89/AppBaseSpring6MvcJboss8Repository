package it.fabiopetricola.springmvc.appback.swagger;

import java.util.Set;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import it.fabiopetricola.springmvc.appback.api.EsempioApi;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
@ApplicationPath("/openapi")
public class SwaggerApiServlet extends Application{
	
	public SwaggerApiServlet() {
        super();
        
        // Configuriamo Swagger Core dicendogli ESPLICITAMENTE di filarsi il controller Spring
        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                .prettyPrint(true)
                .resourceClasses(Set.of(EsempioApi.class.getName())); 

        try {
            new io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder<>()
                    .application(this)
                    .openApiConfiguration(oasConfig)
                    .buildContext(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	//configuro il controller open api per gestire i servizi di swagger
	@Override
	public Set<Class<?>> getClasses(){
		return Set.of(OpenApiResource.class);
	}
}
