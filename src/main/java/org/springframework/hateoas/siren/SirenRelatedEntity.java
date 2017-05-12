package org.springframework.hateoas.siren;

import java.util.List;

/**
 * @author Dietrich Schulten
 */
public class SirenRelatedEntity extends AbstractSirenEntity {

	private List<String> rel;

	SirenRelatedEntity() {
		super();
	}

	public SirenRelatedEntity(List<String> rels, String title, List<String> sirenClasses) {
		super(title, sirenClasses);
		this.rel = rels;
	}

	public List<String> getRel() {
		return rel;
	}
}
