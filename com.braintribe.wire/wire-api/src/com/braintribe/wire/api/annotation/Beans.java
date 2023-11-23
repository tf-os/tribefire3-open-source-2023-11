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
package com.braintribe.wire.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.scope.DefaultScope;
import com.braintribe.wire.api.scope.WireScope;
import com.braintribe.wire.api.space.BeanSpace;

/**
 * The Beans annotation is to be placed on any real implementation (class and not interface) of {@link BeanSpace}.
 * It will mark the class for wire bytecode enriching.
 * 
 * The annotation also controls the default scope of the {@link BeanSpace} it placed on.
 * @author dirk.scheffler
 * @deprecated use {@link Managed} instead. The annotation will be removed in future versions of Wire
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Deprecated
public @interface Beans {
	/**
	 * The default scope for the whole {@link BeanSpace} the annotation is placed on.
	 * The default of this will be the {@link DefaultScope} which leads to a dynamic resolution
	 * of the actual scope from the {@link WireContext}
	 */
	Class<? extends WireScope> defaultScope() default DefaultScope.class;
}
