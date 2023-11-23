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
package tribefire.extension.xml.schemed.xsd.analyzer.modelbuilder;

import java.util.Collection;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmType;

public class DebugOutputBuilder {
	public static void createDebugOutput(Collection<GmType> extractedTypes) {
		// simple output for now
		System.out.println("defined [" + extractedTypes.size() + "] types");
		String prefix = "";
		for (GmType extracted : extractedTypes) {						
			handleGmType( prefix, extracted);
		}
	}


	private static void handleGmType(String prefix, GmType extracted) {
		if (extracted.isGmEntity()) {				
			handleGmEntityType( "\t", (GmEntityType) extracted);
		}
		else if (extracted.isGmEnum()) {
		}
		else if (extracted.isGmCollection()) {
			if (extracted instanceof GmListType) {							
				System.out.print(prefix + "\t\t" + "list");
				handleGmType( prefix + "\t", ((GmListType) extracted).getElementType());
			}
			else if (extracted instanceof GmSetType) {
				System.out.print(prefix + "\t\t" + "set");
				handleGmType( prefix + "\t", ((GmSetType) extracted).getElementType());
			}
			else {
				System.out.print(prefix + "\t\t unsupported " + extracted.getTypeSignature());
			}
		}
		else if (extracted.isGmSimple()) { 
			System.out.print(prefix + "\t\t simple : " + extracted.getTypeSignature());
		}
		else {
			System.out.print(prefix + "\t\t unsupported " + extracted.getTypeSignature());
		}
		
	}


	private static void handleGmEntityType(String prefix, GmEntityType entityType) {
		System.out.print( prefix + "\t" + entityType.getTypeSignature());
		System.out.println();
		for (GmProperty property : entityType.getProperties()) {
			System.out.print( prefix + "\t" + property.getName() + ":");
			GmType type = property.getType();
			handleGmType( prefix + "\t", type);
			System.out.println();
		}
		//System.out.println();
	}
}
