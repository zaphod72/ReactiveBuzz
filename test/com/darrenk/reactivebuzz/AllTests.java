package com.darrenk.reactivebuzz;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.darrenk.reactivebuzz.github.RepositoryTest;
import com.darrenk.reactivebuzz.twitter.TweetTest;

@RunWith(Suite.class)
@SuiteClasses({
	AbstractRESTClientTest.class,
	SearchResultsTest.class,
	RepositoryTest.class,
	TweetTest.class})

public class AllTests {}
