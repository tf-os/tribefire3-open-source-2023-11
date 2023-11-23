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
package com.braintribe.model.processing.cmd.test.provider;

import com.braintribe.model.processing.cmd.test.meta.enumeration.GlobalEnumConstantMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.ModelEnumConstantMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.ModelEnumMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.SimpleEnumConstantMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.SimpleEnumConstantOverrideMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.SimpleEnumMetaData;
import com.braintribe.model.processing.cmd.test.meta.enumeration.SimpleEnumOverrideMetaData;
import com.braintribe.model.processing.cmd.test.model.Color;

/**
 * 
 */
public class EnumMdProvider extends AbstractModelSupplier {

	public static final int PRIORITIZED_MD = 10;

	@Override
	protected void addMetaData() {
		addSimpleEnumTypeMetaData();
		addSimpleEnumOverrideMetaData();
		addSimpleEnumConstantMetaData();
		addSimpleEnumConstantOverrideMetaData();
		addGlobalConstantMd();
		addModelEnumMd();
		addModelConstantMd();
	}

	private void addSimpleEnumTypeMetaData() {
		baseMdEditor.onEnumType(Color.class) //
				.addMetaData(newMd(SimpleEnumMetaData.T, true));
	}

	private void addSimpleEnumOverrideMetaData() {
		fullMdEditor.onEnumType(Color.class) //
				.addMetaData(newMd(SimpleEnumOverrideMetaData.T, true));
	}

	private void addSimpleEnumConstantMetaData() {
		baseMdEditor.onEnumType(Color.class) //
				.addConstantMetaData(Color.GREEN, newMd(SimpleEnumConstantMetaData.T, true));
	}

	private void addSimpleEnumConstantOverrideMetaData() {
		fullMdEditor.onEnumType(Color.class) //
				.addConstantMetaData(Color.GREEN, newMd(SimpleEnumConstantOverrideMetaData.T, true));
	}

	private void addGlobalConstantMd() {
		baseMdEditor.onEnumType(Color.class) //
				.addConstantMetaData(globalMd(false)).addConstantMetaData(Color.GREEN, globalMd(true));
	}

	private GlobalEnumConstantMetaData globalMd(boolean isFromConstant) {
		GlobalEnumConstantMetaData result = newMd(GlobalEnumConstantMetaData.T, true);
		result.setIsFromConstant(isFromConstant);

		return result;
	}

	private void addModelEnumMd() {
		fullMdEditor.addEnumMetaData(newMd(ModelEnumMetaData.T, true));
	}

	private void addModelConstantMd() {
		fullMdEditor.addConstantMetaData(newMd(ModelEnumConstantMetaData.T, true));
	}

}
