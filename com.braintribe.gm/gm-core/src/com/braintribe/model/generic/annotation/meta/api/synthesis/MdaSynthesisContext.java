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
package com.braintribe.model.generic.annotation.meta.api.synthesis;

import java.lang.annotation.Annotation;

/**
 * 
 */
public interface MdaSynthesisContext {

	/**
	 * Creates a new {@link SingleAnnotationDescriptor} and makes sure the inner globalId is set properly. If second parameter is true, the newly
	 * created descriptor is also set as the current descriptor.
	 * <p>
	 * <h3>When should we avoid setting it as current descriptor.</h3>
	 * 
	 * In case of repeatable meta-data, the current descriptor should be a {@link RepeatedAnnotationDescriptor}, which consists of multiple
	 * {@link SingleAnnotationDescriptor}s. When we are creating new single descriptor (other than very first one), setting it as current would
	 * overwrite the previous descriptor. Instead, we create it without setting as current and then use
	 * {@link #setCurrentDescriptorMulti(SingleAnnotationDescriptor, Class)} to set it.
	 */
	SingleAnnotationDescriptor newDescriptor(Class<? extends Annotation> annotationClass);

	/**
	 * associates given {@link AnnotationDescriptor} with the type of currently processed meta-data
	 * 
	 * @see #setCurrentDescriptorMulti(SingleAnnotationDescriptor, Class)
	 */
	void setCurrentDescriptor(AnnotationDescriptor descriptor);

	/**
	 * Associates given {@link AnnotationDescriptor} with the type of currently processed meta-data. In case there already is a descriptor associated,
	 * it creates a new or extends existing {@link RepeatedAnnotationDescriptor} which corresponds to given {@link SingleAnnotationDescriptor}.
	 */
	void setCurrentDescriptorMulti(SingleAnnotationDescriptor descriptor, Class<? extends Annotation> repeatabeAnnoClass);

}
