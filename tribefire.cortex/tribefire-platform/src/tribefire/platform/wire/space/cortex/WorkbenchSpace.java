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
package tribefire.platform.wire.space.cortex;

import static com.braintribe.wire.api.util.Sets.set;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.SimpleIcon;
import com.braintribe.model.resource.source.FileUploadSource;
import com.braintribe.model.style.Color;
import com.braintribe.model.style.Font;
import com.braintribe.model.uitheme.UiTheme;
import com.braintribe.model.workbench.KnownWorkenchPerspective;
import com.braintribe.model.workbench.UiThemeTemplateSource;
import com.braintribe.model.workbench.instruction.AddFolderToPerspective;
import com.braintribe.model.workbench.instruction.CompoundInstruction;
import com.braintribe.model.workbench.instruction.EnsureFolders;
import com.braintribe.model.workbench.instruction.EnsurePerspectives;
import com.braintribe.model.workbench.instruction.UpdateFolder;
import com.braintribe.model.workbench.instruction.UpdateUiStyle;
import com.braintribe.model.workbench.instruction.WorkbenchInstruction;
import com.braintribe.utils.i18n.I18nTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.wire.space.MasterResourcesSpace;

@Managed
public class WorkbenchSpace implements WireSpace {

	
	@Import
	protected MasterResourcesSpace resources;

	protected static final Color color_black = color("#000000");
	protected static final Color color_white = color("#FFFFFF");
	protected static final Color color_blue = color("#0000FF");
	protected static final Color color_red = color("#FF0000");
	protected static final Color color_green = color("#008000");
	protected static final Color color_yellow = color("#FFFF00");
	protected static final Color color_gray = color("#808080");
	protected static final Color color_lightslategray = color("#778899");
	protected static final Color color_slategray = color("#708090");
	protected static final Color color_gainsboro = color("#DCDCDC");
	
	protected static final Font font_openSans_black = font("Open Sans",color_black);
	protected static final Font font_openSans_white = font("Open Sans",color_white);
	protected static final Font font_openSans_gray = font("Open Sans",color_gray);

	@Managed
	public List<WorkbenchInstruction> defaultInstructions() {
		List<WorkbenchInstruction> instructions = new ArrayList<>();
		instructions.add(ensurePerspectives());
		instructions.add(ensureSystemFolders());
		instructions.add(ensureUiStyle());
		instructions.add(updateSystemFoldersDisplayName());
		instructions.add(addEntryPointRooFolder());
		instructions.add(addActionbarFolders());
		instructions.add(addHeaderbarFolders());
		instructions.add(addGlobalActionbarFolders());
		instructions.add(addTabActionbarFolders());
		return instructions;
	}
	
	@Managed
	public List<WorkbenchInstruction> grayishBlueStyleInstructions() {
		List<WorkbenchInstruction> instructions = new ArrayList<>();
		instructions.add(updateHeaderBar());
		instructions.add(updateTabActionBar());
		instructions.add(updateGlobalActionBar());
		UiTheme uiThemeGrayishBlue = uiThemeGrayishBlue();
		instructions.add(updateUiStyle(uiThemeGrayishBlue));
		return instructions;
	}
	
	@Managed
	public List<WorkbenchInstruction> tribefireOrangeStyleInstructions() {
		List<WorkbenchInstruction> instructions = new ArrayList<>();
		instructions.add(clearHeaderBarIcon());
		instructions.add(clearTabActionBarIcons());
		instructions.add(clearGlobalActionBarIcons());
		instructions.add(updateUiStyle(null));
		return instructions;
	}
	
	@Managed	
	private WorkbenchInstruction ensureUiStyle() {
		UpdateUiStyle instruction = UpdateUiStyle.T.create();
		instruction.setEnsureWorkbenchConfiguration(true);
		return instruction;
	}
	
	private WorkbenchInstruction ensurePerspectives() {
		EnsurePerspectives ensure = EnsurePerspectives.T.create();
		return ensure;
	}

	private WorkbenchInstruction updateHeaderBar() {

		UpdateFolder updateFolder = UpdateFolder.T.create();
		updateFolder.setPath("headerbar/tb_Logo");
		updateFolder.setProperty("icon");
		updateFolder.setNewValue(icon("logo.png"));
		
		return updateFolder;
	}
	
