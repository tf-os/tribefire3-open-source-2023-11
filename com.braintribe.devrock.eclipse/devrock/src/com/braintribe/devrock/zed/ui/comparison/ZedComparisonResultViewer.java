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
package com.braintribe.devrock.zed.ui.comparison;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IDisposable;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.DevrockDialog;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.devrock.zarathud.model.common.FingerPrintNode;
import com.braintribe.devrock.zarathud.model.common.Node;
import com.braintribe.devrock.zarathud.model.extraction.ExtractionNode;
import com.braintribe.devrock.zarathud.model.extraction.subs.ContainerNode;
import com.braintribe.devrock.zed.api.comparison.ComparisonIssueClassification;
import com.braintribe.devrock.zed.ui.ZedViewingContext;
import com.braintribe.devrock.zed.ui.transposer.ComparisonTransposer;
import com.braintribe.devrock.zed.ui.transposer.ExtractionTransposer;
import com.braintribe.devrock.zed.ui.transposer.HasContainerTokens;
import com.braintribe.devrock.zed.ui.transposer.ZedExtractionTransposingContext;
import com.braintribe.devrock.zed.ui.viewer.comparison.ComparisonViewer;
import com.braintribe.devrock.zed.ui.viewer.comparison.DetailRequestListener;
import com.braintribe.devrock.zed.ui.viewer.extraction.ExtractionViewer;
import com.braintribe.devrock.zed.ui.viewer.extraction.ZedExtractionViewerContext;
import com.braintribe.devrock.zed.ui.viewer.model.ViewLabelProvider;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ClassOrInterfaceEntity;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.findings.ComparisonIssueType;

public class ZedComparisonResultViewer extends DevrockDialog implements IDisposable, SelectionListener, DetailRequestListener, HasContainerTokens {
	private static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;	

	private final UiSupport uiSupport = DevrockPlugin.instance().uiSupport();
	private ZedComparisonViewerContext context;
	
	private ExtractionTransposer baseExtractionTransposer = new ExtractionTransposer();
	private ExtractionTransposer otherExtractionTransposer = new ExtractionTransposer();
	private ComparisonTransposer comparisonExtractionTransposer = new ComparisonTransposer();
	
	private ComparisonViewer comparisonViewer;
	private ExtractionViewer baseExtractionViewer;
	private ExtractionViewer otherExtractionViewer;
	
	private Button saveNotes;
	private Image saveNotesImage;

	private Button savePrints;
	private Image saveFingerprintsImage;

	private Button switchViewButton;
	private Image switchModeImage;
	private boolean fingerPrintMode = true;
	
	private YamlMarshaller marshaller = new YamlMarshaller();
	
	private ZedExtractionTransposingContext comparisonExtractionContext;
		
			
	public ZedComparisonResultViewer(Shell parentShell) {
		super(parentShell);
		setShellStyle( SHELL_STYLE);
		
		saveNotesImage = uiSupport.images().addImage("zed_cmp_save_notes", ZedComparisonResultViewer.class, "export_notes.png");
		saveFingerprintsImage = uiSupport.images().addImage("zed_cmp_save_fps", ZedComparisonResultViewer.class, "save_file.transparent.png");
		switchModeImage = uiSupport.images().addImage("zed_cmp_switchMode", ViewLabelProvider.class, "rebuild_index.png");
	}
	
