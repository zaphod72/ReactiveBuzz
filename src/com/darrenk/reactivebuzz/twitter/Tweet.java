package com.darrenk.reactivebuzz.twitter;

import org.json.simple.JSONObject;

import com.darrenk.reactivebuzz.SearchResult;

/**
 * Stores the data from a Twitter Tweet JSONObject
 * as returned by a REST API query to Twitter.
 */
public class Tweet implements SearchResult {
	private JSONObject tweet;
	private String name;

	/**
	 * Constructor
	 * @param result JSONOjbect for a Repository
	 */
	public Tweet(JSONObject tweet) {
		this.tweet = tweet;
		this.name = (String) tweet.get("id_str");
	}
	
	@Override
	public JSONObject getJSON() {
		return tweet;
	}

	@Override
	public String getJSONString() {
		return tweet.toJSONString();
	}
	
	@Override
	public String getName() {
		return name;
	}
}
