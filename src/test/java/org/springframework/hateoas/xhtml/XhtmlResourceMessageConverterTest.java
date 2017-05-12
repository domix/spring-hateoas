package org.springframework.hateoas.xhtml;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.affordance.formaction.Select;
import org.springframework.hateoas.affordance.springmvc.sample.test.DummyEventController;
import org.springframework.hateoas.affordance.springmvc.sample.test.ReviewController;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class XhtmlResourceMessageConverterTest {

	private static Map<String, String> NAMESPACES = new HashMap<String, String>();

	static {
		NAMESPACES.put("h", "http://www.w3.org/1999/xhtml");
	}

	@Autowired WebApplicationContext wac;
	MockMvc mvc;

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = { HypermediaType.XHTML })
	static class WebConfig {

		@Bean
		public ReviewController reviewController() {
			return new ReviewController();
		}

		@Bean
		public DummyEventController eventController() {
			return new DummyEventController();
		}
	}

	@Before
	public void setup() {
		this.mvc = webAppContextSetup(this.wac).build();
	}

	@Test
	public void testCreatesHtmlFormForGet() throws Exception {

		mvc.perform(get("http://localhost/events").accept(MediaType.TEXT_HTML))//
				.andDo(print())
				.andExpect(status().isOk())//
				.andExpect(content().contentType(MediaType.TEXT_HTML))//
				.andExpect(xpath("//h:form[@action='http://localhost/events{?eventName}' and @method='GET' and @name='findEventByName']",
						NAMESPACES).exists())
				// TODO: form name
				.andExpect(xpath("//h:form[@action='http://localhost/events{?eventName}' and @method='GET' and "
						+ "@name='findEventByName']/h:div/h:input/@name", NAMESPACES).string("eventName"))
				.andReturn();
	}

	@Test
	public void testCreatesSimpleLinkForGetAffordanceWithoutRequestParams() throws Exception {

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML))//
				.andExpect(status().isOk())//
				.andExpect(content().contentType(MediaType.TEXT_HTML))//
				.andExpect(xpath("//h:a[@href='http://localhost/events/1']", NAMESPACES).exists())//
				.andExpect(xpath("//h:a[@href='http://localhost/events/2']", NAMESPACES).exists());
	}

	@Test
	public void testCreatesHtmlFormForPost() throws Exception {

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())//
				.andExpect(content().contentType(MediaType.TEXT_HTML))//
				.andExpect(xpath("//h:form[@name='addEvent']/@action", NAMESPACES).string("http://localhost/events"))//
				.andExpect(xpath("//h:form[@name='addEvent']/@method", NAMESPACES).string("POST"))//
				.andExpect(xpath("//h:form[@name='addEvent']/h:div/h:select[@name='eventStatus']", NAMESPACES).exists())//
				.andExpect(xpath("//h:form[@name='addEvent']/h:div/h:select[@name='typicalAgeRange']", NAMESPACES).exists());
	}

	// TODO too many divs
	// TODO GET form without input
	// TODO GET iritemplate form has no name
	@Test
	public void testCreatesHtmlFormForPut() throws Exception {

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.TEXT_HTML))
				.andExpect(xpath("//h:form[@name='updateEventWithRequestBody']/@action", NAMESPACES)
						.string("http://localhost/events/1"))
				.andExpect(xpath("//h:form[@name='updateEventWithRequestBody']/h:input[@name='_method']/@value", NAMESPACES)
						.string("PUT"));
	}

	@Test
	public void testCreatesInputFieldWithMinMaxNumber() throws Exception {

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())//
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_HTML))//
				.andExpect(xpath("//h:input[@name='reviewRating.ratingValue']", NAMESPACES).exists())//
				.andExpect(xpath("//h:input[@name='reviewRating.ratingValue']/@type", NAMESPACES).string("number"))//
				.andExpect(xpath("//h:input[@name='reviewRating.ratingValue']/@min", NAMESPACES).string("1"))//
				.andExpect(xpath("//h:input[@name='reviewRating.ratingValue']/@max", NAMESPACES).string("5"))//
				.andExpect(xpath("//h:input[@name='reviewRating.ratingValue']/@value", NAMESPACES).string("3"));
	}

	@Test
	public void testCreatesInputFieldWithDefaultText() throws Exception {

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())//
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_HTML))//
				.andExpect(xpath("//h:input[@name='reviewRating.ratingValue']/@value", NAMESPACES).string("3"));
	}

	/**
	 * Tests if the form contains a personId input field with default value.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testCreatesHiddenInputField() throws Exception {

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())//
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_HTML))//
				.andExpect(xpath("//h:input[@name='personId']", NAMESPACES).exists())//
				.andExpect(xpath("//h:input[@name='personId']/@type", NAMESPACES).string("hidden"))//
				.andExpect(xpath("//h:input[@name='personId']/@value", NAMESPACES).string("123"))//
				.andExpect(xpath("//h:input[@name='firstname']/@value", NAMESPACES).string("Bilbo"));
	}

	/**
	 * Tests if the form contains a select field.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreatesSelectFieldForEnum() throws Exception {

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())//
				.andExpect(content().contentType(MediaType.TEXT_HTML))//
				.andExpect(xpath("//h:select[@name='eventStatus']", NAMESPACES).exists())//
				.andExpect(xpath("//h:select[@name='eventStatus']/h:option[1]/text()", NAMESPACES).string("EVENT_CANCELLED"))//
				.andExpect(xpath("//h:select[@name='eventStatus']/h:option[2]/text()", NAMESPACES).string("EVENT_POSTPONED"))//
				.andExpect(xpath("//h:select[@name='eventStatus']/h:option[3]/text()", NAMESPACES).string("EVENT_SCHEDULED"))//
				.andExpect(xpath("//h:select[@name='eventStatus']/h:option[4]/text()", NAMESPACES).string("EVENT_RESCHEDULED"))//
				.andExpect(xpath("(//h:select[@name='eventStatus']/h:option)[@selected]/text()", NAMESPACES)
						.string("EVENT_SCHEDULED"));
	}

	/**
	 * Tests a list of possible values defined with {@link Select#options()} annotation.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreatesSelectFieldForSelectOptionsBasedPossibleValues() throws Exception {

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())//
				.andExpect(content().contentType(MediaType.TEXT_HTML))//
				.andExpect(
						xpath("//h:form[@name='updateEventWithRequestBody']/h:div/h:select[@name='typicalAgeRange']", NAMESPACES)
								.exists())//
				.andExpect(
						xpath("//h:form[@name='updateEventWithRequestBody']/h:div/h:select[@name='typicalAgeRange']/h:option[1]",
								NAMESPACES).string("7-10"))//
				.andExpect(
						xpath("//h:form[@name='updateEventWithRequestBody']/h:div/h:select[@name='typicalAgeRange']/h:option[2]",
								NAMESPACES).string("11-"));
	}

	/**
	 * Tests if the form contains a multiselect field with three preselected items, matching the person having id 123.
	 *
	 * @throws Exception
	 */
	// @Test
	// public void testCreatesMultiSelectFieldForEnumArray() throws Exception {
	//
	// this.mockMvc.perform(get("/people/customer/123/editor").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
	// .andExpect(content().contentType(MediaType.TEXT_HTML))
	// .andExpect(xpath("//h:select[@name='sports' and @multiple]", namespaces).exists())
	// .andExpect(xpath("//h:select[@name='sports']/h:option", namespaces).nodeCount(Sport.values().length))
	// .andExpect(xpath("(//h:select[@name='sports']/h:option)[@selected]", namespaces).nodeCount(3));
	// }
	//
	// /**
	// * Tests List<Enum> parameter.
	// *
	// * @throws Exception
	// */
	// @Test
	// public void testCreatesMultiSelectFieldForEnumList() throws Exception {
	//
	// this.mockMvc.perform(get("/people/customer/123/editor").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
	// .andExpect(content().contentType(MediaType.TEXT_HTML))
	// .andExpect(xpath("//h:select[@name='gadgets' and @multiple]", namespaces).exists())
	// .andExpect(xpath("//h:select[@name='gadgets']/h:option", namespaces).nodeCount(Gadget.values().length))
	// .andExpect(xpath("(//h:select[@name='gadgets']/h:option)[@selected]", namespaces).nodeCount(0));
	// }
	// /**
	// * Tests List<String> parameter with a list of possible values.
	// *
	// * @throws Exception
	// */
	// @Test
	// @Ignore
	// public void testCreatesMultiSelectFieldForListOfPossibleValuesFixed() throws Exception {
	//
	// this.mockMvc.perform(get("/people/customerByAttribute").accept(MediaType.TEXT_HTML))
	// .andExpect(status().isOk())
	// .andExpect(content().contentType(MediaType.TEXT_HTML))
	// .andExpect(xpath("//h:select[@name='attr' and @multiple]", namespaces).exists())
	// .andExpect(xpath("//h:select[@name='attr']/h:option", namespaces).nodeCount(3))
	// .andExpect(xpath("(//h:select[@name='attr']/h:option)[@selected]", namespaces).string("hungry"));
	//
	// }
	//
	// /**
	// * Tests List<String> parameter with a list of possible values.
	// *
	// * @throws Exception
	// */
	// @Test
	// @Ignore
	// public void testCreatesMultiSelectFieldForListOfPossibleValuesFromSpringBean() throws Exception {
	//
	// this.mockMvc.perform(get("/people/customer/123/details").accept(MediaType.TEXT_HTML))
	// .andExpect(status().isOk())
	// .andExpect(content().contentType(MediaType.TEXT_HTML))
	// .andExpect(xpath("//h:select[@name='detail' and @multiple]", namespaces).exists())
	// .andExpect(xpath("//h:select[@name='detail']/h:option", namespaces).nodeCount(3))
	// .andExpect(xpath("(//h:select[@name='detail']/h:option)[1]", namespaces).string("beard"))
	// .andExpect(xpath("(//h:select[@name='detail']/h:option)[2]", namespaces).string("afterShave"))
	// .andExpect(xpath("(//h:select[@name='detail']/h:option)[3]", namespaces).string("noseHairTrimmer"));
	//
	// }
	//
	//
	// /**
	// * Tests List<String> parameter with a list of numbers.
	// *
	// * @throws Exception
	// */
	// @Test
	// @Ignore("implement code on demand")
	// public void testCreatesOneInputForIntegerListWithInputUpToAny() throws Exception {
	//
	// this.mockMvc.perform(get("/people/customer/123/numbers").accept(MediaType.TEXT_HTML))
	// .andExpect(status().isOk())
	// .andExpect(content().contentType(MediaType.TEXT_HTML))
	// .andExpect(xpath("//h:input[@name='number']", namespaces).nodeCount(1));
	// // expect code-on-demand here
	//
	// }
}
