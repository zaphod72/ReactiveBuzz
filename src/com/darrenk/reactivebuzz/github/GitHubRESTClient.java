package com.darrenk.reactivebuzz.github;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import com.darrenk.reactivebuzz.AbstractRESTClient;
import com.darrenk.reactivebuzz.EncryptUtils;
import com.darrenk.reactivebuzz.SearchResult;
import com.darrenk.reactivebuzz.SearchResults;

/**
 * A REST API client to query for GitHub Repositories
 */
public class GitHubRESTClient extends AbstractRESTClient {
	private static final String HEADER_RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
	private static final String HEADER_RATE_LIMIT_RESET = "X-RateLimit-Reset";
	private static final String HEADER_SEARCH_LINK = "Link";
	private static final String BASE_API_URL = "https://api.github.com/";
	/**
	 * @see https://developer.github.com/v3/#current-version
	 */
	private static final String API_VERSION = "application/vnd.github.v3+json";
	private static final String SEARCH_REPO = "search/repositories?sort=pushed&order=desc&q=";
	//private static final int MAX_RESULTS = 1000;
	/**
	 * @see https://developer.github.com/v3/search/#rate-limit
	 */
	private static final String RATE_LIMIT = "rate_limit";

	/**
	 * Constructor to use for authenticated REST calls
	 * @param key Username
	 * @param token Password
	 */
	public GitHubRESTClient(String username, String password) {
		super(username, password, BASE_API_URL);
	}
	
	/**
	 * Constructor to use for unauthenticated REST calls
	 */
	public GitHubRESTClient() {
		super(BASE_API_URL);
	}
	
	@Override
	protected String encode(String username, String password) {
		 String encoded = "Basic " + EncryptUtils.base64Encode(username + ":" + password);
		 return encoded;
	}
	
	/**
	 * Query for GitHub Repositories
	 * @param query The query parameter
	 * @return The first page of search results
	 * @throws IOException
	 */
	public SearchResults searchRepositories(String query) throws IOException {
		return search(SEARCH_REPO, query);
	}
	
	@Override
	protected String getQueryResultsObjectName() {
		return "items";
	}

	@Override
	protected SearchResult createResult(JSONObject result) {
		return new Repository(result);
	}
	
	@Override
	protected boolean filterResult(JSONObject result) {
		return false;
	}

	@Override
	protected String getNextPageUrl(JSONObject queryResults)
	{
		String nextPageUrl = null;
		String links = connection.getHeaderField(HEADER_SEARCH_LINK);
		if (links != null) {
			Pattern pattern = Pattern.compile("<(.*?)>; rel=\"next\"");
			Matcher matcher = pattern.matcher(links);
			nextPageUrl = (matcher.find() ? matcher.group(1) : null);
		}

		return nextPageUrl;
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
		remainingSearchRequests = (Long) search.get("remaining");
		searchResetTime = (Long) search.get("reset");
	}
	
	@Override
	protected void setRequestProperties() {
		connection.setRequestProperty(HEADER_ACCEPT, API_VERSION);
	}
}
