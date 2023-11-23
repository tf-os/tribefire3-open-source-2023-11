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
package com.braintribe.devrock.api.clipboard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

import com.braintribe.codec.marshaller.bin.BinMarshaller;
import com.braintribe.model.generic.GenericEntity;


/**
 * an generic {@link Transfer} for {@link GenericEntity} based structures - currently root must be a generic entity itself!! 
 * @author pit
 *
 */
public class AssemblyTransfer extends ByteArrayTransfer {
	 private static final String ASSEMBLY_TYPE_NAME = "gm_assembly";
     private static final int ASSEMBLY_TYPE_ID = registerType(ASSEMBLY_TYPE_NAME);
     private static AssemblyTransfer _instance = new AssemblyTransfer();
     
     private static BinMarshaller marshaller = new BinMarshaller();
     
     private AssemblyTransfer() {}
     
     public static AssemblyTransfer getInstance() {
    	 return _instance;
     }
     
	@Override
	protected int[] getTypeIds() {		
		return new int[] {ASSEMBLY_TYPE_ID};
	}

	@Override
	protected String[] getTypeNames() {
		return new String[] {ASSEMBLY_TYPE_NAME};
	}

	 public void javaToNative (Object object, TransferData transferData) {
	        if (object == null || !(object instanceof GenericEntity)) return;
	        	        
	        try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				marshaller.marshall(out, object);
				byte [] buffer = out.toByteArray();
				out.close();
				
				super.javaToNative(buffer, transferData);				
			} catch (Exception e) {
				throw new TransferException("cannot marshall passed data", e);
			} 
	        
	        
	 }
	 
	 public Object nativeToJava(TransferData transferData) {
		 if (isSupportedType(transferData)) {
             
             try {
				byte[] buffer = (byte[])super.nativeToJava(transferData);
				 if (buffer == null) {
					 return null;
				 }
				 ByteArrayInputStream in = new ByteArrayInputStream(buffer);
				 Object result = marshaller.unmarshall(in);
				 in.close();
				 return result;
			} catch (Exception e) {
				throw new TransferException( "cannot unmarshall passed data", e);
			}
		 }
		 return null;

	 }
	 
}
