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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.AbstractJackson2MarshallingIntegrationTest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.support.MappingUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Greg Turnquist
 */
public class Jackson2UberIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	@Before
	public void setUpModule() {

		this.mapper.registerModule(new Jackson2UberModule());
		this.mapper.setHandlerInstantiator(new Jackson2UberModule.UberHandlerInstantiator());
		this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	@Test
	public void rendersSingleLinkAsObject() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost").withSelfRel());

		assertThat(write(resourceSupport), is(MappingUtils.read(new ClassPathResource("resource-support.json", getClass()))));
	}

	@Test
	public void deserializeSingleLink() throws Exception {

		ResourceSupport expected = new ResourceSupport();
		expected.add(new Link("localhost"));
		assertThat(read(MappingUtils.read(new ClassPathResource("resource-support.json", getClass())), ResourceSupport.class),
			is(expected));
	}

	@Test
	public void rendersMultipleLinkAsArray() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost"));
		resourceSupport.add(new Link("localhost2").withRel("orders"));

		assertThat(write(resourceSupport), is(MappingUtils.read(new ClassPathResource("resource-support-2.json", getClass()))));
	}

	@Test
	public void deserializeMultipleLinks() throws Exception {

		ResourceSupport expected = new ResourceSupport();
		expected.add(new Link("localhost"));
		expected.add(new Link("localhost2").withRel("orders"));

		assertThat(read(MappingUtils.read(new ClassPathResource("resource-support-2.json", getClass())), ResourceSupport.class),
			is(expected));
	}

	@Test
	public void rendersSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<String>();
		content.add("first");
		content.add("second");

		Resources<String> resources = new Resources<String>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources), is(MappingUtils.read(new ClassPathResource("resources.json", getClass()))));
	}

	@Test
	public void deserializesSimpleResourcesWithNoLinks() throws Exception {

		List<String> content = new ArrayList<String>();
		content.add("first");
		content.add("second");

		Resources<String> expected = new Resources<String>(content);
		expected.add(new Link("localhost"));

		String resourcesJson = MappingUtils.read(new ClassPathResource("resources.json", getClass()));
		JavaType resourcesType = mapper.getTypeFactory().constructParametricType(Resources.class, String.class);
		Resources<String> result = mapper.readValue(resourcesJson, resourcesType);

		assertThat(result, is(expected));
	}

	@Test
	public void deserializeComplexResourcesSimply() throws IOException {

		List<Resource<String>> content = new ArrayList<Resource<String>>();
		content.add(new Resource<String>("first"));
		content.add(new Resource<String>("second"));

		Resources<Resource<String>> expected = new Resources<Resource<String>>(content);
		expected.add(new Link("localhost"));

		String resourcesJson = MappingUtils.read(new ClassPathResource("resources.json", getClass()));

		JavaType resourcesType = mapper.getTypeFactory().constructParametricType(Resources.class,
			mapper.getTypeFactory().constructParametricType(Resource.class, String.class));

		Resources<Resource<String>> result = mapper.readValue(resourcesJson, resourcesType);

		assertThat(result, is(expected));
	}

	@Test
	public void renderSimpleResource() throws Exception {

		Resource<String> data = new Resource<String>("first", new Link("localhost"));
		assertThat(write(data), is(MappingUtils.read(new ClassPathResource("resource.json", getClass()))));
	}

	@Test
	public void renderResourceWithCustomRel() throws Exception {

		Resource<String> data2 = new Resource<String>("second", new Link("localhost").withRel("custom"));
		assertThat(write(data2), is(MappingUtils.read(new ClassPathResource("resource2.json", getClass()))));
	}

	@Test
	public void renderResourceWithMultipleLinks() throws Exception {

		Resource<String> data3 = new Resource<String>("third",
			new Link("localhost"),
			new Link("second").withRel("second"),
			new Link("third").withRel("third"));
		assertThat(write(data3), is(MappingUtils.read(new ClassPathResource("resource3.json", getClass()))));
	}

	@Test
	public void renderResourceWithMultipleRels() throws Exception {

		Resource<String> data4 = new Resource<String>("third",
			new Link("localhost"),
			new Link("localhost").withRel("http://example.org/rels/todo"),
			new Link("second").withRel("second"),
			new Link("third").withRel("third"));
		assertThat(write(data4), is(MappingUtils.read(new ClassPathResource("resource4.json", getClass()))));
	}

	@Test
	public void deserializeResource() throws IOException {

		JavaType resourceStringType = mapper.getTypeFactory().constructParametricType(Resource.class, String.class);

		Resource expected = new Resource<String>("first", new Link("localhost"));
		Resource<String> actual = mapper.readValue(MappingUtils.read(new ClassPathResource("resource.json", getClass())),
			resourceStringType);

		assertThat(actual, is(expected));

		Resource<String> expected2 = new Resource<String>("second", new Link("localhost").withRel("custom"));
		Resource<String> actual2 = mapper.readValue(MappingUtils.read(new ClassPathResource("resource2.json", getClass())),
			resourceStringType);

		assertThat(actual2, is(expected2));

		Resource<String> expected3 = new Resource<String>("third",
			new Link("localhost"),
			new Link("second").withRel("second"),
			new Link("third").withRel("third"));
		Resource<String> actual3 = mapper.readValue(MappingUtils.read(new ClassPathResource("resource3.json", getClass())),
			resourceStringType);

		assertThat(actual3, is(expected3));

		Resource<String> expected4 = new Resource<String>("third",
			new Link("localhost"),
			new Link("localhost").withRel("http://example.org/rels/todo"),
			new Link("second").withRel("second"),
			new Link("third").withRel("third"));
		Resource<String> actual4 = mapper.readValue(MappingUtils.read(new ClassPathResource("resource4.json", getClass())),
			resourceStringType);

		assertThat(actual4, is(expected4));
	}

	@Test
	public void renderComplexStructure() throws Exception {

		List<Resource<String>> data = new ArrayList<Resource<String>>();
		data.add(new Resource<String>("first", new Link("localhost"), new Link("orders").withRel("orders")));
		data.add(new Resource<String>("second", new Link("remotehost"), new Link("order").withRel("orders")));

		Resources<Resource<String>> resources = new Resources<Resource<String>>(data);
		resources.add(new Link("localhost"));
		resources.add(new Link("/page/2").withRel("next"));

		assertThat(write(resources), is(MappingUtils.read(new ClassPathResource("resources-with-resource-objects.json", getClass()))));
	}

	@Test
	public void deserializeResources() throws Exception {

		List<Resource<String>> data = new ArrayList<Resource<String>>();
		data.add(new Resource<String>("first", new Link("localhost"), new Link("orders").withRel("orders")));
		data.add(new Resource<String>("second", new Link("remotehost"), new Link("order").withRel("orders")));

		Resources expected = new Resources<Resource<String>>(data);
		expected.add(new Link("localhost"));
		expected.add(new Link("/page/2").withRel("next"));

		Resources<Resource<String>> actual = mapper.readValue(MappingUtils.read(new ClassPathResource("resources-with-resource-objects.json", getClass())),
			mapper.getTypeFactory().constructParametricType(Resources.class,
				mapper.getTypeFactory().constructParametricType(Resource.class, String.class)));

		assertThat(actual, is(expected));
	}

	@Ignore
	@Test
	public void deserializeResourcesSimply() throws Exception {

		List<String> data = new ArrayList<String>();
		data.add("first");
		data.add("second");

		Resources expected = new Resources<String>(data);
		expected.add(new Link("localhost"));
		expected.add(new Link("/page/2").withRel("next"));

		Resources<String> actual = mapper.readValue(MappingUtils.read(new ClassPathResource("resources-with-resource-objects.json", getClass())),
			mapper.getTypeFactory().constructParametricType(Resources.class, String.class));

		assertThat(actual, is(expected));
	}


}
