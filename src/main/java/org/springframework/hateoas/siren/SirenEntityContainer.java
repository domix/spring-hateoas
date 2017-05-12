package org.springframework.hateoas.siren;

import java.util.List;
import java.util.Map;

/**
 * @author Dietrich Schulten
 */
public interface SirenEntityContainer {

	List<SirenSubEntity> getEntities();

	public Map<String, Object> getProperties();

	void setLinks(List<SirenLink> links);

	void setProperties(Map<String, Object> properties);

	void setSirenClasses(List<String> sirenClasses);

	void addSubEntity(SirenSubEntity sirenSubEntity);

	void setEmbeddedLinks(List<SirenEmbeddedLink> embeddedLinks);

	void setActions(List<SirenAction> actions);
}
