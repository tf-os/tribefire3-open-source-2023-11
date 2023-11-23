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
package tribefire.extension.tutorial.initializer.wire.space;

import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.tutorial.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.tutorial.initializer.wire.contract.TutorialInitializerContract;
import tribefire.extension.tutorial.model.deployment.LetterCaseTransformProcessor;

@Managed
public class TutorialInitializerSpace extends AbstractInitializerSpace implements TutorialInitializerContract {

	@Import
	private ExistingInstancesContract existingInstances;

	
	@Override
	@Managed
	public ProcessWith processWithLetterCaseTransform() {
		ProcessWith bean = create(ProcessWith.T);
		
		bean.setProcessor(letterCaseTransformProcessor());
		
		return bean;
	
	}
	
	@Managed
	private LetterCaseTransformProcessor letterCaseTransformProcessor() {
		LetterCaseTransformProcessor bean = create(LetterCaseTransformProcessor.T);
		
		bean.setName("Letter Case Transform Processor");
		bean.setExternalId("serviceProcessor.letterCaseTransform");
		bean.setModule(existingInstances.tutorialModule());
		return bean;		
	}
	
}
