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
package tribefire.extension.xml.schemed.xsd.analyzer.resolvers.handlers.type;

import com.braintribe.model.meta.GmType;

import tribefire.extension.xml.schemed.marshaller.commons.QNameExpert;
import tribefire.extension.xml.schemed.model.xsd.QName;

public class TypeResolverResponse {
	private GmType gmType;
	private QName apparentTypeName;
	private QName actualTypeName;
	private boolean alreadyAcquired = false;
	//private Collection<MetaData> metadata = new ArrayList<>();
	
	public GmType getGmType() {
		return gmType;
	}
	public void setGmType(GmType gmType) {
		this.gmType = gmType;
	}
	public QName getApparentTypeName() {
		return apparentTypeName;
	}
	public void setApparentTypeName(QName apparentTypeName) {
		this.apparentTypeName = apparentTypeName;
	}
	public void setApparentTypeName(String apparentTypeName) {
		this.apparentTypeName = QNameExpert.parse(apparentTypeName);
	}
	public QName getActualTypeName() {
		return actualTypeName;
	}
	public void setActualTypeName(QName actualTypeName) {
		this.actualTypeName = actualTypeName;
	}
	public void setActualTypeName(String actualTypeName) {
		this.actualTypeName = QNameExpert.parse(actualTypeName);
	}
	public boolean isAlreadyAcquired() {
		return alreadyAcquired;
	}
	public void setAlreadyAcquired(boolean alreadyAcquired) {
		this.alreadyAcquired = alreadyAcquired;
	}
	/*
	public Collection<MetaData> getMetadata() {
		return metadata;
	}
	public void setMetadata(Collection<MetaData> metadata) {
		this.metadata = metadata;
	}
	*/
	
}
