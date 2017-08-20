/**
 * 
 */
package com.darrenk.reactivebuzz;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.darrenk.reactivebuzz.exception.SearchException;

/**
 * Collection-type class for managing a set of SearchResult objects.
 * The hasNext() method will check for and retrieve result pages as needed.
 */
public class SearchResults implements Iterable<SearchResult> {
	private AbstractRESTClient provider;
	private List<SearchResult> results;
	private boolean hasNextPage;
	
	/**
	 * Constructor for a new set of search results
	 * @param provider Provider of search results
	 * @param results Initial set of search results
	 * @param hasNextPage True if there is an additional page of search results available
	 */
	public SearchResults(AbstractRESTClient provider, List<SearchResult> results, boolean hasNextPage) {
		this.provider = provider;
		this.results = results;
		this.hasNextPage = hasNextPage;
	}
	
	/**
	 * Add a set of search results to the existing set
	 * @param searchResults The set to add
	 * @return true if this collection changed as a result of the call
	 */
	public boolean addAll(SearchResults searchResults) {
		return this.results.addAll(searchResults.results);
	}
	
	/* Get the next page of search results from the server
	 */
	private void getNextResultsPage() throws IOException {
		hasNextPage = provider.getNextSearchResults(this);
	}

	@Override
	public Iterator<SearchResult> iterator() {
		return new SearchResultIterator();
	}

	/**
	 * Inner class implementing the iterator for the outer classes' search results
	 */
	private final class SearchResultIterator implements Iterator<SearchResult> {
		private int index;
		
		/**
		 * Constructor
		 */
		public SearchResultIterator() {
			index = 0;
		}
		
		@Override
		public boolean hasNext() {
			if (index >= results.size() && hasNextPage) {
				try {
					getNextResultsPage();
				} catch (IOException e) {
					throw new SearchException("Failed getting the next search result page", e);
				}
			}
			
			return index < results.size();
		}

		@Override
		public SearchResult next() {
			if (hasNext()) {
				return results.get(index++);
			}

			throw new NoSuchElementException();
		}
		
		/**
		 * Not supported
		 **/
		public void remove() {
		    throw new UnsupportedOperationException();
		}
	}

	/**
	 * Returns the number of search results
	 * @return the number of search results
	 */
	public int size() {
		return results.size();
	}
}
