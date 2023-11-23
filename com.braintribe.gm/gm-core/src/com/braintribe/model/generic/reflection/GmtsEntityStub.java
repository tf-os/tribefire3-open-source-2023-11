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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.GmSystemInterface;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.type.custom.AbstractEntityType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.ValueDescriptor;

/**
 * @author peter.gazdik
 */
@GmSystemInterface
public abstract class GmtsEntityStub extends GmtsBaseEntityStub {

	private volatile long runtimeId;

	public GmtsEntityStub() {
		//
	}

	@Override
	public void read(Property p, PropertyValueReceiver pvr) {
		Object fieldValue = p.getDirectUnsafe(this);

		ValueDescriptor vd = VdHolder.getValueDescriptorIfPossible(fieldValue);
		if (vd != null)
			pvr.receiveVd(vd);
		else
			pvr.receive(fieldValue);
	}

	public Object idOrRid() {
		Object id = getId();
		return id != null ? id : "~" + runtimeId;
	}

	@Override
	public long runtimeId() {
		return runtimeId != 0 ? runtimeId : ensureRuntimeId();
	}

	private synchronized long ensureRuntimeId() {
		return runtimeId != 0 ? runtimeId : (runtimeId = RuntimeIdGenerator.nextId());
	}

	// TODO change that entityType is actually implemented by ITW and type delegates to it...
	@Override
	public abstract GenericModelType type();

	public <T extends GenericEntity> AbstractEntityType<T> abstractEntityType() {
		return (AbstractEntityType<T>) type();
	}

	@Override
	public <T extends GenericEntity> EntityType<T> entityType() {
		return (EntityType<T>) type();
	}

	@Override
	public <T extends EntityReference> T reference() {
		return (T) this.entityType().createReference(this, this.getId());
	}

	@Override
	public <T extends EntityReference> T globalReference() {
		return (T) this.entityType().createGlobalReference(this, this.getGlobalId());
	}

	@Override
	public <T> T clone(CloningContext cloningContext) {
		return entityType().clone(cloningContext, this, null);
	}

	@Override
	public void traverse(TraversingContext traversingContext) {
		entityType().traverse(traversingContext, this);
	}

	@Override
	public boolean isVd() {
		return type().isVd();
	}

	@Override
	public String toString() {
		return abstractEntityType().toString(this);
	}

	@Override
	public final String toSelectiveInformation() {
		return type().getSelectiveInformation(this);
	}

	/** Using rawTypes so it is compatible with the sources generated for the GWT compiler. */
	@SuppressWarnings("rawtypes")
	public EvalContext eval(Evaluator evaluator) {
		return evaluator.eval(this);
	}

	@Override
	public GenericEntity deproxy() {
		return this;
	}
}
