package com.darrenk.reactivebuzz;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class URLConnectionStub extends HttpURLConnection {

	protected int responseCode = HTTP_OK;
	
	protected URLConnectionStub() {
		super(null);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		// Return a JSON array string
		return new ByteArrayInputStream(AbstractRESTClientTest.jsonResults.getBytes());
	}
	
	@Override
	public InputStream getErrorStream() {
		return new ByteArrayInputStream("{\"error\":\"something went wrong\"}".getBytes());
	}
	
	@Override
	public int getResponseCode() {
		return this.responseCode;
	}
	
	public void setReponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	
	@Override
	public void connect() throws IOException {
	}

	@Override
	public void disconnect() {
	}

	@Override
	public boolean usingProxy() {
		return false;
	}

}
