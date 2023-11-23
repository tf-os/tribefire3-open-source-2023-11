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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.devrock.zarathud.model.common.EntityNode;
import com.braintribe.devrock.zarathud.model.common.FieldNode;
import com.braintribe.devrock.zarathud.model.common.FingerPrintCoalescedNode;
import com.braintribe.devrock.zarathud.model.common.FingerPrintNode;
import com.braintribe.devrock.zarathud.model.common.FingerPrintRating;
import com.braintribe.devrock.zarathud.model.common.MethodNode;
import com.braintribe.devrock.zarathud.model.common.Node;
import com.braintribe.devrock.zarathud.model.model.EnumEntityNode;
import com.braintribe.devrock.zarathud.model.model.GenericEntityNode;
import com.braintribe.devrock.zarathud.model.model.ModelAnalysisNode;
import com.braintribe.devrock.zarathud.model.model.PropertyNode;
import com.braintribe.devrock.zed.forensics.fingerprint.HasFingerPrintTokens;
import com.braintribe.devrock.zed.ui.ZedViewingContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.data.natures.HasFieldsNature;
import com.braintribe.zarathud.model.data.natures.HasMethodsNature;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ModelForensicsResult;
import com.braintribe.zarathud.model.forensics.data.ModelEntityReference;
import com.braintribe.zarathud.model.forensics.data.ModelEnumReference;
import com.braintribe.zarathud.model.forensics.data.ModelPropertyReference;

/**
 * transposer for the model analysis tab
 * @author pit
 *
 */
public class ModelAnalysisContentTransposer implements HasFingerPrintTokens{
	
	/**
	 * @param context
	 * @param forensics
	 * @param sortRatingCentric 
	 * @return
	 */
	public static ModelAnalysisNode transposePerFingerPrints( ZedViewingContext context, ModelForensicsResult forensics, boolean sortRatingCentric) {
		
		ModelAnalysisNode maNode = ModelAnalysisNode.T.create();
		
		
		List<FingerPrint> fingerPrintsOfIssues = forensics.getFingerPrintsOfIssues();
		FingerPrint findFirst = fingerPrintsOfIssues.stream().filter(fp -> fp.getEntitySource() instanceof FieldEntity).findFirst().orElse(null);
		System.out.println(findFirst);
		
		
		List<FingerPrintCoalescedNode> transposedFingerPrints = transpose(context, fingerPrintsOfIssues);
		
		
		transposedFingerPrints.sort( new Comparator<FingerPrintCoalescedNode>() {
			@Override
			public int compare(FingerPrintCoalescedNode o1, FingerPrintCoalescedNode o2) {			
				if (sortRatingCentric) { 			
					FingerPrintRating fpr1 = o1.getWorstFingerPrintRating();
					FingerPrintRating fpr2 = o2.getWorstFingerPrintRating();
					int r = compareRatings(fpr1, fpr2);
					if (r != 0) 
						return r;
				}				
				return o1.getMessage().compareTo( o2.getMessage());
				
			}			
		});
				
		
		maNode.getChildren().addAll(transposedFingerPrints);
		
		return maNode;
	}
	
	
	
