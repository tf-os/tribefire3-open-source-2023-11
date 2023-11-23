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
package com.braintribe.model.processing.smart.query.planner.structure.adapter;

import java.util.Arrays;
import java.util.List;

import com.braintribe.model.accessdeployment.smart.meta.LinkPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.OrderedLinkPropertyAssignment;

/**
 * 
 */
public class LinkPropertyAssignmentWrapper implements DqjDescriptor {

	private final LinkPropertyAssignment assignment;
	private final boolean isLinkEntity;

	public LinkPropertyAssignmentWrapper(LinkPropertyAssignment assignment, Level level) {
		this.assignment = assignment;
		this.isLinkEntity = level == Level.linkEntity;
	}

	@Override
	public List<String> getJoinedEntityDelegatePropertyNames() {
		return Arrays.asList(getJoinedEntityDelegatePropertyName());
	}

	@Override
	public String getRelationOwnerDelegatePropertyName(String joinedEntityDelegatePropertyName) {
		return getRelationOwnerDelegatePropertyName();
	}

	@Override
	public ConversionWrapper getRelationOwnerPropertyConversion(String joinedEntityDelegatePropertyName) {
		return null;
	}

	@Override
	public ConversionWrapper getJoinedEntityPropertyConversion(String joinedEntityDelegatePropertyName) {
		return null;
	}

	private String getRelationOwnerDelegatePropertyName() {
		if (isLinkEntity) {
			return assignment.getKey().getProperty().getName();
		} else {
			return assignment.getLinkOtherKey().getName();
		}
	}

	private String getJoinedEntityDelegatePropertyName() {
		if (isLinkEntity) {
			return assignment.getLinkKey().getName();
		} else {
			return assignment.getOtherKey().getProperty().getName();
		}
	}

	public String getLinkIndexPropertyName() {
		return ((OrderedLinkPropertyAssignment) assignment).getLinkIndex().getName();
	}

	public static enum Level {
		linkEntity,
		otherEntity,
	}

	@Override
	public boolean getForceExternalJoin() {
		return true;
	}
}
