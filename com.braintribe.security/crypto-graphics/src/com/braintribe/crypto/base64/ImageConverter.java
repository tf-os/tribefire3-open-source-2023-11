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
package com.braintribe.crypto.base64;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.braintribe.crypto.utils.TextUtils;

public class ImageConverter {
		
	
	public static void main( String [] args){
		File input = new File( args[0]);
		
		try {
			byte[] bytes = new byte[(int) input.length()];
			RandomAccessFile ref = new RandomAccessFile( input, "r");
			ref.readFully( bytes);
			ref.close();
			String base64 = Base64.encodeBytes(bytes);
			 
			TextUtils.writeContentsToFile( base64, new File( args[0] + ".b64"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
