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
package com.braintribe.devrock.commands.zed;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zed.api.comparison.SemanticVersioningLevel;
import com.braintribe.devrock.zed.api.comparison.ZedComparison;
import com.braintribe.devrock.zed.core.comparison.BasicComparator;
import com.braintribe.devrock.zed.ui.comparison.ZedComparisonResultViewer;
import com.braintribe.devrock.zed.ui.comparison.ZedComparisonTargetSelector;
import com.braintribe.devrock.zed.ui.comparison.ZedComparisonViewerContext;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.FingerPrint;

public class RunComparisonCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// Dialog to select analysis target	
		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());

		ZedComparisonTargetSelector selector = new ZedComparisonTargetSelector(shell);
		int retval = selector.open();
		if (retval == org.eclipse.jface.dialogs.Dialog.CANCEL) {
			return null;
		}
		
		Pair<Artifact,Artifact> pair = selector.getSelectedExtractions();
		SemanticVersioningLevel semanticLevel = selector.getSelectedSemanticLevel();
		
		System.out.println(pair.getFirst().asString() + " -> " + pair.getSecond().asString());
		
		ZedComparison comparator = new BasicComparator();
		boolean comparisonResult = comparator.compare( pair.getFirst(), pair.getSecond());
		List<FingerPrint> fingerPrints = comparator.getComparisonContext().getFingerPrints();
		if (!comparisonResult) {
			System.out.println("number of issues found during comparison : " + fingerPrints.size());
		}
		
		// viewer
		
		ZedComparisonResultViewer resultViewer = new ZedComparisonResultViewer( shell);
		ZedComparisonViewerContext context = new ZedComparisonViewerContext();
		context.setBaseArtifact( pair.first);
		context.setOtherArtifact( pair.second);
		context.setFingerPrints(fingerPrints);
		context.setSemanticComparisonLevel(semanticLevel);
		resultViewer.setContext(context);
		resultViewer.open();
		
		return null;
	}
	
	

}
