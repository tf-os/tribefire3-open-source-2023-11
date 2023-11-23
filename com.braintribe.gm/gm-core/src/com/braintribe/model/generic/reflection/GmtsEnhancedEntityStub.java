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

import com.braintribe.model.generic.annotation.GmSystemInterface;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.enhance.SessionUnboundPropertyAccessInterceptor;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.value.ValueDescriptor;

/**
 * 
 */
@GmSystemInterface
public abstract class GmtsEnhancedEntityStub extends GmtsEntityStub implements EnhancedEntity {

	public PropertyAccessInterceptor pai = SessionUnboundPropertyAccessInterceptor.INSTANCE;
	private GmSession session;
	private int flags;

	/**
	 * We might need this in case some third party framework would want to create entities using java reflection.
	 */
	public GmtsEnhancedEntityStub() {
		this(true);
	}

	public GmtsEnhancedEntityStub(boolean initialized) {
		initializePrimitiveFields();

		if (initialized)
			entityType().initialize(this);
	}

	@Override
	public final boolean isEnhanced() {
		return true;
	}

	/**
	 * This method is overridden by sub-types which have at least one primitive property.
	 */
	protected void initializePrimitiveFields() {
		// TODO improve by calling super.initializePrimitiveFields and only generate bytecode for new properties
	}

	@Override
	public final Object read(Property p) {
		return pai.getProperty(p, this, false);
	}

	@Override
	public final void write(Property p, Object value) {
		pai.setProperty(p, this, value, false);
	}

	@Override
	public ValueDescriptor readVd(Property p) {
		return (ValueDescriptor) pai.getProperty(p, this, true);
	}

	@Override
	public void writeVd(Property p, ValueDescriptor value) {
		pai.setProperty(p, this, value, true);
	}

	// ############################################
	// ## . . . . . SessionAttachable . . . . . .##
	// ############################################

	@Override
	public final GmSession session() {
		return session;
	}

	@Override
	public final void attach(GmSession session) {
		this.session = session;
		this.pai = session.getInterceptor();
	}

	@Override
	public final GmSession detach() {
		GmSession retVal = this.session;
		this.session = null;
		this.pai = SessionUnboundPropertyAccessInterceptor.INSTANCE;
		return retVal;
	}

	@Override
	@Deprecated
	public final void attachSession(GmSession session) {
		this.session = session;
		this.pai = session.getInterceptor();
	}

	@Override
	@Deprecated
	public final GmSession detachSession() {
		GmSession retVal = this.session;
		this.session = null;
		this.pai = SessionUnboundPropertyAccessInterceptor.INSTANCE;
		return retVal;
	}

	@Override
	@Deprecated
	public final GmSession accessSession() {
		return this.session;
	}

	// ############################################
	// ## . . . . . . EnhancedEntity . . . . . . ##
	// ############################################

	@Override
	public final int flags() {
		return flags;
	}

	@Override
	public final void assignFlags(int flags) {
		this.flags = flags;
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
}
