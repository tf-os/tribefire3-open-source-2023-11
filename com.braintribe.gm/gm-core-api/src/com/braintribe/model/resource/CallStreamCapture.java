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
package com.braintribe.model.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ForwardDeclaration;
import com.braintribe.model.generic.annotation.Transient;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.session.OutputStreamProvider;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.resource.api.CallStreamCaptureSupport;

@ForwardDeclaration("com.braintribe.gm:transient-resource-model")
public interface CallStreamCapture extends GenericEntity {
	final EntityType<CallStreamCapture> T = EntityTypes.T(CallStreamCapture.class);
	
	@Transient
	OutputStreamProvider getOutputStreamProvider();
	void setOutputStreamProvider(OutputStreamProvider outputStreamProvider);

	default OutputStream openStream() {
		OutputStreamProvider outputStreamProvider = getOutputStreamProvider();
		
		if (outputStreamProvider != null) {
			try {
				return outputStreamProvider.openOutputStream();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		else {
			GmSession session = session();
	
			if (!(session instanceof CallStreamCaptureSupport)) {
				throw new GmSessionRuntimeException(
						"Cannot open capture stream as entity is not attached to a session which supports call stream capturing.");
			}
	
			CallStreamCaptureSupport callStreamCaptureSupport = (CallStreamCaptureSupport) session;
	
			try {
				return callStreamCaptureSupport.openStream(this);
	
			} catch (IOException e) {
				throw new RuntimeException("Error while opening capture stream:", e);
			}
		}
	}
	
	@Override
	default boolean hasTransientData() {
		return getOutputStreamProvider() != null;
	}
}
