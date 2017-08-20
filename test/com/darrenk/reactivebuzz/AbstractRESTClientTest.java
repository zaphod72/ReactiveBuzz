package com.darrenk.reactivebuzz;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.darrenk.reactivebuzz.exception.SearchException;
import com.darrenk.reactivebuzz.github.RepositoryTest;

public class AbstractRESTClientTest {

	public static final String resultsKey = "results";
	
	public static final String jsonResults = "{\n\t" +
			"\"" + resultsKey + "\":[\n\t\t" + 
			RepositoryTest.jsonStrings[0] + "\n\t\t" +
			RepositoryTest.jsonStrings[0] + "\n\t\t" +
			RepositoryTest.jsonStrings[0] + "\n\t\t" +
			RepositoryTest.jsonStrings[0] + "\n\t\t" +
			RepositoryTest.jsonStrings[0] + "\n\t" +
			"]\n}";
	
	private RESTClientStub cli;
	
	@Before
	public void setUp() throws Exception {
		cli = new RESTClientStub("ab", "cd", "efg");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSearch() {
		try {
			SearchResults s = cli.search("search/search", "find me");
			assertEquals("Number of results", 5, s.size()); 

			((URLConnectionStub) cli.connection).setReponseCode(400);
			try {
				s = cli.search("search/search", "find me");
				fail("Expected SearchException");
			} catch (SearchException se) {
				// Ok!
			}
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetNextSearchResults() {
		try {
			SearchResults s = cli.search("search/search", "find me");
			boolean found = cli.getNextSearchResults(s);
			assertTrue("Found more results", found);
			assertEquals("Number of results", 10, s.size()); 
			
			cli.setHasNextPage(false);
			cli.getNextSearchResults(s);
			found = cli.getNextSearchResults(s);
			assertFalse("Found more results", found);
			assertEquals("Number of results", 15, s.size()); 
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testFilterResult() {
		try {
			SearchResults s = cli.search("search/search", "find me");
			cli.setSkipNResults(s.size() + 1);
			boolean found = cli.getNextSearchResults(s);
			assertTrue("Found more results", found);
			assertEquals("Number of results", 9, s.size()); 
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testCheckSearchRateLimits() {
		cli.setRemainingSearchRequests(0);
		assertEquals("Remaining search requests", 0, cli.getRemainingSearchRequests());
		cli.setRemainingSearchRequests(5);
		assertEquals("Remaining search requests", 5, cli.getRemainingSearchRequests());
		
		cli.setSearchResetTimeSecs((System.currentTimeMillis()/1000) - 1);
		assertTrue("Remaining search reset seconds", cli.getRemainingSearchResetSeconds() <=0);
		
		long now = System.currentTimeMillis();
		cli.setSearchResetTimeSecs((now/1000) + 5);
		assertTrue("Remaining search reset seconds", cli.getRemainingSearchResetSeconds() <=5);
		cli.setRemainingSearchRequests(5);
		try {
			cli.checkSearchRateLimits();
			assertTrue("Did not wait for search limit time", (System.currentTimeMillis() - now) < 1000);
			cli.setRemainingSearchRequests(0);
			cli.checkSearchRateLimits();
			assertTrue("Waited for search limit time", (System.currentTimeMillis() - now) > 4000);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
}
