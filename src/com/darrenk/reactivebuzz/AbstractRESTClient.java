package com.darrenk.reactivebuzz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.darrenk.reactivebuzz.exception.SearchException;

/**
 * An abstract class to be extended for issuing REST API search queries.
 */
public abstract class AbstractRESTClient {
	static final protected String HTTP_GET = "GET";
	static final protected String HTTP_POST = "POST";

	static final String HEADER_AUTHORIZATION = "Authorization";
	static final String HEADER_USER_AGENT = "User-Agent";
	static final protected String HEADER_ACCEPT = "Accept";
	static final String HEADER_CONTENT_LENGTH = "Content-Length";

	protected HttpURLConnection connection;
	private String authHeaderValue;
	private final String userAgent;
	private String baseApiUrl;
	protected long remainingSearchRequests;
	protected Long searchResetTime;
	private String nextPageUrl;
	
	/**
	 * Encode authentication parameters
	 * @param key Username or key
	 * @param token Password or token
	 * @return Authorization header value 
	 */
	abstract protected String encode(String key, String token);
	
	/**
	 * Add header properties for the HTTP request
	 */
	abstract protected void setRequestProperties();
	
	/**
	 * Get the HTTP API URL for the next query result page
	 * @param queryResult The JSON object returned from the previous query
	 * @return The HTTP API URL for the next query result page
	 */
	abstract protected String getNextPageUrl(JSONObject queryResult);
	
	/**
	 * Determine if a result is to be skipped from the results returned to a user.
	 * @param result A result to check
	 * @return True if the result is to be skipped
	 */
	abstract protected boolean filterResult(JSONObject result);
	
	/**
	 * Set the search API rate limits from the last REST API response
	 */
	abstract protected void setSearchRateLimits();
	
	/**
	 * Issue a REST API query to get the current REST API search rate limits. 
	 * @throws IOException
	 */
	abstract protected void querySearchRateLimits() throws IOException;
	
	/**
	 * Get the name of the JSON object holding query results
	 * @return The name of the JSON object holding query results
	 */
	abstract protected String getQueryResultsObjectName();
	
	/**
	 * Create a ReactiveBuzz SearchResult object from a query result JSONObject
	 * @param result The JSONObject query result
	 * @return A ReactiveBuzz SearchResult object
	 */
	abstract protected SearchResult createResult(JSONObject result);
	
	/**
	 * Constructor to use for authenticated REST calls
	 * @param key Username or key
	 * @param token Password or secret
	 * @param baseApiUrl The base URL used for all REST API calls
	 */
	public AbstractRESTClient(String key, String token, String baseApiUrl) {
		this(baseApiUrl);
		setAuthHeaderValue(encode(key, token));
	}
	
	/**
	 * Constructor to use for unauthenticated REST calls
	 * @param baseApiUrl The base URL used for all REST API calls
	 */
	public AbstractRESTClient(String baseApiUrl) {
		setBaseApiUrl(baseApiUrl);
		userAgent = this.getClass().getPackage().getName();
	}
	
	/**
	 * Get the REST API URL for a specific method
	 * @param apiPath REST API method
	 * @return REST API URL for the specified method
	 */
	protected String getUrl(String apiPath) {
		return baseApiUrl + apiPath;
	}
	
	/**
	 * Create a HTTP connection with required headers
	 * @param url The HTTP endpoint
	 * @param method The REST API method that will be called
	 * @throws IOException
	 */
	protected void createConnection(String url, String method) throws IOException {
		connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod(method);
		connection.setRequestProperty(HEADER_USER_AGENT, userAgent);
		if (authHeaderValue != null)
			connection.setRequestProperty(HEADER_AUTHORIZATION, authHeaderValue);
		setRequestProperties();
	}
	
	/**
	 * Add body text to a HTTP request
	 * @param bodyText The body text to add
	 * @throws IOException
	 */
	protected void setBody(String bodyText) throws IOException {
		connection.setDoOutput(true);
		byte[] bodyTextBytes = bodyText.getBytes();
		connection.setRequestProperty(HEADER_CONTENT_LENGTH, String.valueOf(bodyTextBytes.length));
		OutputStream os = (OutputStream) connection.getOutputStream();
		os.write(bodyTextBytes);
	}
	
	/**
	 * Set the Authorization header value
	 * @param authHeaderValue the Authorization header value to set
	 */
	protected void setAuthHeaderValue(String authHeaderValue) {
		this.authHeaderValue = authHeaderValue;
	}

