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
package tribefire.cortex.initializer.support.impl.lookup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

 /**
 * This annotation marks a wire contract indicating that it contains managed instances to be looked
 * up.
 *
 * <p>
 * In cooperation with this annotation, a managed instance within the contract can be marked with {@link GlobalId}. If
 * set, the lookup will not check for the globalId being constructed by default globalId calculation, but for the
 * given globalId. <br />
 * (Default globalId pattern: wire://WireModuleSimpleName|InitializerId/WireSpace/managedInstance )
 * 
 * <p>
 * In case <code>lookupOnly</code> is set to <code>true</code>, all managed instances are handled as lookup instances.
 * 
 * <p>
 * Property <code>globalIdPrefix</code> can be set to avoid redundancy in case annotation {@link GlobalId} share
 * commonalities.
 * 
 * <p>
 * <b>Attention: </b>The contract must contain managed instances only!
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface InstanceLookup {
	boolean lookupOnly() default false;
	String globalIdPrefix() default "";
}
