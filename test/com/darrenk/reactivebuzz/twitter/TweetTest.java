package com.darrenk.reactivebuzz.twitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TweetTest {

	private static final String jsonName = "abcd1234";
	private static final String tweetString =
		"{" + // Representation of Tweet JSON
				"\"id_str\":\"" + jsonName + "\"" +
				"\"some_other_field\":1234" +
				"\"another_field\":[5,6,7,8]" +
		"}";
	
	private JSONObject tweet;
	
	@Before
	public void setUp() throws Exception {
		this.tweet = (JSONObject) JSONValue.parse(tweetString);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTweet() {
		assertNotNull("Create tweet", new Tweet(tweet));
		assertNotNull("Create repository", new Tweet(new JSONObject()));
	}

	@Test
	public void testGetJSON() {
		Tweet t = new Tweet(tweet);
		assertEquals("Tweet JSON object", tweet, t.getJSON());
	}

	@Test
	public void testGetJSONString() {
		/* Can't compare strings as the sets they are derived from are not ordered.
		 * So reparse back to JSON objects to compare. */
		
		Tweet t = new Tweet(tweet);
		assertEquals("Tweet JSON string", (JSONObject) JSONValue.parse(tweetString), (JSONObject) JSONValue.parse(t.getJSONString()));
	}

	@Test
	public void testGetName() {
		Tweet t = new Tweet(tweet);
		assertEquals("Tweet name", jsonName, t.getName());
	}

}
