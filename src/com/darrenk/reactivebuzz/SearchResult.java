package com.darrenk.reactivebuzz;

import org.json.simple.JSONObject;

/**
 * Interface for REST API search results
 */
public interface SearchResult {
	
	public JSONObject getJSON();
	
	public String getJSONString();
	
	public String getName();
}
