package org.springframework.hateoas.siren;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;
import static org.springframework.hateoas.affordance.springmvc.AffordanceBuilder.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.TestUtils;
import org.springframework.hateoas.core.Relation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Created by Dietrich on 18.04.2016.
 */
public class SirenMessageConverterTest extends TestUtils {

	public static final Logger LOG = LoggerFactory.getLogger(SirenMessageConverterTest.class);
	private ObjectMapper objectMapper = new ObjectMapper();

	SirenUtils sirenUtils = new SirenUtils();

	@Relation("customer")
	static class Customer {

		private final String customerId = "pj123";
		private final String name = "Peter Joseph";

		public String getCustomerId() {
			return customerId;
		}

		public String getName() {
			return name;
		}
	}

	@RequestMapping("/customers")
	static class DummyCustomersController {

		@RequestMapping("/{customerId}")
		public ResponseEntity<Resource<Customer>> getCustomer(@PathVariable String customerId) {
			return null;
		}
	}

	@Value
	static class OrderItem {

		int orderNumber;
		String productCode;
		Integer quantity;

		@JsonCreator
		public OrderItem(@JsonProperty("orderNumber") int orderNumber, @JsonProperty("productCode") String productCode,
				@JsonProperty("quantity") Integer quantity) {

			this.orderNumber = orderNumber;
			this.productCode = productCode;
			this.quantity = quantity;
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	static class Order extends ResourceSupport {

		int orderNumber = 42;
		int itemCount = 3;
		String status = "pending";
		Resource<Customer> customer = new Resource<Customer>(new Customer());

		public Order() {
			customer.add(linkTo(methodOn(DummyCustomersController.class).getCustomer("pj123")).withSelfRel());
		}
	}

	@RequestMapping("/orders")
	static class DummyOrderController {

		@RequestMapping("/{orderNumber}")
		public ResponseEntity<Resource<Order>> getOrder(@PathVariable int orderNumber) {
			return null;
		}

		@RequestMapping("/{orderNumber}/items")
		public ResponseEntity<Resource<OrderItem>> getOrderItems(@PathVariable int orderNumber) {
			return null;
		}

		@RequestMapping(value = "/{orderNumber}/items", method = RequestMethod.POST)
		public ResponseEntity<Void> addOrderItems(@PathVariable int orderNumber, @RequestBody OrderItem orderItem) {
			return null;
		}

		@RequestMapping
		public ResponseEntity<Resources<Order>> getOrders(@RequestParam List<String> attr) {
			return null;
		}
	}

	@Before
	public void setUp() {

		super.setUp();

		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	@Test
	public void testActions() throws JsonProcessingException {

		Order order = new Order();
		order.add(linkTo(methodOn(DummyOrderController.class).addOrderItems(42, new OrderItem(42, null, null)))
				.withRel("order-items"));
		order.add(linkTo(methodOn(DummyOrderController.class).getOrders(null)).withRel("orders"));
		order.add(new Link("http://example.com{?bar}", "bar"));
		order.add(linkTo(methodOn(DummyOrderController.class).getOrder(42)).withSelfRel());
		order.add(linkTo(methodOn(DummyOrderController.class).getOrder(43)).withRel("next"));
		order.add(linkTo(methodOn(DummyOrderController.class).getOrder(41)).withRel("previous"));
		// no support for non-query links
		order.add(new Link("http://example.com/{foo}", "foo"));

		SirenEntity entity = new SirenEntity();
		sirenUtils.toSirenEntity(entity, order);
		String json = objectMapper.valueToTree(entity).toString();

		System.out.println(json);

		// assertThat(json, hasJsonPath("$.actions", hasSize(3)));
		assertThat(json, hasJsonPath("$.actions[0].fields", hasSize(3)));
		assertThat(json, hasJsonPath("$.actions[0].fields[0].name", equalTo("orderNumber")));
		assertThat(json, hasJsonPath("$.actions[0].fields[0].type", equalTo("number")));
		assertThat(json, hasJsonPath("$.actions[0].fields[0].value", equalTo("42")));
		assertThat(json, hasJsonPath("$.actions[0].method", equalTo("POST")));

		// TODO list query parameter: do something smarter
		assertThat(json, hasJsonPath("$.actions[1].fields[0].name", equalTo("attr")));
		assertThat(json, hasJsonPath("$.actions[1].fields[0].type", equalTo("text")));

		assertThat(json, hasJsonPath("$.actions[2].fields[0].name", equalTo("bar")));
		assertThat(json, hasJsonPath("$.actions[2].fields[0].type", equalTo("text")));
	}
}
