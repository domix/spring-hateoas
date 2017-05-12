package org.springframework.hateoas.siren;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Dietrich Schulten
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SirenFieldValue {

	private Object value;
	private Boolean selected;

	public SirenFieldValue(String title, Object value, Boolean selected) {
		this.value = value;
		this.selected = selected != null && selected == true ? selected : null;
	}

	public Object getValue() {
		return value;
	}

	public Boolean isSelected() {
		return selected;
	}
}
