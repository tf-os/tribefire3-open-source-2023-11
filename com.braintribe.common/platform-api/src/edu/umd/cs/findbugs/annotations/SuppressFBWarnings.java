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
package edu.umd.cs.findbugs.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to suppress FindBugs warnings.<br/>
 * Since FindBugs analyzes byte code (and not sources), warnings cannot be suppressed using comments. For the same reason they cannot be suppressed
 * using {@link java.lang.SuppressWarnings}, because that annotation is ignored by the compiler ({@link java.lang.annotation.RetentionPolicy}
 * <code>SOURCE</code>). Therefore FindBugs uses its own annotation (<code>edu.umd.cs.findbugs.annotations.SuppressFBWarnings</code>) to surpress
 * warnings. Since we do not want to add another dependency only to be able to suppress warnings, this annotation just uses the same fully qualified
 * name. (It's not a copy of the FindBugs annotation, but it's compatible.)
 */
@Retention(RetentionPolicy.CLASS)
public @interface SuppressFBWarnings {
	String[] value();
}
