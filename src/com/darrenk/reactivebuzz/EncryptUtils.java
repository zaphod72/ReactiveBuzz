package com.darrenk.reactivebuzz;

import java.util.Base64;

/**
 * Namespace for encoding and encryption routines
 */
public abstract class EncryptUtils {

	/**
	 * Base64 encoding
	 * @param source The String to encode
	 * @return Base64 encoded String
	 */
	public static String base64Encode(String source) {
		String encoded = Base64.getEncoder().encodeToString(source.getBytes());
		return encoded;
	}
	
	/**
	 * RFC 1738 encoding
	 * @see https://dev.twitter.com/oauth/application-only
	 * @param source The String to encode
	 * @return The encoded String
	 */
	public static String rfc1738encode(String source) {
		// Currently does not change anything
		return source;
	}
	
}
