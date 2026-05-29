package it.fabiopetricola.springmvc.commons.response;

public class JsonResponse<E>{
	
	private E response;
	private ErrorResponse error;
	
	public JsonResponse() {}
	

	
	public E getResponse() {
		return response;
	}
	public void setResponse(E response) {
		this.response = response;
	}
	public ErrorResponse getError() {
		return error;
	}
	public void setError(ErrorResponse error) {
		this.error = error;
	}
	
	
}
