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
package com.braintribe.model.access.smood.distributed;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.access.BasicAccessAdapter;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.smoodstorage.BufferedManipulation;
import com.braintribe.model.smoodstorage.JavaClass;
import com.braintribe.model.smoodstorage.SmoodStorage;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.LongIdGenerator;

public class FilebasedStorage extends BasicAccessAdapter {

	protected File baseDirectory = null;


	@Override
	protected void save(AdapterManipulationReport context) throws ModelAccessException {
		Set<GenericEntity> createdEntities = context.getCreatedEntities();
		if (createdEntities != null) {
			for (GenericEntity ge : createdEntities) {
				if (ge instanceof StandardIdentifiable) {
					StandardIdentifiable si = (StandardIdentifiable) ge;
					if (si.getId() == null) {
						si.setId(LongIdGenerator.provideLongId());
					}
				}
				if (ge instanceof BufferedManipulation) {
					this.saveNewBufferedManipulation((BufferedManipulation) ge);
				} else if (ge instanceof JavaClass) {
					this.saveNewJavaClass((JavaClass) ge);
				} else if (ge instanceof SmoodStorage) {
					this.saveNewSmoodStorage((SmoodStorage) ge);
				}
			}
		}
		Set<GenericEntity> updatedEntities = context.getUpdatedEntities();
		if (updatedEntities != null) {
			for (GenericEntity ge : updatedEntities) {
				if (ge instanceof BufferedManipulation) {
					this.saveNewBufferedManipulation((BufferedManipulation) ge);
				} else if (ge instanceof JavaClass) {
					this.saveNewJavaClass((JavaClass) ge);
				} else if (ge instanceof SmoodStorage) {
					this.saveNewSmoodStorage((SmoodStorage) ge);
				}
			}
		}
		Set<GenericEntity> deletedEntities = context.getDeletedEntities();
		if (deletedEntities != null) {
			for (GenericEntity ge : deletedEntities) {
				if (ge instanceof BufferedManipulation) {
					this.deleteBufferedManipulation((BufferedManipulation) ge);
				} else if (ge instanceof JavaClass) {
					this.deleteJavaClass((JavaClass) ge);
				} else if (ge instanceof SmoodStorage) {
					this.deleteSmoodStorage((SmoodStorage) ge);
				}
			}
		}
		//logger.info("Save not implemented in BasicAccessAdapter. Override this method to persist the changes.");
	}

	protected void deleteSmoodStorage(SmoodStorage ge) throws ModelAccessException {
		String accessId = ge.getAccessId();
		String folderName = "access_"+accessId;
		folderName = FileTools.normalizeFilename(folderName, ' ');
		File accessFolder = new File(this.baseDirectory, folderName);

		if (!accessFolder.exists()) {
			return;
		}

		int seqId = ge.getSequenceNumber();
		File seqFolder = new File(accessFolder, "seqId_"+seqId);
		if (!seqFolder.exists()) {
			return;
		}
		try {
			FileTools.deleteDirectoryRecursively(seqFolder);
		} catch (Exception e) {
			throw new ModelAccessException("Could not delete smood storage "+ge.getId(), e);
		}
	}

	protected void deleteJavaClass(JavaClass ge) throws ModelAccessException {
		File javaClassFolder = new File(this.baseDirectory, "javaClasses");
		if (!javaClassFolder.exists()) {
			return;
		}
		String qualifiedName = ge.getQualifiedName();
		File qnFolder = new File(javaClassFolder, qualifiedName);
		if (!qnFolder.exists()) {
			return;
		}
		int seqId = ge.getSequenceNumber();
		File seqFolder = new File(qnFolder, "seqId_"+seqId);
		if (!seqFolder.exists()) {
			return;
		}
		try {
			FileTools.deleteDirectoryRecursively(seqFolder);
		} catch (Exception e) {
			throw new ModelAccessException("Could not delete java class "+ge.getId(), e);
		}
	}

	protected void deleteBufferedManipulation(BufferedManipulation ge) throws ModelAccessException {
		File buffersFolder = new File(this.baseDirectory, "bufferedManipulations");
		if (!buffersFolder.exists()) {
			return;
		}
		int seqId = ge.getSequenceNumber();
		File seqFolder = new File(buffersFolder, "seqId_"+seqId);
		if (!seqFolder.exists()) {
			return;
		}
		try {
			FileTools.deleteDirectoryRecursively(seqFolder);
		} catch (Exception e) {
			throw new ModelAccessException("Could not delete buffered manipulation "+ge.getId(), e);
		}
	}

