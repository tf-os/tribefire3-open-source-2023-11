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
package com.braintribe.devrock.api.ui.viewers.artifacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.fonts.StyledTextHandler;
import com.braintribe.devrock.api.ui.fonts.StyledTextSequenceData;
import com.braintribe.devrock.api.ui.viewers.StyledStringReasonStyler;
import com.braintribe.devrock.eclipse.model.resolution.nodes.AnalysisNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.DeclaratorNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.DependerNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.Node;
import com.braintribe.devrock.eclipse.model.resolution.nodes.NodeArchetype;
import com.braintribe.devrock.eclipse.model.resolution.nodes.NodeFunction;
import com.braintribe.devrock.eclipse.model.resolution.nodes.PartNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.ProjectNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.ReasonNode;
import com.braintribe.devrock.eclipse.model.storage.Color;
import com.braintribe.devrock.eclipse.model.storage.ViewerContext;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.model.version.VersionInterval;

/**
 * the {@link IStyledLabelProvider} for the {@link TransposedAnalysisArtifactViewer}
 * @author pit
 *
 */
public class AnalysisViewLabelProvider  extends CellLabelProvider implements NodeViewLabelProvider {

	
	private static final String INNER_DEPENDENCY = " \u21E2 ";
	private static String DEPENDER ="\uD83E\uDC44"; // "\u1F844"; //"🡄";
	private static String DEPENDENCY = "\uD83E\uDC46"; //"\u1F846";  //"🡆";
	private static String PARENT_DEPENDER = "\uD83E\uDC47"; // "\u1F847"; //"🡇";
	private static String PARENT_DEPENDENCY = "\uD83E\uDC45"; // "\u1F845"; //"🡅";
	private static String TERMINAL_DEPENDER = "\u258F" + "" + DEPENDER; //▐ 🡄
	
	private static String IMPORT_DEPENDER = "\uD83E\uDC55"; //"🡕";
	private static String IMPORT_DEPENDENCY = "\uD83E\uDC57"; //"🡗";
	//U+258F	
	

	private static String PART = "\u2117";
	
	private static String TERMINAL = "⇤";
	
	private static boolean hardWiredShowNatureFlag = true;
	
	private Styler idStyler;
	private Styler purgedIdStyler;
	private Styler groupStyler;
	private Styler standardVersionStyler;
	private Styler pcVersionStyler;
	private Styler dashStyler;
	private Styler dependencyDetailStyler;
	
	private Styler artifactRelationshipStyler;
	private Styler parentRelationshipStyler;
	private Styler importRelationshipStyler;
	
	private Styler errorStyler;
	
	private Image parentImage;
	private Image importImage;
	private Image projectImage;
	private Image jarImage;
		
	private Image terminalImage;
	private Image clashWinnerImage;
	private Image clashLoserImage;
	private Image failureImage;
	private Image warningImage;
	private Image infoImage;
	
	private Image declaratorImage;
	
	private Image pomPartImage;
	private Image jarPartImage;
	private Image javadocPartImage;
	private Image sourcesPartImage;
	private Image zipPartImage;
	private Image manPartImage;
	private Image anyPartImage;
	
	private UiSupport uiSupport;
	private String uiSupportStylersKey = "standard";
	
	private ViewerContext viewerContext;
	private StyledStringReasonStyler reasonStyler = new StyledStringReasonStyler();
	
	private DetailRequestHandler requestHander;
	
	@Configurable @Required
	public void setUiSupport(UiSupport uiSupport) {
		this.uiSupport = uiSupport;
		
		setupUiSupport();
	}
	
	
	@Override
	public void setRequestHander(DetailRequestHandler requestHander) {
		this.requestHander = requestHander;
	}
	
	
	
	@Configurable
	public void setUiSupportStylersKey(String uiSupportStylersKey) {
		this.uiSupportStylersKey = uiSupportStylersKey;
	}
	
	@Configurable @Required
	public void setViewerContext(ViewerContext viewContext) {
		this.viewerContext = viewContext;
	}
	
