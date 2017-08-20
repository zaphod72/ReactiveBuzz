package com.darrenk.reactivebuzz.github;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RepositoryTest {
	private Map<String, JSONObject> jsonMap;

	private static final String fullName = "testuser\\whirled-peas";
	
	// The first array entry is used in other tests.
	public static final String[] jsonStrings = new String [] {
			"{" + // Representation of GitHub Repository JSON [0]
					"\"name\":\"whirled-peas\"" +
					"\"full_name\":\"testuser\\whirled-peas\"" +
					"\"html_url\":\"https://nowhere.github.com/testuser/whirled-peas\"" +
					"\"some_other_field\":1234" +
					"\"description\":\"brings about whirled peas\"" +
					"\"homepage\":\"http://www.google.com\"" +
					"\"another_field\":[5,6,7,8]" +
			"}",
			"{" + // Repository JSON from no nulls GitHub Repository JSON [1]
					"\"project_name\":\"whirled-peas\"" +
					"\"project_full_name\":\"testuser\\whirled-peas\"" +
					"\"github_address\":\"https://nowhere.github.com/testuser/whirled-peas\"" +
					"\"summary\":\"brings about whirled peas (http://www.google.com)\"" +
			"}",
			"{" + // Repository JSON from null Description [2]
					"\"project_name\":\"whirled-peas\"" +
					"\"project_full_name\":\"testuser\\whirled-peas\"" +
					"\"github_address\":\"https://nowhere.github.com/testuser/whirled-peas\"" +
					"\"summary\":\" (http://www.google.com)\"" +
			"}",
			"{" + // Repository JSON from null Homepage [3]
					"\"project_name\":\"whirled-peas\"" +
					"\"project_full_name\":\"testuser\\whirled-peas\"" +
					"\"github_address\":\"https://nowhere.github.com/testuser/whirled-peas\"" +
					"\"summary\":\"brings about whirled peas\"" +
			"}",
			"{" + // Repository JSON from null Description and Homepage [4]
					"\"project_name\":\"whirled-peas\"" +
					"\"project_full_name\":\"testuser\\whirled-peas\"" +
					"\"github_address\":\"https://nowhere.github.com/testuser/whirled-peas\"" +
					"\"summary\":\"\"" +
			"}" };
	
	private static final String synopsis = 
			"Repository: testuser\\whirled-peas (https://nowhere.github.com/testuser/whirled-peas)\n" +
			"Project: whirled-peas\n" +
			"brings about whirled peas (http://www.google.com)";
					
	@Before
	public void setUp() throws Exception {
		jsonMap = new HashMap<String, JSONObject>();
		for (String json: jsonStrings) {
			jsonMap.put(json, (JSONObject) JSONValue.parse(json));
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRepository() {
		assertNotNull("Create repository", new Repository(jsonMap.get(jsonStrings[0])));
		JSONObject empty = new JSONObject();
		assertNotNull("Create repository", new Repository(empty));
		// No exceptions thrown
	}

	@Test
	public void testGetJSON() {
		JSONObject gitHubRepo = jsonMap.get(jsonStrings[0]);
		Repository repo = new Repository(gitHubRepo);
		assertEquals("GitHub JSON object", jsonMap.get(jsonStrings[1]), repo.getJSON());

		gitHubRepo.remove("description");
		repo = new Repository(gitHubRepo);
		assertEquals("GitHub JSON object with null description", jsonMap.get(jsonStrings[2]), repo.getJSON());

		gitHubRepo.remove("homepage");
		repo = new Repository(gitHubRepo);
		assertEquals("GitHub JSON object with null description and homepage", jsonMap.get(jsonStrings[4]), repo.getJSON());

		// Recreate the original JSONObject
		gitHubRepo = (JSONObject) JSONValue.parse(jsonStrings[0]);
		gitHubRepo.remove("homepage");
		repo = new Repository(gitHubRepo);
		assertEquals("GitHub JSON object with null homepage", jsonMap.get(jsonStrings[3]), repo.getJSON());
	}

	@Test
	public void testGetJSONString() {
		/* Can't compare strings as the sets they are derived from are not ordered.
		 * So reparse back to JSON objects to compare. */
		
		JSONObject gitHubRepo = jsonMap.get(jsonStrings[0]);
		Repository repo = new Repository(gitHubRepo);
		assertEquals("GitHub JSON object", (JSONObject) JSONValue.parse(jsonStrings[1]), (JSONObject) JSONValue.parse(repo.getJSONString()));

		gitHubRepo.remove("description");
		repo = new Repository(gitHubRepo);
		assertEquals("GitHub JSON object with null description", (JSONObject) JSONValue.parse(jsonStrings[2]), (JSONObject) JSONValue.parse(repo.getJSONString()));

		gitHubRepo.remove("homepage");
		repo = new Repository(gitHubRepo);
		assertEquals("GitHub JSON object with null description and homepage", (JSONObject) JSONValue.parse(jsonStrings[4]), (JSONObject) JSONValue.parse(repo.getJSONString()));

		// Recreate the original JSONObject
		gitHubRepo = (JSONObject) JSONValue.parse(jsonStrings[0]);
		gitHubRepo.remove("homepage");
		repo = new Repository(gitHubRepo);
		assertEquals("GitHub JSON object with null homepage", (JSONObject) JSONValue.parse(jsonStrings[3]), (JSONObject) JSONValue.parse(repo.getJSONString()));
	}

	@Test
	public void testToString() {
		JSONObject gitHubRepo = jsonMap.get(jsonStrings[0]);
		Repository repo = new Repository(gitHubRepo);
		assertEquals("Repository string", synopsis, repo.toString());
	}

	@Test
	public void testGetName() {
		JSONObject gitHubRepo = jsonMap.get(jsonStrings[0]);
		Repository repoNoNulls = new Repository(gitHubRepo);
		assertEquals("Repository name", fullName, repoNoNulls.getName());
	}

}
