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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test the translation between {@link UberAction} and {@link HttpMethod}.
 * 
 */
public class UberActionTest {

	@Test
	public void translatesGetToNull() throws Exception {

		assertThat(UberAction.forRequestMethod(HttpMethod.GET), is(nullValue()));
		assertThat(UberAction.READ.toString(), is("read"));
	}

	@Test
	public void translatesPostToAppend() throws Exception {
		
		assertThat(UberAction.forRequestMethod(HttpMethod.POST), is(UberAction.APPEND));
		assertThat(UberAction.APPEND.toString(), is("append"));
	}

	@Test
	public void translatesPutToReplace() throws Exception {

		assertThat(UberAction.forRequestMethod(HttpMethod.PUT), is(UberAction.REPLACE));
		assertThat(UberAction.REPLACE.toString(), is("replace"));
	}

	@Test
	public void translatesDeleteToRemove() throws Exception {

		assertThat(UberAction.forRequestMethod(HttpMethod.DELETE), is(UberAction.REMOVE));
		assertThat(UberAction.REMOVE.toString(), is("remove"));
	}

	@Test
	public void translatesPatchToPartial() throws Exception {

		assertThat(UberAction.forRequestMethod(HttpMethod.PATCH), is(UberAction.PARTIAL));
		assertThat(UberAction.PARTIAL.toString(), is("partial"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void translateOptionsFails() throws Exception {
		UberAction.forRequestMethod(HttpMethod.OPTIONS);
	}
}
