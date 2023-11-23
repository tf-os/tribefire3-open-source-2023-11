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
package com.braintribe.common.lcd;

import com.braintribe.common.lcd.GenericTask.GenericTaskException;

/**
 * Generic interface that may be used for classes that perform tasks using a (nullable) context provided by the calling classes. One purpose of this
 * interface is to avoid creating undesirable dependencies.
 *
 * @author michael.lafite
 *
 * @param <T>
 *            the type of the task context.
 *
 * @see GenericTaskWithContext
 * @see GenericTask
 */
public interface GenericTaskWithNullableContext<T> {

	void perform(T taskContext) throws GenericTaskException;

}
