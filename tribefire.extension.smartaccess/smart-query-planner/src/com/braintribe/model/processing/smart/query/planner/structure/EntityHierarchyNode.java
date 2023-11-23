// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.processing.smart.query.planner.structure;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;

/**
 * 
 */
public class EntityHierarchyNode {

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private final GmEntityType gmEntityType;
	private final EntityType<?> entityType;
	private final EntityHierarchyNode superNode;

	private final List<EntityHierarchyNode> subNodes = newList();
	private final List<Property> additionalProperties = newList();

	public EntityHierarchyNode(GmEntityType gmEntityType, EntityHierarchyNode superNode) {
		this(gmEntityType, typeReflection.<EntityType<?>> getType(gmEntityType.getTypeSignature()), superNode);
	}

	public EntityHierarchyNode(EntityHierarchyNode node, EntityHierarchyNode superNode) {
		this(node.gmEntityType, node.entityType, superNode);
	}

	private EntityHierarchyNode(GmEntityType gmEntityType, EntityType<?> entityType, EntityHierarchyNode superNode) {
		this.gmEntityType = gmEntityType;
		this.entityType = entityType;
		this.superNode = superNode;

		indexAdditionalProperties();
	}

	private void indexAdditionalProperties() {
		EntityType<?> superType = superNode != null ? typeReflection.getEntityType(superNode.gmEntityType.getTypeSignature()) : null;

		for (Property p: entityType.getProperties()) {
			boolean isInherited = superType != null && superType.findProperty(p.getName()) != null;
			if (!isInherited)
				additionalProperties.add(p);
		}
	}

	public void appendSubNode(EntityHierarchyNode subNode) {
		subNodes.add(subNode);
	}

	public GmEntityType getGmEntityType() {
		return gmEntityType;
	}

	public EntityType<?> getEntityType() {
		return entityType;
	}

	public List<EntityHierarchyNode> getSubNodes() {
		return subNodes;
	}

	public List<Property> getAdditionalProperties() {
		return additionalProperties;
	}

	@Override
	public String toString() {
		return "EntityHierarchyNode [" + entityType.getTypeSignature() + "]";
	}

}
