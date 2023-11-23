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

import tribefire.extension.xml.schemed.model.xsd.QName;

public class QNameExpert {
	
	public static QName parse( javax.xml.namespace.QName qname) {
		if (qname == null)
			return null;
	
		QName qName = QName.T.create();
		qName.setLocalPart( qname.getLocalPart());
		String namespaceURI = qname.getNamespaceURI();
		if (namespaceURI != null && namespaceURI.length() > 0) {
			qName.setNamespaceUri( namespaceURI);
		}
		
		String prefix = qname.getPrefix();
		if (prefix != null && prefix.length() > 0) {
			qName.setPrefix( prefix);
		}
		return qName;		
	}
	
	public static QName parse( String prefix, String localPart) {
		if (localPart == null || localPart.length() == 0)
			return null;
		QName qName = QName.T.create();
	
		qName.setPrefix( prefix);
		qName.setLocalPart( localPart);				
		return qName;
	}
	
	public static QName parse( String prefix, String localPart, String namespace) {
		if (localPart == null || localPart.length() == 0)
			return null;
		QName qName = QName.T.create();
	
		qName.setPrefix( prefix);
		qName.setLocalPart( localPart);				
		qName.setNamespaceUri(namespace);
		return qName;
	}
	
	
	
	public static QName parse( String string) {
		if (string == null || string.length() == 0)
			return null;
		QName qName = QName.T.create();
		String [] parts = string.split(":");
		if (parts.length >  1) {
			qName.setPrefix( parts[0]);
			qName.setLocalPart( parts[1]);				
		}
		else {
			qName.setLocalPart( string);
		}				
		return qName;
	}

	public static String toString(QName qName) {
		String prefix = qName.getPrefix();
		if (prefix != null && prefix.length() > 0) {
			return prefix + ":" + qName.getLocalPart();
		}
		return qName.getLocalPart();
	}

}
