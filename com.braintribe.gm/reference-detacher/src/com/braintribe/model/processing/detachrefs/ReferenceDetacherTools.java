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
package com.braintribe.model.processing.detachrefs;

import com.braintribe.logging.Logger;
import com.braintribe.model.meta.data.QualifiedProperty;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.processing.meta.cmd.CmdResolver;

/**
 * 
 */
public class ReferenceDetacherTools {

	private static final Logger log = Logger.getLogger(ReferenceDetacherTools.class);

	public static String canDetachValueFrom(QualifiedProperty qualifiedProperty, CmdResolver cmdResolver) {
		try {
			if (cmdResolver.getMetaData().entityType(qualifiedProperty.getEntityType()).property(qualifiedProperty.getProperty()).is(Mandatory.T))
				return "property is mandatory";

		} catch (Exception e) {
			log.error("Failed to resolve 'MandatoryProperty' meta data", e);
		}

		return null;
	}

}
