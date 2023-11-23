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
package com.braintribe.model.version;

import java.util.function.Supplier;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * something that can deliver major, minor and revision version parts
 * @author pit/dirk
 *
 */
@Abstract
public interface HasMajorMinorRevision extends HasMajorMinor {
	
	EntityType<HasMajorMinorRevision> T = EntityTypes.T(HasMajorMinorRevision.class);

	static final String revision = "revision";
	
	/**
	 * @return - value standing for the revision
	 */
	Integer getRevision();
	void setRevision( Integer revision);
	
	default int revision() {
		Integer rv = getRevision();
		if (rv != null) {
			return rv;
		}
		return 0;
	}
	
	/**
	 * returns precision level based on field positions as returned by {@link #readNumericField(int)}
	 */
	default int continuousPrecision() {
		if (getMinor() == null)
			return 0;
		
		if (getRevision() == null)
			return 1;
		
		return 2;
	}
	
	/**
	 * returns the successor of this major/minor/revision based on the {@link #continuousPrecision()}. The supplier gives the instance to be filled with the respective values.
	 * 
	 * <ul>
	 * 	<li>2 -> 3</li>
	 * 	<li>2.4 -> 2.5</li>
	 * 	<li>2.7.8 -> 2.7.9</li>
	 * </ul>
	 * 
	 */
	default <T extends HasMajorMinorRevision> T successor(Supplier<T> supplier) {
		T v = supplier.get();
		
		int precision = continuousPrecision();
		for (int i = 0; i < precision; i++) {
			v.writeNumericField(i, readNumericField(i));
		}
		
		v.writeNumericField(precision, readNumericField(precision) + 1);
		
		return v;
	}
	
	/**
	 * returns a value of a numeric field by its position
	 * <ul>
	 * 	<li>0 - major</li>
	 *  <li>1 - minor</li>
	 *  <li>2 - revision</li>
	 * </ul>
	 */
	default int readNumericField(int i) {
		switch (i) {
		case 0: return getMajor();
		case 1: return getMinor();
		case 2: return getRevision();
		default:
			throw new IllegalArgumentException("unsupported numeric field " + i + ". (0 = major, 1 = minor, 2 = revision)"); 
		}
	}
	
	/**
	 * sets a value of a numeric field by its position
	 * <ul>
	 * 	<li>0 - major</li>
	 *  <li>1 - minor</li>
	 *  <li>2 - revision</li>
	 * </ul>
	 */
	default void writeNumericField(int i, int n) {
		switch (i) {
		case 0: setMajor(n); break;
		case 1: setMinor(n); break;
		case 2: setRevision(n); break;
		default:
			throw new IllegalArgumentException("unsupported numeric field " + i + ". (0 = major, 1 = minor, 2 = revision)"); 
		}
	}

}
