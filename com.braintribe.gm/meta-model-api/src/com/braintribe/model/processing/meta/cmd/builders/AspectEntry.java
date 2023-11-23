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
package com.braintribe.model.processing.meta.cmd.builders;

import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;

/**
 * 
 */
public class AspectEntry<T, A extends SelectorContextAspect<? super T>> {

	private Class<A> aspect;
	private T value;

	public AspectEntry() {
	}

	public AspectEntry(Class<A> aspect, T value) {
		this.aspect = aspect;
		this.value = value;
	}

	public Class<A> getAspect() {
		return aspect;
	}

	public void setAspect(Class<A> aspect) {
		this.aspect = aspect;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

}
