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
package com.braintribe.devrock.zed.ui.transposer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.braintribe.devrock.zarathud.model.classpath.ClasspathDuplicateNode;
import com.braintribe.devrock.zarathud.model.common.ArtifactNode;
import com.braintribe.devrock.zarathud.model.common.FingerPrintRating;
import com.braintribe.devrock.zed.forensics.fingerprint.HasFingerPrintTokens;
import com.braintribe.devrock.zed.ui.ZedViewingContext;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.forensics.ClasspathForensicsResult;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;
import com.braintribe.zarathud.model.forensics.data.ClasspathDuplicate;

/**
 * the transposer to get a displayable nodes from the {@link ClasspathForensicsResult}
 * 
 * @author pit
 *
 */
public class ClasspathAnalysisContentTransposer implements HasFingerPrintTokens {

	/**
	 * transpose the {@link ClasspathForensicsResult} to a {@link List} of {@link ClasspathDuplicateNode}
	 * @param context - the {@link ZedViewingContext}
	 * @param classpathForensicsResult - the {@link ClasspathForensicsResult}
	 * @return - a {@link List} of {@link ClasspathDuplicateNode}, empty if none
	 */
	public List<ClasspathDuplicateNode> transpose(ZedViewingContext context, ClasspathForensicsResult classpathForensicsResult) {
		
		List<FingerPrint> fingerPrintsOfIssues = classpathForensicsResult.getFingerPrintsOfIssues();
		ForensicsRating worstRatingOfFingerPrints = context.getRatingRegistry().getWorstRatingOfFingerPrints(fingerPrintsOfIssues);
		
		
					
		List<ClasspathDuplicate> duplicates = classpathForensicsResult.getDuplicates();
				
		List<ClasspathDuplicateNode> nodes = new ArrayList<>( duplicates.size());
		for (ClasspathDuplicate duplicate : duplicates) {
							
			nodes.addAll( transpose( context, duplicate, CommonContentTransposer.transpose(worstRatingOfFingerPrints)));
		}
		nodes.sort( new Comparator<ClasspathDuplicateNode>() {

			@Override
			public int compare(ClasspathDuplicateNode o1, ClasspathDuplicateNode o2) {
				return o1.getDuplicateType().getName().compareTo( o2.getDuplicateType().getName());				
			}
			
		});
		return nodes;
	}

	/**
	 * transpose a single {@link ClasspathDuplicate} from zed into {@link ClasspathDuplicateNode}s, several if required 
	 * @param context - the {@link ZedViewingContext}
	 * @param duplicate - the {@link ClasspathDuplicate}
	 * @param rating - the overall {@link FingerPrintRating} to assign
	 * @return - a {@link List} of {@link ClasspathDuplicateNode} (also contains all references in terminal)
	 */
	private List<ClasspathDuplicateNode> transpose(ZedViewingContext context, ClasspathDuplicate duplicate, FingerPrintRating rating) {
		List<ClasspathDuplicateNode> nodes = new ArrayList<>();
		for (ZedEntity z : duplicate.getReferencersInTerminal()) {
			ClasspathDuplicateNode node = ClasspathDuplicateNode.T.create();
			node.setDuplicateType( duplicate.getType());
			node.setReferencingType( z);			
			node.setRating( rating);
			
			List<Artifact> artifacts = duplicate.getDuplicates();
			for (Artifact artifact : artifacts) { 			
				node.getChildren().add( transpose(context, artifact));
			}		
			nodes.add(node);
		}
		return nodes;			
		
	}

	/**
	 * transpose the owning artifact of the duplicates
	 * @param context - the {@link ZedViewingContext}
	 * @param artifact - the {@link Artifact} as from zed
	 * @return - the transposed {@link ArtifactNode}
	 */
	private ArtifactNode transpose( ZedViewingContext context, Artifact artifact) {
		ArtifactNode node = ArtifactNode.T.create();		
		node.setIdentification( VersionedArtifactIdentification.create( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
		node.setIsTerminal(false);		
		return node;
				
	}
}
