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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zarathud.model.common.FingerPrintRating;
import com.braintribe.devrock.zarathud.model.common.ReferenceNode;
import com.braintribe.devrock.zarathud.model.dependency.DependencyAnalysisNode;
import com.braintribe.devrock.zarathud.model.dependency.DependencyKind;
import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.devrock.zed.forensics.fingerprint.HasFingerPrintTokens;
import com.braintribe.devrock.zed.forensics.fingerprint.filter.FingerPrintFilter;
import com.braintribe.devrock.zed.ui.ZedViewingContext;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.ArtifactForensicsResult;
import com.braintribe.zarathud.model.forensics.DependencyForensicsResult;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;
import com.braintribe.zarathud.model.forensics.data.ArtifactReference;
import com.braintribe.zarathud.model.forensics.findings.DependencyForensicIssueTypes;

/**
 * transposes a {@link DependencyForensicsResult} into a list of nodes
 * it contains :
 * a) the correct dependencies, i.e. declared and missing
 * b) the excessive dependencies, i.e. superfluous dependencies
 * c) the forward dependencies, i.e. virtual dependencies  
 * @author pit
 *
 */
public class DependencyAnalysisContentTransposer {

	/**
	 * @param context
	 * @param forensics
	 * @return
	 */
	public static List<DependencyAnalysisNode> transpose(ZedViewingContext context, DependencyForensicsResult forensics) {
		List<DependencyAnalysisNode> result = new ArrayList<>();		
		List<String> missingDeclarationNames = forensics.getMissingDeclarations().stream().map( a -> a.toVersionedStringRepresentation()).collect(Collectors.toList());
		List<String> forwaredDeclarationNames = forensics.getForwardedReferences().values().stream().map( a -> a.toVersionedStringRepresentation()).collect(Collectors.toList());
		Map<String,ArtifactForensicsResult> referenceMap = new HashMap<>();
		forensics.getMissingArtifactDetails().forEach( afr -> referenceMap.put( afr.getArtifact().toVersionedStringRepresentation(), afr));
		
		List<FingerPrint> fingerPrintsOfIssues = forensics.getFingerPrintsOfIssues();		
				
		// add required dependencies / means : declared and missing
		List<Artifact> requiredDeclarations = forensics.getRequiredDeclarations();
		for (Artifact artifact : requiredDeclarations) {
			// a) drop runtime
			if (artifact.getArtifactId().equals("rt")) { 
				continue;
			}
			DependencyAnalysisNode transposedArtifact;
			// can be missing or confirmed
			if (missingDeclarationNames.contains( artifact.toVersionedStringRepresentation())) { 
				transposedArtifact = transpose(artifact, DependencyKind.missing);							
				enhance(transposedArtifact, referenceMap);
				Pair<Boolean, FingerPrintRating> pair =  isOverridden(context, fingerPrintsOfIssues, DependencyForensicIssueTypes.MissingDependencyDeclarations, artifact);				
				transposedArtifact.setRating( pair.second);
				transposedArtifact.setOverridden(pair.first);
				
			}
			else {
				// filter out forward here 
				if (forwaredDeclarationNames.contains( artifact.toVersionedStringRepresentation())) {
					continue;
				}
				transposedArtifact = transpose(artifact, DependencyKind.confirmed);								 
			}
			result.add(transposedArtifact);
		}
		
		// add excessive dependencies
		List<Artifact> excessDeclarations = forensics.getExcessDeclarations();
		for (Artifact artifact : excessDeclarations) {
			// mark according finger print (might be ignorable)			
			DependencyAnalysisNode transposedArtifact = transpose(artifact, DependencyKind.excess);			
			Pair<Boolean, FingerPrintRating> pair  =  isOverridden(context, fingerPrintsOfIssues, DependencyForensicIssueTypes.ExcessDependencyDeclarations, artifact);				
			transposedArtifact.setRating( pair.second);
			transposedArtifact.setOverridden(pair.first);
			result.add(transposedArtifact);
		}
		// add forward dependencies
 		Map<ArtifactReference, Artifact> forwaredDeclarations = forensics.getForwardedReferences();
		Map<String, DependencyAnalysisNode> namedArtifact = new HashMap<>();
		Map<String,ReferenceNode> namedReference = new HashMap<>();
		for (Map.Entry<ArtifactReference, Artifact> entry : forwaredDeclarations.entrySet()) {
			Artifact artifact = entry.getValue();						
			String artifactKey = artifact.toVersionedStringRepresentation();
			DependencyAnalysisNode node = namedArtifact.get(artifactKey);
			if (node == null) {
				node = transpose(artifact, DependencyKind.forward);
				namedArtifact.put( artifactKey, node);
				result.add( node);
			}													
			ArtifactReference reference = entry.getKey();
			String referenceKey = reference.getSource().getName() + ":" + reference.getTarget().getName();
			ReferenceNode referenceNode = namedReference.get(referenceKey);
			if (referenceNode == null) {
				referenceNode = CommonContentTransposer.transpose(reference);
				node.getReferences().add(referenceNode);
				namedReference.put(referenceKey, referenceNode);
			}
			else {
				referenceNode.setCount( referenceNode.getCount() + 1);
			}
		}	
		
		result.sort( new Comparator<DependencyAnalysisNode>() {
			@Override
			public int compare(DependencyAnalysisNode o1, DependencyAnalysisNode o2) {				
				return o1.getIdentification().compareTo(o2.getIdentification());
			}			
		});
		
		return result;		
	}
	