	@Configurable @Required
	public void setContext(ZedComparisonViewerContext context) {
		this.context = context;
	}
	
	

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected Point getDrInitialSize() {
		return new Point( 900, 800);
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText("zed's comparison result");
		
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Artifact baseArtifact = context.getBaseArtifact();
		Artifact otherArtifact = context.getOtherArtifact();
		
		
		//String title = baseArtifact.getArtifactId() + "#" + baseArtifact.getVersion() + " vs " + otherArtifact.getArtifactId() + "#" + otherArtifact.getVersion();
		
		Composite composite = (Composite) super.createDialogArea(parent);							
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        layout.verticalSpacing=2;        
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
       
        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayout(layout);
        buttons.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
        
        
        Label spacer = new Label(buttons, SWT.NONE);
        spacer.setLayoutData(new GridData( SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
        
        switchViewButton = new Button( buttons, SWT.None);
        switchViewButton.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        switchViewButton.setImage(switchModeImage);
        switchViewButton.setToolTipText("changes the hierarchical order of the shown issues: centered on issues or on afflicted entities");
        switchViewButton.addSelectionListener( this);
        
        saveNotes = new Button(buttons, SWT.NONE);
        saveNotes.setImage(saveNotesImage);
        saveNotes.setLayoutData(new GridData( SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
        saveNotes.setToolTipText("Save the found issues as release notes");
        saveNotes.addSelectionListener(this);               
        // for now, it's deactivated
        saveNotes.setEnabled(false);
        
        savePrints = new Button(buttons, SWT.NONE);
        savePrints.setImage(saveFingerprintsImage);
        savePrints.setLayoutData(new GridData( SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
        savePrints.setToolTipText("Save the found issues as Fingerprints as YAML");
        savePrints.addSelectionListener(this);        

        comparisonExtractionContext = new ZedExtractionTransposingContext();
        comparisonExtractionContext.setDetailed(false);
        
        
        
        ZedExtractionTransposingContext extractionContext = new ZedExtractionTransposingContext();
        //
    	// comparison fingerprint data 
    	//
        comparisonViewer = new ComparisonViewer();
        Node fingerPrintNode = comparisonExtractionTransposer.transposeFingerPrintsAsTop( comparisonExtractionContext, context);                
    	comparisonViewer.setUiSupport(uiSupport);    	
    	comparisonViewer.setTopNode( fingerPrintNode);
    	comparisonViewer.setListener( this);
    	
    	String name1 = baseArtifact.toVersionedStringRepresentation();
    	String name2 = otherArtifact.toVersionedStringRepresentation();
    	
    	//SashForm sashForm = new SashForm(composite, SWT.VERTICAL);
	  
    	Composite comparisonViewerComposite = comparisonViewer.createControl( composite, "found issues comparing " + name1 + " to " + name2);
    	comparisonViewerComposite.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true, 4, 2));
    	    	    	    	    	    	    	    
        //
    	// extraction data : base
    	//
    	comparisonExtractionContext.setDetailed(true);
    	Node baseArtifactNode = baseExtractionTransposer.transposeArtifact(extractionContext, baseArtifact);
    	    	
    	    
    	
    	baseExtractionViewer = new ExtractionViewer( new ZedExtractionViewerContext());
    	baseExtractionViewer.setUiSupport(uiSupport);    	
    	baseExtractionViewer.setArtifactNode( baseArtifactNode);
	  
    	Composite baseModuleViewerComposite = baseExtractionViewer.createControl(composite, name1);
    	baseModuleViewerComposite.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true, 2, 3)); 
    	
    	//
    	
    	Node otherArtifactNode = otherExtractionTransposer.transposeArtifact(extractionContext, otherArtifact);
    	
    	otherExtractionViewer = new ExtractionViewer( new ZedExtractionViewerContext());
    	otherExtractionViewer.setUiSupport(uiSupport);    	
    	otherExtractionViewer.setArtifactNode( otherArtifactNode);
	  
    	Composite otherModuleViewerComposite = otherExtractionViewer.createControl( composite, name2);
    	otherModuleViewerComposite.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true, 2, 3));     	       
        
        return composite;
	}

	@Override
	public void acknowledgeIssueSelected(Node node) {
		if (node == null) {
			return;
		}
		if (node instanceof FingerPrintNode == false) {
			return;
		}
		
		FingerPrintNode selectedNode = (FingerPrintNode) node;
		
		// find out what to send as selection to the extraction viewers 
		
		// a) target (s) 
		// b) colours? base vs other? 
		
		FingerPrint fingerPrint = selectedNode.getFingerPrint();
		List<GenericEntity> data = fingerPrint.getComparisonIssueData();
		
		if (data.size() == 0) {
			
			Node baseNode = baseExtractionTransposer.getNodeOfEntity( fingerPrint.getEntitySource());
			if (baseNode != null) {
				baseExtractionViewer.select( (ExtractionNode) baseNode);
			}				
			
			Node otherNode = otherExtractionTransposer.getNodeOfEntity( fingerPrint.getEntityComparisonTarget());
			if (otherNode != null) {
				otherExtractionViewer.select((ExtractionNode) otherNode);
			}
		}
		else {
			List<ExtractionNode> nodesToSelectInBase = new ArrayList<>();
			List<ExtractionNode> nodesToSelectInOther = new ArrayList<>();
			List<GenericEntity> comparisonIssueData = fingerPrint.getComparisonIssueData();
			ComparisonIssueType cit = fingerPrint.getComparisonIssueType();
			
			boolean relatedToSurplus = ComparisonIssueClassification.isCollectionComparisonIssue( cit.name()) && ComparisonIssueClassification.isSurplus( cit.name());
			boolean relatedToMissing = ComparisonIssueClassification.isCollectionComparisonIssue( cit.name()) && !ComparisonIssueClassification.isSurplus( cit.name());
		
			for (GenericEntity ge : comparisonIssueData) {
				GenericEntity se;
				if (ge instanceof TypeReferenceEntity) {
					TypeReferenceEntity tre = (TypeReferenceEntity) ge;
					se = tre.getReferencedType();
				}
				else {
					se = ge;
				}
				
				boolean isAContainerTarget = se instanceof ClassOrInterfaceEntity;				
				
			
				Node node2 = baseExtractionTransposer.getNodeOfEntity(se);
				if (node2 != null && relatedToMissing && !isAContainerTarget) {
					nodesToSelectInBase.add( (ExtractionNode) node2);
				}
			
				Node node3 = otherExtractionTransposer.getNodeOfEntity(se);
				if (node3 != null && relatedToSurplus && !isAContainerTarget) {
					nodesToSelectInOther.add( (ExtractionNode) node3);
				}
							
			}
			if (nodesToSelectInBase.size() == 0) {
				Node baseNode = baseExtractionTransposer.getNodeOfEntity( fingerPrint.getEntitySource());
				if (baseNode != null) {
					ExtractionNode extractionNode = (ExtractionNode) baseNode;
					nodesToSelectInBase.add( extractionNode);
					// surplus stuff -> open the container node in the base (methods, exceptions et al)
					//if (relatedToSurplus) {
						Map<String,ContainerNode> nodes = extractionNode.getContainerNodes();
						System.out.println( nodes.keySet().stream().collect(Collectors.joining(",")));
						ContainerNode cn =  identifyNode(nodes, cit);
						if (cn != null)  {
							nodesToSelectInBase.add(cn);
						}
					//}					
				}
			}
			
			baseExtractionViewer.select(nodesToSelectInBase);
			
			if (nodesToSelectInOther.size() == 0) {
				Node otherNode = otherExtractionTransposer.getNodeOfEntity( fingerPrint.getEntityComparisonTarget());
				if (otherNode != null) {
					ExtractionNode extractionNode = (ExtractionNode) otherNode;
					nodesToSelectInOther.add( extractionNode);
					// other? 
					//if (releatedToMissing) {
						Map<String,ContainerNode> nodes = extractionNode.getContainerNodes();
						System.out.println( nodes.keySet().stream().collect(Collectors.joining(",")));
						ContainerNode cn =  identifyNode(nodes, cit);
						if (cn != null)  {
							nodesToSelectInOther.add(cn);
						}
					//}			
				}
			}
			// missing stuff -> open the container node in the other (methods, exceptions et al)
			otherExtractionViewer.select(nodesToSelectInOther);
			
			
		}				
	}
	
