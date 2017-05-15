/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.hateoas.uber;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Dietrich Schulten
 */
public class UberLinkDiscoverer implements LinkDiscoverer {

	private ObjectMapper objectMapper;

	UberLinkDiscoverer() {
		this.objectMapper = UberConfiguration.configureUberObjectMapper(new ObjectMapper());
	}

	/**
	 * Finds a single link with the given relation type in the given {@link String} representation.
	 *
	 * @param rel must not be {@literal null} or empty.
	 * @param representation must not be {@literal null} or empty.
	 * @return the first link with the given relation type found, or {@literal null} if none was found.
	 */
	@Override
	public Link findLinkWithRel(String rel, String representation) {

		try {
			UberDocument uberDocument = this.objectMapper.readValue(representation, UberDocument.class);

			return extractLink(rel, uberDocument);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Finds a single link with the given relation type in the given {@link InputStream} representation.
	 *
	 * @param rel must not be {@literal null} or empty.
	 * @param representation must not be {@literal null} or empty.
	 * @return the first link with the given relation type found, or {@literal null} if none was found.
	 */
	@Override
	public Link findLinkWithRel(String rel, InputStream representation) {

		try {
			UberDocument uberDocument = this.objectMapper.readValue(representation, UberDocument.class);

			return extractLink(rel, uberDocument);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns all links with the given relation type found in the given {@link String} representation.
	 *
	 * @param rel must not be {@literal null} or empty.
	 * @param representation must not be {@literal null} or empty.
	 * @return
	 */
	@Override
	public List<Link> findLinksWithRel(String rel, String representation) {

		try {
			UberDocument uberDocument = this.objectMapper.readValue(representation, UberDocument.class);

			return extractLinks(rel, uberDocument);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns all links with the given relation type found in the given {@link InputStream} representation.
	 *
	 * @param rel must not be {@literal null} or empty.
	 * @param representation must not be {@literal null} or empty.
	 * @return
	 */
	@Override
	public List<Link> findLinksWithRel(String rel, InputStream representation) {

		try {
			UberDocument uberDocument = this.objectMapper.readValue(representation, UberDocument.class);

			return extractLinks(rel, uberDocument);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean supports(MediaType mediaType) {
		return mediaType.isCompatibleWith(MediaTypes.UBER_JSON);
	}

	/**
	 * Look for a specific link based on rel.
	 *
	 * @param rel
	 * @param uberDocument
	 * @return
	 */
	private Link extractLink(String rel, UberDocument uberDocument) {

		for (Link link : uberDocument.getUber().getLinks()) {
			if (link.getRel().equals(rel)) {
				return link;
			}
		}
		return null;
	}

	private List<Link> extractLinks(String rel, UberDocument uberDocument) {

		List<Link> links = new ArrayList<Link>();

		for (Link link : uberDocument.getUber().getLinks()) {
			if (link.getRel().equals(rel)) {
				links.add(link);
			}
		}

		return links;
	}
}
