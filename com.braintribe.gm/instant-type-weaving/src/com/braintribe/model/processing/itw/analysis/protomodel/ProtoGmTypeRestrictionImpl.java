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
package com.braintribe.model.processing.itw.analysis.protomodel;

import java.util.List;

import com.braintribe.model.generic.pseudo.GenericEntity_pseudo;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.weaving.ProtoGmType;
import com.braintribe.model.weaving.restriction.ProtoGmTypeRestriction;

/**
 * Pseudo-implementation of {@link GmProperty}
 * 
 * @see GenericEntity_pseudo
 * 
 * @author peter.gazdik
 */
public class ProtoGmTypeRestrictionImpl extends GenericEntity_pseudo implements ProtoGmTypeRestriction {

	private List<ProtoGmType> types;
	private List<ProtoGmType> keyTypes;
	private boolean allowVd;
	private boolean allowKeyVd;

	@Override
	public List<ProtoGmType> getTypes() {
		return types;
	}

	public void setTypes(List<ProtoGmType> types) {
		this.types = types;
	}

	@Override
	public List<ProtoGmType> getKeyTypes() {
		return keyTypes;
	}

	public void setKeyTypes(List<ProtoGmType> keyTypes) {
		this.keyTypes = keyTypes;
	}

	@Override
	public boolean getAllowVd() {
		return allowVd;
	}

	@Override
	public void setAllowVd(boolean allowVd) {
		this.allowVd = allowVd;
	}

	@Override
	public boolean getAllowKeyVd() {
		return allowKeyVd;
	}

	@Override
	public void setAllowKeyVd(boolean allowKeyVd) {
		this.allowKeyVd = allowKeyVd;
	}

}
