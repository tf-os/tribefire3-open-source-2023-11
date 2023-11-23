// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2019 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.impl;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.removeLast;

import java.util.List;
import java.util.Set;

import com.braintribe.build.cmd.assets.impl.modules.api.ClasspathConfiguration;
import com.braintribe.build.cmd.assets.impl.modules.api.TfsClasspathOptimizer;
import com.braintribe.build.cmd.assets.impl.modules.api.TfsComponentSetup;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.asset.natures.TribefireModule;
import com.braintribe.setup.tools.TfSetupTools;

/**
 * @author peter.gazdik
 */
public class GreedyClasspathOptimizer implements TfsClasspathOptimizer {

	@Override
	public void pimpMyClasspaths(ClasspathConfiguration cp) {
		new GreedyOptimizingPrimer(cp).pimpIt();
	}

	private static class GreedyOptimizingPrimer {

		private final ClasspathConfiguration cp;

		public GreedyOptimizingPrimer(ClasspathConfiguration cp) {
			this.cp = cp;
		}

		protected void pimpIt() {
			promoteEverythingPromotable();
		}

		/**
		 * We can only promote a solution iff all it's deps can be promoted. So the algorithm does the following:
		 * <ol>
		 * <li>Create a new list consisting of module cp items.</li>
		 * <li>Remove one item from this list and try to promote it, by doing the following steps with it:</li>
		 * <li>Get a list of the item's transitive deps in the DFS order (i.e. every dependency before it's depender).</li>
		 * <li>Iterate over the list and as long as you can promote a solution, do it (the order in the list guarantees the above mentioned condition
		 * holds). NOTE: Items already on the platform classpath are considered promoted, so e.g. platform lib deps cannot be prevented from promotion
		 * with a {@link TribefireModule#getPrivateDeps() private dependencies} flag</li>
		 * <li>If you cannot promote a solution, we are done here, the item itself cannot be promoted, so we continue with step 2 by removing another
		 * item.</li>
		 * </ol>
		 */
		private void promoteEverythingPromotable() {
			for (TfsComponentSetup moduleSetup : cp.moduleSetups) {
				List<AnalysisArtifact> moduleCpItems = newList(moduleSetup.classpath);
				Set<AnalysisArtifact> handledCpItems = TfSetupTools.analysisArtifactSet();

				while (!moduleCpItems.isEmpty())
					handleFirst(moduleCpItems, moduleSetup, handledCpItems);
			}
		}

		/* Starts with first moduleCpItem and promotes as many of it's deps as possible. On the first problem it stops */
		private void handleFirst(List<AnalysisArtifact> moduleCpItems, TfsComponentSetup originModuleSetup, Set<AnalysisArtifact> handledCpItems) {
			AnalysisArtifact moduleCpItem = removeLast(moduleCpItems);

			// If an item was already handled, then also it's dependencies have been handled, no need to do anything
			if (handledCpItems.contains(moduleCpItem))
				return;

			cp.promoteSolutionAndItsDepsIfPossible(moduleCpItem, originModuleSetup, false, handledCpItems);
		}

	}
}
