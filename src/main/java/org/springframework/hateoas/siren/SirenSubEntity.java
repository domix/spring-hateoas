package org.springframework.hateoas.siren;

import java.util.List;

/**
 * Common subtype for Siren subentities
 *
 * @author Dietrich Schulten
 */
public abstract class SirenSubEntity extends SirenRelatedEntity {

	public SirenSubEntity(List<String> rels, String title, List<String> sirenClasses) {
		super(rels, title, sirenClasses);
	}

	public SirenSubEntity() {
		super();
	}
}
