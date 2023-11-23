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
package com.braintribe.model.processing.management;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.management.MetaModelValidationResult;
import com.braintribe.model.management.MetaModelValidationViolation;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.management.impl.validator.BasicCheck;
import com.braintribe.model.processing.management.impl.validator.PropertiesConsistencyCheck;
import com.braintribe.model.processing.management.impl.validator.TypesAllUsedAreDeclaredCheck;
import com.braintribe.model.processing.management.impl.validator.TypesIsPlainConsistencyCheck;
import com.braintribe.model.processing.management.impl.validator.TypesLoopsInHierarchyCheck;
import com.braintribe.model.processing.management.impl.validator.ValidatorCheck;

public class MetaModelValidator {
	
	private List<ValidatorCheck> checksList = null;
	
	public MetaModelValidator() {
		
	}
	
	public void setChecksList(List<ValidatorCheck> checksList) {
		this.checksList = checksList;
	}
	
	private List<ValidatorCheck> getChecksList() {
		if (checksList == null) {
			checksList = asList( //

					new BasicCheck(), // must be the first one, as it returns false if MM is null
					new TypesLoopsInHierarchyCheck(), //
					new TypesAllUsedAreDeclaredCheck(), //
					new TypesIsPlainConsistencyCheck(), //
					new PropertiesConsistencyCheck() //
			);
		}
		return checksList;
	}
	
	public MetaModelValidationResult validate(GmMetaModel metaModel) {
		List<MetaModelValidationViolation> violations = new ArrayList<MetaModelValidationViolation>();

		for (ValidatorCheck check : getChecksList()) {
			boolean continueWithNextCheck = check.check(metaModel, violations);
			if (!continueWithNextCheck) {
				break;
			}
		}
		
		MetaModelValidationResult res = MetaModelValidationResult.T.create();
		res.setValid(violations.size() == 0);
		res.setViolations(violations);
		return res;
	}

}
