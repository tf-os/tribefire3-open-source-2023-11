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
package com.braintribe.model.generic.reflection;

import com.braintribe.common.lcd.function.TriConsumer;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.BasicCriterion;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;

/**
 * @author gunther.schenk
 */
public abstract class EntityVisitor implements TraversingVisitor {

	/** @see TraversingVisitor#visitTraversing(com.braintribe.model.generic.reflection.TraversingContext) */
	@Override
	final public void visitTraversing(TraversingContext traversingContext) {
		BasicCriterion criterion = traversingContext.getTraversingStack().peek();
		if (criterion instanceof EntityCriterion) {
			GenericEntity entity = (GenericEntity) traversingContext.getObjectStack().peek(); 
			visitEntity(entity, (EntityCriterion) criterion, traversingContext);
		}

	}

	protected abstract void visitEntity (GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext);

	public static EntityVisitor onVisitEntity(TriConsumer<GenericEntity, EntityCriterion, TraversingContext> onVisitEntity) {
		return new EntityVisitor() {
			@Override
			protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
				onVisitEntity.accept(entity, criterion, traversingContext);
			}
		};
	}

}
