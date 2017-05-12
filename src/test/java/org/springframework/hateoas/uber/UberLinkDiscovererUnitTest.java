/*
 * Copyright 2013-2015 the original author or authors.
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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.core.AbstractLinkDiscovererUnitTest;
import org.springframework.hateoas.hal.HalLinkDiscoverer;

/**
 * Unit tests for {@link HalLinkDiscoverer}.
 * 
 * @author Oliver Gierke
 */
public class UberLinkDiscovererUnitTest extends AbstractLinkDiscovererUnitTest {

	LinkDiscoverer discoverer = new UberLinkDiscoverer();
	String sample;

	@Before
	public void setUp() throws IOException {

		this.discoverer = new UberLinkDiscoverer();
		this.sample = read(new ClassPathResource("link-discovery.json", getClass()));
	}
	/**
	 * @see #314
	 */
	@Test
	public void discoversFullyQualifiedRel() {
		assertThat(getDiscoverer().findLinkWithRel("http://foo.com/bar", this.sample), is(notNullValue()));
	}

	@Override
	protected LinkDiscoverer getDiscoverer() {
		return discoverer;
	}

	@Override
	protected String getInputString() {
		return this.sample;
	}

	@Override
	protected String getInputStringWithoutLinkContainer() {
		return "{ \"uber\" : { \"data\" : []}}";
	}
}
