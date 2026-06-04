package it.fabiopetricola.springmvc.appback.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.fabiopetricola.springmvc.appback.service.EsempioService;
import it.fabiopetricola.springmvc.commons.response.ErrorResponse;
import it.fabiopetricola.springmvc.commons.response.JsonResponse;

/**
 * Servizio REST di esempio documentato con swagger 3.0
 * @author Fabio Petricola
 * */
@RestController
@Tag(name = "Servizio di esempio")
@RequestMapping(value="/example")
public class EsempioApi {
	
	@Autowired
	private EsempioService esempioService;
	
	@Operation(summary="Servizio che testa il funzionamento di lettura delle properties esterne",description="Servizio che testa il funzionamento di lettura delle properties esterne")
	@ApiResponses(value= {
			@ApiResponse(
					responseCode = "200",
					description = "Lettura properties esterni ok",
					content = {
						@Content(
							mediaType = "application/json",
							schema=@Schema(implementation = JsonResponse.class)) 
					}),
			@ApiResponse(responseCode="500",description = "Errore durante la lettura dei file di properties")
	})
	@RequestMapping(value="/example",method=RequestMethod.GET)
	public JsonResponse<String> esempio(){
		JsonResponse<String> jsonResponse=new JsonResponse<String>();
		ErrorResponse error=null;
		try {
			jsonResponse.setResponse(esempioService.defaultMessage());
		}catch(Exception e) {
			error=new ErrorResponse();
			error.setCode("500");
			error.setMsg(e.getMessage());
		}finally {
			if(error!=null) {
				jsonResponse.setError(error);
			}
		}
		return jsonResponse;
	}
	
}