	private WorkbenchInstruction clearHeaderBarIcon() {

		UpdateFolder updateFolder = UpdateFolder.T.create();
		updateFolder.setPath("headerbar/tb_Logo");
		updateFolder.setProperty("icon");
		updateFolder.setNewValue(null);
		
		return updateFolder;
	}
	private WorkbenchInstruction updateGlobalActionBar() {

		UpdateFolder updateFolderHomeNew = UpdateFolder.T.create();
		updateFolderHomeNew.setPath("global-actionbar/$new");
		updateFolderHomeNew.setProperty("icon");
		updateFolderHomeNew.setNewValue(icon("new-32.png"));

		UpdateFolder updateFolderUpload = UpdateFolder.T.create();
		updateFolderUpload.setPath("global-actionbar/$upload");
		updateFolderUpload.setProperty("icon");
		updateFolderUpload.setNewValue(icon("upload-32.png"));

		UpdateFolder updateFolderUndo = UpdateFolder.T.create();
		updateFolderUndo.setPath("global-actionbar/$undo");
		updateFolderUndo.setProperty("icon");
		updateFolderUndo.setNewValue(icon("undo-32.png"));

		UpdateFolder updateFolderRedo = UpdateFolder.T.create();
		updateFolderRedo.setPath("global-actionbar/$redo");
		updateFolderRedo.setProperty("icon");
		updateFolderRedo.setNewValue(icon("redo-32.png"));

		UpdateFolder updateFolderCommit = UpdateFolder.T.create();
		updateFolderCommit.setPath("global-actionbar/$commit");
		updateFolderCommit.setProperty("icon");
		updateFolderCommit.setNewValue(icon("commit-32.png"));

		
		List<WorkbenchInstruction> instructions = new ArrayList<>();
		instructions.add(updateFolderHomeNew);
		instructions.add(updateFolderUpload);
		instructions.add(updateFolderUndo);
		instructions.add(updateFolderRedo);
		instructions.add(updateFolderCommit);
		
		CompoundInstruction instruction = CompoundInstruction.T.create();
		instruction.setInstructions(instructions);
		
		return instruction;
	}

	private WorkbenchInstruction clearGlobalActionBarIcons() {

		UpdateFolder updateFolderHomeNew = UpdateFolder.T.create();
		updateFolderHomeNew.setPath("global-actionbar/$new");
		updateFolderHomeNew.setProperty("icon");
		updateFolderHomeNew.setNewValue(null);

		UpdateFolder updateFolderUpload = UpdateFolder.T.create();
		updateFolderUpload.setPath("global-actionbar/$upload");
		updateFolderUpload.setProperty("icon");
		updateFolderUpload.setNewValue(null);

		UpdateFolder updateFolderUndo = UpdateFolder.T.create();
		updateFolderUndo.setPath("global-actionbar/$undo");
		updateFolderUndo.setProperty("icon");
		updateFolderUndo.setNewValue(null);

		UpdateFolder updateFolderRedo = UpdateFolder.T.create();
		updateFolderRedo.setPath("global-actionbar/$redo");
		updateFolderRedo.setProperty("icon");
		updateFolderRedo.setNewValue(null);

		UpdateFolder updateFolderCommit = UpdateFolder.T.create();
		updateFolderCommit.setPath("global-actionbar/$commit");
		updateFolderCommit.setProperty("icon");
		updateFolderCommit.setNewValue(null);

		
		List<WorkbenchInstruction> instructions = new ArrayList<>();
		instructions.add(updateFolderHomeNew);
		instructions.add(updateFolderUpload);
		instructions.add(updateFolderUndo);
		instructions.add(updateFolderRedo);
		instructions.add(updateFolderCommit);
		
		CompoundInstruction instruction = CompoundInstruction.T.create();
		instruction.setInstructions(instructions);
		
		return instruction;
	}

