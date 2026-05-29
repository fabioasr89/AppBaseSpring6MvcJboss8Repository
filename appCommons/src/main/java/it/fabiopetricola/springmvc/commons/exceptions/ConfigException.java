package it.fabiopetricola.springmvc.commons.exceptions;

public class ConfigException extends Exception{

	private static final long serialVersionUID = 1L;
	private String message;
	public ConfigException() {
		super();
	}
	
	public ConfigException(String message) {
		super();
		this.message=message;
		
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
