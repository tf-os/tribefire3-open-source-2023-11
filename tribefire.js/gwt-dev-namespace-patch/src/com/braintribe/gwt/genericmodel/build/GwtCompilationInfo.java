// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.gwt.genericmodel.build;

import java.util.HashSet;
import java.util.Set;

/**
 * @author peter.gazdik
 */
public class GwtCompilationInfo {

	private static Set<String> essentialTypes = new HashSet<>();

	public static void addEssentialType(String typeSignature) {
		essentialTypes.add(typeSignature);
	}

	public static Set<String> getEssentialTypes() {
		return essentialTypes;
	}

}
