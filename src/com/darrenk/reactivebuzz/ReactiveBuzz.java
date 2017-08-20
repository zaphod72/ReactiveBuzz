package com.darrenk.reactivebuzz;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.darrenk.reactivebuzz.exception.SearchException;
import com.darrenk.reactivebuzz.github.GitHubRESTClient;
import com.darrenk.reactivebuzz.twitter.TwitterRESTClient;

/**
 * Namespace for main()
 */
public final class ReactiveBuzz {
	private static final int MAX_PROJECTS = 10;
	private static final int MAX_TWEETS_PER_PROJECT = 20;
	private static final String PROP_FILE = "config.properties";
	private static final String GITHUB_QUERY = "reactive";
	
	// Normally these values would be in a keystore
	private static String twitterOAUTH2Key;
	private static String twitterOAUTH2Token;
	//  or supplied by a user over HTTPS
	private static String githubUser;
	private static String githubPwd;
	
	private static Properties properties = new Properties();
	
	/**
	 * Main entry point for the application
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String propFile = parseArgs(args);
		getProperties(propFile);
		int maxProjects = MAX_PROJECTS;
		if (properties.containsKey("MaxProjects")) {
			maxProjects = Integer.parseInt(properties.getProperty("MaxProjects"));
		}
		int maxTweetsPerProject = MAX_TWEETS_PER_PROJECT;
		if (properties.containsKey("MaxTweetsPerProject")) {
			maxTweetsPerProject = Integer.parseInt(properties.getProperty("MaxTweetsPerProject"));
		}

		try {
			Map<SearchResult, SearchResults> results = search(maxProjects, maxTweetsPerProject);
			printResults(results, maxTweetsPerProject);
		} catch (SearchException e) {
			e.printStackTrace();
		}
	}
	
	/* Print the query results
	*/
	private static void printResults(Map<SearchResult, SearchResults> results, int maxTweetsPerProject) {
		boolean outputTweetSummary = Boolean.parseBoolean(properties.getProperty("OutputTweetSummary", "false"));
		for (Entry<SearchResult, SearchResults> result: results.entrySet()) {
			System.out.println("GitHub project:");
			System.out.println(result.getKey().toString());	
			int nTweets = 0;
			for (SearchResult tweet: result.getValue()) {
				System.out.println("----------------");
				if (outputTweetSummary) {
					System.out.println(tweet.getName() + ": " + tweet.getJSON().get("text"));
				} else {
					System.out.println(tweet.getJSONString());
				}
				nTweets++;
				if (nTweets == maxTweetsPerProject) {
					break;
				}
			}
			System.out.println("==================");
		}
	}
	
	/* Run REST API queries and build results map
	 */
	private static Map<SearchResult, SearchResults> search(int maxProjects, int maxTweetsPerProject) throws IOException {
		Map<SearchResult, SearchResults> mapResults = new HashMap<SearchResult, SearchResults>();
		// Create the Twitter and GitHub clients
		GitHubRESTClient githubClient = new GitHubRESTClient(githubUser, githubPwd);
		TwitterRESTClient twitterClient = new TwitterRESTClient(twitterOAUTH2Key, twitterOAUTH2Token);
		
		// Search GitHub
		SearchResults githubResults = githubClient.searchRepositories(GITHUB_QUERY);
		for (SearchResult githubResult: githubResults) {
			// Search Twitter for a GitHub project
			SearchResults twitterResults = twitterClient.searchTweets("github " + githubResult.getName().replace('/', ' '));
			if (twitterResults.size() > 0) {
				// Add the Twitter results to the GitHub result
				mapResults.put(githubResult, twitterResults);
			}
			// If we don't have enough results then get the next Twitter search result page
			while (twitterResults.size() < maxTweetsPerProject) {
				if (!twitterClient.getNextSearchResults(twitterResults)) {
					break;
				}
			}
			
			// The githubResults for loop will keep requesting new results pages as long as they exist
			// Break out of the loop if we have enough results
			if (mapResults.size() >= maxProjects) {
				break;
			}
		}
		
		return mapResults;
	}
	
	/* Parse the input arguments and return the properties filename
	 */
	private static String parseArgs(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-help")) {
				System.err.println("Usage: java " + ReactiveBuzz.class.getName() + " -config <config_file>");
				System.exit(0);
			} else if (args[i].equals("-config")) {
				i++;
				if (i >= args.length) {
					System.err.println("Missing configuration file name following -config");
					System.exit(1);
				}
				return args[i];
			} else {
				System.err.println("Unknown argument: " + args[i] + ".  Enter java " + ReactiveBuzz.class.getName() + " -help for options.");
				System.exit(1);	
			}
		}
		
		return null;
	}
	
	/* Load properties file
	*/
	private static void getProperties(String propFile) throws IOException {
		InputStream is = null;
		if (propFile != null) {
			is = new FileInputStream(propFile);
		} else {
			is = ReactiveBuzz.class.getClassLoader().getResourceAsStream(PROP_FILE);
			if (is == null) {
				throw new FileNotFoundException("Properties file: " + PROP_FILE + " not found on the classpath");
			}
		}
		
		getPropertyValues(is);
	}
	
	/* Get property values from a .properties file
	 */
	private static void getPropertyValues(InputStream is) throws IOException {
		properties.load(is);
 
 		twitterOAUTH2Key = properties.getProperty("TwitterConsumerKey");
		twitterOAUTH2Token = properties.getProperty("TwitterConsumerSecret");
		githubUser = properties.getProperty("GitHubUsername");
		githubPwd = properties.getProperty("GitHubPassword");
		
		if (twitterOAUTH2Key == null || twitterOAUTH2Token == null || twitterOAUTH2Key.isEmpty() || twitterOAUTH2Token.isEmpty()) {
			System.err.println("ERROR: The TwitterConsumerKey and TwitterConsumerSecret must be set in the .properties file.");
			System.exit(1);
		}
	}

}
