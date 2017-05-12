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
import java.util.Arrays;

import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * A message converter that converts any object into an Uber document before bundling up into an
 * {@link HttpOutputMessage}, or that converts any incoming {@link HttpInputMessage} into an object.
 * 
 * @author Dietrich Schulten
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class UberMessageConverter extends AbstractHttpMessageConverter<Object> {

	private ObjectMapper objectMapper;

	public UberMessageConverter(ObjectMapper objectMapper) {

		Assert.notNull(objectMapper, "ObjectMapper must not be null");

		this.objectMapper = objectMapper;

		setSupportedMediaTypes(Arrays.asList(MediaTypes.UBER_JSON));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.http.converter.AbstractHttpMessageConverter#supports(java.lang.Class)
	 */
	@Override
	protected boolean supports(Class<?> clazz) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.http.converter.AbstractHttpMessageConverter#readInternal(java.lang.Class, org.springframework.http.HttpInputMessage)
	 */
	@Override
	protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {

		return this.objectMapper.readValue(inputMessage.getBody(), clazz);
	}

	@Override
	protected void writeInternal(Object t, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {

		Object uberDocument = UberUtils.toUber(t, this.objectMapper);
		
		JsonGenerator jsonGenerator = this.objectMapper.getFactory()
			.createGenerator(outputMessage.getBody(), JsonEncoding.UTF8);

		// A workaround for JsonGenerators not applying serialization features
		// https://github.com/FasterXML/jackson-databind/issues/12
		if (this.objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
			jsonGenerator.useDefaultPrettyPrinter();
		}

		try {
			this.objectMapper.writeValue(jsonGenerator, uberDocument);
		} catch (JsonProcessingException ex) {
			throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
		}
	}
}
