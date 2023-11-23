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
package tribefire.extension.audit.processing;

import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;

public class PropertyInfo {
	private TrackMode trackMode;
	private Property property;
	private boolean changeValueOccured = false;
	private boolean incrementalValueChangeOccured = false;
	
	public PropertyInfo(TrackMode trackMode, Property property) {
		super();
		this.trackMode = trackMode;
		this.property = property;
	}

	public TrackMode getTrackMode() {
		return trackMode;
	}
	
	public Property getProperty() {
		return property;
	}
	
	public boolean isSetTypeProperty() {
		return property.getType().getTypeCode() == TypeCode.setType;
	}

	public boolean isListTypeProperty() {
		return property.getType().getTypeCode() == TypeCode.listType;
	}

	public boolean getIncrementalValueChangeOccured() {
		return incrementalValueChangeOccured;
	}

	public void incrementalValueChangeOccured() {
		this.incrementalValueChangeOccured = true;
	}

	public boolean getChangeValueOccured() {
		return changeValueOccured;
	}

	public void changeValueOccured() {
		this.changeValueOccured = true;
	}

	@Override
	public String toString() {
		return "PropertyInfo[name=" + property.getName() + ", trackMode=" + trackMode + ", changeValueOccured: "+changeValueOccured+", incrementalValueChangeOccured: "+incrementalValueChangeOccured+"]"; 
	}
}
