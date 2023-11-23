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
package com.braintribe.model.processing.workbench.experts;

import com.braintribe.model.processing.workbench.WorkbenchInstructionContext;
import com.braintribe.model.processing.workbench.WorkbenchInstructionExpert;
import com.braintribe.model.processing.workbench.WorkbenchInstructionProcessorException;
import com.braintribe.model.workbench.instruction.CompoundInstruction;
import com.braintribe.model.workbench.instruction.WorkbenchInstruction;

/**
 * A logical instruction expert that delegates it's elements to other experts.   
 */
public class CompoundInstructionExpert implements WorkbenchInstructionExpert<CompoundInstruction>{


	@Override
	public void process(CompoundInstruction compoundInstruction, WorkbenchInstructionContext context) throws WorkbenchInstructionProcessorException {
		for (WorkbenchInstruction instruction : compoundInstruction.getInstructions()) {
			WorkbenchInstructionExpert<WorkbenchInstruction> expert = context.getExpertForInstruction(instruction);
			expert.process(instruction, context);
		}
	}
	
	
}