	private WorkbenchInstruction updateTabActionBar() {

		UpdateFolder updateFolderHomeExplorer = UpdateFolder.T.create();
		updateFolderHomeExplorer.setPath("tab-actionbar/$explorer/$homeConstellation");
		updateFolderHomeExplorer.setProperty("icon");
		updateFolderHomeExplorer.setNewValue(icon("home-16.png"));

		UpdateFolder updateFolderChangesExplorer = UpdateFolder.T.create();
		updateFolderChangesExplorer.setPath("tab-actionbar/$explorer/$changesConstellation");
		updateFolderChangesExplorer.setProperty("icon");
		updateFolderChangesExplorer.setNewValue(icon("changes-16.png"));

		UpdateFolder updateFolderTransientChangesExplorer = UpdateFolder.T.create();
		updateFolderTransientChangesExplorer.setPath("tab-actionbar/$explorer/$transientChangesConstellation");
		updateFolderTransientChangesExplorer.setProperty("icon");
		updateFolderTransientChangesExplorer.setNewValue(icon("changes-16.png"));

		UpdateFolder updateFolderClipboardExplorer = UpdateFolder.T.create();
		updateFolderClipboardExplorer.setPath("tab-actionbar/$explorer/$clipboardConstellation");
		updateFolderClipboardExplorer.setProperty("icon");
		updateFolderClipboardExplorer.setNewValue(icon("clipboard-16.png"));

		UpdateFolder updateFolderNotificationsExplorer = UpdateFolder.T.create();
		updateFolderNotificationsExplorer.setPath("tab-actionbar/$explorer/$notificationsConstellation");
		updateFolderNotificationsExplorer.setProperty("icon");
		updateFolderNotificationsExplorer.setNewValue(icon("notification-16.png"));
		
		UpdateFolder updateFolderHomeSelection = UpdateFolder.T.create();
		updateFolderHomeSelection.setPath("tab-actionbar/$selection/$homeConstellation");
		updateFolderHomeSelection.setProperty("icon");
		updateFolderHomeSelection.setNewValue(icon("home-16.png"));

		UpdateFolder updateFolderChangesSelection = UpdateFolder.T.create();
		updateFolderChangesSelection.setPath("tab-actionbar/$selection/$changesConstellation");
		updateFolderChangesSelection.setProperty("icon");
		updateFolderChangesSelection.setNewValue(icon("changes-16.png"));

		UpdateFolder updateFolderTransientChangesSelection = UpdateFolder.T.create();
		updateFolderTransientChangesSelection.setPath("tab-actionbar/$selection/$transientChangesConstellation");
		updateFolderTransientChangesSelection.setProperty("icon");
		updateFolderTransientChangesSelection.setNewValue(icon("changes-16.png"));

		UpdateFolder updateFolderClipboardSelection = UpdateFolder.T.create();
		updateFolderClipboardSelection.setPath("tab-actionbar/$selection/$clipboardConstellation");
		updateFolderClipboardSelection.setProperty("icon");
		updateFolderClipboardSelection.setNewValue(icon("clipboard-16.png"));

		UpdateFolder updateFolderQuickAccssSelection = UpdateFolder.T.create();
		updateFolderQuickAccssSelection.setPath("tab-actionbar/$selection/$quickAccessConstellation");
		updateFolderQuickAccssSelection.setProperty("icon");
		updateFolderQuickAccssSelection.setNewValue(icon("magnifier-16.png"));

		List<WorkbenchInstruction> instructions = new ArrayList<>();
		instructions.add(updateFolderHomeExplorer);
		instructions.add(updateFolderChangesExplorer);
		instructions.add(updateFolderTransientChangesExplorer);
		instructions.add(updateFolderClipboardExplorer);
		instructions.add(updateFolderNotificationsExplorer);
		instructions.add(updateFolderHomeSelection);
		instructions.add(updateFolderChangesSelection);
		instructions.add(updateFolderTransientChangesSelection);
		instructions.add(updateFolderClipboardSelection);
		instructions.add(updateFolderQuickAccssSelection);
		
		
		CompoundInstruction instruction = CompoundInstruction.T.create();
		instruction.setInstructions(instructions);
		
		
		return instruction;
	}
	
