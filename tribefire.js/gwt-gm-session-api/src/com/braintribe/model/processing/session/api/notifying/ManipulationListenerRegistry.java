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
package com.braintribe.model.processing.session.api.notifying;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.notifying.js.JsManipulationListener;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(namespace=GmCoreApiInteropNamespaces.manipulation)
public interface ManipulationListenerRegistry {

	@JsIgnore
	void add(ManipulationListener listener);

	@JsIgnore
	void addFirst(ManipulationListener listener);

	@JsIgnore
	void remove(ManipulationListener listener);
	
	@JsMethod(name="add")
	default void _add(Object listener) {
		if (!(listener instanceof JsManipulationListener)) {
			add((ManipulationListener) listener);
			return;
		}
		
		add(manipulation -> ((JsManipulationListener) listener).noticeManipulation(manipulation));
	}

	@JsMethod(name="addFirst")
	default void _addFirst(Object listener) {
		if (!(listener instanceof JsManipulationListener)) {
			addFirst((ManipulationListener) listener);
			return;
		}
		
		addFirst(manipulation -> ((JsManipulationListener) listener).noticeManipulation(manipulation));
	}

	@JsMethod(name="remove")
	default void _remove(Object listener) {
		if (!(listener instanceof JsManipulationListener)) {
			remove((ManipulationListener) listener);
			return;
		}
		
		remove(manipulation -> ((JsManipulationListener) listener).noticeManipulation(manipulation));
	}
}
