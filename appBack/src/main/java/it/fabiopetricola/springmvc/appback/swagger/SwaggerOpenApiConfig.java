package it.fabiopetricola.springmvc.appback.swagger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import it.fabiopetricola.springmvc.appback.api.SwaggerConfigApi;
/**
 * Classe di configurazione che si avvia subito dopo il contesto di spring e che
 * scansiona tramite il meccanismo delle java reflection i tag openAPi dei nostri
 * RestController e li setta dentro l'oggetto OpenAPI
 * @author Fabio Petricola
 * */

@Configuration
public class SwaggerOpenApiConfig {
	@Autowired
	private OpenAPI openApi;
	
	private final static String ROOT_CONTEXT="/appBack";
	
	@EventListener(value = ContextRefreshedEvent.class)
	public void addRestControllerInOpenApiContext(ContextRefreshedEvent event) {
		ApplicationContext appContext=event.getApplicationContext();
		//recupero i rest controller
		 Map<String, Object> restControllers=appContext.getBeansWithAnnotation(RestController.class);
		 restControllers.forEach((name,bean)->{
			Class<?> controllerClass=ClassUtils.getUserClass(bean);
			if(controllerClass.equals(SwaggerConfigApi.class)) {
				//tolgo dalla scansione il rest controller che espone il servizio json di swagger
				return;
			}
			io.swagger.v3.oas.annotations.tags.Tag tagRest = 
				    AnnotationUtils.findAnnotation(controllerClass, io.swagger.v3.oas.annotations.tags.Tag.class);	
			if(tagRest!=null) {
				Tag tag=createTag(tagRest.name(), tagRest.description());
				this.openApi.addTagsItem(tag);
			}
			
			io.swagger.v3.oas.annotations.media.Schema schemaRest = 
				    AnnotationUtils.findAnnotation(controllerClass, io.swagger.v3.oas.annotations.media.Schema.class);
			if(schemaRest!=null) {
				Schema schema=createSchema(schemaRest.name(),schemaRest.description());
				this.openApi.schema(schema.getName(), schema);
			}
			if (this.openApi.getPaths() == null) {
                this.openApi.setPaths(new io.swagger.v3.oas.models.Paths());
            }
			String urlBaseRest="";
			if (this.openApi.getServers() == null || this.openApi.getServers().isEmpty()) {
			    io.swagger.v3.oas.models.servers.Server server = new io.swagger.v3.oas.models.servers.Server();
			    // Se il contextPath è vuoto usa "/", altrimenti usa il path configurato
			    server.setUrl(ROOT_CONTEXT);
			    server.setDescription("Server Applicativo");
			    this.openApi.setServers(java.util.Collections.singletonList(server));
			}
			Map<String,Object> datiMapping=getPathInfoFromMethod(controllerClass,null);
			if(!datiMapping.isEmpty()) {
				urlBaseRest=(String) datiMapping.get("path");
			}
			Method[] methods=controllerClass.getDeclaredMethods();
			ApiResponses apiResponsesModel=null;
			Operation operation=null;
			for(Method method:methods) {
				operation = new Operation();
				io.swagger.v3.oas.annotations.responses.ApiResponses apiResponses=AnnotationUtils.findAnnotation(method, io.swagger.v3.oas.annotations.responses.ApiResponses.class);
				if(apiResponses!=null) {
					io.swagger.v3.oas.annotations.responses.ApiResponse[] dataApiResponse= apiResponses.value();
					apiResponsesModel=createApiResponses(apiResponses);
	                operation.setResponses(apiResponsesModel);
				}
			
                if (tagRest != null) {
                     operation.addTagsItem(tagRest.name()); // Lega il metodo al tag del controller
                }
				
				Map<String,Object> datiMappingMethod=getPathInfoFromMethod(null,method);
				String urlMethod="";
				RequestMethod requestMethod=null;
				if(!datiMappingMethod.isEmpty()) {
					urlMethod=(String) datiMappingMethod.get("path");
				    requestMethod=(RequestMethod) datiMappingMethod.get("method");
				    
				    // 1. Calcoli prima l'url reale completo dell'endpoint
				    String urlCompleto=generaPathCompleto(urlBaseRest, urlMethod);
				    
				    // 2. Recuperi il PathItem usando l'URL COMPLETO
				    io.swagger.v3.oas.models.PathItem pathItem = this.openApi.getPaths().get(urlCompleto);
				    
				    // 3. Se non esiste ancora per questo URL, lo crei e lo inserisci nell'OpenAPI
				    if (pathItem == null) {
				        pathItem = new io.swagger.v3.oas.models.PathItem();
				        this.openApi.getPaths().addPathItem(urlCompleto, pathItem);
				    }
					switch (requestMethod) {
					case GET:
						pathItem.setGet(operation);
						break;
					case POST:
						pathItem.setPost(operation);
						break;
					case PUT:
						pathItem.setPut(operation);
						break;
					case DELETE:
						pathItem.setDelete(operation);
						break;
					default:
						break;
					}
					List<io.swagger.v3.oas.models.parameters.Parameter> parameters=getParametersFormMethod(method);
					
					operation.setParameters(parameters);
					//GESTIONE DEI PARAMETRI COMPLESSI, COME REQUESTBODY E REQUESTPART
					java.lang.reflect.Parameter[] javaParameters = method.getParameters();
					io.swagger.v3.oas.models.parameters.RequestBody requestBodyModel = null;
					Schema<Object> multipartSchema = null;
					Content contentModel = null;
					for (java.lang.reflect.Parameter javaParam : javaParameters) {
					    org.springframework.web.bind.annotation.RequestBody requestBodyAnno = 
					            AnnotationUtils.findAnnotation(javaParam, org.springframework.web.bind.annotation.RequestBody.class);
					    org.springframework.web.bind.annotation.RequestPart requestPartAnno = 
					            AnnotationUtils.findAnnotation(javaParam, org.springframework.web.bind.annotation.RequestPart.class);
					    if (requestBodyAnno != null) {
					        Class<?> bodyClass = javaParam.getType();
					        
					        // Converto il DTO in schemi OpenAPI (lo registriamo nei Components globali)
					        Map<String, Schema> bodySchemas = ModelConverters.getInstance().readAll(bodyClass);
					        if (this.openApi.getComponents() == null) {
					            this.openApi.setComponents(new io.swagger.v3.oas.models.Components());
					        }
					        bodySchemas.forEach((schemaName, schema) -> this.openApi.getComponents().addSchemas(schemaName, schema));
					        
					        // creo il RequestBody per l'operazione
					        requestBodyModel = new io.swagger.v3.oas.models.parameters.RequestBody();
					        requestBodyModel.setRequired(requestBodyAnno.required());
					        
					        // Creo il Content (di default application/json)
					        contentModel = new Content();
					        MediaType mediaTypeModel = new MediaType();
					        mediaTypeModel.setSchema(new Schema<>().$ref("#/components/schemas/" + bodyClass.getSimpleName()));
					        contentModel.addMediaType("application/json", mediaTypeModel);
					        
					        requestBodyModel.setContent(contentModel);
					        
					        // Agganciamo il RequestBody all'operazione
					        operation.setRequestBody(requestBodyModel);
					       
					    }
					    //GESTIONE DEI REQUEST PART
					    if (requestPartAnno != null) {
					        if (requestBodyModel == null) {
					            requestBodyModel = new io.swagger.v3.oas.models.parameters.RequestBody();
					            requestBodyModel.setRequired(true); // Se c'è un upload, il body multipart è richiesto
					            contentModel = new Content();
					            
					            // Creiamo lo schema contenitore ad hoc per l'oggetto del form
					            multipartSchema = new Schema<>();
					            multipartSchema.setType("object");
					            
					            MediaType mediaTypeModel = new MediaType();
					            mediaTypeModel.setSchema(multipartSchema);
					            contentModel.addMediaType("multipart/form-data", mediaTypeModel);
					            requestBodyModel.setContent(contentModel);
					            operation.setRequestBody(requestBodyModel);
					        }
					        
					        // Determiniamo il nome della proprietà nel form (es. "file", "documento", "datiDettaglio")
					        String partName = requestPartAnno.value().isEmpty() ? requestPartAnno.name() : requestPartAnno.value();
					        if (partName.isEmpty()) {
					            partName = javaParam.getName();
					        }
					        
					        // Determiniamo lo schema del singolo pezzo (se è un file o un DTO)
					        Class<?> partType = javaParam.getType();
					        Schema<?> partSchema = null;
					        
					        if (partType.getName().equals("org.springframework.web.multipart.MultipartFile") || 
					            partType.getName().equals("jakarta.servlet.http.Part")) {
					            // È un file binario
					            partSchema = new Schema<>();
					            partSchema.setType("string");
					            partSchema.setFormat("binary");
					        } else {
					            // È un DTO complesso inviato insieme al file (es. application/json dentro il form)
					            Map<String, Schema> partSchemas = ModelConverters.getInstance().readAll(partType);
					            if (this.openApi.getComponents() == null) {
					                this.openApi.setComponents(new io.swagger.v3.oas.models.Components());
					            }
					            partSchemas.forEach((schemaName, schema) -> this.openApi.getComponents().addSchemas(schemaName, schema));
					            partSchema = new Schema<>().$ref("#/components/schemas/" + partType.getSimpleName());
					        }
					        
					        // Aggiungiamo questa proprietà all'oggetto multipart globale del metodo
					        multipartSchema.addProperty(partName, partSchema);
					    }
					    
					}
				}
				
			}
		 });
	}
	
	
	private List<io.swagger.v3.oas.models.parameters.Parameter> getParametersFormMethod(Method method) {
		List<io.swagger.v3.oas.models.parameters.Parameter> parameters=new ArrayList<io.swagger.v3.oas.models.parameters.Parameter>();
		java.lang.reflect.Parameter[] javaParameters = method.getParameters();
		for (java.lang.reflect.Parameter javaParam : javaParameters) {
			if (AnnotationUtils.findAnnotation(javaParam, org.springframework.web.bind.annotation.RequestBody.class) != null) {
	            continue; 
	        }
		    // Cerchiamo l'annotazione @Parameter di Swagger sul parametro in input
		    io.swagger.v3.oas.annotations.Parameter paramAnno = 
		            AnnotationUtils.findAnnotation(javaParam, io.swagger.v3.oas.annotations.Parameter.class);
		    
		    // Controlliamo se è un parametro di Spring (@PathVariable o @RequestParam)
		    org.springframework.web.bind.annotation.PathVariable pathVar = 
		            AnnotationUtils.findAnnotation(javaParam, org.springframework.web.bind.annotation.PathVariable.class);
		            
		    org.springframework.web.bind.annotation.RequestParam reqParam = 
		            AnnotationUtils.findAnnotation(javaParam, org.springframework.web.bind.annotation.RequestParam.class);
		    
		    // Se è un parametro rilevante per l'API, creiamo il modello OpenAPI
		    if (pathVar != null || reqParam != null || paramAnno != null) {
		        io.swagger.v3.oas.models.parameters.Parameter parameterModel = new io.swagger.v3.oas.models.parameters.Parameter();
		        
		        //Determiniamo il NOME del parametro
		        String paramName = javaParam.getName(); // Fallback sul nome del parametro Java
		        if (pathVar != null && !pathVar.value().isEmpty()) paramName = pathVar.value();
		        if (reqParam != null && !reqParam.value().isEmpty()) paramName = reqParam.value();
		        if (paramAnno != null && !paramAnno.name().isEmpty()) paramName = paramAnno.name();
		        
		        parameterModel.setName(paramName);
		        
		        //  Determiniamo DOVE si trova (IN: path o query)
		        if (pathVar != null) {
		            parameterModel.setIn("path");
		            parameterModel.setRequired(true); 
		        } else if (reqParam != null) {
		            parameterModel.setIn("query");
		            parameterModel.setRequired(reqParam.required());
		        } else if (paramAnno != null) {
		            parameterModel.setIn(paramAnno.in().toString().toLowerCase());
		            parameterModel.setRequired(paramAnno.required());
		        }
		        
		        if (paramAnno != null) {
		            parameterModel.setDescription(paramAnno.description());
		        }
		        
		        // 4. Mappiamo il Tipo di Dato (Schema) in base alla classe Java (String, Integer, ecc.)
		        String typeName = javaParam.getType().getSimpleName().toLowerCase();
		        io.swagger.v3.oas.models.media.Schema<?> schema = new io.swagger.v3.oas.models.media.Schema<>();
		        
		        switch (typeName) {
		            case "int", "integer", "long" -> schema.setType("integer");
		            case "boolean" -> schema.setType("boolean");
		            case "double", "float" -> schema.setType("number");
		            default -> schema.setType("string"); // Per String, Date, UUID, ecc.
		        }
		        parameterModel.setSchema(schema);
		        
		        parameters.add(parameterModel);
		        
		        
		    }
		}
		return parameters;
	}
	
	
	