	private ContainerNode identifyNode( Map<String,ContainerNode> map, ComparisonIssueType cit) {
		switch (cit) {
		
		case missingAnnotations:
		case surplusAnnotations:
			return map.get( ANNOTATIONS);
		case missingEnumValues:
		case surplusEnumValues:
			return map.get( VALUES);
		case missingFields:
		case surplusFields:
			return map.get( FIELDS);
		case missingImplementedInterfaces:
		case surplusImplementedInterfaces:
			return map.get( IMPLEMENTED_INTERFACES);
		case missingImplementingClasses:
		case surplusImplementingClasses:
			return map.get( IMPLEMENTING_TYPES);
		case missingInCollection:
		case surplusInCollection:
			break;
		case missingMethodArguments:
		case surplusMethodArguments:
			return map.get( ARGUMENT_TYPES);		
		case missingMethodExceptions:
		case surplusMethodExceptions:
			return map.get(THROWN_EXCEPTIONS);
		case missingMethods:
		case surplusMethods:
			return map.get( METHODS);
		case missingSubInterfaces:
		case surplusSubInterfaces:
			break;			
		case missingSubTypes:
		case surplusSubTypes:
			return map.get( DERIVED_TYPES);
		case missingSuperInterfaces:		
		case surplusSuperInterfaces:
			return map.get( SUPER_INTERFACES);			
		default:
			break;
		
		}
		return null;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {}

	@Override
	public void widgetSelected(SelectionEvent arg0) {
		Widget widget = arg0.widget;
		
		if (widget == saveNotes) {
			// TODO : impl
		}
		else if (widget == savePrints) {
			store();
		}		
		else if (widget == switchViewButton) {
			Node node = null;
			if (fingerPrintMode) {
				fingerPrintMode = false;
				node = comparisonExtractionTransposer.transposeOwnersAsTop( comparisonExtractionContext, context);
			}
			else {
				fingerPrintMode = true;
				node = comparisonExtractionTransposer.transposeFingerPrintsAsTop( comparisonExtractionContext, context);
			}
			comparisonViewer.switchNode( node);
		}
	}
	
	/**
	 * stores the passed {@link ZedViewingContext} into a {@link File} selected by the user 
	 * @param context - the {@link ZedViewingContext} to store 
	 */
	public void store() {
		// select file 
		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());

		String preselected = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ZED_COMPARISON_DUMP_LAST_FILE, null);
		
		FileDialog fd = new FileDialog(shell, SWT.SAVE);
		if (preselected != null) {
			File lastFile = new File( preselected);								
			fd.setFileName(lastFile.getName());
			fd.setFilterPath( lastFile.getParent());
		}
		fd.setFilterExtensions( new String[] {"*.yaml"});
		
		String selectedFile = fd.open();
		
		if (selectedFile == null) {
			return;
		}
		
		File file = new File( selectedFile);
		// store using YAML
		try (OutputStream out = new FileOutputStream(file)) {
			marshaller.setWritePooled(true);
			marshaller.marshall(out, context.getFingerPrints());
		}
		catch (Exception ee) {
			DevrockPluginStatus status = new DevrockPluginStatus("Cannnot marshall current comparison data to File [" + file.getAbsolutePath() + "] ", ee);
			DevrockPlugin.instance().log(status);				
		}						
	}

	

}
