/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.hateoas.hal.forms;

import static org.springframework.hateoas.affordance.Suggestions.*;
import static org.springframework.hateoas.hal.forms.HalFormsDocument.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.affordance.Suggestions;
import org.springframework.hateoas.affordance.Suggestions.*;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.Jackson2HalModule.EmbeddedMapper;
import org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.hal.Jackson2HalModule.HalLinkListDeserializer;
import org.springframework.hateoas.hal.Jackson2HalModule.HalLinkListSerializer;
import org.springframework.hateoas.hal.Jackson2HalModule.OptionalListJackson2Serializer;
import org.springframework.hateoas.hal.LinkMixin;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Serialize/Deserialize all the parts of HAL-Form documents using Jackson.
 *
 * @author Dietrich Schulten
 * @author Greg Turnquist
 */
public class Jackson2HalFormsModule extends SimpleModule {

	private static final long serialVersionUID = -4496351128468451196L;

	public Jackson2HalFormsModule() {

		super("hal-forms-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));

		setMixInAnnotation(Link.class, LinkMixin.class);
		setMixInAnnotation(Suggestions.class, SuggestionsMixin.class);
		setMixInAnnotation(ValueSuggestions.class, ValueSuggestionsMixin.class);

		addSerializer(RemoteSuggestions.class, new RemoteSuggestionsSerializer());
		addSerializer(ExternalSuggestions.class, new ExternalSuggestionSerializer());
		addDeserializer(Suggestions.class, new SuggestDeserializer());
	}

	//
	// Serializers
	//

	/**
	 * Serialize {@link List} of {@link Template}s into HAL-Forms format.
	 */
	static class HalFormsTemplateListSerializer extends ContainerSerializer<List<Template>> implements ContextualSerializer {

		private static final long serialVersionUID = 1L;

		private static final String RELATION_MESSAGE_TEMPLATE = "_templates.%s.title";

		private final BeanProperty property;
		private final MessageSourceAccessor messageSource;

		public HalFormsTemplateListSerializer(BeanProperty property, MessageSourceAccessor messageSource) {

			super(TypeFactory.defaultInstance().constructType(List.class));
			this.property = property;
			this.messageSource = messageSource;
		}

		public HalFormsTemplateListSerializer(MessageSourceAccessor messageSource) {
			this(null, messageSource);
		}

		public HalFormsTemplateListSerializer() {
			this(null, null);
		}

		@Override
		public void serialize(List<Template> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			// sort templates according to their relation
			Map<String, List<Object>> sortedTemplates = new LinkedHashMap<String, List<Object>>();

			for (Template template : value) {
				if (sortedTemplates.get(template.getKey()) == null) {
					sortedTemplates.put(template.getKey(), new ArrayList<Object>());
				}
				sortedTemplates.get(template.getKey()).add(toHalFormsTemplate(template));
			}

			TypeFactory typeFactory = provider.getConfig().getTypeFactory();
			JavaType keyType = typeFactory.constructSimpleType(String.class, new JavaType[0]);
			JavaType valueType = typeFactory.constructCollectionType(ArrayList.class, Object.class);
			JavaType mapType = typeFactory.constructMapType(HashMap.class, keyType, valueType);

			MapSerializer serializer = MapSerializer.construct(Collections.<String> emptySet(), mapType, true, null,
				provider.findKeySerializer(keyType, null), new OptionalListJackson2Serializer(this.property), null);

			if (!sortedTemplates.isEmpty()) {
				serializer.serialize(sortedTemplates, gen, provider);
			}
		}

		/**
		 * Wraps the given link into a HAL specific extension.
		 *
		 * @param template must not be {@literal null}.
		 * @return
		 */
		private HalFormsTemplate toHalFormsTemplate(Template template) {

			String key = template.getKey();
			String title = getTitle(key);

			if (title == null) {
				title = getTitle(key.contains(":") ? key.substring(key.indexOf(":") + 1) : key);
			}

			return new HalFormsTemplate(template, title);
		}

		/**
		 * Returns the title for the given local link relation resolved through the configured {@link MessageSourceAccessor}
		 *
		 * @param localRel must not be {@literal null} or empty.
		 * @return
		 */
		private String getTitle(String localRel) {

			Assert.hasText(localRel, "Local relation must not be null or empty!");

			try {
				return this.messageSource == null ? null
					: this.messageSource.getMessage(String.format(RELATION_MESSAGE_TEMPLATE, localRel));
			} catch (NoSuchMessageException o_O) {
				return null;
			}
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
		public boolean hasSingleElement(List<Template> value) {
			return value.size() == 1;
		}

		/**
		 * Method called to check whether given serializable value is
		 * considered "empty" value (for purposes of suppressing serialization
		 * of empty values).
		 * Default implementation will consider only null values to be empty.
		 * NOTE: replaces {@link #isEmpty(Object)}, which was deprecated in 2.5
		 *
		 * @param provider
		 * @param value
		 * @since 2.5
		 */
		@Override
		public boolean isEmpty(SerializerProvider provider, List<Template> value) {
			return value.isEmpty();
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
			return new HalFormsTemplateListSerializer(property, null);
		}
	}

	/**
	 * Serialize {@link RemoteSuggestions} properties into HAL-Forms format.
	 */
	static class RemoteSuggestionsSerializer extends StdSerializer<RemoteSuggestions> {

		public RemoteSuggestionsSerializer() {
			super(TypeFactory.defaultInstance().constructType(RemoteSuggestions.class));
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		public void serialize(RemoteSuggestions value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonGenerationException {

			jgen.writeStartObject();
			jgen.writeStringField("href", value.getTemplate().toString());

			if (!value.getTemplate().getVariables().isEmpty()) {
				jgen.writeObjectField("templated", true);
			}

			writePromptAndValueFields(value, jgen);

			jgen.writeEndObject();
		}
	}

	/**
	 * Serialize {@link ExternalSuggestions} properties into HAL-Forms format.
	 */
	static class ExternalSuggestionSerializer extends StdSerializer<ExternalSuggestions> {

		public ExternalSuggestionSerializer() {
			super(TypeFactory.defaultInstance().constructType(ExternalSuggestions.class));
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		public void serialize(ExternalSuggestions value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException {

			jgen.writeStartObject();
			jgen.writeStringField("embedded", value.getReference());

			writePromptAndValueFields(value, jgen);

			jgen.writeEndObject();
		}
	}

	static class HalEmbeddedResourcesSerializer extends ContainerSerializer<Collection<?>>
			implements ContextualSerializer {

		private static final long serialVersionUID = 1L;
		private final BeanProperty property;
		private final EmbeddedMapper embeddedMapper;

		public HalEmbeddedResourcesSerializer(EmbeddedMapper embeddedMapper) {
			this(null, embeddedMapper);
		}

		public HalEmbeddedResourcesSerializer(BeanProperty property, EmbeddedMapper embeddedMapper) {
			super(Collection.class, false);
			this.embeddedMapper = embeddedMapper;
			this.property = property;
		}

		@Override
		public void serialize(Collection<?> value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException {
			Map<String, Object> embeddeds = embeddedMapper.map(value);

			provider.findValueSerializer(Map.class, property).serialize(embeddeds, jgen, provider);
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new HalEmbeddedResourcesSerializer(property, embeddedMapper);
		}

		@Override
		public JavaType getContentType() {
			return null;
		}

		@Override
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		@Override
		public boolean hasSingleElement(Collection<?> value) {
			return value.size() == 1;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}

		@Override
		public boolean isEmpty(SerializerProvider provider, Collection<?> value) {
			return value.isEmpty();
		}

	}

	/**
	 * Write {@link Suggestions} into a property entry.
	 *
	 * @param suggestions
	 * @param generator
	 * @throws IOException
	 */
	private static void writePromptAndValueFields(Suggestions suggestions, JsonGenerator generator) throws IOException {

		if (suggestions.getPromptField() != null) {
			generator.writeObjectField("prompt-field", suggestions.getPromptField());
		}

		if (suggestions.getValueField() != null) {
			generator.writeObjectField("value-field", suggestions.getValueField());
		}
	}

	//
	// Deserializers
	//

	/**
	 * Deserialize an entire <a href="https://rwcbook.github.io/hal-forms/">HAL-Forms</a> document.
	 */
	static class HalFormsDocumentDeserializer extends JsonDeserializer<HalFormsDocument> {

		private final HalLinkListDeserializer linkDeser = new HalLinkListDeserializer();
		private final HalFormsTemplateListDeserializer templateDeser = new HalFormsTemplateListDeserializer();

		@Override
		public HalFormsDocument deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException {

			HalFormsDocument.HalFormsDocumentBuilder halFormsDocumentBuilder = halFormsDocument();

			while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {

				if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
					throw new JsonParseException(jp, "Expected property ", jp.getCurrentLocation());
				}

				jp.nextToken();

				if ("_links".equals(jp.getCurrentName())) {
					halFormsDocumentBuilder.links(this.linkDeser.deserialize(jp, ctxt));
				} else if ("_templates".equals(jp.getCurrentName())) {
					halFormsDocumentBuilder.templates(this.templateDeser.deserialize(jp, ctxt));
				}
			}

			return halFormsDocumentBuilder.build();
		}
	}

	/**
	 * Deserialize an object of HAL-Forms {@link Template}s into a {@link List} of {@link Template}s.
	 */
	static class HalFormsTemplateListDeserializer extends ContainerDeserializerBase<List<Template>> {

		public HalFormsTemplateListDeserializer() {
			super(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Template.class));
		}

		/**
		 * Accessor for declared type of contained value elements; either exact
		 * type, or one of its supertypes.
		 */
		@Override
		public JavaType getContentType() {
			return null;
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
		 * @param jp Parsed used for reading JSON content
		 * @param ctxt Context that can be used to access information about
		 * this deserialization activity.
		 * @return Deserialized value
		 */
		@Override
		public List<Template> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

			List<Template> result = new ArrayList<Template>();
			String relation;
			Template template;

			// links is an object, so we parse till we find its end.
			while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {

				if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
					throw new JsonParseException(jp, "Expected relation name", jp.getCurrentLocation());
				}

				// save the relation in case the link does not contain it
				relation = jp.getText();

				if (JsonToken.START_ARRAY.equals(jp.nextToken())) {
					while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
						template = jp.readValueAs(Template.class);
						template.setKey(relation);
						result.add(template);
					}
				} else {
					template = jp.readValueAs(Template.class);
					template.setKey(relation);
					result.add(template);
				}
			}

			return result;
		}
	}

	/**
	 * Deserialize all of the {@link Suggestions} properties.
	 */
	static class SuggestDeserializer extends JsonDeserializer<Suggestions> {

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		public Suggestions deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

			JsonDeserializer<Object> deser = ctxt.findRootValueDeserializer(ctxt.constructType(Object.class));

			List<Object> list = new ArrayList<Object>();

			String textField = null;
			String valueField = null;
			String embeddedRel = null;
			String href = null;
			
			while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {

				if ("values".equals(jp.getCurrentName())) {

					jp.nextToken();

					while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
						list.add(deser.deserialize(jp, ctxt));
					}

				} else if ("embedded".equals(jp.getCurrentName())) {
					embeddedRel = jp.getText();
				} else if ("href".equals(jp.getCurrentName())) {
					href = jp.getText();
				} else if ("prompt-field".equals(jp.getCurrentName())) {
					textField = jp.getText();
				} else if ("value-field".equals(jp.getCurrentName())) {
					valueField = jp.getText();
				}
			}

			if (valueField != null) {
				return values(list).withPromptField(textField).withValueField(valueField);
			} else if (href != null) {
				return remote(href).withPromptField(textField).withValueField(valueField);
			} else if (embeddedRel != null) {
				return external(embeddedRel).withPromptField(textField).withValueField(valueField);
			}

			return NONE;
		}
	}

	/**
	 * Deserialize a {@link MediaType} embedded inside a HAL-Forms document.
	 */
	static class MediaTypesDeserializer extends ContainerDeserializerBase<List<MediaType>> {

		private static final long serialVersionUID = -7218376603548438390L;

		public MediaTypesDeserializer() {
			super(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, MediaType.class));
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentType()
		 */
		@Override
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentDeserializer()
		 */
		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		public List<MediaType> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			return MediaType.parseMediaTypes(p.getText());
		}
	}

