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
package com.braintribe.codec.marshaller.api;

/**
 * Specifies how missing/unknown types, properties, etc. are handled during decoding process.
 * 
 * @author michael.lafite
 */
public class DecodingLenience {

	private boolean typeLenient;
	private boolean propertyLenient;
	private boolean propertyClassCastExceptionLenient;
	private boolean enumConstantLenient;

	/** Creates a new <code>DecodingLenience</code> instance where all leniency properties are disabled. */
	public DecodingLenience() {
		// nothing to do
	}

	/** Creates a new <code>DecodingLenience</code> instance and {@link #setLenient(boolean) sets the leniency} as specified. */
	public DecodingLenience(boolean lenient) {
		setLenient(lenient);
	}

	/** @see #setTypeLenient(boolean) */
	public boolean isTypeLenient() {
		return this.typeLenient;
	}

	/** Whether or not to ignore missing types. */
	public void setTypeLenient(boolean typeLenient) {
		this.typeLenient = typeLenient;
	}

	/** @see #setPropertyLenient(boolean) */
	public boolean isPropertyLenient() {
		return this.propertyLenient;
	}

	/** Whether or not to ignore missing properties. */
	public void setPropertyLenient(boolean propertyLenient) {
		this.propertyLenient = propertyLenient;
	}

	/** @see #setPropertyClassCastExceptionLenient(boolean) */
	public boolean isPropertyClassCastExceptionLenient() {
		return this.propertyClassCastExceptionLenient;
	}

	/** Whether or not to ignore {@link ClassCastException}s when property value cannot be cast to property type. */
	public void setPropertyClassCastExceptionLenient(boolean propertyClassCastExceptionLenient) {
		this.propertyClassCastExceptionLenient = propertyClassCastExceptionLenient;
	}

	/** @see #setEnumConstantLenient(boolean) */
	public boolean isEnumConstantLenient() {
		return this.enumConstantLenient;
	}

	/** Whether or not to ignore missing enum constants. */
	public void setEnumConstantLenient(boolean enumConstantLenient) {
		this.enumConstantLenient = enumConstantLenient;
	}

	/**
	 * Returns <code>true</code>, if ALL lenience properties are enabled, otherwise <code>false</code>.
	 * 
	 * @see #isTypeLenient()
	 * @see #isPropertyLenient()
	 * @see #isPropertyClassCastExceptionLenient()
	 * @see #isEnumConstantLenient()
	 * @see #setLenient(boolean)
	 */
	public boolean isLenient() {
		return this.typeLenient && this.propertyLenient && this.propertyClassCastExceptionLenient && this.enumConstantLenient;
	}

	/**
	 * Convenience method that sets all lenience properties at once.
	 * 
	 * @see #setTypeLenient(boolean)
	 * @see #setPropertyLenient(boolean)
	 * @see #setPropertyClassCastExceptionLenient(boolean)
	 * @see #setEnumConstantLenient(boolean)
	 * @see #isLenient()
	 */
	public void setLenient(boolean lenient) {
		this.typeLenient = lenient;
		this.propertyLenient = lenient;
		this.propertyClassCastExceptionLenient = lenient;
		this.enumConstantLenient = lenient;
	}

	public DecodingLenience typeLenient(boolean lenient) {
		setTypeLenient(lenient);
		return this;
	}

	public DecodingLenience propertyLenient(boolean lenient) {
		setPropertyLenient(lenient);
		return this;
	}

	public DecodingLenience propertyClassCastExceptionLenient(boolean lenient) {
		setPropertyClassCastExceptionLenient(lenient);
		return this;
	}

	public DecodingLenience enumConstantLenient(boolean lenient) {
		setEnumConstantLenient(lenient);
		return this;
	}

}
