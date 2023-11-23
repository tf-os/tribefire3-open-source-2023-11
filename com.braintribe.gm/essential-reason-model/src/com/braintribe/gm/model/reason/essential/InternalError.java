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
package com.braintribe.gm.model.reason.essential;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.annotation.Transient;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface InternalError extends Reason {
	EntityType<InternalError> T = EntityTypes.T(InternalError.class);
	
	/**
	 * @return - an attached {@link Exception}, will not be persisted!
	 */
	@Transient
	Throwable getJavaException();
	void setJavaException(Throwable javaException);

	
	static InternalError from(Throwable t) {
		return from(t, t.getMessage());
	}
	
	static InternalError from(Throwable t, String text) {
		InternalError error = T.create();
		error.setJavaException(t);
		error.setText(text);
		return error;
	}
	
	static InternalError create(String text) {
		InternalError error = T.create();
		error.setText(text);
		return error;
	}
}
