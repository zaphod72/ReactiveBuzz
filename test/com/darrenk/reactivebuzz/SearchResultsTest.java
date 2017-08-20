package com.darrenk.reactivebuzz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.darrenk.reactivebuzz.twitter.Tweet;

public class SearchResultsTest {
	private RESTClientStub cli;
	private ArrayList<SearchResult> results;
	private JSONObject tweet;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		cli = new RESTClientStub("a", "b", "c");
		tweet = (JSONObject) JSONValue.parse("{\"id_str\":\"abcd1234\"}");
		results = new ArrayList<SearchResult>();
		results.add(new Tweet(tweet));
		results.add(new Tweet(tweet));
		results.add(new Tweet(tweet));
		results.add(new Tweet(tweet));
		results.add(new Tweet(tweet));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.darrenk.reactivebuzz.SearchResults#SearchResults(com.darrenk.reactivebuzz.AbstractRESTClient, java.util.Collection, boolean)}.
	 */
	@Test
	public void testSearchResults() {
		SearchResults s = new SearchResults(cli, results, false);
		assertEquals("Number of results", results.size(), s.size());

		s = new SearchResults(cli, results, true);
		assertEquals("Number of results", results.size(), s.size());
	}

	/**
	 * Test method for {@link com.darrenk.reactivebuzz.SearchResults#addAll(com.darrenk.reactivebuzz.SearchResults)}.
	 */
	@Test
	public void testAddAll() {
		SearchResults s = new SearchResults(cli, results, false);
		assertEquals("Number of results", results.size(), s.size());
		s.addAll(new SearchResults(cli, results, false));
		assertEquals("Number of results", results.size(), s.size());
		s.addAll(new SearchResults(cli, results, true));
		assertEquals("Number of results", results.size(), s.size());
	}

	/**
	 * Test method for {@link com.darrenk.reactivebuzz.SearchResults#iterator()}.
	 */
	@Test
	public void testIterator() {
		SearchResults s = new SearchResults(cli, results, false);
		Iterator<SearchResult> iter = s.iterator();
		int nResults = 0;
		while (iter.hasNext()) {
			SearchResult r = iter.next();
			assertEquals("Is a JSON tweet", tweet, r.getJSON());
			nResults++;
		}
		assertEquals("Number of results", results.size(), nResults);
		
		try {
			iter.next();
			fail("Expected NoSuchElementException");
		} catch (NoSuchElementException e) {
			// Ok!
		}
		
		try {
			cli = new RESTClientStub("a", "b", "c");
			s = cli.search("123", "find me");
		} catch (IOException e) {
			fail(e.getMessage());
		}
		iter = s.iterator();
		nResults = 0;
		while (iter.hasNext()) {
			iter.next();
			nResults++;
			if (nResults == results.size()*2 + 1) {
				break;
			}
		}
		cli.setHasNextPage(false);
		// At this point the iterator is on the 3rd page of results and has
		// a link to the next page. So now there are four total pages of results.
		while (iter.hasNext()) {
			iter.next();
			nResults++;
		}
		assertEquals("Number of results", results.size()*4, nResults);		
	}

	/**
	 * Test method for {@link com.darrenk.reactivebuzz.SearchResults#size()}.
	 */
	@Test
	public void testSize() {
		SearchResults s = new SearchResults(cli, results, false);
		assertEquals("Number of results", results.size(), s.size());
	}

	@Test
	public void testFilterResult() {
		try {
			cli.setHasNextPage(true);
			SearchResults s = cli.search("wer", "rty");
			Iterator<SearchResult> iter = s.iterator();
			int nResults = 0;
			cli.setSkipNResults(s.size() + 1);
			while (iter.hasNext() && nResults < 20) {
				iter.next();
				nResults++;
			}
			assertEquals("Number of results", 20, nResults);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
}