	protected void saveNewSmoodStorage(SmoodStorage ge) throws ModelAccessException {
		String accessId = ge.getAccessId();
		String folderName = "access_"+accessId;
		folderName = FileTools.normalizeFilename(folderName, ' ');
		File accessFolder = new File(this.baseDirectory, folderName);

		if (!accessFolder.exists()) {
			accessFolder.mkdirs();
			try {
				IOTools.spit(new File(accessFolder, "accessid.txt"), accessId, "UTF-8", false);
			} catch (Exception e) {
				throw new ModelAccessException("Could not create accessid.txt", e);
			}
		}

		int seqId = ge.getSequenceNumber();
		File seqFolder = new File(accessFolder, "seqId_"+seqId);
		if (!seqFolder.exists()) {
			seqFolder.mkdirs();
		}

		try {
			String data = ge.getEncodedData();
			if (data == null) {
				data = "_null_";
			}
			IOTools.spit(new File(seqFolder, "encodedData_"+ge.getId()), data, "UTF-8", false);
		} catch(Exception e) {
			throw new ModelAccessException("Could not write encoded data.", e);
		}

		Set<BufferedManipulation> bmSet = ge.getBufferedManipulations();
		if (bmSet != null) {
			File bmFolder = new File(seqFolder, "buffers");
			if (!bmFolder.exists()) {
				bmFolder.mkdirs();
			}

			for (BufferedManipulation bm : bmSet) {

				File bmFile = new File(bmFolder, "bmref_"+bm.getId());
				if (!bmFile.exists()) {
					try {
						bmFile.createNewFile();
					} catch (Exception e) {
						throw new ModelAccessException("Could not create reference to "+bm.getId(), e);
					}
				}

			}
		}
		Set<JavaClass> javaClassSet = ge.getClassDependencies();
		if (javaClassSet != null) {
			File jcFolder = new File(seqFolder, "deps");
			if (!jcFolder.exists()) {
				jcFolder.mkdirs();
			}

			for (JavaClass jc : javaClassSet){

				File jcFile  = new File(jcFolder, "jcref_"+jc.getId());
				if (!jcFile.exists()) {
					try {
						jcFile.createNewFile();
					} catch (Exception e) {
						throw new ModelAccessException("Could not create reference to "+jc.getId(), e);
					}
				}
			}
		}
	}

	protected void saveNewJavaClass(JavaClass ge) throws ModelAccessException {
		File javaClassFolder = new File(this.baseDirectory, "javaClasses");
		if (!javaClassFolder.exists()) {
			javaClassFolder.mkdirs();
		}
		String qualifiedName = ge.getQualifiedName();
		File qnFolder = new File(javaClassFolder, qualifiedName);
		if (!qnFolder.exists()) {
			qnFolder.mkdirs();
		}
		int seqId = ge.getSequenceNumber();
		File seqFolder = new File(qnFolder, "seqId_"+seqId);
		if (!seqFolder.exists()) {
			seqFolder.mkdirs();
		}
		String filename = "jc_"+ge.getId()+"_"+ge.getMd5();
		try {
			IOTools.spit(new File(seqFolder, filename), ge.getClassData(), "UTF-8", false);
		} catch (Exception e) {
			throw new ModelAccessException("Could not write java class", e);
		}
	}

	protected void saveNewBufferedManipulation(BufferedManipulation ge) throws ModelAccessException {
		File buffersFolder = new File(this.baseDirectory, "bufferedManipulations");
		if (!buffersFolder.exists()) {
			buffersFolder.mkdirs();
		}
		int seqId = ge.getSequenceNumber();
		File seqFolder = new File(buffersFolder, "seqId_"+seqId);
		if (!seqFolder.exists()) {
			seqFolder.mkdirs();
		}
		String filename = "bm_"+ge.getId();
		try {
			IOTools.spit(new File(seqFolder, filename), ge.getEncodedManipulation(), "UTF-8", false);
		} catch (Exception e) {
			throw new ModelAccessException("Could not write buffered manipulation", e);
		}
	}

