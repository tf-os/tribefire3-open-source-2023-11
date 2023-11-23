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
package com.braintribe.transport.ftp.enums;

/**
 *
 */
public enum FileType {
    /***
     * A constant used to indicate the file(s) being transfered should
     * be treated as ASCII.  This is the default file type.  All constants
     * ending in <code>FILE_TYPE</code> are used to indicate file types.
     ***/
    ASCII(0),

    /***
     * A constant used to indicate the file(s) being transfered should
     * be treated as EBCDIC.  Note however that there are several different
     * EBCDIC formats.  All constants ending in <code>FILE_TYPE</code>
     * are used to indicate file types.
     ***/
    EBCDIC(1),

   
    /***
     * A constant used to indicate the file(s) being transfered should
     * be treated as a binary image, i.e., no translations should be
     * performed.  All constants ending in <code>FILE_TYPE</code> are used to
     * indicate file types.
     ***/
    BINARY(2),

    /***
     * A constant used to indicate the file(s) being transfered should
     * be treated as a local type.  All constants ending in
     * <code>FILE_TYPE</code> are used to indicate file types.
     ***/
    LOCAL(3);

    private int arg;
	
	FileType(int arg) {
		this.arg = arg;
	}
	
	public int getFileType() {
		return this.arg;
	}
}