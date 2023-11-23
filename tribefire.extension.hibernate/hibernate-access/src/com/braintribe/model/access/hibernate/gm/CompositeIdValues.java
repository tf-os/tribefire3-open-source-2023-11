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
package com.braintribe.model.access.hibernate.gm;

import java.io.Serializable;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Stream;

import com.braintribe.model.accessdeployment.jpa.meta.JpaCompositeId;
import com.braintribe.model.generic.tools.GmValueCodec;

/**
 * Serializable object used as id value for hibernate. This can be used to support composite-id mappings with composite keys consisting of up to 30
 * columms.
 * 
 * @see JpaCompositeId
 * 
 * @author peter.gazdik
 */
public class CompositeIdValues implements Serializable {

	private static final long serialVersionUID = 5204860165642867993L;

	public static final int MAX_NUMBER_OF_COLUMNS = 30;

	private final Object[] values = new Object[MAX_NUMBER_OF_COLUMNS];

	// ###########################################################
	// ## . . . . . . . . Coding To/From String . . . . . . . . ##
	// ###########################################################

	/**
	 * Not that this implementation is not useful for cases when editing should be supported, because it is not possible to parse it properly (type
	 * Information is not preserved). This has to be fixed, after DEVCX... is done.
	 * <p>
	 * Format: ${value0},${value1}...
	 */
	public String encodeAsString() {
		int nonNullCount = 1 + lastNonNullPosition();

		return GmValueCodec.streamToGmString(Stream.of(values).limit(nonNullCount), "", "");
	}

	public static String encodeAsString(Object value) {
		return ((CompositeIdValues) value).encodeAsString();
	}

	public static CompositeIdValues from(Object encodedId) {
		return decodeFromString((String) encodedId);
	}

	public static CompositeIdValues decodeFromString(String encodedId) {
		Object[] parsedArray = GmValueCodec.linearCollectionFromString(encodedId, "", "").toArray();

		return fromValues(parsedArray);
	}

	public static CompositeIdValues fromValues(Object... values) {
		CompositeIdValues result = new CompositeIdValues();
		System.arraycopy(values, 0, result.values, 0, values.length);

		return result;
	}

	// ###########################################################
	// ## . . . . . . equals / hashCode / toString . . . . . . .##
	// ###########################################################

	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		CompositeIdValues other = (CompositeIdValues) obj;
		return Arrays.equals(values, other.values);
	}

	@Override
	public String toString() {
		int max = lastNonNullPosition();

		StringJoiner sj = new StringJoiner(",", "Composite-Id(", ")");
		for (int i = 0; i <= max; i++)
			sj.add("" + values[i]);

		return sj.toString();
	}

	private int lastNonNullPosition() {
		int i = values.length;
		while (--i >= 0 && values[i] == null) {
			// do nothing
		}

		return i;
	}

	// ###########################################################
	// ## . . . . . . . . . . accessors . . . . . . . . . . . . ##
	// ###########################################################

	// @formatter:off
	public Object getValue0() {	return values[0]; }
	public void setValue0(Object value) { this.values[0] = value; }

	public Object getValue1() {	return values[1]; }
	public void setValue1(Object value) { this.values[1] = value; }

	public Object getValue2() {	return values[2]; }
	public void setValue2(Object value) { this.values[2] = value; }

	public Object getValue3() {	return values[3]; }
	public void setValue3(Object value) { this.values[3] = value; }

	public Object getValue4() {	return values[4]; }
	public void setValue4(Object value) { this.values[4] = value; }

	public Object getValue5() {	return values[5]; }
	public void setValue5(Object value) { this.values[5] = value; }

	public Object getValue6() {	return values[6]; }
	public void setValue6(Object value) { this.values[6] = value; }

	public Object getValue7() {	return values[7]; }
	public void setValue7(Object value) { this.values[7] = value; }

	public Object getValue8() {	return values[8]; }
	public void setValue8(Object value) { this.values[8] = value; }

	public Object getValue9() {	return values[9]; }
	public void setValue9(Object value) { this.values[9] = value; }

	public Object getValue10() {	return values[10]; }
	public void setValue10(Object value) { this.values[10] = value; }

	public Object getValue11() {	return values[11]; }
	public void setValue11(Object value) { this.values[11] = value; }

	public Object getValue12() {	return values[12]; }
	public void setValue12(Object value) { this.values[12] = value; }

	public Object getValue13() {	return values[13]; }
	public void setValue13(Object value) { this.values[13] = value; }

	public Object getValue14() {	return values[14]; }
	public void setValue14(Object value) { this.values[14] = value; }

	public Object getValue15() {	return values[15]; }
	public void setValue15(Object value) { this.values[15] = value; }

	public Object getValue16() {	return values[16]; }
	public void setValue16(Object value) { this.values[16] = value; }

	public Object getValue17() {	return values[17]; }
	public void setValue17(Object value) { this.values[17] = value; }

	public Object getValue18() {	return values[18]; }
	public void setValue18(Object value) { this.values[18] = value; }

	public Object getValue19() {	return values[19]; }
	public void setValue19(Object value) { this.values[19] = value; }

	public Object getValue20() {	return values[20]; }
	public void setValue20(Object value) { this.values[20] = value; }

	public Object getValue21() {	return values[21]; }
	public void setValue21(Object value) { this.values[21] = value; }

	public Object getValue22() {	return values[22]; }
	public void setValue22(Object value) { this.values[22] = value; }

	public Object getValue23() {	return values[23]; }
	public void setValue23(Object value) { this.values[23] = value; }

	public Object getValue24() {	return values[24]; }
	public void setValue24(Object value) { this.values[24] = value; }

	public Object getValue25() {	return values[25]; }
	public void setValue25(Object value) { this.values[25] = value; }

	public Object getValue26() {	return values[26]; }
	public void setValue26(Object value) { this.values[26] = value; }

	public Object getValue27() {	return values[27]; }
	public void setValue27(Object value) { this.values[27] = value; }

	public Object getValue28() {	return values[28]; }
	public void setValue28(Object value) { this.values[28] = value; }

	public Object getValue29() {	return values[29]; }
	public void setValue29(Object value) { this.values[29] = value; }
	// @formatter:on

}
