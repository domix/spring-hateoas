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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.Data;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.UriTemplateComponents;
import org.springframework.hateoas.affordance.ActionDescriptor;
import org.springframework.hateoas.affordance.Affordance;
import org.springframework.hateoas.affordance.springmvc.SpringActionDescriptor;
import org.springframework.hateoas.affordance.support.DataTypeUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class UberUtils {

	static final Set<String> FILTER_RESOURCE_SUPPORT = new HashSet<String>(Arrays.asList("class", "links", "id"));
	static final Set<String> FILTER_BEAN = new HashSet<String>(Arrays.asList("class"));

	/**
	 * Transform an object into a {@link NewUberDocument}.
	 * 
	 * @param object
	 * @param objectMapper
	 * @return
	 */
	public static NewUberDocument toUber(final Object object, final ObjectMapper objectMapper) {

		if (object == null) {
			return null;
		}

		if (object instanceof NewUberDocument) {
			return (NewUberDocument) object;
		}

		if (object instanceof Iterable) {

		} else if (object instanceof Map) {

		}

		throw new IllegalArgumentException("Don't know how to handle type : " + object.getClass());
	}

	/**
	 * Recursively converts object to nodes of uber data.
	 *
	 * @param objectNode to append to
	 * @param object to convert
	 */
	public static void toUberData(AbstractUberNode objectNode, Object object) {
		Set<String> filtered = FILTER_RESOURCE_SUPPORT;
		if (object == null) {
			return;
		}
		try {
			// TODO: move all returns to else branch of property descriptor handling
			if (object instanceof Resource) {
				Resource<?> resource = (Resource<?>) object;
				objectNode.addLinks(resource.getLinks());
				toUberData(objectNode, resource.getContent());
				return;
			} else if (object instanceof Resources) {
				Resources<?> resources = (Resources<?>) object;

				// TODO set name using EVO see HypermediaSupportBeanDefinitionRegistrar

				objectNode.addLinks(resources.getLinks());

				Collection<?> content = resources.getContent();
				toUberData(objectNode, content);
				return;
			} else if (object instanceof ResourceSupport) {
				ResourceSupport resource = (ResourceSupport) object;

				objectNode.addLinks(resource.getLinks());

				// wrap object attributes below to avoid endless loop

			} else if (object instanceof Collection) {
				Collection<?> collection = (Collection<?>) object;
				for (Object item : collection) {
					// TODO name must be repeated for each collection item
					UberNode itemNode = new UberNode();
					objectNode.addData(itemNode);
					toUberData(itemNode, item);

					// toUberData(objectNode, item);
				}
				return;
			}
			if (object instanceof Map) {
				Map<?, ?> map = (Map<?, ?>) object;
				for (Entry<?, ?> entry : map.entrySet()) {
					String key = entry.getKey().toString();
					Object content = entry.getValue();
					Object value = getContentAsScalarValue(content);
					UberNode entryNode = new UberNode();
					objectNode.addData(entryNode);
					entryNode.setName(key);
					if (value != null) {
						entryNode.setValue(value);
					} else {
						toUberData(entryNode, content);
					}
				}
			} else {
				PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(object);// BeanUtils
				// .getPropertyDescriptors(bean.getClass());
				for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
					String name = propertyDescriptor.getName();
					if (filtered.contains(name)) {
						continue;
					}
					UberNode propertyNode = new UberNode();
					Method readMethod = propertyDescriptor.getReadMethod();
					readMethod.setAccessible(true);
					Object content = readMethod.invoke(object);

					Object value = getContentAsScalarValue(content);
					propertyNode.setName(name);
					objectNode.addData(propertyNode);
					if (value != null) {
						// for each scalar property of a simple bean, add valuepair nodes to data
						propertyNode.setValue(value);
					} else {
						toUberData(propertyNode, content);
					}
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException("failed to transform object " + object, ex);
		}
	}

	private static PropertyDescriptor[] getPropertyDescriptors(Object bean) {
		try {
			return Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors();
		} catch (IntrospectionException e) {
			throw new RuntimeException("failed to get property descriptors of bean " + bean, e);
		}
	}

	private static Object getContentAsScalarValue(Object content) {
		final Object value;
		if (content == null) {
			value = UberNode.NULL_VALUE;
		} else if (DataTypeUtils.isSingleValueType(content.getClass())) {
			value = content.toString();
		} else {
			value = null;
		}
		return value;
	}

	/**
	 * Converts single link to uber node.
	 *
	 * @param href to use
	 * @param actionDescriptor to use for action and model, never null
	 * @param rels of the link
	 * @return uber link
	 */
	public static UberNode toUberLink(String href, ActionDescriptor actionDescriptor, String... rels) {
		return toUberLink(href, actionDescriptor, Arrays.asList(rels));
	}

	/**
	 * Converts single link to uber node.
	 *
	 * @param href to use
	 * @param actionDescriptor to use for action and model, never null
	 * @param rels of the link
	 * @return uber link
	 */
	public static UberNode toUberLink(String href, ActionDescriptor actionDescriptor, List<String> rels) {
		Assert.notNull(actionDescriptor, "actionDescriptor must not be null");
		UberNode uberLink = new UberNode();
		uberLink.setRel(rels);
		UriTemplateComponents partialUriTemplateComponents = new UriTemplate(href)
				.expand(Collections.<String, Object>emptyMap()).asComponents();
		uberLink.setUrl(partialUriTemplateComponents.getBaseUri());
		uberLink.setModel(getModelProperty(href, actionDescriptor));
		if (actionDescriptor != null) {
			uberLink.setAction(UberAction.forRequestMethod(actionDescriptor.getHttpMethod()));
		}
		return uberLink;
	}

	static String getModelProperty(String href, ActionDescriptor actionDescriptor) {

		UriTemplate uriTemplate = new UriTemplate(href);
		String model;

		switch (actionDescriptor.getHttpMethod()) {
			case GET:
			case DELETE: {
				model = buildModel(uriTemplate.getVariableNames(), "{?", ",", "}", "%s");
				break;
			}
			case POST:
			case PUT:
			case PATCH: {
				model = buildModel(uriTemplate.getVariableNames(), "", "&", "", "%s={%s}");
				break;
			}
			default:
				model = null;
		}
		return StringUtils.isEmpty(model) ? null : model;
	}

	public static List<ActionDescriptor> getActionDescriptors(Link link) {
		List<ActionDescriptor> actionDescriptors;
		if (link instanceof Affordance) {
			actionDescriptors = ((Affordance) link).getActionDescriptors();
		} else {
			actionDescriptors = Arrays.asList((ActionDescriptor) new SpringActionDescriptor("get", HttpMethod.GET));
		}
		return actionDescriptors;
	}

	public static List<String> getRels(Link link) {
		List<String> rels;
		if (link instanceof Affordance) {
			rels = ((Affordance) link).getRels();
		} else {
			rels = Arrays.asList(link.getRel());
		}
		return rels;
	}

	private static String buildModel(List<String> variableNames, String prefix, String separator, String suffix,
			String parameterTemplate) {

		if (variableNames.isEmpty()) {
			return "";
		}

		List<String> transformedVariableNames = new ArrayList<String>();

		for (String variable : variableNames) {
			transformedVariableNames.add(String.format(parameterTemplate, variable, variable));
		}

		return prefix + StringUtils.collectionToDelimitedString(transformedVariableNames, separator) + suffix;
	}

	/**
	 * Turn a {@list List} of {@link Link}s into a {@link Map}, where you can see ALL the rels of a given
	 * link.
	 *
	 * @param links
	 * @return a map with links mapping onto a {@link List} of rels
	 */
	public static Map<String, LinkAndRels> urlRelMap(List<Link> links) {

		Map<String, LinkAndRels> urlRelMap = new LinkedHashMap<String, LinkAndRels>();

		for (Link link : links) {
			if (!urlRelMap.containsKey(link.getHref())) {
				urlRelMap.put(link.getHref(), new LinkAndRels());
			}
			LinkAndRels linkAndRels = urlRelMap.get(link.getHref());
			linkAndRels.setLink(link);
			linkAndRels.getRels().add(link.getRel());
		}

		return urlRelMap;
	}

	/**
	 * Holds both a {@link Link} and related {@literal rels}.
	 * 
	 */
	@Data
	public static class LinkAndRels {

		 private Link link;
		 private List<String> rels = new ArrayList<String>();
	}
}
