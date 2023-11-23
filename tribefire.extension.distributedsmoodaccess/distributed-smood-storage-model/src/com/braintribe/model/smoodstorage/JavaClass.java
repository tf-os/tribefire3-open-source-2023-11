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
package com.braintribe.model.smoodstorage;

import com.braintribe.model.generic.StandardStringIdentifiable;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface JavaClass extends StandardStringIdentifiable {

	EntityType<JavaClass> T = EntityTypes.T(JavaClass.class);

	String qualifiedName = "qualifiedName";
	String classData = "classData";
	String sequenceNumber = "sequenceNumber";
	String md5 = "md5";

	void setQualifiedName(String qualifiedName);
	String getQualifiedName();

	void setClassData(String classData); // BASE64 byte[]
	String getClassData();

	void setSequenceNumber(int sequenceNumber);
	int getSequenceNumber();

	void setMd5(String md5);
	String getMd5();

}