	private static Pair<Boolean, FingerPrintRating> isOverridden( ZedViewingContext context, List<FingerPrint> fingerPrintsOfIssues, DependencyForensicIssueTypes type, Artifact artifact) {

		String aiAsString = artifact.toUnversionedStringRepresentation();
		
		// mark according finger print (might be ignorable)
		System.out.println(aiAsString);
		
		FingerPrint key = FingerPrintExpert.build( context.getArtifact(), type.toString());
		key.getSlots().put( HasFingerPrintTokens.ISSUE_KEY, aiAsString);
		
		FingerPrintFilter fpf = new FingerPrintFilter( key);
		List<FingerPrint> matches = fingerPrintsOfIssues.stream().filter( fpf).collect(Collectors.toList());
		// can only be one match
		if (matches.size() > 0) {
			FingerPrint associatedFingerPrint = matches.get(0);
			Pair<FingerPrint,ForensicsRating> activeRating = context.getRatingRegistry().getActiveRating(associatedFingerPrint);
			FingerPrint registeredFingerPint = activeRating.first;			
			FingerPrintRating rating = CommonContentTransposer.transpose( activeRating.second);			
			return Pair.of( registeredFingerPint.getOrigin() != null, rating);						
		}
		return Pair.of(false, null);		
	}
		
	
	/**
	 * @param artifact
	 * @param kind
	 * @return
	 */
	private static DependencyAnalysisNode transpose( Artifact artifact, DependencyKind kind) {
		DependencyAnalysisNode node = DependencyAnalysisNode.T.create();		
		node.setIdentification( VersionedArtifactIdentification.create( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
		node.setKind(kind);
		if (kind == DependencyKind.forward && artifact.getIsIncomplete()) {
			node.setIncompleteForwardReference( true);
		}
		return node;		
	}
	
	/**
	 * @param node
	 * @param referenceData
	 * @return
	 */
	private static DependencyAnalysisNode enhance(DependencyAnalysisNode node, Map<String,ArtifactForensicsResult> referenceData) {
		
		String key = node.getIdentification().asString();
		ArtifactForensicsResult afr = referenceData.get(key);
		if (afr == null) {		
			return node;
		}
		

		Map<String,ReferenceNode> namedReference = new HashMap<>();
		List<ArtifactReference> references = afr.getReferences();
		for (ArtifactReference reference : references) {			
			String referenceKey = reference.getSource().getName() + ":" + reference.getTarget().getName();
			ReferenceNode referenceNode = namedReference.get(referenceKey);
			if (referenceNode == null) {
				referenceNode = CommonContentTransposer.transpose(reference);
				namedReference.put(referenceKey, referenceNode);
				node.getReferences().add(referenceNode);
			}
			else {
				referenceNode.setCount( referenceNode.getCount() + 1);
			}
		}
			
		return node;
	}
	
}