	private WorkbenchInstruction clearTabActionBarIcons() {

		UpdateFolder updateFolderHomeExplorer = UpdateFolder.T.create();
		updateFolderHomeExplorer.setPath("tab-actionbar/$explorer/$homeConstellation");
		updateFolderHomeExplorer.setProperty("icon");
		updateFolderHomeExplorer.setNewValue(null);

		UpdateFolder updateFolderChangesExplorer = UpdateFolder.T.create();
		updateFolderChangesExplorer.setPath("tab-actionbar/$explorer/$changesConstellation");
		updateFolderChangesExplorer.setProperty("icon");
		updateFolderChangesExplorer.setNewValue(null);

		UpdateFolder updateFolderClipboardExplorer = UpdateFolder.T.create();
		updateFolderClipboardExplorer.setPath("tab-actionbar/$explorer/$clipboardConstellation");
		updateFolderClipboardExplorer.setProperty("icon");
		updateFolderClipboardExplorer.setNewValue(null);

		UpdateFolder updateFolderNotificationsExplorer = UpdateFolder.T.create();
		updateFolderNotificationsExplorer.setPath("tab-actionbar/$explorer/$notificationsConstellation");
		updateFolderNotificationsExplorer.setProperty("icon");
		updateFolderNotificationsExplorer.setNewValue(null);

		
		
		UpdateFolder updateFolderHomeSelection = UpdateFolder.T.create();
		updateFolderHomeSelection.setPath("tab-actionbar/$selection/$homeConstellation");
		updateFolderHomeSelection.setProperty("icon");
		updateFolderHomeSelection.setNewValue(null);

		UpdateFolder updateFolderChangesSelection = UpdateFolder.T.create();
		updateFolderChangesSelection.setPath("tab-actionbar/$selection/$changesConstellation");
		updateFolderChangesSelection.setProperty("icon");
		updateFolderChangesSelection.setNewValue(null);

		UpdateFolder updateFolderClipboardSelection = UpdateFolder.T.create();
		updateFolderClipboardSelection.setPath("tab-actionbar/$selection/$clipboardConstellation");
		updateFolderClipboardSelection.setProperty("icon");
		updateFolderClipboardSelection.setNewValue(null);

		UpdateFolder updateFolderQuickAccssSelection = UpdateFolder.T.create();
		updateFolderQuickAccssSelection.setPath("tab-actionbar/$selection/$quickAccessConstellation");
		updateFolderQuickAccssSelection.setProperty("icon");
		updateFolderQuickAccssSelection.setNewValue(null);

		UpdateFolder updateFolderExpertUiSelection = UpdateFolder.T.create();
		updateFolderExpertUiSelection.setPath("tab-actionbar/$selection/$expertUI");
		updateFolderExpertUiSelection.setProperty("icon");
		updateFolderExpertUiSelection.setNewValue(null);

		
		List<WorkbenchInstruction> instructions = new ArrayList<>();
		instructions.add(updateFolderHomeExplorer);
		instructions.add(updateFolderChangesExplorer);
		instructions.add(updateFolderClipboardExplorer);
		instructions.add(updateFolderNotificationsExplorer);
		instructions.add(updateFolderHomeSelection);
		instructions.add(updateFolderChangesSelection);
		instructions.add(updateFolderClipboardSelection);
		instructions.add(updateFolderQuickAccssSelection);
		instructions.add(updateFolderExpertUiSelection);
		
		
		CompoundInstruction instruction = CompoundInstruction.T.create();
		instruction.setInstructions(instructions);
		
		
		return instruction;
	}

	@Managed
	private WorkbenchInstruction addEntryPointRooFolder() {
		Folder folderToAdd = Folder.T.create();
		folderToAdd.setName(KnownWorkenchPerspective.root.toString());
		folderToAdd.setDisplayName(I18nTools.createLs("Entry Points"));
		
		AddFolderToPerspective addFolder = AddFolderToPerspective.T.create();
		addFolder.setPerspectiveName(KnownWorkenchPerspective.root.toString());
		addFolder.setOverrideExisting(false);
		addFolder.setUseExistingFolder(true);
		addFolder.setFolderToAdd(folderToAdd);
		
		return addFolder;
	}

	
	@Managed
	private WorkbenchInstruction addActionbarFolders() {
		Folder folderToAdd = Folder.T.create();
		folderToAdd.setName(KnownWorkenchPerspective.actionBar.toString());
		folderToAdd.setDisplayName(I18nTools.createLs("Action Bar"));
		AddFolderToPerspective addFolder = AddFolderToPerspective.T.create();
		addFolder.setPerspectiveName(KnownWorkenchPerspective.actionBar.toString());
		addFolder.setOverrideExisting(false);
		addFolder.setUseExistingFolder(true);
		addFolder.setFolderToAdd(folderToAdd);
		
		return addFolder;
	}

