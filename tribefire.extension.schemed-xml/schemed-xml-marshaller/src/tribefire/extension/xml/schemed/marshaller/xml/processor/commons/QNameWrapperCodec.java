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
package tribefire.extension.xml.schemed.marshaller.xml.processor.commons;

import javax.xml.namespace.QName;

import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.common.lcd.equalscheck.IgnoreCaseEqualsCheck;


public class QNameWrapperCodec extends HashSupportWrapperCodec<QName> {
	private static final IgnoreCaseEqualsCheck check = new IgnoreCaseEqualsCheck();
	
	
	public QNameWrapperCodec() {
		super(true);
	}

	@Override
	protected int entityHashCode(QName e) {
		int result = 23;
		
		result = 31 * result + e.getLocalPart().hashCode();
		if (e.getPrefix() != null) {
			result = 31 * result + e.getPrefix().hashCode();
		}
		if (e.getNamespaceURI() != null) {
			result = 31 * result + e.getNamespaceURI().hashCode();
		}
		
		return result;
	}

	@Override
	protected boolean entityEquals(QName e1, QName e2) {
		if (!check.equals( e1.getLocalPart(), e2.getLocalPart()))
			return false;
		if (!check.equals( e1.getPrefix(), e2.getPrefix()))
			return false;
		if (!check.equals( e1.getNamespaceURI(), e2.getNamespaceURI()))
			return false;			
		return true;
	}

}
