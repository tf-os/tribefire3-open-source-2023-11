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
package com.braintribe.web.multipart.impl;

public interface FormDataMultipartConstants {
	public static final int BOUNDARY_TYPE_PART = -1;
	public static final int BOUNDARY_TYPE_TERMINAL = -2;

	public static final byte CR = 0x0D;
	public static final byte LF = 0x0A;
	public static final byte HYPHEN = 0x2D;
	public static final byte[] HTTP_LINEBREAK = new byte[] { CR, LF };
	public static final byte[] MULTIPART_HYPHENS = new byte[] { HYPHEN, HYPHEN };
//	public static final byte[] MULTIPART_OPENING = new byte[] { CR, LF, HYPHEN, HYPHEN };
}