	/**
	 * Create new HAL-Forms serializers based on the context.
	 */
	public static class HalFormsHandlerInstantiator extends HalHandlerInstantiator {

		private final Map<Class<?>, Object> serializers = new HashMap<Class<?>, Object>();

		public HalFormsHandlerInstantiator(RelProvider resolver, CurieProvider curieProvider,
										   MessageSourceAccessor messageSource, boolean enforceEmbeddedCollections) {

			super(resolver, curieProvider, messageSource, enforceEmbeddedCollections);

			EmbeddedMapper mapper = new EmbeddedMapper(resolver, curieProvider, enforceEmbeddedCollections);

			this.serializers.put(HalLinkListSerializer.class, new HalLinkListSerializer(curieProvider, mapper, messageSource));
			this.serializers.put(HalFormsTemplateListSerializer.class, new HalFormsTemplateListSerializer(messageSource));
			this.serializers.put(HalEmbeddedResourcesSerializer.class, new HalEmbeddedResourcesSerializer(mapper));
		}

		private Object findInstance(Class<?> type) {
			return this.serializers.get(type);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * com.fasterxml.jackson.databind.cfg.HandlerInstantiator#deserializerInstance(com.fasterxml.jackson.databind.
		 * DeserializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated,
														Class<?> deserClass) {

			Object jsonDeser = findInstance(deserClass);
			return jsonDeser != null ? (JsonDeserializer<?>) jsonDeser
				: super.deserializerInstance(config, annotated, deserClass);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#keyDeserializerInstance(com.fasterxml.jackson.
		 * databind. DeserializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated,
													   Class<?> keyDeserClass) {

			Object keyDeser = findInstance(keyDeserClass);
			return keyDeser != null ? (KeyDeserializer) keyDeser
				: super.keyDeserializerInstance(config, annotated, keyDeserClass);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * com.fasterxml.jackson.databind.cfg.HandlerInstantiator#serializerInstance(com.fasterxml.jackson.databind.
		 * SerializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {

			Object jsonSer = findInstance(serClass);
			return jsonSer != null ? (JsonSerializer<?>) jsonSer : super.serializerInstance(config, annotated, serClass);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * com.fasterxml.jackson.databind.cfg.HandlerInstantiator#typeResolverBuilderInstance(com.fasterxml.jackson.
		 * databind .cfg.MapperConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated,
																  Class<?> builderClass) {

			Object builder = findInstance(builderClass);
			return builder != null ? (TypeResolverBuilder<?>) builder
				: super.typeResolverBuilderInstance(config, annotated, builderClass);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * com.fasterxml.jackson.databind.cfg.HandlerInstantiator#typeIdResolverInstance(com.fasterxml.jackson.databind.
		 * cfg. MapperConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {

			Object resolver = findInstance(resolverClass);
			return resolver != null ? (TypeIdResolver) resolver
				: super.typeIdResolverInstance(config, annotated, resolverClass);
		}
	}
}
