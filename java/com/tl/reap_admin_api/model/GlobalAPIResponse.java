package com.tl.reap_admin_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalAPIResponse {
    private String status;
	private String message;
	private String result;
	

	public GlobalAPIResponse() {

	}
	

	public GlobalAPIResponse(String status, String message) {
		this.status = status;
		this.message = message;
	}
	


	public GlobalAPIResponse(String status, String message, String result) {
		this.status = status;
		this.message = message;
		this.result = result;
	}


	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


	public String getResult() {
		return result;
	}


	public void setResult(String result) {
		this.result = result;
	}
}