	@Managed
	private WorkbenchInstruction addHeaderbarFolders() {
		Folder folderToAdd = Folder.T.create();
		folderToAdd.setName(KnownWorkenchPerspective.headerBar.toString());
		folderToAdd.setDisplayName(I18nTools.createLs("Header Bar"));
		
		AddFolderToPerspective addFolder = AddFolderToPerspective.T.create();
		addFolder.setPerspectiveName(KnownWorkenchPerspective.headerBar.toString());
		addFolder.setOverrideExisting(false);
		addFolder.setUseExistingFolder(true);
		addFolder.setFolderToAdd(folderToAdd);
		
		return addFolder;
		
	}
	
	@Managed
	private WorkbenchInstruction addGlobalActionbarFolders() {
		Folder folderToAdd = Folder.T.create();
		folderToAdd.setName(KnownWorkenchPerspective.globalActionBar.toString());
		folderToAdd.setDisplayName(I18nTools.createLs("Global Action Bar"));
		
		AddFolderToPerspective addFolder = AddFolderToPerspective.T.create();
		addFolder.setPerspectiveName(KnownWorkenchPerspective.globalActionBar.toString());
		addFolder.setOverrideExisting(false);
		addFolder.setUseExistingFolder(true);
		addFolder.setFolderToAdd(folderToAdd);
		
		return addFolder;
	}

	@Managed
	private WorkbenchInstruction addTabActionbarFolders() {
		Folder folderToAdd = Folder.T.create();
		folderToAdd.setName(KnownWorkenchPerspective.tabActionBar.toString());
		folderToAdd.setDisplayName(I18nTools.createLs("Tab Action Bar"));
		
		AddFolderToPerspective addFolder = AddFolderToPerspective.T.create();
		addFolder.setPerspectiveName(KnownWorkenchPerspective.tabActionBar.toString());
		addFolder.setOverrideExisting(false);
		addFolder.setUseExistingFolder(true);
		addFolder.setFolderToAdd(folderToAdd);
		
		return addFolder;
	}
	
	private UpdateFolder updateFolder(String path, String propertyName, Icon newValue, boolean overrideExisting) {

		UpdateFolder updateFolder = UpdateFolder.T.create();
		updateFolder.setPath(path);
		updateFolder.setProperty(propertyName);
		updateFolder.setNewValue(newValue);
		updateFolder.setOverrideExisting(overrideExisting);
		
		return updateFolder;

	}
	
	private UpdateFolder updateFolder(String path, String propertyName, String newValue, boolean overrideExisting) {
		
		UpdateFolder updateFolder = UpdateFolder.T.create();
		updateFolder.setPath(path);
		updateFolder.setProperty(propertyName);
		updateFolder.setNewValue(I18nTools.createLs(newValue));
		updateFolder.setOverrideExisting(overrideExisting);
		
		return updateFolder;
		
	}
	
