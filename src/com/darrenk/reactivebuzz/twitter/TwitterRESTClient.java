package com.darrenk.reactivebuzz.twitter;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.json.simple.JSONObject;

import com.darrenk.reactivebuzz.AbstractRESTClient;
import com.darrenk.reactivebuzz.EncryptUtils;
import com.darrenk.reactivebuzz.SearchResult;
import com.darrenk.reactivebuzz.SearchResults;

/**
 * A REST API client to query for Twitter Tweets
 */
public class TwitterRESTClient extends AbstractRESTClient {
	private static final String BASE_API_URL = "https://api.twitter.com/1.1/";
	private static final int MAX_SEARCH_PAGE_SIZE = 100;
	private static final String SEARCH_TWEETS_API = "search/tweets.json";
	private static final String SEARCH_RECENT_TWEETS =
			SEARCH_TWEETS_API + "?result_type=recent&count=" + MAX_SEARCH_PAGE_SIZE + "&q=";

	/* OAUTH2 */
	private static final String BASE_OAUTH2_API_URL = "https://api.twitter.com/";
	private static final String OAUTH2_TOKEN = "oauth2/token";
	private static final String OAUTH2_TOKEN_REQ_BODY = "grant_type=client_credentials";
	private static final String OAUTH2_TOKEN_TYPE_VALUE = "Bearer";
	
	/**
	 * @see https://dev.twitter.com/rest/public/rate-limiting
	 */
	private static final String HEADER_RATE_LIMIT_REMAINING = "X-Rate-Limit-Remaining";
	private static final String HEADER_RATE_LIMIT_RESET = "X-Rate-Limit-Reset";
	private static final String RATE_LIMIT = "application/rate_limit_status.json?resources=search";
	
	/**
	 * Constructor to use for authenticated REST calls
	 * for Twitter OAUTH2 authentication and authorization.
	 * @param key Consumer key
	 * @param token Consumer secret
	 */
	public TwitterRESTClient(String key, String secret) throws IOException {
		super(key, secret, BASE_OAUTH2_API_URL);
		createConnection(getUrl(OAUTH2_TOKEN), HTTP_POST);
		setOAuthRequest();
		setBearerToken(getResponse(200));
		setBaseApiUrl(BASE_API_URL);
	}
	
	@Override
	protected String encode(String key, String token) {
		String tokenCreds = EncryptUtils.rfc1738encode(key) + ":" + EncryptUtils.rfc1738encode(token);
		String encoded = "Basic " + EncryptUtils.base64Encode(tokenCreds);
		return encoded;
	}
	
	@Override
	protected String getQueryResultsObjectName() {
		return "statuses";
	}
	
	@Override
	protected SearchResult createResult(JSONObject result) {
		return new Tweet(result);
	}
	
	/**
	 * Query for Twitter Tweets
	 * @param query The query parameter
	 * @return The first page of search results
	 * @throws IOException
	 */
	public SearchResults searchTweets(String query) throws IOException {
		return search(SEARCH_RECENT_TWEETS, query);
	}

	@Override
	protected boolean filterResult(JSONObject result) {
		return !result.containsKey("retweeted_status");
	}
	
	@Override
	protected String getNextPageUrl(JSONObject queryResults)
	{
		String nextPageUrl = null;
		JSONObject searchMetadata = (JSONObject) queryResults.get("search_metadata");
		if (searchMetadata.containsKey("next_results")) {
			nextPageUrl = SEARCH_TWEETS_API + searchMetadata.get("next_results");
		}
		
		return nextPageUrl;
	}
	
	/**
	 * This registered application can use a bearer token for application-only authentication
	 * @param bearerToken
	 */
	private void setBearerToken(JSONObject bearerJSONToken) {
		String tokenType = (String) bearerJSONToken.get("token_type");
		if (tokenType.compareToIgnoreCase(OAUTH2_TOKEN_TYPE_VALUE) != 0) {
			// This is unrecoverable and may be a security problem
			// or the REST API has changed.
			throw new RuntimeException("Unexpected Twitter OAUTH2 token type value: " + tokenType);
		}
		setAuthHeaderValue(OAUTH2_TOKEN_TYPE_VALUE + " " + (String) bearerJSONToken.get("access_token"));
	}

	/*
	 *  Set the body text for an OAUTH2 token request
	 */
	private void setOAuthRequest() throws IOException {
		setBody(OAUTH2_TOKEN_REQ_BODY);
	}

	@Override
	protected void setSearchRateLimits() {
		remainingSearchRequests = Long.parseLong(connection.getHeaderField(HEADER_RATE_LIMIT_REMAINING));
		searchResetTime = Long.parseLong(connection.getHeaderField(HEADER_RATE_LIMIT_RESET));
	}
	
	@Override
	protected void querySearchRateLimits() throws IOException {
		createConnection(getUrl(RATE_LIMIT), HTTP_GET);
		JSONObject rateLimits = getResponse(HttpURLConnection.HTTP_OK);
		JSONObject resources = (JSONObject) rateLimits.get("resources");
		JSONObject search = (JSONObject) resources.get("search");
		JSONObject searchTweets = (JSONObject) search.get("/search/tweets");
		this.remainingSearchRequests = (Long) searchTweets.get("remaining");
		this.searchResetTime = (Long) searchTweets.get("reset");
	}
	
	@Override
	protected void setRequestProperties() {
		connection.setRequestProperty("Host", "api.twitter.com");
	}
}
