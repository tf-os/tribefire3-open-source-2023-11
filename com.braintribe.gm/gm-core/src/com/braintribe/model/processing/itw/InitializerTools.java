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
package com.braintribe.model.processing.itw;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.tools.GmValueCodec;
import com.braintribe.model.generic.tools.GmValueCodec.ElementType;
import com.braintribe.model.generic.tools.GmValueCodec.EnumParsingMode;
import com.braintribe.model.generic.value.NullDescriptor;

/**
 * @author peter.gazdik
 */
public class InitializerTools {

	public static final String NOW_STRING = "now()";
	public static final String UUID_STRING = "uuid()";
	public static final String NULL_STRING = GmValueCodec.NULL_STRING;

	// #####################################
	// ## . . . . . . Parsing . . . . . . ##
	// #####################################

	/**
	 * @param enumParsingMode
	 *            when parsing <tt>initializerString</tt> in the GWT generators, we might not have enums initialized, so we do not want to invoke
	 *            {@code GMF.getEnumType(signature)}, but rather just return {@code Enum.class} as an indicator that given string represents an enum.
	 */
	public static Object parseInitializer(String initializerString, EnumParsingMode enumParsingMode, EnumHint[] enumHints) {
		return parseInitializer(initializerString, enumParsingMode, enumHints, null);
	}

	private static Object parseInitializer(String initializerString, EnumParsingMode enumParsingMode, EnumHint[] enumHints, ElementType elementType) {
		String s = initializerString;

		if (s == null)
			return null;

		s = s.trim();
		if (s.isEmpty())
			return null;

		// Date - now
		if (s.equalsIgnoreCase(NOW_STRING))
			return Now.T.create();

		if (s.equals(NULL_STRING))
			return NullDescriptor.T.create();

		if (s.equalsIgnoreCase(UUID_STRING))
			return UUID.randomUUID().toString();

		if (enumHints != null)
			s = applyEnumHintIfNeeded(s, elementType, enumHints);

		return GmValueCodec.objectFromGmString(s, enumParsingMode,
				ctx -> parseInitializer(ctx.element(), ctx.enumParsingMode(), enumHints, ctx.elementType()));
	}

	private static String applyEnumHintIfNeeded(String s, ElementType elementType, EnumHint[] enumHints) {
		EnumHint enumHint = elementType == ElementType.mapKey ? enumHints[0] : enumHints[1];

		if (enumHint != null && enumHint.constants.contains(s))
			return "enum(" + enumHint.signature + "," + s + ")";
		else
			return s;
	}

	// #####################################
	// ## . . . . . Stringifying . . . . .##
	// #####################################

	/**
	 * @param initializer
	 *            cannot be null!
	 */
	public static String stringifyInitializer(Object initializer) {
		Object o = initializer;
		Objects.requireNonNull(o, "Cannot stringify null!");

		if (o instanceof Now)
			return NOW_STRING;

		if (o instanceof NullDescriptor)
			return NULL_STRING;

		return GmValueCodec.objectToGmString(initializer);
	}

	public static class EnumHint {
		public final String signature;
		public final Set<String> constants;

		public EnumHint(String signature, Set<String> constants) {
			this.signature = signature;
			this.constants = constants;
		}
	}
}