	/**
	 * Issue a REST API query
	 * TODO: Handle the incomplete results / timeout case
	 */
	public SearchResults search(String apiPath, String query) throws IOException {
		checkSearchRateLimits();
		String urlQuery = URLEncoder.encode(query, "UTF-8");
		createConnection(getUrl(apiPath) + urlQuery, HTTP_GET);
		return search();
	}
	
	/**
	 * Get the next page of search results and add them to the cached results.
	 * @param results The existing result set.
	 * @return True if new results are returned
	 * @throws IOException
	 */
	public boolean getNextSearchResults(SearchResults results) throws IOException {
		checkSearchRateLimits();
		int nResults = results.size();
		// While loop in case filtering removes all results from a page
		while (nextPageUrl != null && results.size() == nResults) {
			createConnection(nextPageUrl, HTTP_GET);
			results.addAll(search());
		}
		
		return results.size() > nResults;
	}
	
	/**
	 * Issues the REST search, determines new search rate limits, and converts the JSON results
	 * to ReactiveBuzz SearchResult objects
	 * @return a new SearchResults containing the SearchResult objects.
	 */
	private SearchResults search() throws IOException {
		List<SearchResult> searchResults = new ArrayList<SearchResult>();
		JSONObject queryResult = getResponse(HttpURLConnection.HTTP_OK);
		JSONArray jsonResults = (JSONArray) queryResult.get(getQueryResultsObjectName());
		for (Object jsonResult: jsonResults) {
			if (!filterResult((JSONObject) jsonResult)) {
				searchResults.add(createResult((JSONObject) jsonResult));
			}
		}
		nextPageUrl = getNextPageUrl(queryResult);
		setSearchRateLimits();
		
		return new SearchResults(this, searchResults, hasNextResultsPage());
	}

	/**
	 * Get the REST API response, check for errors and encode the response to JSON
	 * @param expectedResponse
	 * @return
	 * @throws IOException
	 */
	protected JSONObject getResponse(int expectedResponse) throws IOException {
		try {
			if (connection.getResponseCode() == expectedResponse) {
				BufferedReader br = new BufferedReader(
			             new InputStreamReader(connection.getInputStream()));
	            StringBuilder sb = new StringBuilder();
	            String line;
	            while ((line = br.readLine()) != null) {
	                sb.append(line);
	            }
	            br.close();
	    		return (JSONObject) JSONValue.parse(sb.toString());
			}
			// Error condition
			InputStream is = connection.getErrorStream();
			if (is == null) {
				is = connection.getInputStream();
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
	  		String line;
	  		while ((line = br.readLine()) != null) {
	  			sb.append(line);
	  		}
	  		br.close();
	  		// TODO: Parse the JSON error object
	  		throw new SearchException(sb.toString());
		}
		finally {
			connection.disconnect();
		}
	}
	
	/**
	 * @return True if there are additional pages of search results
	 */
	protected boolean hasNextResultsPage() {
		return nextPageUrl != null;
	}

	/**
	 * Set the base REST API URL used for all REST methods
	 * @param baseApiUrl The base REST API URL
	 */
	protected void setBaseApiUrl(String baseApiUrl) {
		this.baseApiUrl = baseApiUrl;
	}

	/**
	 * How many REST API searches can be issued before reaching the server limit. 
	 * @return The number of REST API searches that can be issued before reaching the server limit. 
	 */
	public long getRemainingSearchRequests() {
		return this.remainingSearchRequests;
	}
	
	/**
	 * How many seconds until the REST API search limit resets to the maximum. 
	 * @return The number of seconds until the REST API search limit resets to the maximum. 
	 */
	public long getRemainingSearchResetSeconds() {
		return searchResetTime - System.currentTimeMillis()/1000;
	}
	
	/**
	 * Check the search rate limits and sleep for the required amount of time
	 * if no queries can be issued.
	 * @throws IOException 
	 */
	void checkSearchRateLimits() throws IOException {
		if (searchResetTime == null) {
			return; // Haven't made any API calls yet.
		}
		
		if (remainingSearchRequests > 0) {
			return;
		}
		
		if (getRemainingSearchResetSeconds() <= 0) {
			querySearchRateLimits();
			if (remainingSearchRequests > 0) {
				return;
			}
		}
		
		try {
			// TODO: Should be using threads
			// and println from main() only.
			System.out.println(
					"Search rate limit hit in " + this.getClass().getSimpleName() +
					". Waiting for " + getRemainingSearchResetSeconds() + " seconds.");
			Thread.sleep(1000 * getRemainingSearchResetSeconds());
		} catch (InterruptedException e) {
			// Ok, keep going.
		}
	}

}
