package com.vinurl.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public final class Url {

	private final URL url;

	private Url(URL url) {
		this.url = url;
	}

	public static Url parse(String input) {
		if (input == null || input.isBlank()) {
			return null;
		}

		try {
			URI uri = new URI(input.trim()).normalize();

			if (uri.getScheme() == null || uri.getHost() == null) {
				return null;
			}

			return new Url(uri.toURL());

		} catch (URISyntaxException | MalformedURLException e) {
			return null;
		}
	}

	public static boolean isValid(String input) {
		return parse(input) != null;
	}

	public Url base() {
		try {
			return new Url(new URI(url.getProtocol(), url.getAuthority(), null, null, null).toURL());
		} catch (Exception e) {
			return null;
		}
	}

	public int length() {
		return url.toString().length();
	}

	@Override
	public String toString() {
		return url.toString();
	}
}