	@Override
	protected Collection<GenericEntity> loadPopulation() throws ModelAccessException {

		Set<SmoodStorage> smoodStorages = new HashSet<SmoodStorage>();
		Map<String,JavaClass> javaClasses = new HashMap<String,JavaClass>();
		Map<String,BufferedManipulation> bufferedManipulations = new HashMap<String,BufferedManipulation>();

		File javaClassFolder = new File(this.baseDirectory, "javaClasses");
		if (javaClassFolder.exists()) {
			this.loadJavaClasses(javaClassFolder, javaClasses);
		}
		File bufferedManipulationsFolder = new File(this.baseDirectory, "bufferedManipulations");
		if (bufferedManipulationsFolder.exists()) {
			this.loadBufferedManipulations(bufferedManipulationsFolder, bufferedManipulations);
		}

		File[] accessIdFolders = this.baseDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith("access_")) {
					return true;
				}
				return false;
			}
		});
		if (accessIdFolders != null) {
			for (File f : accessIdFolders) {
				this.loadAccessIdFolder(f, smoodStorages, javaClasses, bufferedManipulations);
			}
		}

		//GMF.createEnhancedSet() is no longer available
		//Collection<GenericEntity> result = GMF.createEnhancedSet(GenericEntity.class);
		Collection<GenericEntity> result = new HashSet<GenericEntity>();
		result.addAll(smoodStorages);
		result.addAll(javaClasses.values());
		result.addAll(bufferedManipulations.values());
		return result;
	}


	protected void loadAccessIdFolder(File accessIdFolder, Set<SmoodStorage> smoodStorages, Map<String,JavaClass> javaClasses, Map<String,BufferedManipulation> bufferedManipulations) throws ModelAccessException {

		File accessIdFile = new File(accessIdFolder, "accessid.txt");
		String accessId = null;
		try {
			accessId = IOTools.slurp(accessIdFile, "UTF-8").trim();
		} catch (Exception e) {
			throw new ModelAccessException("Could not read accessId from "+accessIdFile, e);
		}

		File[] sequenceFolders = accessIdFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if ((dir.isDirectory()) && (name.startsWith("seqId_"))) {
					return true;
				}
				return false;
			}
		});
		for (File seqFolder : sequenceFolders) {

			String folderName = seqFolder.getName();
			int idx = folderName.indexOf("_");
			int sequenceId = Integer.parseInt(folderName.substring(idx+1).trim());

			this.loadSmoodStorage(accessId, sequenceId, seqFolder, smoodStorages, javaClasses, bufferedManipulations);

		}
	}

	protected void loadSmoodStorage(String accessId, int sequenceId, File folder, Set<SmoodStorage> smoodStorages, Map<String,JavaClass> javaClasses, Map<String,BufferedManipulation> bufferedManipulations) throws ModelAccessException {

		File[] dataFiles = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith("encodedData_")) {
					return true;
				}
				return false;
			}
		});
		String encodedData = null;
		String id = null;
		int size = 0;

		if ((dataFiles != null) && (dataFiles.length > 0)) {
			File dataFile = dataFiles[0];
			try {
				encodedData = IOTools.slurp(dataFile, "UTF-8");
			} catch (Exception e) {
				throw new ModelAccessException("Could not load encoded data.", e);
			}
			if (encodedData.equals("_null_")) {
				encodedData = null;
			}

			String filename = dataFile.getName();
			int idx = filename.indexOf("_");
			id = filename.substring(idx+1);

			size = (int) dataFile.length();
		}

		SmoodStorage smoodStorage = SmoodStorage.T.create();
		smoodStorage.setAccessId(accessId);
		smoodStorage.setEncodedData(encodedData);
		smoodStorage.setId(id);
		smoodStorage.setSequenceNumber(sequenceId);
		smoodStorage.setSize(size);
		//Collection initialization is no longer needed, and GMF.createEnhancedSet() is no more.
		//smoodStorage.setBufferedManipulations(GMF.createEnhancedSet(BufferedManipulation.class));

		this.loadBufferedManipulationReferences(smoodStorage, folder, bufferedManipulations);
		this.loadJavaClassReferences(smoodStorage, folder, javaClasses);

		smoodStorages.add(smoodStorage);
	}

	protected void loadJavaClassReferences(SmoodStorage smoodStorage, File folder, Map<String, JavaClass> javaClasses) {

		File jcDepFolder = new File(folder, "deps");
		if (!jcDepFolder.exists()) {
			return;
		}

		File[] depFiles = jcDepFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith("jcref_")) {
					return true;
				}
				return false;
			}
		});

		if (depFiles != null) {

			//GMF.createEnhancedSet() is no longer available
			//Set<JavaClass> javaClassSet = GMF.createEnhancedSet(JavaClass.class);
			
			smoodStorage.setClassDependencies(new HashSet<JavaClass>());
			Set<JavaClass> javaClassSet = smoodStorage.getClassDependencies();
			
			for (File depFile : depFiles) {

				String filename = depFile.getName();
				int idx = filename.indexOf("_");
				long jcId = Long.parseLong(filename.substring(idx+1));

				JavaClass jc = javaClasses.get(""+jcId);
				if (jc != null) {
					javaClassSet.add(jc);
				}

			}

			//Unnecessary as javaClassSet was obtained through smoodStorage.getClassDependencies();
			//smoodStorage.setClassDependencies(javaClassSet);
		}
	}

	protected void loadBufferedManipulationReferences(SmoodStorage smoodStorage, File folder, Map<String,BufferedManipulation> bufferedManipulations) {

		File bufferFolder = new File(folder, "buffers");
		if (!bufferFolder.exists()) {
			return;
		}

		File[] refFiles = bufferFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith("bmref_")) {
					return true;
				}
				return false;
			}
		});

		if (refFiles != null) {

			//GMF.createEnhancedSet() is no longer available
			//Set<BufferedManipulation> bufferedManipulationSet = GMF.createEnhancedSet(BufferedManipulation.class);
			
			smoodStorage.setBufferedManipulations(new HashSet<BufferedManipulation>());
			Set<BufferedManipulation> bufferedManipulationSet = smoodStorage.getBufferedManipulations();
			
			for (File refFile : refFiles) {

				String filename = refFile.getName();
				int idx = filename.indexOf("_");
				long bmId = Long.parseLong(filename.substring(idx+1));

				BufferedManipulation bm = bufferedManipulations.get(""+bmId);
				if (bm != null) {
					bufferedManipulationSet.add(bm);
				}

			}

			//Unnecessary as bufferedManipulationSet was obtained through smoodStorage.getBufferedManipulations()
			//smoodStorage.setBufferedManipulations(bufferedManipulationSet);
		}

		int totalSize = 0;
		for (BufferedManipulation bm : smoodStorage.getBufferedManipulations()) {
			totalSize += bm.getSize();
		}
		smoodStorage.setBufferedManipulationsSize(totalSize);
	}



	protected void loadBufferedManipulations(File bufferedManipulationsFolder, Map<String, BufferedManipulation> bufferedManipulations) throws ModelAccessException {

		File[] seqFolders = bufferedManipulationsFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith("seqId_")) {
					return true;
				}
				return false;
			}
		});
		if (seqFolders != null) {
			for (File seqFolder : seqFolders) {

				String seqName = seqFolder.getName();
				int idx = seqName.indexOf("_");
				int seqId = Integer.parseInt(seqName.substring(idx+1));

				File[] dataFiles = seqFolder.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						if (name.startsWith("bm_")) {
							return true;
						}
						return false;
					}
				});

				if (dataFiles != null) {

					for (File dataFile : dataFiles) {

						String filename = dataFile.getName();
						idx = filename.indexOf("_");
						String id = filename.substring(idx+1);

						String manipBuffer = null;
						try {
							manipBuffer = IOTools.slurp(dataFile, "UTF-8");
						} catch (Exception e) {
							throw new ModelAccessException("Could not load java class file.", e);
						}

						BufferedManipulation bufferedManipulation = BufferedManipulation.T.create();
						bufferedManipulation.setEncodedManipulation(manipBuffer);
						bufferedManipulation.setId(id);
						bufferedManipulation.setSequenceNumber(seqId);
						bufferedManipulation.setSize((int) dataFile.length());

						bufferedManipulations.put(id, bufferedManipulation);
					}

				}
			}
		}
	}


	protected void loadJavaClasses(File javaClassFolder, Map<String,JavaClass> javaClasses) throws ModelAccessException {

		File[] jcFolders = javaClassFolder.listFiles();
		if (jcFolders != null) {
			for (File jcFolder : jcFolders) {

				String qualifiedName = jcFolder.getName();

				File[] seqFolders = jcFolder.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						if (name.startsWith("seqId_")) {
							return true;
						}
						return false;
					}
				});
				if (seqFolders != null) {
					for (File seqFolder : seqFolders) {

						String seqName = seqFolder.getName();
						int idx = seqName.indexOf("_");
						int seqId = Integer.parseInt(seqName.substring(idx+1));

						File[] dataFiles = seqFolder.listFiles(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name) {
								if (name.startsWith("jc_")) {
									return true;
								}
								return false;
							}
						});

						if (dataFiles != null) {

							for (File dataFile : dataFiles) {

								String filename = dataFile.getName();
								int idx1 = filename.indexOf("_");
								int idx2 = filename.lastIndexOf("_");
								String id = filename.substring(idx1+1, idx2);
								String md5 = filename.substring(idx2+1);

								String classData = null;
								try {
									classData = IOTools.slurp(dataFile, "UTF-8");
								} catch (Exception e) {
									throw new ModelAccessException("Could not load java class file.", e);
								}

								JavaClass javaClass = JavaClass.T.create();
								javaClass.setClassData(classData);
								javaClass.setId(id);
								javaClass.setMd5(md5);
								javaClass.setQualifiedName(qualifiedName);
								javaClass.setSequenceNumber(seqId);

								javaClasses.put(id, javaClass);
							}

						}
					}
				}
			}
		}

	}


	@Configurable
	@Required
	public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}
}
