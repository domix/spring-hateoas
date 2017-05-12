/*
 * Copyright 2017 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Greg Turnquist
 */
@Data
@Builder(builderMethodName = "uberData")
@JsonPropertyOrder({"id", "name", "label", "rel", "url", "templated", "action", "transclude", "model", "sending",
	"accepting", "value", "data"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UberData {

	private String id;

	private String name;

	@JsonProperty("rel")
	@Singular
	private List<String> rels;

	private String label;

	private String url;

	private boolean templated = false;

	private UberAction action = UberAction.READ;

	private boolean transclude = false;

	private String model;

	private List<String> sending;

	private List<String> accepting;

	private Object value;

	@Singular("oneData")
	private List<UberData> data;

	UberData(String id, String name, List<String> rels, String label, String url, boolean templated,
					UberAction action, boolean transclude, String model, List<String> sending, List<String> accepting, Object value,
					List<UberData> data) {

		this.id = id;
		this.name = name;
		this.rels = rels;
		this.label = label;
		this.url = url;
		this.templated = templated;
		this.action = action;
		this.transclude = transclude;
		this.model = model;
		this.sending = sending;
		this.accepting = accepting;
		this.value = value;
		this.data = data;
	}

	UberData() {
	}

	public UberAction getAction() {

		if (action == UberAction.READ) {
			return null;
		}
		return action;
	}

	public void setAction(String action) {
		this.action = UberAction.valueOf(action.toUpperCase());
	}

	/**
	 * Do not render rels if empty.
	 * 
	 * @return
	 */
	public List<String> getRels() {
		
		if (this.rels == null || this.rels.isEmpty()) {
			return null;
		}
		return this.rels;
	}

	/**
	 * Do not render data if empty.
	 * @return
	 */
	public List<UberData> getData() {

		if (this.data == null || this.data.isEmpty()) {
			return null;
		}
		return this.data;
	}

	public boolean hasLinks() {
		return (this.rels != null) && (!this.rels.isEmpty());
	}

	/**
	 * Fetch all the links found in this {@link UberData}.
	 * 
	 * @return
	 */
	@JsonIgnore
	public List<Link> getLinks() {

		List<Link> links = new ArrayList<Link>();

		if (this.hasLinks()) {
			for (String rel : this.rels) {
				links.add(new Link(this.url, rel));
			}
		}

		return links;
	}

	/**
	 * Use {@link Boolean} to NOT serialize {@literal false}.
	 * @return
	 */
	public Boolean isTemplated() {
		return this.templated ? this.templated : null;
	}

	/**
	 * Use {@link Boolean} to NOT serialize {@literal false}.
	 * @return
	 */
	public Boolean isTransclude() {
		return this.transclude ? this.transclude : null;
	}

}