	private WorkbenchInstruction updateSystemFoldersDisplayName() {
		
		List<WorkbenchInstruction> instructions = new ArrayList<>();
		instructions.add(updateFolder("actionbar", "displayName", "Action Bar", true));
		instructions.add(updateFolder("headerbar", "displayName", "Header Bar", true));
		instructions.add(updateFolder("tab-actionbar", "displayName", "Tab Action Bar", true));
		instructions.add(updateFolder("global-actionbar", "displayName", "Global Action Bar", true));
		
		
		instructions.add(updateFolder("actionbar/$workWithEntity", "displayName", "Open", true));
		instructions.add(updateFolder("actionbar/$gimaOpener", "displayName", "Edit", true));
		instructions.add(updateFolder("actionbar/$deleteEntity", "displayName", "Delete", true));
		instructions.add(updateFolder("actionbar/$changeInstance", "displayName", "Assign", true));
		instructions.add(updateFolder("actionbar/$clearEntityToNull", "displayName", "Remove", true));
		
		instructions.add(updateFolder("actionbar/$addToCollection", "displayName", "Add", true));
		instructions.add(updateFolder("actionbar/$insertBeforeToList", "displayName", "Insert Before", true));
		instructions.add(updateFolder("actionbar/$removeFromCollection", "displayName", "Remove", true));
		instructions.add(updateFolder("actionbar/$clearCollection", "displayName", "Clear", true));
		instructions.add(updateFolder("actionbar/$exchangeContentView", "displayName", "View", true));
		instructions.add(updateFolder("actionbar/$addToClipboard", "displayName", "Add to Clipboard", true));
		instructions.add(updateFolder("actionbar/$refreshEntities", "displayName", "Refresh", true));
		instructions.add(updateFolder("actionbar/$ResourceDownload", "displayName", "Download", true));
		instructions.add(updateFolder("actionbar/$executeServiceRequest", "displayName", "Execute", true));
		instructions.add(updateFolder("actionbar/$executeServiceRequest", "icon", icon("run-16.png","run-32.png"), true));
		
		CompoundInstruction ci = CompoundInstruction.T.create();
		ci.setInstructions(instructions);
		
		return ci;
		
	}

	private WorkbenchInstruction ensureSystemFolders() {

		List<String> paths = new ArrayList<>();
		
		// System folders in Actionbar	
		paths.add("actionbar");
		paths.add("actionbar/$exchangeContentView");
		paths.add("actionbar/$workWithEntity");
		paths.add("actionbar/$gimaOpener");
		paths.add("actionbar/$deleteEntity");
		paths.add("actionbar/$changeInstance");
		paths.add("actionbar/$clearEntityToNull");
		paths.add("actionbar/$addToCollection");
		paths.add("actionbar/$insertBeforeToList");
		paths.add("actionbar/$removeFromCollection");
		paths.add("actionbar/$clearCollection");
		paths.add("actionbar/$refreshEntities");
		paths.add("actionbar/$ResourceDownload");
		paths.add("actionbar/$executeServiceRequest");
		paths.add("actionbar/$addToClipboard");

		 
		// System folders in Headerbar
		paths.add("headerbar");
		paths.add("headerbar/tb_Logo");
		
		paths.add("headerbar/$quickAccess-slot");
		paths.add("headerbar/$globalState-slot");
		
		paths.add("headerbar/$settingsMenu");
		//paths.add("headerbar/$settingsMenu/$switchTo");
		paths.add("headerbar/$settingsMenu/$reloadSession");
		paths.add("headerbar/$settingsMenu/$showSettings");
		paths.add("headerbar/$settingsMenu/$uiTheme");
		//paths.add("headerbar/$settingsMenu/$persistActionGroup");
		//paths.add("headerbar/$settingsMenu/$persistHeaderBar");
		//paths.add("headerbar/$settingsMenu/$automaticFoldersImporter");
		paths.add("headerbar/$settingsMenu/$showAbout");
		
		paths.add("headerbar/$userMenu");
		paths.add("headerbar/$userMenu/$showUserProfile");
		paths.add("headerbar/$userMenu/$showLogout");

		
		// System folders in Tab-Bar
		paths.add("tab-actionbar");
		paths.add("tab-actionbar/$explorer");
		paths.add("tab-actionbar/$explorer/$homeConstellation");
		paths.add("tab-actionbar/$explorer/$changesConstellation");
		paths.add("tab-actionbar/$explorer/$transientChangesConstellation");
		paths.add("tab-actionbar/$explorer/$clipboardConstellation");
		paths.add("tab-actionbar/$explorer/$notificationsConstellation");
		
		paths.add("tab-actionbar/$selection");
		paths.add("tab-actionbar/$selection/$homeConstellation");
		paths.add("tab-actionbar/$selection/$changesConstellation");
		paths.add("tab-actionbar/$selection/$transientChangesConstellation");
		paths.add("tab-actionbar/$selection/$clipboardConstellation");
		paths.add("tab-actionbar/$selection/$quickAccessConstellation");
		paths.add("tab-actionbar/$selection/$expertUI");
		
		// System folders in GlobalAction-Bar
		paths.add("global-actionbar");
		paths.add("global-actionbar/$new");
		paths.add("global-actionbar/$dualSectionButtons");
		paths.add("global-actionbar/$upload");
		paths.add("global-actionbar/$undo");
		paths.add("global-actionbar/$redo");
		paths.add("global-actionbar/$commit");
		
		
		EnsureFolders ensure = EnsureFolders.T.create();
		ensure.setWithDisplayName(true);
		ensure.setPaths(paths);
		return ensure;
	}


