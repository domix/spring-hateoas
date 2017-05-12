package org.springframework.hateoas.siren;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Dietrich Schulten
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"class", "title", "rel", "properties", "entities", "actions", "links"})
public class AbstractSirenNode {

	private String title;

	public AbstractSirenNode(String title) {
		this.title = title;
	}

	public AbstractSirenNode() {
	}

	public String getTitle() {
		return title;
	}
}
