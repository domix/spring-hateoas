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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.springframework.hateoas.support.MappingUtils.*;
import static org.springframework.hateoas.uber.UberContainer.*;
import static org.springframework.hateoas.uber.UberConfiguration.*;
import static org.springframework.hateoas.uber.UberData.*;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Greg Turnquist
 */
public class UberSerializationTest {

	ObjectMapper objectMapper;

	@Before
	public void setUp() {
		this.objectMapper = configureUberObjectMapper(new ObjectMapper());
	}

	@Test
	public void deserializingUberDocumentShouldWork() throws IOException {

		UberDocument uberDocument = this.objectMapper.readValue(
			read(new ClassPathResource("reference-1.json", getClass())), UberDocument.class);

		assertThat(uberDocument.getUber().getLinks().size(), is(8));

		assertThat(uberDocument.getUber().getLinks().get(0), is(new Link("http://example.org/", "self")));
		assertThat(uberDocument.getUber().getLinks().get(1), is(new Link("http://example.org/list/", "collection")));
		assertThat(uberDocument.getUber().getLinks().get(2), is(new Link("http://example.org/search{?title}", "search")));
		assertThat(uberDocument.getUber().getLinks().get(3), is(new Link("http://example.org/search{?title}", "collection")));
		assertThat(uberDocument.getUber().getLinks().get(4), is(new Link("http://example.org/list/1", "item")));
		assertThat(uberDocument.getUber().getLinks().get(5), is(new Link("http://example.org/list/1", "http://example.org/rels/todo")));
		assertThat(uberDocument.getUber().getLinks().get(6), is(new Link("http://example.org/list/2", "item")));
		assertThat(uberDocument.getUber().getLinks().get(7), is(new Link("http://example.org/list/2", "http://example.org/rels/todo")));

		assertThat(uberDocument.getUber().getData().size(), is(5));

		assertThat(uberDocument.getUber().getData().get(0), is(uberData()
			.rel("self")
			.url("http://example.org/")
			.build()));

		assertThat(uberDocument.getUber().getData().get(1), is(uberData()
			.name("list")
			.label("ToDo List")
			.rel("collection")
			.url("http://example.org/list/")
			.build()));

		assertThat(uberDocument.getUber().getData().get(2), is(uberData()
			.name("search")
			.label("Search")
			.rel("search")
			.rel("collection")
			.url("http://example.org/search{?title}")
			.templated(true)
			.build()));

		assertThat(uberDocument.getUber().getData().get(3), is(uberData()
			.name("todo")
			.rel("item")
			.rel("http://example.org/rels/todo")
			.url("http://example.org/list/1")
			.oneData(uberData()
				.name("title")
				.label("Title")
				.value("Clean house")
				.build())
			.oneData(uberData()
				.name("dueDate")
				.label("Date Due")
				.value("2014-05-01")
				.build())
			.build()));

		assertThat(uberDocument.getUber().getData().get(4), is(uberData()
			.name("todo")
			.rel("item")
			.rel("http://example.org/rels/todo")
			.url("http://example.org/list/2")
			.oneData(uberData()
				.name("title")
				.label("Title")
				.value("Paint the fence")
				.build())
			.oneData(uberData()
				.name("dueDate")
				.label("Date Due")
				.value("2014-06-01")
				.build())
			.build()));
	}

