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
package com.braintribe.model.processing.vde.evaluator.api.aspects;

import java.util.List;
import java.util.function.Supplier;

import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.vde.evaluator.api.VdeContextAspect;

/**
 * Represents Provider for current selected ModelPath
 */

public interface SelectedModelPathsAspect extends VdeContextAspect<List<ModelPath>> {
	// this should stay empty
}
