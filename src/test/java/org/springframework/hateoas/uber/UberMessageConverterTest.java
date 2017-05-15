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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.*;
import static org.springframework.hateoas.uber.UberContainer.*;
import static org.springframework.hateoas.uber.UberConfiguration.*;
import static org.springframework.hateoas.uber.UberData.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Greg Turnquist
 */
public class UberMessageConverterTest {

	ObjectMapper mapper;
	HttpMessageConverter<Object> messageConverter;

	@Before
	public void setUp() throws Exception {

		this.mapper = configureUberObjectMapper(new ObjectMapper());
		this.messageConverter = new UberMessageConverter(this.mapper);
	}

	@Test
	public void verifyBasicAttributes() {

		assertThat(this.messageConverter.getSupportedMediaTypes(), hasItems(MediaTypes.UBER_JSON));
		assertThat(this.messageConverter.canRead(UberContainer.class, MediaTypes.UBER_JSON), is(true));
		assertThat(this.messageConverter.canWrite(UberContainer.class, MediaTypes.UBER_JSON), is(true));
	}

	@Test
	public void canReadAnUberDocumentMessage() throws IOException {

		HttpInputMessage message = new HttpInputMessage() {
			@Override
			public InputStream getBody() throws IOException {
				return new ClassPathResource("reference-1.json", getClass()).getInputStream();
			}

			@Override
			public HttpHeaders getHeaders() {
				return new HttpHeaders();
			}
		};

		Object convertedMessage = this.messageConverter.read(UberDocument.class, message);

		assertThat(convertedMessage, instanceOf(UberDocument.class));

		UberDocument uberDocument = (UberDocument) convertedMessage;

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
	public void canWriteAnUberDocumentMessage() throws IOException {

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

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();

		HttpOutputMessage convertedMessage = new HttpOutputMessage() {
			@Override
			public OutputStream getBody() throws IOException {
				return stream;
			}

			@Override
			public HttpHeaders getHeaders() {
				return new HttpHeaders();
			}
		};

		UberDocument wrapper = new UberDocument(uberContainer);

		this.messageConverter.write(wrapper, MediaTypes.UBER_JSON, convertedMessage);

		UberDocument copy = this.mapper.readValue(stream.toString(), UberDocument.class);

		assertThat(copy, is(wrapper));

	}
}
