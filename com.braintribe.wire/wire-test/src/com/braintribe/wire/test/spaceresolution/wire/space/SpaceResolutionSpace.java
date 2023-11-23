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
package com.braintribe.wire.test.spaceresolution.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.test.spaceresolution.wire.contract.AlternativeSpaceResolutionFirstContract;
import com.braintribe.wire.test.spaceresolution.wire.contract.ContractResolutionFirstContract;
import com.braintribe.wire.test.spaceresolution.wire.contract.SpaceResolutionContract;
import com.braintribe.wire.test.spaceresolution.wire.contract.SpaceResolutionFirstContract;

@Managed
public class SpaceResolutionSpace implements SpaceResolutionContract {
	@Import
	private ContractResolutionFirstContract importedByContract1;
	
	@Import
	private ContractResolutionFirstSpace importedBySpace1;
	
	@Import
	private SpaceResolutionFirstContract importedByContract2;
	
	@Import
	private SpaceResolutionFirstSpace importedBySpace2;
	
	@Import
	private AlternativeSpaceResolutionFirstContract alternativeImportedByContract2;
	
	@Override
	public ContractResolutionFirstContract importedByContract1() {
		return importedByContract1;
	}
	
	@Override
	public ContractResolutionFirstContract importedBySpace1() {
		return importedBySpace1;
	}
	
	@Override
	public SpaceResolutionFirstContract importedByContract2() {
		return importedByContract2;
	}
	
	@Override
	public SpaceResolutionFirstContract importedBySpace2() {
		return importedBySpace2;
	}
	
	@Override
	public AlternativeSpaceResolutionFirstContract importedByAlterativeContract2() {
		return alternativeImportedByContract2;
	}
}
