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
package com.braintribe.devrock.zed.commons;

import java.net.URL;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * wrapper codec for the {@link CodingSet} or {@link CodingMap} based structures with {@link ArtifactIdentification} 
 * @author Pit
 *
 */
public class UrlWrapperCodec extends HashSupportWrapperCodec<URL> {
	
	public UrlWrapperCodec() {
		super(true);
	}

	@Override
	protected int entityHashCode(URL e) {			    
		return e.hashCode();
	}

	@Override
	protected boolean entityEquals(URL e1, URL e2) {
		
		if (
				(e1 == null && e2 != null) ||
				(e1 != null && e2 == null)
			)
		return false;
		
		return e1.sameFile(e2);
	
	}

}
