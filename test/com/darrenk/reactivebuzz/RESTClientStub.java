package com.darrenk.reactivebuzz;

import java.io.IOException;

import org.json.simple.JSONObject;

import com.darrenk.reactivebuzz.github.Repository;

public class RESTClientStub extends AbstractRESTClient {

	private int resultsToSkip = 0;
	private boolean hasNextPage = true;
	
	public RESTClientStub(String key, String token, String baseApiUrl) {
		super(key, token, baseApiUrl);
		this.connection = new URLConnectionStub();
	}

	public RESTClientStub(String baseApiUrl) {
		super(baseApiUrl);
	}

	@Override
	protected String encode(String key, String token) {
		return "I'm encoded";
	}

	@Override
	protected void setRequestProperties() {
	}

	@Override
	protected String getNextPageUrl(JSONObject queryResult) {
		return hasNextPage ? "next/page" : null;
	}
	
	public void setHasNextPage(boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}

	@Override
	protected boolean filterResult(JSONObject result) {
		return resultsToSkip-- > 0;
	}

	public void setSkipNResults(int resultsToSkip) {
		this.resultsToSkip = resultsToSkip;
	}
	
	@Override
	protected void setSearchRateLimits() {
	}

	public void setSearchResetTimeSecs(Long searchResetTimeSecs) {
		this.searchResetTime = searchResetTimeSecs;
	}
	
	public void setRemainingSearchRequests(long remainingSearchRequests) {
		this.remainingSearchRequests = remainingSearchRequests;
	}
	
	@Override
	protected void querySearchRateLimits() throws IOException {
	}

	@Override
	protected String getQueryResultsObjectName() {
		return AbstractRESTClientTest.resultsKey;
	}

	@Override
	protected SearchResult createResult(JSONObject result) {
		return new Repository(result);
	}

	@Override
	protected void createConnection(String url, String method) throws IOException {
		setRequestProperties();
	}

}