	private String generaPathCompleto(String basePath,String relativePath) {
		basePath=(basePath!=null)?basePath:"";
		relativePath=(relativePath!=null)?relativePath:"";
		if(!basePath.isEmpty() && !basePath.startsWith("/")) {
			basePath="/"+basePath;
		}
		if(basePath.endsWith("/")) {
			basePath=basePath.substring(0,basePath.length()-1);
		}
		if(!relativePath.isEmpty() && !relativePath.startsWith("/")) {
			relativePath="/"+relativePath;
		}
		return basePath+relativePath;
	}
	
	private Map<String,Object> getPathInfoFromMethod(Class classe,Method method) {
		Map<String,Object> datiPath=new LinkedHashMap<String,Object>();
		String keyMethod="method";
		String keyPath="path";
		RequestMethod requestMethod=null;
		RequestMapping requestMapping=null;
		String pathValue="";
		if(classe!=null) {
			requestMapping=AnnotatedElementUtils.findMergedAnnotation(classe, RequestMapping.class);
		}else if(method!=null) {
			requestMapping=AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
		}
		if(requestMapping!=null) {
			if (method != null) {
	            requestMethod = requestMapping.method().length > 0 ? requestMapping.method()[0] : RequestMethod.GET;
	        }
			pathValue = requestMapping.path().length > 0 ? requestMapping.path()[0] : "";
			if (pathValue.isEmpty() && requestMapping.value().length > 0) {
			    pathValue = requestMapping.value()[0];
			}
			datiPath.put(keyMethod, requestMethod);
			datiPath.put(keyPath, pathValue);
		}
		return datiPath;
	}
	
