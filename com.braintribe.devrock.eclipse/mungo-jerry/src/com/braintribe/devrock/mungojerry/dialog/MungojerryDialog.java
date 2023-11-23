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
package com.braintribe.devrock.mungojerry.dialog;

import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.braintribe.build.gwt.ModuleCheckProtocol;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mungojerry.dialog.experts.ModuleDeclarationWriter;
import com.braintribe.devrock.mungojerry.dialog.tab.AnalysisController;
import com.braintribe.devrock.mungojerry.dialog.tab.GwtModuleScannerErrorTab;
import com.braintribe.devrock.mungojerry.dialog.tab.GwtModuleScannerMasterTab;
import com.braintribe.devrock.mungojerry.dialog.tab.GwtModuleScannerModulesTab;
import com.braintribe.devrock.mungojerry.dialog.tab.ParentPage;

/**
 * @author pit
 *
 */
public class MungojerryDialog extends Dialog implements ParentPage {
	private Font bigFont;
		
	protected CTabFolder detailTabFolder = null;
	private Display display;
	private AnalysisController analyisController;
	private ModuleDeclarationWriter moduleDeclarationWriter;

	private GwtModuleScannerMasterTab masterTab;
	private GwtModuleScannerModulesTab modulesTab;
	private GwtModuleScannerErrorTab errorTab;
	
	private Image infoImage;
	private Image errorImage;
	private Image blankImage;
	
	private Text message;
	private Label messageLabel;
	
	private Button saveButton;
	
	public MungojerryDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
		display = parentShell.getDisplay();
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( MungojerryDialog.class, "information.png");
		infoImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( MungojerryDialog.class, "error.gif");
		errorImage = imageDescriptor.createImage();
		
		blankImage = createTransparentImage( parentShell.getDisplay(), 16, 16);
	}
		
	@Configurable @Required
	public void setAnalyisController(AnalysisController analyisController) {
		this.analyisController = analyisController;
	}
	
	@Required @Configurable
	public void setModuleDeclarationWriter(ModuleDeclarationWriter moduleDeclarationWriter) {
		this.moduleDeclarationWriter = moduleDeclarationWriter;
	}
		
	@Override
	protected Control createDialogArea(Composite parent) {
		Font initialFont = parent.getFont();
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( getShell().getDisplay(), fontDataBig);
		
		Composite composite = (Composite) super.createDialogArea(parent);		
		
		int nColumns= 5;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        layout.verticalSpacing=2;        
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
        
        // tabs 
        Composite masterComposite = new Composite(composite, SWT.NONE);
        masterComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2,1));
        masterComposite.setLayout( layout);
        
        Label label = new Label( masterComposite, SWT.NONE);
        label.setText( "Declarations");
        label.setFont(bigFont);
        label.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 5,1));
              
		masterTab = new GwtModuleScannerMasterTab( display);
		Composite masterTabComposite = masterTab.createControl( masterComposite);		
		masterTab.setParentPage( this);
		masterTab.setAnalysisController(analyisController);		
		masterTabComposite.setToolTipText("Shows modules declared in this artifact");
		masterTabComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true));
        
        Composite detailComposite = new Composite(composite, SWT.NONE);
        detailComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 3,1));
        detailComposite.setLayout( layout);
        
        label = new Label( detailComposite, SWT.NONE);
        label.setText( "Detail");
        label.setFont(bigFont);
        label.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 5,1));
        
        // tab folder
    	detailTabFolder = new CTabFolder( detailComposite, SWT.NONE);
		detailTabFolder.setBackground( parent.getBackground());
		detailTabFolder.setSimple( false);		
		detailTabFolder.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true));
		
		CTabItem detailItem = new CTabItem(detailTabFolder, SWT.NONE);
		detailItem.setText("Dependencies");
		modulesTab = new GwtModuleScannerModulesTab(display);
		Composite modulesComposite = modulesTab.createControl( detailTabFolder);
		detailItem.setControl(modulesComposite);
		modulesTab.setAnalysisController(analyisController);
		modulesTab.setParentPage( this);
		modulesComposite.setToolTipText("Shows inheritance details about the currently selected module");
		
		CTabItem errorItem = new CTabItem(detailTabFolder,  SWT.NONE);
		errorItem.setText("Problems");
		errorTab = new GwtModuleScannerErrorTab(display);
		Composite errorComposite = errorTab.createControl(detailTabFolder);
		errorItem.setControl(errorComposite);
		errorTab.setAnalysisController(analyisController);
		errorTab.setParentPage( this);
		errorComposite.setToolTipText("Shows problems found in the currently selected module");
		
		
		// msg
		Composite messageComposite = new Composite( composite, SWT.NONE);
		messageComposite.setLayout( layout);
		messageComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 5, 1));
		
		messageLabel = new Label( messageComposite, SWT.NONE);
		messageLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
		messageLabel.setImage( blankImage);
		
		message = new Text( messageComposite, SWT.NONE);
		message.setEnabled(false);
		message.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, true));
		message.setFont(bigFont);
		detailTabFolder.setSelection(0);
		
		masterTab.setup();
		
		
		
		return composite;	
	}
	
	static Image createTransparentImage(Display display, int width, int height) {
	    // allocate an image data
	    ImageData imData = new ImageData(width, height, 24, new PaletteData(0xff0000,0x00ff00, 0x0000ff));
	    imData.setAlpha(0, 0, 0); // just to force alpha array allocation with the right size
	    Arrays.fill(imData.alphaData, (byte) 0); // set whole image as transparent
	    // Initialize image from transparent image data
	    return new Image(display, imData);
	}
	
	
	
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		// Change parent layout data to fill the whole bar
		  parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		  saveButton = createButton(parent, IDialogConstants.NO_ID, "Save gwt file", true);
		  saveButton.addSelectionListener( new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent event) {				
					if (event.widget == saveButton ) {						
						ModuleCheckProtocol protocol = masterTab.getCurrentlySelectedProtocol();
						if (protocol != null) {
							String moduleName = protocol.getModuleName();
							moduleDeclarationWriter.save( moduleName);
						}
					}										
				}
				  
			});

		  // Create a spacer label
		  Label spacer = new Label(parent, SWT.NONE);
		  spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		  // Update layout of the parent composite to count the spacer
		  GridLayout layout = (GridLayout)parent.getLayout();
		  layout.numColumns++;
		  layout.makeColumnsEqualWidth = false;

		  createButton(parent, IDialogConstants.OK_ID,"OK", false);
		  createButton(parent, IDialogConstants.CANCEL_ID,"Close", false);	
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText( "Mungojerry GWT plugin");
		super.configureShell(newShell);
	}

	@Override
	protected Point getInitialSize() {	
		return new Point( 800, 600);
	}

	
	@Override
	public int open() {	
		return super.open();
	}

	@Override
	public boolean close() {
		errorImage.dispose();
		infoImage.dispose();
		blankImage.dispose();
		bigFont.dispose();
		masterTab.dispose();
		return super.close();
	}

	@Override
	public Button getSaveModuleButton() {
		return null;
	}

	@Override
	public void showErrors(ModuleCheckProtocol protocol) {
		errorTab.setup(protocol);
	}

	@Override
	public void showDependencies(String moduleName) {	
		modulesTab.setup(moduleName);
	}

	@Override
	public void setDescription(String description) {
		message.setText(description);
		messageLabel.setImage(infoImage);
	}

	@Override
	public void setErrorMessage(String message) {
		this.message.setText(message);
		this.messageLabel.setImage(errorImage);
	}
	
}