	//##################### UI Style ###################
	
	
	
	private WorkbenchInstruction updateUiStyle(UiTheme uiTheme) {
		
		UpdateUiStyle updateUiStyle = UpdateUiStyle.T.create();
		updateUiStyle.setOverrideExisting(true);
		
		Resource dynamicStylesheet = Resource.T.create();
		dynamicStylesheet.setName("dynamic-stylesheet");
		dynamicStylesheet.setMimeType("text/css");
		dynamicStylesheet.setCreator("cortex");
		dynamicStylesheet.setCreated(new Date());
		
		UiThemeTemplateSource dynamicStylesheetSource = UiThemeTemplateSource.T.create();
		dynamicStylesheetSource.setTemplate(resource("explorer-style-template.css"));
		dynamicStylesheetSource.setUiTheme(uiTheme);
		dynamicStylesheet.setResourceSource(dynamicStylesheetSource);
		
		updateUiStyle.setStylesheet(dynamicStylesheet);
		updateUiStyle.setColorsToEnsure(
					set(color_black,color_white,color_blue,color_red,color_green,color_yellow,color_gray)
				);
		

		return updateUiStyle;
	}

	
	private UiTheme uiThemeGrayishBlue() {
		
		UiTheme theme = UiTheme.T.create();
		theme.setSelectColor(color_lightslategray);
		theme.setSelectInactiveColor(color_gainsboro);
		theme.setHooverColor(color_slategray);
		theme.setBasicFont(font_openSans_black);
		theme.setCaptionFont(font_openSans_gray);
		theme.setHeaderFont(font_openSans_gray);
		theme.setMenuFont(font_openSans_black);
		theme.setTabFont(font_openSans_white);
		theme.setTetherFont(font_openSans_black);
		theme.setGlobalId("tribefire.demo.cartridge.wb.uiTheme.grayish");
				
		return theme;
	}
	
	
	protected AdaptiveIcon icon (String... names) {
		AdaptiveIcon icon = AdaptiveIcon.T.create();
		icon.setName(names[0]+" Icon");
		for (String name : names) {
			Resource rep = resource(name);
			icon.getRepresentations().add(rep);
		}
		return icon;
	}
	
	protected SimpleIcon icon(String name) {
		SimpleIcon newIcon = SimpleIcon.T.create();
		newIcon.setName(name+" Icon");
		newIcon.setImage(resource(name));
		return newIcon;
	}
	
	protected Resource resource(String filePath) {

		Path path = null;
		try {
			path = resources.webInf("Resources/Icons/workbench/"+filePath).asPath();
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to resolve " + filePath);
		}

		FileUploadSource source = FileUploadSource.T.create();
		source.setLocalFilePath(path.toString());

		Resource resource = Resource.T.create();
		resource.setResourceSource(source);
		return resource;

	}
	
	
	protected static Color color (String hex) {
		String cut = cutHex(hex);
		Color c = Color.T.create();
		c.setRed(hexToR(cut));
		c.setGreen(hexToG(cut));
		c.setBlue(hexToB(cut));
		return c;		
	}

	private static int hexToR(String h) {
		return Integer.parseInt(h.substring(0,2),16);
	}
	private static  int hexToG(String h) {
		return Integer.parseInt(h.substring(2,4),16);
	}
	private static int hexToB(String h) {
		return Integer.parseInt(h.substring(4,6),16);
	}
	
	private static String cutHex(String h) {
		return (h.startsWith("#")) ? h.substring(1,7): h;
	}
	
	protected static Font font(String family, Color color) {
		Font font = Font.T.create();
		font.setFamily(family);
		font.setColor(color);
		return font;
	}
}
