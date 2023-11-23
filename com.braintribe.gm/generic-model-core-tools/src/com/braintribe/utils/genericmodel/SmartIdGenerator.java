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
/**
 *
 */
package com.braintribe.utils.genericmodel;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.processing.IdGenerator;

/**
 * A {@link #setDelegate(IdGenerator) delegate} based {@link IdGenerator} that first checks the id property (if there is
 * none, <code>null</code> is returned; if the id is set, the id is returned) before passing the task to the delegate.
 * <br>
 * The implementation is based on <code>biz.i2z.service.ecm.access.impl.generator.SmartIdGenerator</code>.
 *
 * @author michael.lafite
 */
public class SmartIdGenerator implements IdGenerator {

	protected static Logger logger = Logger.getLogger(SmartIdGenerator.class);

	protected IdGenerator delegate;

	public SmartIdGenerator() {
		this(new GuidGenerator());
	}

	public SmartIdGenerator(final IdGenerator delegate) {
		setDelegate(delegate);
	}

	public IdGenerator getDelegate() {
		return this.delegate;
	}

	public void setDelegate(final IdGenerator delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object generateId(final GenericEntity entity) throws Exception {
		final Object idValue = entity.getId();
		if (idValue != null) {
			logger.debug(() -> "Won't generate id for instance of entity type '" + entity.entityType().getTypeSignature()
					+ "', because the id is already set (to '" + idValue + "'). Returning id ...");
			return idValue;
		}

		return this.delegate.generateId(entity);
	}

}
