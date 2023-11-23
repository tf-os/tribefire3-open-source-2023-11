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
import java.util.function.Consumer;

/**
 * @author peter.gazdik
 */
public abstract class AnnotationDescriptor implements Comparable<AnnotationDescriptor> {

	protected final Class<? extends Annotation> annotationClass;

	protected AnnotationDescriptor(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}

	public Class<? extends Annotation> getAnnotationClass() {
		return annotationClass;
	}

	@Override
	public int compareTo(AnnotationDescriptor o) {
		return this.annotationClass.getName().compareTo(o.annotationClass.getName());
	}

	public abstract void withSourceCode(Consumer<String> sourceCodeConsumer);

}
