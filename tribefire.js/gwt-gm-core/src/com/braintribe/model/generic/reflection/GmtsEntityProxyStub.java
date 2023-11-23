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
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.enhance.EntityProxy;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.ValueDescriptor;

/**
 * 
 */
@GmSystemInterface
public abstract class GmtsEntityProxyStub extends GmtsBaseEntityStub implements EntityProxy {

	private PropertyAccessInterceptor pai;
	protected GenericEntity delegate;

	@Override
	public final boolean isEnhanced() {
		return delegate.isEnhanced();
	}

	@Override
	public final Object read(Property p) {
		return pai.getProperty(p, delegate, false);
	}
	
	@Override
	public final void write(Property p, Object value) {
		pai.setProperty(p, delegate, value, false);
	}

	@Override
	public ValueDescriptor readVd(Property p) {
		return (ValueDescriptor) pai.getProperty(p, delegate, true);
	}

	@Override
	public void writeVd(Property p, ValueDescriptor value) {
		pai.setProperty(p, delegate, value, true);
	}
	
	@Override
	public void read(Property p, PropertyValueReceiver pvr) {
		Object fieldValue = p.getDirectUnsafe(delegate);

		ValueDescriptor vd = VdHolder.getValueDescriptorIfPossible(fieldValue);
		if (vd != null) {
			pvr.receiveVd(vd);
		} else {
			pvr.receive(fieldValue);
		}
	}

	@Override
	public GenericEntity deproxy() {
		return delegate;
	}

	// ############################################
	// ## . . . . . SessionAttachable . . . . . .##
	// ############################################

	@Override
	public final GmSession session() {
		return delegate.session();
	}
	
	@Override
	public final void attach(GmSession session) {
		delegate.attach(session);
	}

	@Override
	public final GmSession detach() {
		return delegate.detach();
	}

	@Override
	@Deprecated
	public final void attachSession(GmSession session) {
		delegate.attach(session);
	}

	@Override
	@Deprecated
	public final GmSession detachSession() {
		return detach();
	}

	@Override
	@Deprecated
	public final GmSession accessSession() {
		return delegate.session();
	}
	
	// ############################################
	// ## . . . . . . EnhancedEntity . . . . . . ##
	// ############################################

	private EnhancedEntity asEnhancedEntity(String requiredFor) {
		if (delegate.isEnhanced()) {
			return (EnhancedEntity)delegate;
		}
		else {
			throw new UnsupportedOperationException(requiredFor +" method not supported by delegate");
		}
	}
	
	@Override
	public final int flags() {
		return asEnhancedEntity("flags").flags();
	}

	@Override
	public final void assignFlags(int flags) {
		asEnhancedEntity("assignFlags").assignFlags(flags);
	}

	@Override
	public void assignPai(PropertyAccessInterceptor pai) {
		if (pai == null)
			throw new IllegalArgumentException("pai may not be null");
		this.pai = pai;
	}
	
	@Override
	public void pushPai(PropertyAccessInterceptor pai) {
		pai.next = this.pai;
		assignPai(pai);
	}
	
	@Override
	public PropertyAccessInterceptor popPai() {
		PropertyAccessInterceptor poppedPai = this.pai;
		assignPai(poppedPai.next);
		return poppedPai;
	}

	@Override
	public String toSelectiveInformation() {
		return delegate.toSelectiveInformation();
	}

	@Override
	public GenericModelType type() {
		return delegate.type();
	}

	@Override
	public <T extends GenericEntity> EntityType<T> entityType() {
		return delegate.entityType();
	}

	@Override
	public <T extends EntityReference> T reference() {
		return delegate.reference();
	}

	@Override
	public <T extends EntityReference> T globalReference() {
		return delegate.globalReference();
	}
	
	@Override
	public boolean isVd() {
		return delegate.isVd();
	}

	@Override
	public long runtimeId() {
		return delegate.runtimeId();
	}

	@Override
	public <T> T clone(CloningContext cloningContext) {
		return delegate.clone(cloningContext);
	}

	@Override
	public void traverse(TraversingContext traversingContext) {
		delegate.traverse(traversingContext);
	}

	@Override
	public void delegate(GenericEntity entity) {
		delegate = entity;
	}

	@Override
	public GenericEntity delegate() {
		return delegate;
	}
	

}
