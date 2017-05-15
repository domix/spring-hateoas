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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Greg Turnquist
 */
@Data
@Builder(builderMethodName = "uberDocument")
@JsonPropertyOrder({"version", "data", "error"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UberContainer {

	private String version;

	@Singular("oneData")
	private List<UberData> data;

	private UberError error;

	public UberContainer(Object toWrap) {

	}

	UberContainer(String version, List<UberData> data, UberError error) {

		this.version = version;
		this.data = data;
		this.error = error;
	}

	UberContainer() {
	}

	/**
	 * Extra rel and url from every {@link UberData} entry.
	 * 
	 * @return
	 */
	@JsonIgnore
	public List<Link> getLinks() {

		List<Link> links = new ArrayList<Link>();

		for (UberData item : this.data) {
			links.addAll(item.getLinks());
		}

		return links;
	}

}
