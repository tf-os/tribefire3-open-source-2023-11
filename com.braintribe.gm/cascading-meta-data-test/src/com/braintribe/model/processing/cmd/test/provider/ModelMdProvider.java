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

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.selector.RoleSelector;
import com.braintribe.model.processing.cmd.test.meta.model.BasicModelMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.ConjunctionSelectorMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.DisjunctionSelectorMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.InheritedModelMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.NegationSelectorMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.PriorityMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.SessionScopedMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.SimpleModelMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.StaticContextMetaData;
import com.braintribe.model.processing.cmd.test.meta.model.UseCaseMetaData;
import com.braintribe.model.processing.cmd.test.meta.selector.StaticContextSelector;

/**
 * 
 */
public class ModelMdProvider extends AbstractModelSupplier {

	public static final int PRIORITIZED_MD = 10;

	@Override
	protected void addMetaData() {
		addSimpleModelMD();
		addPriorityModelMD();
		addSimpleSelectorMD();
		addLogicalSelectorMD();
		addSessionModelMD();
		addStaticContextMD();
		addUseCaseMD();
		addInheritedMd();
	}

	private void addSimpleModelMD() {
		fullMdEditor.addModelMetaData(BasicModelMetaData.T.create());
	}

	private void addPriorityModelMD() {
		List<PriorityMetaData> pmds = newList();

		for (int i = 1; i <= PRIORITIZED_MD; i++) {
			PriorityMetaData pmd = PriorityMetaData.T.create();
			pmd.setPriorityValue(i);
			pmds.add(append(pmd, i));
		}

		Collections.shuffle(pmds);
		fullMdEditor.addModelMetaData(pmds.toArray(new MetaData[PRIORITIZED_MD]));
	}

	private void addSimpleSelectorMD() {
		fullMdEditor.addModelMetaData(append(newMd(SimpleModelMetaData.T, true), TRUE_SELECTOR));
		fullMdEditor.addModelMetaData(append(newMd(SimpleModelMetaData.T, false), FALSE_SELECTOR));
	}

	private void addLogicalSelectorMD() {
		fullMdEditor.addModelMetaData(append(newMd(ConjunctionSelectorMetaData.T, true), and(TRUE_SELECTOR, TRUE_SELECTOR, TRUE_SELECTOR)));
		fullMdEditor.addModelMetaData(append(newMd(ConjunctionSelectorMetaData.T, false), and(TRUE_SELECTOR, TRUE_SELECTOR, FALSE_SELECTOR)));

		fullMdEditor.addModelMetaData(append(newMd(DisjunctionSelectorMetaData.T, true), or(TRUE_SELECTOR, FALSE_SELECTOR, TRUE_SELECTOR)));
		fullMdEditor.addModelMetaData(append(newMd(DisjunctionSelectorMetaData.T, false), or(FALSE_SELECTOR, FALSE_SELECTOR, FALSE_SELECTOR)));

		fullMdEditor.addModelMetaData(append(newMd(NegationSelectorMetaData.T, true), not(FALSE_SELECTOR)));
		fullMdEditor.addModelMetaData(append(newMd(DisjunctionSelectorMetaData.T, false), not(TRUE_SELECTOR)));
	}

	private void addSessionModelMD() {
		RoleSelector admin = RoleSelector.T.create();
		admin.setRoles(asSet("admin"));

		RoleSelector user = RoleSelector.T.create();
		user.setRoles(asSet("user"));

		fullMdEditor.addModelMetaData(append(newMd(SessionScopedMetaData.T, true), admin));
		fullMdEditor.addModelMetaData(append(newMd(SessionScopedMetaData.T, false), user));
	}

	private void addStaticContextMD() {
		StaticContextSelector yes = staticContextSelector("YES");
		StaticContextSelector no = staticContextSelector("NO");

		fullMdEditor.addModelMetaData(append(newMd(StaticContextMetaData.T, true), yes, 0));
		fullMdEditor.addModelMetaData(append(newMd(StaticContextMetaData.T, false), no, 1000));
	}

	private void addUseCaseMD() {
		fullMdEditor.addModelMetaData(append(newMd(UseCaseMetaData.T, true), useCase("test-case")));
	}

	private void addInheritedMd() {
		baseMdEditor.addModelMetaData(newMd(InheritedModelMetaData.T, true));
	}

	private Set<String> asSet(String... strings) {
		return new HashSet<String>(Arrays.asList(strings));
	}

}