	private Tag createTag(String name,String description) {
		name=(name==null)?"":name;
		description=(description==null)?"":description;
		Tag tag=new Tag();
		tag.setDescription(description);
		tag.setName(name);
		return tag;
		
	}
	
	private Schema createSchema(String name,String description) {
		name=(name==null)?"":name;
		description=(description==null)?"":description;
		Schema schema=new Schema();
		schema.setDescription(description);
		schema.setName(name);
		return schema;
	}
	
	
	private Operation createOperation(String summary, String description) {
		summary=(summary==null)?"":summary;
		description=(description==null)?"":description;
		Operation operation=new Operation();
		operation.setSummary(summary);
		operation.setDescription(description);
		return operation;
	}
	
	private ApiResponses createApiResponses(io.swagger.v3.oas.annotations.responses.ApiResponses apiResponses) {
		if(apiResponses==null) {
			return null;
		}
		ApiResponses apiResponsesModel=new ApiResponses();
		io.swagger.v3.oas.annotations.responses.ApiResponse[] apiResponseList=apiResponses.value();
		for(io.swagger.v3.oas.annotations.responses.ApiResponse apiRes:apiResponseList) {
			ApiResponse apiResponseModel=new ApiResponse();
			apiResponseModel.setDescription(apiRes.description());
			
			io.swagger.v3.oas.annotations.media.Content[] contents=apiRes.content();
			Content contentModel=null;
			for(io.swagger.v3.oas.annotations.media.Content content:contents) {
				contentModel=new Content();
				if(content.schema()!=null && content.schema().implementation()!=Void.class) {
					Class<?> implementationClass = content.schema().implementation();
					// Questo metodo analizza ricorsivamente la classe e crea le definizioni dei campi.
				    Map<String, Schema> perClassSchemas = ModelConverters.getInstance().readAll(implementationClass);
				 // In questo modo Swagger sa come sono fatti i tuoi DTO a livello globale
				    if (openApi.getComponents() == null) {
				        openApi.setComponents(new io.swagger.v3.oas.models.Components());
				    }
				    perClassSchemas.forEach((schemaName, schema) -> {
				        openApi.getComponents().addSchemas(schemaName, schema);
				    });
				    Schema<?> referenceSchema = new Schema<>().$ref("#/components/schemas/" + implementationClass.getSimpleName());
				    MediaType mediaTypeModel = new MediaType();
				    mediaTypeModel.setSchema(referenceSchema);
				    
				    //eterminiamo il content-type (se non specificato nell'annotazione, usiamo application/json di default)
				    String mediaTypeName = (content.mediaType() != null && !content.mediaType().isEmpty()) 
				                            ? content.mediaType() 
				                            : "application/json";
				    
				    // 7. Agganciamo il MediaType al ContentModel
				    contentModel.addMediaType(mediaTypeName, mediaTypeModel);
				}
			}

			apiResponseModel.content(contentModel);
			apiResponsesModel.addApiResponse(apiRes.responseCode(), apiResponseModel);	
		}
		return apiResponsesModel;
	}
	
	public Content[] createContent(io.swagger.v3.oas.annotations.media.Content[] contents) {
		Content[] contentsModel=null;
		if(contents!=null && contents.length>0) {
			contentsModel=new Content[contents.length];
			for(io.swagger.v3.oas.annotations.media.Content contentAnnotation:contents) {
				Content contentModel=new Content();
				if(contentAnnotation.schema()!=null && contentAnnotation.schema().implementation()!=null) {
					
				}
			}
		}
		return null;
	}

}