	/**
	 * initialize ui stuff 
	 */
	private void setupUiSupport() {
				
		idStyler = uiSupport.stylers( uiSupportStylersKey).addStyler("id", SWT.BOLD);
		purgedIdStyler = uiSupport.stylers( uiSupportStylersKey).addStyler("purgedId", SWT.ITALIC);
		groupStyler = uiSupport.stylers( uiSupportStylersKey).addDefaultStyler("group");
		
		Color defaultColor = Color.create(0, 128, 0);
		Color color = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_VERSION_STANDARD, defaultColor);
		
		standardVersionStyler = uiSupport.stylers( uiSupportStylersKey).addStyler("version", null, asSwtColor(color), null, null);
		
		defaultColor = Color.create(134, 0, 0);
		color = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_VERSION_PC, defaultColor);
		pcVersionStyler = uiSupport.stylers( uiSupportStylersKey).addStyler("pc-version", null, asSwtColor(color), null, null);
		
		dashStyler = uiSupport.stylers(uiSupportStylersKey).addStyler("dash", null, SWT.COLOR_DARK_GRAY, null, null);
		dependencyDetailStyler = uiSupport.stylers( uiSupportStylersKey).addStyler("dependencyDetail", null, SWT.COLOR_DARK_GRAY, null, null);
		
		defaultColor = Color.create(0, 128, 0);
		
		color = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_AXIS_DEPENDENCY, defaultColor);
		artifactRelationshipStyler = uiSupport.stylers( uiSupportStylersKey).addStyler("artifactRelationship", SWT.BOLD, asSwtColor(color), null, null);

		defaultColor = Color.create(128, 128, 128);
		color = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_AXIS_PARENT, defaultColor);
		parentRelationshipStyler = uiSupport.stylers( uiSupportStylersKey).addStyler("parentRelationship", SWT.BOLD, asSwtColor(color), null, null);
				
		defaultColor = Color.create(0, 0, 0);
		color = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_AXIS_IMPORT, defaultColor);
		importRelationshipStyler = uiSupport.stylers( uiSupportStylersKey).addStyler("parentRelationship", SWT.BOLD, asSwtColor(color), null, null);
		
		
		errorStyler = uiSupport.stylers( uiSupportStylersKey).addStyler("error", SWT.BOLD, SWT.COLOR_DARK_RED, null, null);
				
		
		failureImage = uiSupport.images().addImage("failure", AnalysisViewLabelProvider.class, "error.gif");
		warningImage = uiSupport.images().addImage("warning", AnalysisViewLabelProvider.class, "warning.png");
		infoImage = uiSupport.images().addImage("info", AnalysisViewLabelProvider.class, "info_obj.gif");
		
		// parents and imports
		importImage = uiSupport.images().addImage("import", AnalysisViewLabelProvider.class, "include_obj.gif");				
		parentImage = uiSupport.images().addImage("parent", AnalysisViewLabelProvider.class, "parent_pom.gif");
		declaratorImage = uiSupport.images().addImage("declarator", AnalysisViewLabelProvider.class, "requester.png");
		
		jarImage = uiSupport.images().addImage("jar", AnalysisViewLabelProvider.class, "jar_obj.gif");
		projectImage = uiSupport.images().addImage("project", AnalysisViewLabelProvider.class, "project_obj.gif");  
	
		// standard artifact
		terminalImage = uiSupport.images().addImage("terminal", AnalysisViewLabelProvider.class, "terminal.png");		
		
		// clashes
		clashWinnerImage = uiSupport.images().addImage("clashWinner", AnalysisViewLabelProvider.class, "include_obj.gif");							
		clashLoserImage = uiSupport.images().addImage("clashLoser", AnalysisViewLabelProvider.class, "exclude_obj.gif");
		
		// parts
		pomPartImage = uiSupport.images().addImage("pomPart", AnalysisViewLabelProvider.class, "solution.pom.gif");				
		jarPartImage = uiSupport.images().addImage("jarPart", AnalysisViewLabelProvider.class, "solution.jar.gif");				
		javadocPartImage = uiSupport.images().addImage("javadocPart", AnalysisViewLabelProvider.class, "solution.javadoc.gif");				
		sourcesPartImage = uiSupport.images().addImage("sourcesPart", AnalysisViewLabelProvider.class, "solution.source.gif");
		zipPartImage = uiSupport.images().addImage("zipPart", AnalysisViewLabelProvider.class, "solution.zip.gif");
		manPartImage = uiSupport.images().addImage("manPart", AnalysisViewLabelProvider.class, "solution.man.gif");
		anyPartImage = uiSupport.images().addImage("anyPart", AnalysisViewLabelProvider.class, "star.png");
		
		// projects 
		
		
		reasonStyler.setUiSupport(uiSupport);
						
	}

	private org.eclipse.swt.graphics.Color asSwtColor(Color color) {
		return new org.eclipse.swt.graphics.Color( color.getRed(), color.getGreen(), color.getBlue());
	}
	
	
	@Override
	public String getToolTipText(Object element) {
		if (element instanceof AnalysisNode) {
			AnalysisNode node = (AnalysisNode) element;
			boolean isObsolete = requestHander.acknowledgeObsoleteCheckRequest( node.getSolutionIdentification());
		
			AnalysisDependency backingDependency = node.getBackingDependency();
			AnalysisArtifact backingArtifact = node.getBackingSolution();
			StringBuilder sb = new StringBuilder();
			
			if (backingDependency != null) {
				// 
				sb.append( backingDependency.getGroupId());
				sb.append( ":");
				sb.append( backingDependency.getArtifactId());
				sb.append( "#");
				sb.append( backingDependency.getVersion());
				
				String classifier = backingDependency.getClassifier();
				if (classifier != null) {
					sb.append( ", classifier : " + classifier);
				}
				
				String scope = backingDependency.getScope();
				if (scope == null) {
					scope = "compile";
				}
				sb.append(", scope : " + scope);
				String type = backingDependency.getType();
				if (type != null) {
					sb.append( ", packaging : " + type);
				}			
				boolean optional = backingDependency.getOptional();
				if (optional) {
					sb.append( ", optional");
				}
				
				if (backingArtifact != null) {
					Reason reason = backingArtifact.getFailure();
					if (reason != null) {
						sb.append( "\nfailed : " + reason.stringify());
					}			
				}
				return sb.toString();		
			}
			else if (backingArtifact != null) {
				
				sb.append( backingArtifact.getGroupId());
				sb.append( ":");
				sb.append( backingArtifact.getArtifactId());
				sb.append( "#");
				sb.append( backingArtifact.getVersion());
				Reason reason = backingArtifact.getFailure();
				if (reason != null) {
					sb.append( "\nfailed : " + reason.stringify());
				}			
				return sb.toString();	
			}
	
			if (node.getFunction() == NodeFunction.depender) {
				
			}	
		}
		else if (element instanceof PartNode) {
			PartNode partNode = (PartNode) element;
			return partNode.getPart().asString();
		}
		else if (element instanceof ReasonNode) {
			ReasonNode reasonNode = (ReasonNode) element;		
				return reasonNode.getText();
		}
		else if (element instanceof DeclaratorNode) {
			DeclaratorNode declaratorNode = (DeclaratorNode) element;
			StringBuilder sb = new StringBuilder();
			sb.append( declaratorNode.getDependingDependency().getGroupId());
			sb.append( ":");
			sb.append( declaratorNode.getDependingDependency().getArtifactId());
			sb.append( "#");
			sb.append( declaratorNode.getDependingDependency().getVersion());
			return sb.toString();
		}
		else if (element instanceof ProjectNode) {
			ProjectNode projectNode = (ProjectNode) element;
			StringBuilder sb = new StringBuilder();
			sb.append( projectNode.getName());
			sb.append( " -> ");
			sb.append( projectNode.getReplacedSolution().asString());
			return sb.toString();
		}

		return null;
	}

	@Override
	public Image getImage(Object arg0) {
		if (! (hardWiredShowNatureFlag || viewerContext.getShowNature())) {
			return null;
		}
		
		if (arg0 instanceof AnalysisNode) {		
			AnalysisNode node = (AnalysisNode) arg0;
			
			ensureNodeFunction(node);
			
			// failure
		
			AnalysisArtifact backingSolution = node.getBackingSolution();
			if (backingSolution != null) {
				if (backingSolution.hasFailed()) {
					return failureImage;
				}
			}
									
			switch (node.getFunction()) {
				case terminal :
					return terminalImage;
				case imports:
					return importImage;
				case parent:
					return parentImage;					
				case clashWinner:
					return clashWinnerImage;
				case clashLoser:
					return clashLoserImage;
				case clash:
					return anyPartImage;
				case depender:									
				case standard:
					if (node.getIsAProject()) {
						return projectImage;
					}
					else {
						return jarImage;
					}
				default:
					return null; 			
			}
		}
		else if (arg0 instanceof DependerNode) {						
			DependerNode node = (DependerNode) arg0;
			if (node.getIsAProject()) {
				return projectImage;
			}
			else {
				return jarImage;
			}
			
		}
		else if (arg0 instanceof PartNode) {
			PartNode node = (PartNode) arg0;
			switch (node.getArchetype()) {
				case jar:
					return jarPartImage;
				case javadoc:
					return javadocPartImage;
				case pom:					
					return pomPartImage;
				case sources:
					return sourcesPartImage;
				case zip: 
					return zipPartImage;
				case man:
					return manPartImage;
				default:
				case other:
					return anyPartImage;					
			}
		}				
		else if (arg0 instanceof ReasonNode) {
			ReasonNode node = (ReasonNode) arg0;
			
			switch (node.getClassification()) {
				case failure:
					return failureImage;
				case warning:
					return warningImage;				
				case info:				
				case origination:
					return infoImage;
				default:
					break;				
			}
		}	
		else if (arg0 instanceof ProjectNode) {			
			return projectImage;
		}
		else if (arg0 instanceof DeclaratorNode) {			
			return declaratorImage;
		}
		return null;								
	}

	@Override
	public StyledString getStyledText(Object obj) {
						
		Pair<String,List<StyledTextSequenceData>> prefix = null;	
		Node node = (Node) obj;
		if ( !node.getTopLevel()) {
			prefix = buildPrefixForNode( (Node) obj);
			//offset = prefix.first.length();						
		}
	
		
		if (obj instanceof AnalysisNode) {
			// analysis node
			AnalysisNode analysisNode = (AnalysisNode) obj;
			boolean isObsolete = requestHander.acknowledgeObsoleteCheckRequest( analysisNode.getSolutionIdentification());
			// dependency -> solution 
			VersionedArtifactIdentification dependency = analysisNode.getDependencyIdentification();					
			VersionedArtifactIdentification artifact = analysisNode.getSolutionIdentification();
			
			// if it's a failed toplevel - a add prefix  
			if (analysisNode.getTopLevel() && analysisNode.getBackingSolution() != null && analysisNode.getBackingSolution().getFailure() != null) {
				//String failPrefixText = StyledStringReasonStyler.errorMark + " ";
				//StyledTextSequenceData stsd = new StyledTextSequenceData(0, failPrefixText.length(), errorStyler);				
				//Pair<String,List<StyledTextSequenceData>> failPrefix = Pair.of(failPrefixText, Collections.singletonList(stsd));
				Pair<String,List<StyledTextSequenceData>> tagForAnalysisNode = buildTagForAnalysisNode( analysisNode, dependency, artifact, analysisNode.getBackingDependency());
				//return merge( failPrefix, tagForAnalysisNode);
				return StyledTextHandler.merge( prefix, tagForAnalysisNode);
			}
			else {								
				// dependency -> solution 				
				Pair<String,List<StyledTextSequenceData>> tagForAnalysisNode = buildTagForAnalysisNode( analysisNode, dependency, artifact, analysisNode.getBackingDependency());
				return StyledTextHandler.merge( prefix, tagForAnalysisNode);
			}
		}
		else if (obj instanceof PartNode) {
			// part node
			PartNode partNode = (PartNode) obj;
			String partPrefix = PART + " ";
			StyledTextSequenceData stsd = new StyledTextSequenceData(0, partPrefix.length(),dependencyDetailStyler);
			String txt = partPrefix + partNode.getPart().asString();
			StyledString styledString = new StyledString( txt);
			return StyledTextHandler.applyRanges(styledString, Collections.singletonList( stsd));						
		}
		else if (obj instanceof DependerNode) {
			// depender node
			DependerNode dependerNode = (DependerNode) obj;
			VersionedArtifactIdentification dependerArtifact = dependerNode.getDependerArtifact();
			VersionedArtifactIdentification dependerDependency = dependerNode.getDependency();		
			Pair<String,List<StyledTextSequenceData>> tagForDependerNode = buildTagForDependerNode( dependerArtifact, dependerDependency);
			return StyledTextHandler.merge( prefix, tagForDependerNode);
		}
		else if (obj instanceof ReasonNode) {
			ReasonNode reasonNode = (ReasonNode) obj; 
			return reasonStyler.process( reasonNode.getBackingReason());
		}
		else if (obj instanceof DeclaratorNode) {
			DeclaratorNode declaratorNode = (DeclaratorNode) obj;
			VersionedArtifactIdentification dependency = declaratorNode.getDependingDependency();					
			VersionedArtifactIdentification artifact = declaratorNode.getDeclaratorArtifact();
			// dependency -> solution
			Pair<String, List<StyledTextSequenceData>> tagForDeclaratorNode = buildTagForAnalysisNode( null, dependency, artifact, declaratorNode.getBackingDependingDependency());
			return StyledTextHandler.merge( prefix, tagForDeclaratorNode);
		}
		else if (obj instanceof ProjectNode) {
			ProjectNode projectNode = (ProjectNode) obj;
			Pair<String,List<StyledTextSequenceData>> tagForProjectNode = buildTagForProjectNode( projectNode);
			return StyledTextHandler.merge( prefix, tagForProjectNode);
		}
		return new StyledString();
	}
	
	private void ensureNodeFunction(Node node) {		
		if (node.getFunction() == null)
			node.setFunction( NodeFunction.standard);
		if (node.getArchetype() == null)
			node.setArchetype( NodeArchetype.jar);
	}
	
	
	private Pair<String, List<StyledTextSequenceData>> buildPrefixForNode(Node node) {
		ensureNodeFunction(node);
		String txt = null;
		Styler styler = null;
		switch (node.getFunction()) {
			case terminal:
				txt = TERMINAL_DEPENDER;
				styler = artifactRelationshipStyler;
				break;
			case depender:
				if (node instanceof DependerNode) {
					DependerNode dnode = (DependerNode) node;
					if (dnode.getIsParentDepender()) {
						txt = PARENT_DEPENDER;
						styler = parentRelationshipStyler;
					}
					else if (dnode.getIsTerminal()){
						txt = TERMINAL_DEPENDER;
						styler = artifactRelationshipStyler;
					}
					else {
						txt = DEPENDER;
						styler = artifactRelationshipStyler;						
					}				
				}
				else {
					return null; // should not happen... 
				}				
				break;							
			case standard:
				txt = DEPENDENCY;
				styler = artifactRelationshipStyler;
				break;
			case parent:
				txt = PARENT_DEPENDENCY;
				styler = parentRelationshipStyler;
				break;					
			case imports:
				txt = IMPORT_DEPENDENCY;				
				styler = importRelationshipStyler;
				break;				
			default:
				return null; 			
		}		
		
		return Pair.of( txt+= " ", Collections.singletonList(new StyledTextSequenceData(0, txt.length(), styler)));
		
	}			
	
	private Styler stylerForVersion( String versionAsString) {
		Version version = Version.parse(versionAsString);
		String qualifier = version.getQualifier();
		if (qualifier == null) {
			return standardVersionStyler;
		}
		
		if (version.isPreliminary()) {
			return pcVersionStyler;
		}
		return standardVersionStyler;
	}

	private Pair<String, List<StyledTextSequenceData>> buildTagForProjectNode( ProjectNode node) {
		List<StyledTextSequenceData> sequences = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		
		// <projectName>:<replaced solution>
		
		
		// project name 
		int s = 0;
		sb.append( node.getName());		
		sequences.add( new StyledTextSequenceData(s, sb.length(), idStyler));
		int l = sb.length();
		// dash
		sb.append( INNER_DEPENDENCY);
		sequences.add( new StyledTextSequenceData(l, sb.length(), dashStyler));
		l = sb.length();
		
		AnalysisArtifact artifact = node.getReplacedSolution();
		// replaced solution
		if (viewerContext.getShowGroupIds()) {
			String sgId = artifact.getGroupId();		
			int cS = sb.length();
			sb.append( sgId);
			sb.append( " ");		
			int cE = sb.length();
			sequences.add( new StyledTextSequenceData(cS, cE, groupStyler));		
		}
		
		String saId = artifact.getArtifactId();		
		int cS = sb.length();
		sb.append( saId);
		int cE = sb.length();
		sequences.add( new StyledTextSequenceData(cS, cE, idStyler));
		
		int cvS = sb.length();
		sb.append( " ");
		sb.append( artifact.getVersion());
		int cvE = sb.length();
		Styler styler = stylerForVersion( artifact.getVersion());
		sequences.add( new StyledTextSequenceData(cvS, cvE, styler));					

		
		String txt = sb.toString();		
		return Pair.of( txt, sequences);
		
	}
	
	/**
	 * @param depender
	 * @param dependerDependency
	 * @return
	 */
	private Pair<String, List<StyledTextSequenceData>> buildTagForDependerNode(VersionedArtifactIdentification depender, VersionedArtifactIdentification dependerDependency) {
		List<StyledTextSequenceData> sequences = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		
		String daGid = depender.getGroupId();
		String daAid = depender.getArtifactId();
		String daVrs = depender.getVersion();

		// dependency artifact 
		if (viewerContext.getShowGroupIds()) {
			sb.append( daGid);
			sb.append( " ");
			sequences.add( new StyledTextSequenceData(0, sb.length(), groupStyler));
		}
		
		int cAid = sb.length();
		sb.append( daAid);
		sequences.add( new StyledTextSequenceData(cAid, sb.length(), idStyler));
		
		int cV = sb.length();
		sb.append( " ");
		sb.append( daVrs);
		Styler styler = stylerForVersion( daVrs);
		sequences.add( new StyledTextSequenceData(cV, sb.length(), styler));
		
		 
				 
		
		if (viewerContext.getShowDependencies()) {
			int cD = sb.length();
			sb.append( INNER_DEPENDENCY);
			sequences.add( new StyledTextSequenceData(cD, sb.length(), dashStyler));
			
			// depender dependency 
			String ddGid = dependerDependency.getGroupId();
			String ddAid = dependerDependency.getArtifactId();
			String ddVrs = dependerDependency.getVersion();
			
			if (viewerContext.getShowGroupIds() && !daGid.equals( ddGid)) {
				sb.append( ddGid);
				sb.append( " ");
				sequences.add( new StyledTextSequenceData(0, sb.length(), groupStyler));
			}
			
			cAid = sb.length();
			sb.append( ddAid);
			sequences.add( new StyledTextSequenceData(cAid, sb.length(), idStyler));
			
			cV = sb.length();
			sb.append( " ");
					
			VersionInterval interval = VersionExpression.parseVersionInterval( ddVrs);
			if (viewerContext.getShowShortNotation()) {
				sb.append( interval.asShortNotation());
			}
			else {
				sb.append( interval.asString());
			}		
			styler = stylerForVersion( ddVrs);
			sequences.add( new StyledTextSequenceData(cV, sb.length(), styler));
		}
 
		
		String txt = sb.toString();		
		return Pair.of(txt, sequences);
	}

	/**
	 * groupid:artifactId#version[Range] -> [groupId]:[artifactId]#version 	 
	 * @param dependency - the {@link VersionedArtifactIdentification} of the dependency
	 * @param artifact - the {@link VersionedArtifactIdentification} of the artiafact
	 * @param backingDependency - the {@link AnalysisDependency}
	 * @param offset - the offset
	 * @return
	 */
	private Pair<String,List<StyledTextSequenceData>> buildTagForAnalysisNode( AnalysisNode node, VersionedArtifactIdentification dependency, VersionedArtifactIdentification artifact, AnalysisDependency backingDependency) {
		
		List<StyledTextSequenceData> sequences = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		String gId = null, aId = null;
		
		boolean isObsolete = node != null ? requestHander.acknowledgeObsoleteCheckRequest( node.getSolutionIdentification()) : false;
		
		
		// dependency identification
		if (dependency != null && 
				(
						viewerContext.getShowDependencies())
						|| artifact == null
				){
		
			gId = dependency.getGroupId();
			aId = dependency.getArtifactId();
			String v = dependency.getVersion();
			
			// 
			if (viewerContext.getShowGroupIds()) {
				sb.append( gId);
				sb.append( " ");
				sequences.add( new StyledTextSequenceData(0, sb.length(), groupStyler));
			}
			
			int cAid = sb.length();
			sb.append( aId);
			if (node == null || !isObsolete) { 
				sequences.add( new StyledTextSequenceData(cAid, sb.length(), idStyler));
			}
			else {
				sequences.add( new StyledTextSequenceData(cAid, sb.length(), purgedIdStyler));
			}
			
			int cV = sb.length();
			sb.append( " ");
			VersionInterval interval = VersionExpression.parseVersionInterval( v);
			if (viewerContext.getShowShortNotation()) {
				sb.append( interval.asShortNotation());
			}
			else {
				sb.append( interval.asString());
			}
			Styler styler = stylerForVersion( v);
			sequences.add( new StyledTextSequenceData(cV, sb.length(), styler));
			
			// need to attach functional data here
			// scope, classifier:type, optional
			if (backingDependency != null && viewerContext.getShowDependencies()) {
				int bD = sb.length();
				sb.append(" "); // spacing after the version
				if (backingDependency.getOptional()) {
					sb.append("optional");
					sb.append(" "); // spacing after optional 
				}				
				sb.append( backingDependency.getScope());
				sb.append(" "); // spacing after scope
				String classifier = backingDependency.getClassifier();
				String type = backingDependency.getType();
				if (type == null)
					type = "jar";
				if (classifier != null) {
					sb.append( " " + classifier + ":" + type);
				}
				else {
					sb.append( " " + type);
				}
				sequences.add( new StyledTextSequenceData(bD, sb.length(), dependencyDetailStyler));
				
			}
			 
			if (artifact != null) {
				int cD = sb.length();
				sb.append( INNER_DEPENDENCY);
				sequences.add( new StyledTextSequenceData(cD, sb.length(), dashStyler));
			}
		}
		
		// solution identification
		if (artifact != null) {
			if (viewerContext.getShowGroupIds()) {
				String sgId = artifact.getGroupId();
				if (gId == null || !sgId.equals( gId)) {
					int cS = sb.length();
					sb.append( sgId);
					sb.append( " ");		
					int cE = sb.length();
					sequences.add( new StyledTextSequenceData(cS, cE, groupStyler));
				}
			}
			
			String saId = artifact.getArtifactId();
			if (aId == null || !saId.equals( aId)) {
				int cS = sb.length();
				sb.append( saId);
				int cE = sb.length();
				if (node == null || !isObsolete) {
					sequences.add( new StyledTextSequenceData(cS, cE, idStyler));
				}
				else {				
					sequences.add( new StyledTextSequenceData(cS, cE, purgedIdStyler));
				}			
			}
			int cS = sb.length();
			sb.append( " ");
			sb.append( artifact.getVersion());
			int cE = sb.length();
			Styler styler = stylerForVersion(artifact.getVersion());
			sequences.add( new StyledTextSequenceData(cS, cE, styler));					
		}
			
		String txt = sb.toString();					
		return Pair.of(txt, sequences);
	}
	

	@Override
	public void update(ViewerCell arg0) {		
	}
	
	@Override
	public void dispose() {					
		super.dispose();
	}
	
	

}