	@Test
	public void serializingUberDocumentShouldWork() throws IOException {

		UberContainer uberContainer = uberDocument()
			.version("1.0")
			.oneData(uberData()
				.rel("self")
				.url("http://example.org/")
				.build())
			.oneData(uberData()
				.name("list")
				.label("ToDo List")
				.rel("collection")
				.url("http://example.org/list/")
				.build())
			.oneData(uberData()
				.name("search")
				.label("Search")
				.rel("search")
				.rel("collection")
				.url("http://example.org/search{?title}")
				.templated(true)
				.build())
			.oneData(uberData()
				.name("todo")
				.rel("item")
				.rel("http://example.org/rels/todo")
				.url("http://example.org/list/1")
				.data(Arrays.asList(
					uberData()
						.name("title")
						.label("Title")
						.value("Clean house")
						.build(),
					uberData()
						.name("dueDate")
						.label("Date Due")
						.value("2014-05-01")
						.build()
				))
				.build())
			.oneData(uberData()
				.name("todo")
				.rel("item")
				.rel("http://example.org/rels/todo")
				.url("http://example.org/list/2")
				.oneData(uberData()
					.name("title")
					.label("Title")
					.value("Paint the fence")
					.build())
				.oneData(uberData()
					.name("dueDate")
					.label("Date Due")
					.value("2014-06-01")
					.build())
				.build())
			.build();


		String actual = this.objectMapper.writeValueAsString(new UberDocument(uberContainer));
		assertThat(actual, is(read(new ClassPathResource("nicely-formatted-reference.json", getClass()))));
	}

	@Test
	public void errorsShouldDeserializeProperly() throws IOException {

		UberDocument uberDocument = this.objectMapper.readValue(
			read(new ClassPathResource("reference-2.json", getClass())), UberDocument.class);

		assertThat(uberDocument.getUber().getError(), is(notNullValue()));

		assertThat(uberDocument.getUber().getError().getData().size(), is(4));

		assertThat(uberDocument.getUber().getError().getData().get(0), is(uberData()
			.name("type")
			.rel("https://example.com/rels/http-problem#type")
			.value("out-of-credit")
			.build()));
		assertThat(uberDocument.getUber().getError().getData().get(1), is(uberData()
			.name("title")
			.rel("https://example.com/rels/http-problem#title")
			.value("You do not have enough credit")
			.build()));
		assertThat(uberDocument.getUber().getError().getData().get(2), is(uberData()
			.name("detail")
			.rel("https://example.com/rels/http-problem#detail")
			.value("Your balance is 30, but the cost is 50.")
			.build()));
		assertThat(uberDocument.getUber().getError().getData().get(3), is(uberData()
			.name("balance")
			.rel("https://example.com/rels/http-problem#balance")
			.value("30")
			.build()));
	}

	@Test
	public void anotherSpecUberDocumentToTest() throws IOException {

		UberDocument uberDocument = this.objectMapper.readValue(
			read(new ClassPathResource("reference-3.json", getClass())), UberDocument.class);

		assertThat(uberDocument.getUber().getLinks().size(), is(6));
		assertThat(uberDocument.getUber().getLinks().get(0), is(new Link("http://example.org/", "self")));
		assertThat(uberDocument.getUber().getLinks().get(1), is(new Link("http://example.org/profiles/people-and-places", "profile")));
		assertThat(uberDocument.getUber().getLinks().get(2), is(new Link("http://example.org/people/", "collection")));
		assertThat(uberDocument.getUber().getLinks().get(3), is(new Link("http://example.org/people/", "http://example.org/rels/people")));
		assertThat(uberDocument.getUber().getLinks().get(4), is(new Link("http://example.org/places/", "collection")));
		assertThat(uberDocument.getUber().getLinks().get(5), is(new Link("http://example.org/places/", "http://example.org/rels/places")));

		assertThat(uberDocument.getUber().getData().get(2).getId(), is("people"));
		assertThat(uberDocument.getUber().getData().get(2).getData().get(0).getAction(), is(UberAction.APPEND));
		assertThat(uberDocument.getUber().getData().get(2).getData().get(2).getData().get(3).getName(), is("avatarUrl"));
		assertThat(uberDocument.getUber().getData().get(2).getData().get(2).getData().get(3).isTransclude(), is(true));
		assertThat(uberDocument.getUber().getData().get(2).getData().get(2).getData().get(3).getUrl(), is("http://example.org/avatars/1"));
		assertThat(uberDocument.getUber().getData().get(2).getData().get(2).getData().get(3).getValue().toString(), is("User Photo"));
		assertThat(uberDocument.getUber().getData().get(2).getData().get(2).getData().get(3).getAccepting(), hasItems("image/*"));
	}
}