	// 
	public static ModelAnalysisNode transposePerOwner( ZedViewingContext context, ModelForensicsResult forensics, boolean sortRatingCentric) {
		
		ModelAnalysisNode maNode = ModelAnalysisNode.T.create();
		
		// finger prints associated with model forensics 
		List<FingerPrint> fingerPrintsOfIssues = forensics.getFingerPrintsOfIssues();
		
		// add all model entities and enum
		Map<GenericEntity, GenericEntity> modelEntities = new HashMap<>( forensics.getModelEntityReferences().size() + forensics.getModelEnumEntities().size()); 
		forensics.getModelEntityReferences().stream().forEach( me -> modelEntities.put( me.getType(), me));
		forensics.getModelEnumEntities().stream().forEach( me -> modelEntities.put( me.getType(), me));
		
		
		// get all involved entitie, enums and methods/fields  
		Set<GenericEntity> owners = fingerPrintsOfIssues.stream().map( fp -> fp.getEntitySource()).collect( Collectors.toSet());
		
		Map<GenericEntity, Map<String, FingerPrintCoalescedNode>> ownerToCoalescedNodeMap = new HashMap<>();
		Map<GenericEntity, Node> logicalOwnerToNodeMap = new HashMap<>();
		
		// 		
		List<Node> ownerNodes = new ArrayList<>( fingerPrintsOfIssues.size());

		// iterate over fingerprint owners (targets)
		for (GenericEntity owner : owners) {
			// find all fingerprints assigned to it 
			List<FingerPrint> assignedFingerPrints = fingerPrintsOfIssues.stream().filter(fp -> fp.getEntitySource() == owner).collect(Collectors.toList());
			if (assignedFingerPrints.size() == 0) {
				// no issues found ...
				continue;
			}			
			// create coalesced node for owner 
			List<FingerPrintCoalescedNode> transposedCoalescedFingerPrints = transpose(context, assignedFingerPrints);
			transposedCoalescedFingerPrints.sort( new Comparator<FingerPrintCoalescedNode>() {
				@Override
				public int compare(FingerPrintCoalescedNode o1, FingerPrintCoalescedNode o2) {			
					return o1.getMessage().compareTo( o2.getMessage());
				}			
			});
											
			GenericEntity logicalOwner = determineOwningEntityOrEnumReference( owner, modelEntities);
												
			// create owner node : 
			Node geNode = logicalOwnerToNodeMap.get(logicalOwner);
			if (geNode == null) {
				if (logicalOwner instanceof ModelEntityReference) {
					geNode = transpose(context, (ModelEntityReference) logicalOwner, (FingerPrintNode) null);
				}
				else { 
					geNode = transpose(context, (ModelEnumReference) logicalOwner, (FingerPrintNode) null);
				}
				logicalOwnerToNodeMap.put(logicalOwner, geNode);
				ownerNodes.add(geNode);
			}
			
			Map<String, FingerPrintCoalescedNode> map = ownerToCoalescedNodeMap.get(logicalOwner);
			FingerPrintCoalescedNode coalescedNode = transposedCoalescedFingerPrints.get(0);
			
			// nothing found for this owner -> create map
			if (map == null) {				
				map = new HashMap<>();
				ownerToCoalescedNodeMap.put(logicalOwner, map);							
			}
			
 
			FingerPrintCoalescedNode fpcn = map.get( coalescedNode.getMessage());
			if (fpcn == null) { // nothing stored for this coalesced node for this owner -> construct node
				map.put( coalescedNode.getMessage(), coalescedNode);			
				geNode.getChildren().addAll( transposedCoalescedFingerPrints);			
			}
			else { // owner has a coalesced node already, just add
				fpcn.getChildren().addAll(coalescedNode.getChildren());
			}
			List<FingerPrintRating> ratings = geNode.getChildren().stream().map( cn -> cn.getWorstFingerPrintRating()).collect(Collectors.toList());
			FingerPrintRating worst = getWorstRating(ratings);
			geNode.setWorstFingerPrintRating(worst);						
		}
				
		ownerNodes.sort( new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				if (sortRatingCentric) {
					FingerPrintRating fpr1 = o1.getWorstFingerPrintRating();
					FingerPrintRating fpr2 = o2.getWorstFingerPrintRating();
					int r = compareRatings(fpr1, fpr2);
					if (r != 0) {
						return r;
					}
				}
				String cmp1 = extractComparableProperty(o1);
				String cmp2 = extractComparableProperty(o2);
				return cmp1.compareTo(cmp2);
			}			
		});
															
		maNode.getChildren().addAll( ownerNodes);
		
		return maNode;
	}
	
	private static FingerPrintRating getWorstRating( Collection<FingerPrintRating> ratings) {
		FingerPrintRating worst = FingerPrintRating.ignore;
		for (FingerPrintRating fpr : ratings) {
			if (compareRatings(worst, fpr) > 0)
				worst = fpr;
		}
		return worst;
	}
	
	private static int compareRatings( FingerPrintRating fpr1, FingerPrintRating fpr2) {
		if (fpr1 == null) {
			fpr1 = FingerPrintRating.ignore; 
		}				
		if (fpr2 == null) {
			fpr2 = FingerPrintRating.ignore;
		}
		int r = fpr1.compareTo( fpr2) * -1;	
		return r;							
	}

	private static GenericEntity determineOwningEntityOrEnumReference(GenericEntity owner, Map<GenericEntity, GenericEntity> modelEntities) {
		if (owner instanceof ModelEntityReference || owner instanceof ModelEnumReference) { 
			return owner;			
		}
		if (owner instanceof MethodEntity) {
			MethodEntity me = (MethodEntity) owner;
			return determineOwningEntityOrEnumReference(me.getOwner(), modelEntities);
		}
		else if (owner instanceof FieldEntity) {
			FieldEntity fe = (FieldEntity) owner;
			return determineOwningEntityOrEnumReference(fe.getOwner(), modelEntities);			
		}
		else if (owner instanceof ModelPropertyReference) {
			ModelPropertyReference mpr = (ModelPropertyReference) owner;
			return mpr.getOwner();
		}
		else if (owner instanceof InterfaceEntity) {
			InterfaceEntity ie = (InterfaceEntity) owner;
			GenericEntity ref = modelEntities.get(ie);
			if (ref != null) {
				return ref;
			}
			System.out.println("No acceptable IE owner found");
		}
		System.out.println("No acceptable owner found for :" + owner.getClass().getName());
		return null;
	}

	private static String extractComparableProperty(Node node) {
		if (node instanceof GenericEntityNode) {
			GenericEntityNode gNode =  (GenericEntityNode) node;
			return gNode.getName();
		}
		else if (node instanceof EnumEntityNode) {
			EnumEntityNode eNode =  (EnumEntityNode) node;
			return eNode.getName();
		}
		else {
			return "";
		}
	}
	
	
	/**
	 * transposes the fingerprints into coalesced nodes, i.e. one node per ISSUE
	 * @param context - the {@link ZedViewingContext}
	 * @param fingerprints - a {@link List} of {@link FingerPrint}
	 * @param tr - the {@link TransposerRegistry}
	 * @return - a {@link List} of {@link FingerPrintNode}
	 */
	private static List<FingerPrintCoalescedNode> transpose(ZedViewingContext context, List<FingerPrint> fingerprints) {				
		Map<String, FingerPrintCoalescedNode> coalescedNodes = new HashMap<>();
		Map<String, List<FingerPrintNode>> fingerPrintsOfIssue = new HashMap<>();
		
		// create structure
		for (FingerPrint fp : fingerprints) {			
			String issue = fp.getSlots().get( ISSUE);
			FingerPrintCoalescedNode cnode = coalescedNodes.get(issue);
			if (cnode == null) {
				cnode = FingerPrintCoalescedNode.T.create();
				cnode.setMessage(issue);
				coalescedNodes.put(issue, cnode);
			}
			// transpose the finger print (as a quirky way to get it done quickly..)
			//GenericEntity target = tr.getMatch(fp);
			GenericEntity target = fp.getEntitySource();
			FingerPrintNode fingerPrintNode = transpose( context, fp, target);
			List<FingerPrintNode> con = fingerPrintsOfIssue.computeIfAbsent(issue, k -> new ArrayList<>());
			con.add(fingerPrintNode);					
		}
		
		// process the coalesce node
		for (Map.Entry<String, FingerPrintCoalescedNode> entry: coalescedNodes.entrySet()) {
			List<FingerPrintNode> associatedFingerPrints = fingerPrintsOfIssue.get(entry.getKey());			
					
			List<FingerPrint> fps = associatedFingerPrints.stream().map( fpn -> fpn.getFingerPrint()).collect(Collectors.toList());
			FingerPrintCoalescedNode coalescedNode = entry.getValue();
			
			FingerPrintRating worstRating = FingerPrintRating.ignore;
			for (FingerPrintNode fn : associatedFingerPrints) {
				FingerPrintRating rating = fn.getRating();
				if (worstRating.compareTo(rating) < 0) {
					worstRating = rating;
				}
				coalescedNode.getChildren().addAll( fn.getChildren());
				fps.add(fn.getFingerPrint());
			}

			coalescedNode.setWorstFingerPrintRating(worstRating);								
		}
								
		return new ArrayList<>(coalescedNodes.values());
	}
	
	
	
	/**
	 * transposes a single {@link FingerPrint} linked to its target
	 * @param context - the {@link ZedViewingContext}
	 * @param fp - the {@link FingerPrint}
	 * @param target - the {@link GenericEntity} target 
	 * @return - the instrumented {@link FingerPrintNode}
	 */
	private static FingerPrintNode transpose(ZedViewingContext context, FingerPrint fp, GenericEntity target) {
		FingerPrintNode node = CommonContentTransposer.transpose(context, fp);
		if (target != null) {
			if (target instanceof ModelEntityReference) {
				GenericEntityNode entityNode = transpose(context, (ModelEntityReference) target, node);
				node.getChildren().add(entityNode);				
			}
			else if (target instanceof ModelPropertyReference) {
				PropertyNode propertyNode = transpose(context, (ModelPropertyReference) target, node);
				node.getChildren().add(propertyNode);
			}
			else if (target instanceof MethodEntity) {
				MethodNode methodNode = transpose(context, (MethodEntity) target, node);				
				node.getChildren().add(methodNode);
			}
			else if (target instanceof ModelEnumReference) {
				EnumEntityNode enumNode = transpose( context, (ModelEnumReference) target, node);
				node.getChildren().add( enumNode);
				
			}			
			else if (target instanceof FieldEntity) {
				FieldNode fieldNode = transpose( context, (FieldEntity) target, node);
				node.getChildren().add( fieldNode);
			}
			else if (target instanceof InterfaceEntity) {
				EntityNode interfaceNode = transpose( context, (ZedEntity) target, node);
				node.getChildren().add(interfaceNode);
			}
		}
		return node;
	}

	/**
	 * @param context
	 * @param target
	 * @param relatedFingerPrintNode
	 * @return
	 */
	private static FieldNode transpose(ZedViewingContext context, FieldEntity target, FingerPrintNode relatedFingerPrintNode) {
		FieldNode node = FieldNode.T.create();
		node.setName( target.getName());
		node.setType(target.getType().getReferencedType().getName());
		node.setRelatedFingerPrint(relatedFingerPrintNode);
		HasFieldsNature owner = target.getOwner();
		if (owner instanceof ModelEntityReference) {
			node.getChildren().add( transpose(context, (ModelEntityReference) owner, relatedFingerPrintNode));	
		}
		else if (owner instanceof InterfaceEntity) { 
			InterfaceEntity ie = (InterfaceEntity) owner;
			if (Boolean.TRUE.equals( ie.getGenericNature()))  {			
				node.getChildren().add( transposeAsGenericEntity(context,  (ZedEntity) owner, relatedFingerPrintNode));
			}
			else {
				node.getChildren().add( transpose(context, (ZedEntity) owner, relatedFingerPrintNode));
			}
		}
		return node;
	}

	/**
	 * @param context
	 * @param target
	 * @param relatedFingerPrintNode
	 * @return
	 */
	private static EnumEntityNode transpose(ZedViewingContext context, ModelEnumReference target, FingerPrintNode relatedFingerPrintNode) {
		EnumEntityNode node = EnumEntityNode.T.create();
		node.setRelatedFingerPrint( relatedFingerPrintNode);
		node.setName( target.getEnumEntity().getName());
		return node;
	}

	/**
	 * transpose a single GenericEntity
	 * @param context - the {@link ZedViewingContext}
	 * @param mer - the {@link ModelEntityReference}
	 * @return - an {@link GenericEntityNode}
	 */
	private static GenericEntityNode transpose( ZedViewingContext context, ModelEntityReference mer, FingerPrintNode relatedFingerPrintNode) {
		GenericEntityNode geNode = GenericEntityNode.T.create();
		geNode.setRelatedFingerPrint( relatedFingerPrintNode);
		geNode.setName(mer.getType().getName());
		return geNode;
	}
	
	private static GenericEntityNode transposeAsGenericEntity( ZedViewingContext context, ZedEntity e, FingerPrintNode relatedFingerPrintNode) {
		GenericEntityNode geNode = GenericEntityNode.T.create();
		geNode.setRelatedFingerPrint( relatedFingerPrintNode);
		geNode.setName(e.getName());
		return geNode;
	}
	

	/**
	 * transposes a {@link ZedEntity}
	 * @param context - the {@link ZedViewingContext}
	 * @param ze - the {@link ZedEntity}
	 * @return - the {@link EntityNode}
	 */
	private static EntityNode transpose( ZedViewingContext context, ZedEntity ze, FingerPrintNode relatedFingerPrintNode) {
		EntityNode en = EntityNode.T.create();
		en.setName( ze.getName());
		en.setRelatedFingerPrint( relatedFingerPrintNode);
		en.setModuleName( ze.getModuleName());		
		return en;
	}
	
	/**
	 * transposes a {@link MethodEntity}
	 * @param context - the {@link ZedViewingContext}
	 * @param me - the {@link MethodEntity}
	 * @return - the {@link MethodNode}
	 */
	private static MethodNode transpose(ZedViewingContext context, MethodEntity me, FingerPrintNode relatedFingerPrintNode) {
		MethodNode mn = MethodNode.T.create();
		
		TypeReferenceEntity returnType = me.getReturnType();
		mn.setReturnType( transpose(context, returnType.getReferencedType(), relatedFingerPrintNode));
		
		List<TypeReferenceEntity> argumentTypes = me.getArgumentTypes();
		mn.getParameterTypes().addAll(argumentTypes.stream().map( tf -> tf.getReferencedType()).map( z -> transpose( context, z, relatedFingerPrintNode)).collect( Collectors.toList()));
				
		mn.setName(me.getName());
						
		HasMethodsNature owner = me.getOwner();
		if (owner instanceof ModelEntityReference) {
			mn.getChildren().add( transpose(context, (ModelEntityReference) owner, relatedFingerPrintNode));	
		}
		else if (owner instanceof InterfaceEntity) { 
			InterfaceEntity ie = (InterfaceEntity) owner;
			if (Boolean.TRUE.equals( ie.getGenericNature()))  {			
				mn.getChildren().add( transposeAsGenericEntity(context,  (ZedEntity) owner, relatedFingerPrintNode));
			}
			else {
				mn.getChildren().add( transpose(context, (ZedEntity) owner, relatedFingerPrintNode));
			}
		}
		
		return mn;
	}

	/**
	 * transposes a {@link ModelPropertyReference}
	 * @param context - the {@link ZedViewingContext}
	 * @param mpr - the {@link ModelPropertyReference}
	 * @return - the {@link PropertyNode}
	 */
	private static PropertyNode transpose(ZedViewingContext context, ModelPropertyReference mpr, FingerPrintNode relatedFingerPrintNode) {
		PropertyNode pn = PropertyNode.T.create();
		String name = mpr.getName();
		pn.setName( name);
		
		ZedEntity tpye = mpr.getType();
		if (tpye != null) {
			pn.setType( tpye.getName());
		}
		else {
			MethodEntity getter = mpr.getGetter();
			MethodEntity setter = mpr.getSetter();
			if (getter != null) {
				pn.setType( getter.getReturnType().getReferencedType().getName());
			}
			else if (setter != null){
				pn.setType( setter.getArgumentTypes().get(0).getReferencedType().getName());
			}
			else {
				pn.setType("<unknown>");
			}
		}
		
		// find literal 
		InterfaceEntity ie = (InterfaceEntity) mpr.getOwner().getType();
		
		FieldEntity tag  = null;
		for (FieldEntity fe : ie.getFields()) {
			if (fe.getName().equalsIgnoreCase( name)) {
				tag = fe;
				break;
			}			
		}
		if (tag != null) {
			Object initializer = tag.getInitializer();
			if (initializer instanceof String) {
				pn.setLiteral( (String) initializer);				
			}
		}
		
		ModelEntityReference owner = mpr.getOwner();		
		pn.getChildren().add( transpose(context, owner, relatedFingerPrintNode));				
		return pn;
	}
	
	

}
