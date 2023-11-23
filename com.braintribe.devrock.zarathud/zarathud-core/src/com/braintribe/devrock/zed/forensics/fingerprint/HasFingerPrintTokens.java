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
package com.braintribe.devrock.zed.forensics.fingerprint;

import com.braintribe.zarathud.model.forensics.FingerPrint;

/**
 * all tokens a {@link FingerPrint} uses
 * @author pit
 */
public interface HasFingerPrintTokens {
	String ISSUE = "issue";
	String PROPERTY = "property";
	String ENTITY = "entity";
	String METHOD = "method";
	String FIELD = "field";
	String PACKAGE = "package";
	String TYPE = "type";
	String ARTIFACT = "artifact";
	String GROUP = "group";
	
	// not for identification of the owner of the fingerprint,
	// but for precise drill down, i.e. in case of EXCESS/MISSING dependencies, the ArtifactIdentification of the dependency
	String ISSUE_KEY = "issue_key";
	
	String FINGER_PRINT_PART_KEY = "fps";
	String FINGER_PRINT_LOCAL_KEY = "fingerprint.yaml";
}
