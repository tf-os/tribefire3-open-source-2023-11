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
import java.lang.annotation.Repeatable;

import com.braintribe.model.generic.annotation.meta.CompoundUnique;
import com.braintribe.model.generic.annotation.meta.CompoundUniques;

/**
 * 
 */
public interface MdaSynthesisContext {

	/**
	 * Creates a new {@link SingleAnnotationDescriptor} and makes sure the inner globalId is set properly.
	 */
	SingleAnnotationDescriptor newDescriptor(Class<? extends Annotation> annotationClass);

	SingleAnnotationDescriptor newDescriptor(Class<? extends Annotation> annotationClass, boolean setAsCurrentDescriptor);

	/**
	 * @return {@link AnnotationDescriptor} associated with the type of currently processed meta-data. Usually this returns null, because we don't
	 *         expect multiple MetaData of the same type, but in some cases this is possible when the annotation is handled as {@link Repeatable},
	 *         like {@link CompoundUnique} / {@link CompoundUniques}.
	 */
	AnnotationDescriptor getCurrentDescriptor();

	/**
	 * associates given {@link AnnotationDescriptor} with the type of currently processed meta-data
	 * 
	 * @see #getCurrentDescriptor()
	 */
	void setCurrentDescriptor(AnnotationDescriptor descriptor);

	/**
	 * Associates given {@link AnnotationDescriptor} with the type of currently processed meta-data. In case there already is a descriptor associated,
	 * it creates a new or extends existing {@link RepeatedAnnotationDescriptor} which corresponds to given {@link SingleAnnotationDescriptor}.
	 * 
	 * @see #getCurrentDescriptor()
	 */
	void setCurrentDescriptorMulti(SingleAnnotationDescriptor descriptor, Class<? extends Annotation> repeatabeAnnoClass);

}
