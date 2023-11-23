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
package com.braintribe.devrock.zed.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.devrock.zed.forensics.fingerprint.register.RatingRegistry;
import com.braintribe.zarathud.model.forensics.FingerPrintOrigin;
import com.braintribe.zarathud.model.storage.ViewingContextStorageContainer;

/**
 * class to store/load a {@link ZedViewingContext} as a persisted modelled data dump (currently YAML)
 * 
 * uses two storage locker slots : 
 * 	StorageLockerSlots.SLOT_ZED_VIEWER_LAST_FILE for the last full data file, 
 * 	StorageLockerSlots.SLOT_ZED_FP_CUSTOM_FILE for the last fingerprint file 
 * 
 * @author pit
 *
 */
public class ViewingContextStorageHandler {
	
	private YamlMarshaller marshaller;
	{
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	
	
	
	/**
	 * stores the passed {@link ZedViewingContext} into a {@link File} selected by the user 
	 * @param context - the {@link ZedViewingContext} to store 
	 */
	public void store( ZedViewingContext context) {
		// select file 
		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());

		String preselected = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ZED_VIEWER_LAST_FILE,null);
		
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
		boolean success = store (context, file);
		if (success) {
			DevrockPlugin.instance().storageLocker().setValue( StorageLockerSlots.SLOT_ZED_VIEWER_LAST_FILE, selectedFile);
		}
	}
	
	/**
	 * stores the passed the {@link ZedViewingContext} into the passed {@link File}
	 * @param context - the {@link ZedViewingContext}
	 * @param file - the {@link File}
	 * @return - true if it was able to successfully store the file, false otherwise 
	 */
	public boolean store( ZedViewingContext context, File file) {
		ViewingContextStorageContainer vcsc = ViewingContextStorageContainer.T.create();
		vcsc.setArtifact(context.getArtifact());
		vcsc.setAnalyzerProcessingReturnReason(context.getAnalyzerReturnReason());
		vcsc.setClasspathForensicsResult(context.getClasspathForensicsResult());
		vcsc.setDependencyForensicsResult(context.getDependencyForensicsResult());
		vcsc.setIssues( context.getIssues());
		vcsc.setModelForensicsResult(context.getModelForensicsResult());
		vcsc.setModuleForensicsResult(context.getModuleForensicsResult());
		vcsc.setWorstRating(context.getWorstRating());
		vcsc.setActiveRatings( context.getRatingRegistry().getCurrentRatings());
		
	
		// store using YAML
		try (OutputStream out = new FileOutputStream(file)) {
			marshaller.setWritePooled(true);
			marshaller.marshall(out, vcsc);
		}
		catch (Exception ee) {
			DevrockPluginStatus status = new DevrockPluginStatus("Cannnot marshall current analysis data to File [" + file.getAbsolutePath() + "] ", ee);
			DevrockPlugin.instance().log(status);
			return false;
		}
		return true;
	}
	
	/**
	 * loads the {@link ZedViewingContext} from a selected file 
	 * @return - the {@link ZedViewingContext} or null if it failed
	 */
	public ZedViewingContext load()  {
		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());

		String preselected = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ZED_VIEWER_LAST_FILE,null);
		
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		if (preselected != null) {
			File lastFile = new File( preselected);								
			fd.setFileName(lastFile.getName());
			fd.setFilterPath( lastFile.getParent());
		}
		fd.setFilterExtensions( new String[] {"*.yaml"});
		
		String selectedFile = fd.open();
		
		if (selectedFile == null) {
			return null;
		}
		
		File file = new File( selectedFile);
		if (!file.exists()) {
			DevrockPluginStatus status = new DevrockPluginStatus("File [" + file.getAbsolutePath() + "] doesn't exist", IStatus.ERROR);
			DevrockPlugin.instance().log(status);
			return null;
		}
		ZedViewingContext context =  load( file);
		if (context != null) {
			DevrockPlugin.instance().storageLocker().setValue( StorageLockerSlots.SLOT_ZED_VIEWER_LAST_FILE, selectedFile);
		}
		return context;
	}
	
	/**
	 * loads a {@link ZedViewingContext} from the passed {@link File}
	 * @param file - the {@link File} to load from 
	 * @return - the {@link ZedViewingContext} or null if it failed to load
	 */
	public ZedViewingContext load(File file ) {
				
		ViewingContextStorageContainer vcsc;
		try ( InputStream in = new FileInputStream( file)) {		
				Object obj = marshaller.unmarshall( in);
				vcsc = (ViewingContextStorageContainer) obj;
		} catch (Exception e) {		
			DevrockPluginStatus status = new DevrockPluginStatus("File [" + file.getAbsolutePath() + "] cannot be read", e);
			DevrockPlugin.instance().log(status);
			return null;
		}			
		
		
 
		// build ZedViewContext from it
		ZedViewingContext zvc = new ZedViewingContext();
		zvc.setAnalyzerReturnReason( vcsc.getAnalyzerProcessingReturnReason());
		zvc.setArtifact( vcsc.getArtifact());
		zvc.setClasspathForensicsResult( vcsc.getClasspathForensicsResult());
		zvc.setDependencyForensicsResult( vcsc.getDependencyForensicsResult());
		zvc.setModelForensicsResult( vcsc.getModelForensicsResult());
		zvc.setModuleForensicsResult( vcsc.getModuleForensicsResult());
		zvc.setWorstRating( vcsc.getWorstRating());
		
		// rating registry 
		RatingRegistry ratingRegistry = new RatingRegistry();
		ratingRegistry.addRatingOverrides( vcsc.getActiveRatings(), FingerPrintOrigin.CUSTOM);
		zvc.setRatingRegistry( ratingRegistry);
		
		
		return zvc;
	}

	/**
	 * stores the currently active ratings from the {@link ZedViewingContext} to a YAML file
	 * @param context - the {@link ZedViewingContext}
	 */
	public void storeRatings(ZedViewingContext context) {
		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());

		String preselected = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ZED_FP_CUSTOM_FILE,null);
		
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
		boolean success = storeRatings(context, file);
		if (success) {
			DevrockPlugin.instance().storageLocker().setValue( StorageLockerSlots.SLOT_ZED_FP_CUSTOM_FILE, selectedFile);
		}
	}
	
	public boolean storeRatings(ZedViewingContext context, File file) {
		try (OutputStream out = new FileOutputStream(file)) {
			marshaller.setWritePooled(false);
			marshaller.marshall(out, context.getRatingRegistry().getCurrentRatings());
			return true;
		}
		catch (Exception e) {
			DevrockPluginStatus status = new DevrockPluginStatus("File [" + file.getAbsolutePath() + "] cannot be written to", IStatus.ERROR);
			DevrockPlugin.instance().log(status);
			return false;
		}
		
	}
	
	

}
