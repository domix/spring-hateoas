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

import static org.springframework.hateoas.JacksonHelper.*;
import static org.springframework.hateoas.uber.NewUberDocument.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author Greg Turnquist
 */
public class Jackson2UberModule extends SimpleModule {

	public Jackson2UberModule() {
		super("uber-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));


		setMixInAnnotation(ResourceSupport.class, ResourceSupportMixin.class);
		setMixInAnnotation(Resource.class, ResourceMixin.class);
		setMixInAnnotation(Resources.class, ResourcesMixin.class);
	}

	static class UberResourceSupportSerializer extends ContainerSerializer<ResourceSupport> implements ContextualSerializer {

		private final BeanProperty property;

		UberResourceSupportSerializer(BeanProperty property) {

			super(ResourceSupport.class, false);
			this.property = property;
		}

		UberResourceSupportSerializer() {
			this(null);
		}

		@Override
		public void serialize(ResourceSupport value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			UberDocumentWrapper uber = new UberDocumentWrapper(uberDocument()
				.version("1.0")
				.data(UberData.toUberData(value).getData())
				.build());

			provider
				.findValueSerializer(UberDocumentWrapper.class, property)
				.serialize(uber, gen, provider);
		}

		/**
		 * Accessor for finding declared (static) element type for
		 * type this serializer is used for.
		 */
		@Override
		public JavaType getContentType() {
			return null;
		}

		/**
		 * Accessor for serializer used for serializing contents
		 * (List and array elements, Map values etc) of the
		 * container for which this serializer is used, if it is
		 * known statically.
		 * Note that for dynamic types this may return null; if so,
		 * caller has to instead use {@link #getContentType()} and
		 * {@link SerializerProvider#findValueSerializer}.
		 */
		@Override
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		/**
		 * Method called to determine if the given value (of type handled by
		 * this serializer) contains exactly one element.
		 * Note: although it might seem sensible to instead define something
		 * like "getElementCount()" method, this would not work well for
		 * containers that do not keep track of size (like linked lists may
		 * not).
		 *
		 * @param value
		 */
		@Override
		public boolean hasSingleElement(ResourceSupport value) {
			return false;
		}

		/**
		 * Method that needs to be implemented to allow construction of a new
		 * serializer object with given {@link TypeSerializer}, used when
		 * addition type information is to be embedded.
		 *
		 * @param vts
		 */
		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}

		/**
		 * Method called to see if a different (or differently configured) serializer
		 * is needed to serialize values of specified property.
		 * Note that instance that this method is called on is typically shared one and
		 * as a result method should <b>NOT</b> modify this instance but rather construct
		 * and return a new instance. This instance should only be returned as-is, in case
		 * it is already suitable for use.
		 *
		 * @param prov Serializer provider to use for accessing config, other serializers
		 * @param property Method or field that represents the property
		 * (and is used to access value to serialize).
		 * Should be available; but there may be cases where caller can not provide it and
		 * null is passed instead (in which case impls usually pass 'this' serializer as is)
		 * @return Serializer to use for serializing values of specified property;
		 * may be this instance or a new instance.
		 * @throws JsonMappingException
		 */
		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
			return new UberResourceSupportSerializer(property);
		}
	}

	static class UberResourceSerializer extends ContainerSerializer<Resource<?>> implements ContextualSerializer {

		private final BeanProperty property;

		UberResourceSerializer(BeanProperty property) {

			super(Resource.class, false);
			this.property = property;
		}

		UberResourceSerializer() {
			this(null);
		}

		@Override
		public void serialize(Resource<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			UberDocumentWrapper uber = new UberDocumentWrapper(uberDocument()
				.version("1.0")
				.data(UberData.toUberData(value).getData())
				.build());

			provider
				.findValueSerializer(UberDocumentWrapper.class, property)
				.serialize(uber, gen, provider);
		}

		/**
		 * Accessor for finding declared (static) element type for
		 * type this serializer is used for.
		 */
		@Override
		public JavaType getContentType() {
			return null;
		}

		/**
		 * Accessor for serializer used for serializing contents
		 * (List and array elements, Map values etc) of the
		 * container for which this serializer is used, if it is
		 * known statically.
		 * Note that for dynamic types this may return null; if so,
		 * caller has to instead use {@link #getContentType()} and
		 * {@link SerializerProvider#findValueSerializer}.
		 */
		@Override
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		/**
		 * Method called to determine if the given value (of type handled by
		 * this serializer) contains exactly one element.
		 * Note: although it might seem sensible to instead define something
		 * like "getElementCount()" method, this would not work well for
		 * containers that do not keep track of size (like linked lists may
		 * not).
		 *
		 * @param value
		 */
		@Override
		public boolean hasSingleElement(Resource<?> value) {
			return false;
		}

		/**
		 * Method that needs to be implemented to allow construction of a new
		 * serializer object with given {@link TypeSerializer}, used when
		 * addition type information is to be embedded.
		 *
		 * @param vts
		 */
		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}

		/**
		 * Method called to see if a different (or differently configured) serializer
		 * is needed to serialize values of specified property.
		 * Note that instance that this method is called on is typically shared one and
		 * as a result method should <b>NOT</b> modify this instance but rather construct
		 * and return a new instance. This instance should only be returned as-is, in case
		 * it is already suitable for use.
		 *
		 * @param prov Serializer provider to use for accessing config, other serializers
		 * @param property Method or field that represents the property
		 * (and is used to access value to serialize).
		 * Should be available; but there may be cases where caller can not provide it and
		 * null is passed instead (in which case impls usually pass 'this' serializer as is)
		 * @return Serializer to use for serializing values of specified property;
		 * may be this instance or a new instance.
		 * @throws JsonMappingException
		 */
		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
			return new UberResourceSerializer(property);
		}
	}

	static class UberResourcesSerializer extends ContainerSerializer<Resources<?>> implements ContextualSerializer {

		private BeanProperty property;

		UberResourcesSerializer(BeanProperty property) {

			super(Resources.class, false);
			this.property = property;
		}

		UberResourcesSerializer() {
			this(null);
		}
		
		@Override
		public void serialize(Resources<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			UberDocumentWrapper uber = new UberDocumentWrapper(uberDocument()
				.version("1.0")
				.data(UberData.toUberData(value).getData())
				.build());

			provider
				.findValueSerializer(UberDocumentWrapper.class, property)
				.serialize(uber, gen, provider);
		}

		/**
		 * Accessor for finding declared (static) element type for
		 * type this serializer is used for.
		 */
		@Override
		public JavaType getContentType() {
			return null;
		}

		/**
		 * Accessor for serializer used for serializing contents
		 * (List and array elements, Map values etc) of the
		 * container for which this serializer is used, if it is
		 * known statically.
		 * Note that for dynamic types this may return null; if so,
		 * caller has to instead use {@link #getContentType()} and
		 * {@link SerializerProvider#findValueSerializer}.
		 */
		@Override
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		/**
		 * Method called to determine if the given value (of type handled by
		 * this serializer) contains exactly one element.
		 * Note: although it might seem sensible to instead define something
		 * like "getElementCount()" method, this would not work well for
		 * containers that do not keep track of size (like linked lists may
		 * not).
		 *
		 * @param value
		 */
		@Override
		public boolean hasSingleElement(Resources<?> value) {
			return false;
		}

		/**
		 * Method that needs to be implemented to allow construction of a new
		 * serializer object with given {@link TypeSerializer}, used when
		 * addition type information is to be embedded.
		 *
		 * @param vts
		 */
		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}

		/**
		 * Method called to see if a different (or differently configured) serializer
		 * is needed to serialize values of specified property.
		 * Note that instance that this method is called on is typically shared one and
		 * as a result method should <b>NOT</b> modify this instance but rather construct
		 * and return a new instance. This instance should only be returned as-is, in case
		 * it is already suitable for use.
		 *
		 * @param prov Serializer provider to use for accessing config, other serializers
		 * @param property Method or field that represents the property
		 * (and is used to access value to serialize).
		 * Should be available; but there may be cases where caller can not provide it and
		 * null is passed instead (in which case impls usually pass 'this' serializer as is)
		 * @return Serializer to use for serializing values of specified property;
		 * may be this instance or a new instance.
		 * @throws JsonMappingException
		 */
		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
			return new UberResourcesSerializer(property);
		}
	}

	static class UberResourceSupportDeserializer extends ContainerDeserializerBase<ResourceSupport> implements ContextualDeserializer {

		private JavaType contentType;

		UberResourceSupportDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		UberResourceSupportDeserializer() {
			this(TypeFactory.defaultInstance().constructSimpleType(UberDocumentWrapper.class, new JavaType[0]));
		}
		

		/**
		 * Method that can be called to ask implementation to deserialize
		 * JSON content into the value type this serializer handles.
		 * Returned instance is to be constructed by method itself.
		 * <p>
		 * Pre-condition for this method is that the parser points to the
		 * first event that is part of value to deserializer (and which
		 * is never JSON 'null' literal, more on this below): for simple
		 * types it may be the only value; and for structured types the
		 * Object start marker or a FIELD_NAME.
		 * </p>
		 * The two possible input conditions for structured types result
		 * from polymorphism via fields. In the ordinary case, Jackson
		 * calls this method when it has encountered an OBJECT_START,
		 * and the method implementation must advance to the next token to
		 * see the first field name. If the application configures
		 * polymorphism via a field, then the object looks like the following.
		 * <pre>
		 *      {
		 *          "@class": "class name",
		 *          ...
		 *      }
		 *  </pre>
		 * Jackson consumes the two tokens (the <tt>@class</tt> field name
		 * and its value) in order to learn the class and select the deserializer.
		 * Thus, the stream is pointing to the FIELD_NAME for the first field
		 * after the @class. Thus, if you want your method to work correctly
		 * both with and without polymorphism, you must begin your method with:
		 * <pre>
		 *       if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
		 *         jp.nextToken();
		 *       }
		 *  </pre>
		 * This results in the stream pointing to the field name, so that
		 * the two conditions align.
		 * Post-condition is that the parser will point to the last
		 * event that is part of deserialized value (or in case deserialization
		 * fails, event that was not recognized or usable, which may be
		 * the same event as the one it pointed to upon call).
		 * Note that this method is never called for JSON null literal,
		 * and thus deserializers need (and should) not check for it.
		 *
		 * @param p Parsed used for reading JSON content
		 * @param ctxt Context that can be used to access information about
		 * this deserialization activity.
		 * @return Deserialized value
		 */
		@Override
		public ResourceSupport deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

			UberDocumentWrapper uber = p.getCodec().readValue(p, UberDocumentWrapper.class);

			ResourceSupport resourceSupport = new ResourceSupport();

			resourceSupport.add(uber.getUber().getLinks());

			return resourceSupport;
		}

		/**
		 * Accessor for declared type of contained value elements; either exact
		 * type, or one of its supertypes.
		 */
		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		/**
		 * Method called to see if a different (or differently configured) deserializer
		 * is needed to deserialize values of specified property.
		 * Note that instance that this method is called on is typically shared one and
		 * as a result method should <b>NOT</b> modify this instance but rather construct
		 * and return a new instance. This instance should only be returned as-is, in case
		 * it is already suitable for use.
		 *
		 * @param ctxt Deserialization context to access configuration, additional
		 * deserializers that may be needed by this deserializer
		 * @param property Method, field or constructor parameter that represents the property
		 * (and is used to assign deserialized value).
		 * Should be available; but there may be cases where caller can not provide it and
		 * null is passed instead (in which case impls usually pass 'this' deserializer as is)
		 * @return Deserializer to use for deserializing values of specified property;
		 * may be this instance or a new instance.
		 * @throws JsonMappingException
		 */
		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {

			if (property != null) {
				JavaType vc = property.getType().getContentType();
				return new UberResourceSupportDeserializer(vc);
			} else {
				return new UberResourceSupportDeserializer(ctxt.getContextualType());
			}
		}

		/**
		 * Accesor for deserializer use for deserializing content values.
		 */
		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}
	}

	static class UberResourceDeserializer extends ContainerDeserializerBase<Resource<?>> implements ContextualDeserializer {

		private JavaType contentType;

		UberResourceDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		UberResourceDeserializer() {
			this(TypeFactory.defaultInstance().constructSimpleType(UberDocumentWrapper.class, new JavaType[0]));
		}

		/**
		 * Method that can be called to ask implementation to deserialize
		 * JSON content into the value type this serializer handles.
		 * Returned instance is to be constructed by method itself.
		 * <p>
		 * Pre-condition for this method is that the parser points to the
		 * first event that is part of value to deserializer (and which
		 * is never JSON 'null' literal, more on this below): for simple
		 * types it may be the only value; and for structured types the
		 * Object start marker or a FIELD_NAME.
		 * </p>
		 * The two possible input conditions for structured types result
		 * from polymorphism via fields. In the ordinary case, Jackson
		 * calls this method when it has encountered an OBJECT_START,
		 * and the method implementation must advance to the next token to
		 * see the first field name. If the application configures
		 * polymorphism via a field, then the object looks like the following.
		 * <pre>
		 *      {
		 *          "@class": "class name",
		 *          ...
		 *      }
		 *  </pre>
		 * Jackson consumes the two tokens (the <tt>@class</tt> field name
		 * and its value) in order to learn the class and select the deserializer.
		 * Thus, the stream is pointing to the FIELD_NAME for the first field
		 * after the @class. Thus, if you want your method to work correctly
		 * both with and without polymorphism, you must begin your method with:
		 * <pre>
		 *       if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
		 *         jp.nextToken();
		 *       }
		 *  </pre>
		 * This results in the stream pointing to the field name, so that
		 * the two conditions align.
		 * Post-condition is that the parser will point to the last
		 * event that is part of deserialized value (or in case deserialization
		 * fails, event that was not recognized or usable, which may be
		 * the same event as the one it pointed to upon call).
		 * Note that this method is never called for JSON null literal,
		 * and thus deserializers need (and should) not check for it.
		 *
		 * @param p Parsed used for reading JSON content
		 * @param ctxt Context that can be used to access information about
		 * this deserialization activity.
		 * @return Deserialized value
		 */
		@Override
		public Resource<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

			UberDocumentWrapper uber = p.getCodec().readValue(p, UberDocumentWrapper.class);

			for (UberData uberData : uber.getUber().getData()) {
				if (uberData.getLabel() != null && uberData.getLabel().equals(Resource.class.getCanonicalName() + ".content")) {
					return new Resource<Object>(uberData.getValue(), uber.getUber().getLinks());
				}
			}
			throw new IllegalStateException("No data entry containing a 'value' was found in this document!");
		}

		/**
		 * Accessor for declared type of contained value elements; either exact
		 * type, or one of its supertypes.
		 */
		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		/**
		 * Method called to see if a different (or differently configured) deserializer
		 * is needed to deserialize values of specified property.
		 * Note that instance that this method is called on is typically shared one and
		 * as a result method should <b>NOT</b> modify this instance but rather construct
		 * and return a new instance. This instance should only be returned as-is, in case
		 * it is already suitable for use.
		 *
		 * @param ctxt Deserialization context to access configuration, additional
		 * deserializers that may be needed by this deserializer
		 * @param property Method, field or constructor parameter that represents the property
		 * (and is used to assign deserialized value).
		 * Should be available; but there may be cases where caller can not provide it and
		 * null is passed instead (in which case impls usually pass 'this' deserializer as is)
		 * @return Deserializer to use for deserializing values of specified property;
		 * may be this instance or a new instance.
		 * @throws JsonMappingException
		 */
		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {

			if (property != null) {
				JavaType vc = property.getType().getContentType();
				return new UberResourceDeserializer(vc);
			} else {
				return new UberResourceDeserializer(ctxt.getContextualType());
			}
		}

		/**
		 * Accesor for deserializer use for deserializing content values.
		 */
		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}
	}

	static class UberResourcesDeserializer extends ContainerDeserializerBase<Resources<?>> implements ContextualDeserializer {

		private JavaType contentType;

		UberResourcesDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		UberResourcesDeserializer() {
			this(TypeFactory.defaultInstance().constructSimpleType(UberDocumentWrapper.class, new JavaType[0]));
		}
		
		/**
		 * Method called to see if a different (or differently configured) deserializer
		 * is needed to deserialize values of specified property.
		 * Note that instance that this method is called on is typically shared one and
		 * as a result method should <b>NOT</b> modify this instance but rather construct
		 * and return a new instance. This instance should only be returned as-is, in case
		 * it is already suitable for use.
		 *
		 * @param ctxt Deserialization context to access configuration, additional
		 * deserializers that may be needed by this deserializer
		 * @param property Method, field or constructor parameter that represents the property
		 * (and is used to assign deserialized value).
		 * Should be available; but there may be cases where caller can not provide it and
		 * null is passed instead (in which case impls usually pass 'this' deserializer as is)
		 * @return Deserializer to use for deserializing values of specified property;
		 * may be this instance or a new instance.
		 * @throws JsonMappingException
		 */
		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {

			if (property != null) {
				JavaType vc = property.getType().getContentType();
				return new UberResourcesDeserializer(vc);
			} else {
				return new UberResourcesDeserializer(ctxt.getContextualType());
			}
		}

		/**
		 * Accessor for declared type of contained value elements; either exact
		 * type, or one of its supertypes.
		 */
		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		/**
		 * Accesor for deserializer use for deserializing content values.
		 */
		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}

		/**
		 * Method that can be called to ask implementation to deserialize
		 * JSON content into the value type this serializer handles.
		 * Returned instance is to be constructed by method itself.
		 * <p>
		 * Pre-condition for this method is that the parser points to the
		 * first event that is part of value to deserializer (and which
		 * is never JSON 'null' literal, more on this below): for simple
		 * types it may be the only value; and for structured types the
		 * Object start marker or a FIELD_NAME.
		 * </p>
		 * The two possible input conditions for structured types result
		 * from polymorphism via fields. In the ordinary case, Jackson
		 * calls this method when it has encountered an OBJECT_START,
		 * and the method implementation must advance to the next token to
		 * see the first field name. If the application configures
		 * polymorphism via a field, then the object looks like the following.
		 * <pre>
		 *      {
		 *          "@class": "class name",
		 *          ...
		 *      }
		 *  </pre>
		 * Jackson consumes the two tokens (the <tt>@class</tt> field name
		 * and its value) in order to learn the class and select the deserializer.
		 * Thus, the stream is pointing to the FIELD_NAME for the first field
		 * after the @class. Thus, if you want your method to work correctly
		 * both with and without polymorphism, you must begin your method with:
		 * <pre>
		 *       if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
		 *         jp.nextToken();
		 *       }
		 *  </pre>
		 * This results in the stream pointing to the field name, so that
		 * the two conditions align.
		 * Post-condition is that the parser will point to the last
		 * event that is part of deserialized value (or in case deserialization
		 * fails, event that was not recognized or usable, which may be
		 * the same event as the one it pointed to upon call).
		 * Note that this method is never called for JSON null literal,
		 * and thus deserializers need (and should) not check for it.
		 *
		 * @param p Parsed used for reading JSON content
		 * @param ctxt Context that can be used to access information about
		 * this deserialization activity.
		 * @return Deserialized value
		 */
		@Override
		public Resources<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

			UberDocumentWrapper uber = p.getCodec().readValue(p, UberDocumentWrapper.class);
			List<Object> content = new ArrayList<Object>();

			for (UberData uberData : uber.getUber().getData()) {

				if (!uberData.isLink()) {

					List<Link> resourceLinks = new ArrayList<Link>();
					Resource<?> resource = null;

					for (UberData item : uberData.getData()) {
						if (item.isLink()) {
							for (String rel : item.getRels()) {
								resourceLinks.add(new Link(item.getUrl()).withRel(rel));
							}
						} else {
							resource = new Resource<Object>(item.getValue());
						}
					}

					if (resource != null) {
						resource.add(resourceLinks);
						content.add(resource);
					} else {
						throw new RuntimeException("No content!");
					}
				}
			}



			if (isResourcesOfResource(this.getContentType())) {
				/*
				 * Either return a Resources<Resource<T>>...
				 */

				return new Resources<Object>(content, uber.getUber().getLinks());
			} else {
				/*
				 * ...or return a Resources<T>
				 */

				List<Object> resourceLessContent = new ArrayList<Object>();

				for (Object item : content) {
					Resource<?> resource = (Resource<?>) item;
					resourceLessContent.add(resource.getContent());
				}
				
				return new Resources<Object>(resourceLessContent, uber.getUber().getLinks());
			}
		}
	}

	static class UberHandlerInstantiator extends HandlerInstantiator {

		private final Map<Class<?>, Object> serializers = new HashMap<Class<?>, Object>();

		UberHandlerInstantiator() {

			this.serializers.put(UberResourceSerializer.class, new UberResourceSerializer());
		}

		/**
		 * Method called to get an instance of deserializer of specified type.
		 *
		 * @param config Deserialization configuration in effect
		 * @param annotated Element (Class, Method, Field, constructor parameter) that
		 * had annotation defining class of deserializer to construct (to allow
		 * implementation use information from other annotations)
		 * @param deserClass Class of deserializer instance to return
		 * @return Deserializer instance to use
		 */
		@Override
		public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) {
			return (JsonDeserializer<?>) findInstance(deserClass);
		}

		/**
		 * Method called to get an instance of key deserializer of specified type.
		 *
		 * @param config Deserialization configuration in effect
		 * @param annotated Element (Class, Method, Field, constructor parameter) that
		 * had annotation defining class of key deserializer to construct (to allow
		 * implementation use information from other annotations)
		 * @param keyDeserClass Class of key deserializer instance to return
		 * @return Key deserializer instance to use
		 */
		@Override
		public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
			return (KeyDeserializer) findInstance(keyDeserClass);
		}

		/**
		 * Method called to get an instance of serializer of specified type.
		 *
		 * @param config Serialization configuration in effect
		 * @param annotated Element (Class, Method, Field) that
		 * had annotation defining class of serializer to construct (to allow
		 * implementation use information from other annotations)
		 * @param serClass Class of serializer instance to return
		 * @return Serializer instance to use
		 */
		@Override
		public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
			return (JsonSerializer<?>) findInstance(serClass);
		}

		/**
		 * Method called to get an instance of TypeResolverBuilder of specified type.
		 *
		 * @param config Mapper configuration in effect (either SerializationConfig or
		 * DeserializationConfig, depending on when instance is being constructed)
		 * @param annotated annotated Element (Class, Method, Field) that
		 * had annotation defining class of builder to construct (to allow
		 * implementation use information from other annotations)
		 * @param builderClass Class of builder instance to return
		 * @return TypeResolverBuilder instance to use
		 */
		@Override
		public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) {
			return (TypeResolverBuilder<?>) findInstance(builderClass);
		}

		/**
		 * Method called to get an instance of TypeIdResolver of specified type.
		 *
		 * @param config Mapper configuration in effect (either SerializationConfig or
		 * DeserializationConfig, depending on when instance is being constructed)
		 * @param annotated annotated Element (Class, Method, Field) that
		 * had annotation defining class of resolver to construct (to allow
		 * implementation use information from other annotations)
		 * @param resolverClass Class of resolver instance to return
		 * @return TypeResolverBuilder instance to use
		 */
		@Override
		public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
			return (TypeIdResolver) findInstance(resolverClass);
		}

		private Object findInstance(Class<?> type) {

			Object result = this.serializers.get(type);
			return result != null ? result : BeanUtils.instantiateClass(type);
		}
	}

}
