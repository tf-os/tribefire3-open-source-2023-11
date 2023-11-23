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
package tribefire.extension.xml.schemed.marshaller.commons;

public interface HasCommonTokens {
	public static String PACKAGE_STANDARD_XML_TYPES = "tribefire.extension.xml.schemed.model.standards";
	static final String SCHEMED_XML_ROOT = "tribefire.extension.schemed-xml";
	public static String MODEL_NAME_STANDARD_XML_TYPES = SCHEMED_XML_ROOT + ":schemed-xml-standards-model";
	public static String MODEL_VERSION_STANDARD_XML_TYPES = "1.0.1";
	public static String [] types = new String [] {"NMTOKEN", "language", "TOKEN", "normalizedString", "NCName", "ID", "IDREF", "AnyAttribute", "AnyType"};
	

}
