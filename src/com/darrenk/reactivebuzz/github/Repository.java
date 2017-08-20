package com.darrenk.reactivebuzz.github;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import com.darrenk.reactivebuzz.SearchResult;

/**
 * Represents a subset of the data from a GitHub Repository JSONObject
 * as returned by a REST API query to GitHub.
 */
public class Repository implements SearchResult {
	private JSONObject synopsis;
	private String name;

	/**
	 * Constructor
	 * @param result JSONOjbect for a Repository
	 */
	public Repository(JSONObject result) {
		Map<String, String> elements = new HashMap<String,String>();
		elements.put("project_name", (String) result.get("name"));
		elements.put("project_full_name", (String) result.get("full_name"));
		elements.put("github_address", (String) result.get("html_url"));
		String description = (String) result.get("description");
		if (description == null) {
			description = "";
		}
		String homepage = (String) result.get("homepage");
		if (homepage != null) {
			elements.put("summary", description + " (" + homepage + ")");
		} else {
			elements.put("summary", description);
		}
		
		synopsis = new JSONObject(elements);
		name = (String) result.get("full_name");
	}
	
	@Override
	public JSONObject getJSON() {
		return synopsis;
	}

	@Override
	public String getJSONString() {
		return synopsis.toJSONString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Repository: ");
		sb.append(synopsis.get("project_full_name"));
		sb.append(" (");
		sb.append(synopsis.get("github_address"));
		sb.append(")\nProject: ");
		sb.append(synopsis.get("project_name"));
		sb.append("\n");
		sb.append(synopsis.get("summary"));
		return sb.toString();
	}
	
	@Override
	public String getName() {
		return name;
	}
}
