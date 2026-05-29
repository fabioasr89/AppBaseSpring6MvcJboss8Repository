package it.fabiopetricola.springmvc.appback.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.resource.ResourceTransformer;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import jakarta.servlet.http.HttpServletRequest;
/**
 * Trasformerresource custom creato per sovrascrivere gli url di default dei webjars interni allo swagger-ui
 * con gli url esposti dallo swagger applicativo
 * 
 * */
public class SwaggerTrasformerResource implements ResourceTransformer{

	@Override
	public Resource transform(HttpServletRequest request, Resource resource, ResourceTransformerChain transformerChain)
			throws IOException {
		String fileName=resource.getFilename();
		if("swagger-initializer.js".equals(fileName) || "index.html".equals(fileName)) {
			//trasformo il contenuto del file in stringa e sostituisco gli url
			String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
		
			 content = content.replace("https://petstore.swagger.io/v2/swagger.json", 
                     "/appBack/openapi/openapi.json");
			 return new TransformedResource(resource, content.getBytes(StandardCharsets.UTF_8));
		}
		return transformerChain.transform(request,resource);
	}

}
