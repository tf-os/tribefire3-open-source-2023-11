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
package com.braintribe.model.io.metamodel.render.context;

import java.util.Collection;
import java.util.stream.Collectors;

import com.braintribe.model.generic.annotation.GlobalId;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.meta.GmModels;

/**
 * @author peter.gazdik
 */
public class SourceWriterTools {

	public static boolean elmentNeedsGlobalId(GmModelElement element) {
		String globalId = element.getGlobalId();
		return globalId != null && !globalId.equals(GmModels.naturalGlobalId(element));
	}

	public static String join(Collection<String> strings, String separator) {
		return strings.stream() //
				.collect(Collectors.joining(separator));
	}

	// used from within context builders only
	public static String getGlobalIdAnnotationSource(String globalId) {
		return GlobalId.class.getSimpleName() + "(\"" + globalId + "\")";
	}

}
