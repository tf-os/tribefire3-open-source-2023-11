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
package tribefire.cortex.dcsa.analysis.impl;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaResourceBasedOperation;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.api.ResourceBuilder;
import com.braintribe.utils.FileTools;

import tribefire.cortex.model.api.dcsa.FillCortexDcsaSharedStorage;

/**
 * @author peter.gazdik
 */
public class CortexDcsaSsFiller {

	private Supplier<DcsaSharedStorage> sharedStorageSupplier;

	private Marshaller marshaller;
	private ResourceBuilder resourceBuilder;

	@Required
	public void setSharedStorageSupplier(Supplier<DcsaSharedStorage> sharedStorageSupplier) {
		this.sharedStorageSupplier = sharedStorageSupplier;
	}

	@Required
	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public void setResourceBuilder(ResourceBuilder resourceBuilder) {
		this.resourceBuilder = resourceBuilder;
	}

	public Boolean doIt(FillCortexDcsaSharedStorage request) {
		DcsaSharedStorage ss = sharedStorageSupplier.get();

		if (ss == null)
			return false;

		File folder = getInputFolder(request);
		doImportTo(ss, folder);

		return true;
	}

	private File getInputFolder(FillCortexDcsaSharedStorage request) {
		File file = new File(request.getSharedStorageDiagnosticFolder());
		if (!file.isDirectory())
			throw new IllegalArgumentException("Given sharedStorageDiagnosticFolder is not an existing folder: " + file.getAbsolutePath());

		return file;
	}

	private void doImportTo(DcsaSharedStorage ss, File folder) {
		List<SsFile> files = getSsFilesSorted(folder);

		Iterator<SsFile> it = files.iterator();

		while (it.hasNext()) {
			SsFile operationFile = it.next();
			CsaOperation csaOp = toCsaOp(operationFile.file);

			if (csaOp instanceof CsaResourceBasedOperation) {
				SsFile payloadFile = it.next();
				if (operationFile.index != payloadFile.index)
					throw new IllegalArgumentException("No resource found for operation file: " + operationFile.file.getName());

				attachResource((CsaResourceBasedOperation) csaOp, payloadFile.file);
			}

			ss.storeOperation("cortex", csaOp);
		}
	}

	/**
	 * We assume the input folder contains files like these:
	 * 
	 * <pre>
	 * 00001-APPEND_DATA_MANIPULATION.json
	 * 00001-APPEND_DATA_MANIPULATION-191120191541167c0d1d2fe9b6407da2.json
	 * 00002-STORE_RESOURCE.json
	 * 00002-STORE_RESOURCE-200121174743300ef0c9000c2148108a.json
	 * 00003-DELETE_RESOURCE.json
	 * </pre>
	 *
	 * As there is no guarantee about their order, we sort it ourselves.
	 */
	private List<SsFile> getSsFilesSorted(File folder) {
		List<File> files = asList(folder.listFiles());

		return files.stream() //
				.filter(this::isSharedStorageFile) //
				.map(SsFile::new) //
				.sorted() //
				.collect(Collectors.toList());
	}

	private boolean isSharedStorageFile(File file) {
		return file.getName().matches("[\\d]+-.*");
	}
	
	static class SsFile implements Comparable<SsFile> {

		final int index;
		final File file;

		public SsFile(File file) {
			this.file = file;
			this.index = parseIndex(file);
		}

		private int parseIndex(File file) {
			String name = file.getName();
			int i = name.indexOf("-");
			if (i < 0)
				throw new IllegalArgumentException("File name doesn't match the pattern '${number}-${anything}: " + name);

			return Integer.parseInt(name.substring(0, i));
		}

		@Override
		public int compareTo(SsFile o) {
			if (this == o)
				return 0;

			if (index != o.index)
				return index - o.index;
			else
				return file.getName().length() - o.file.getName().length();
		}

	}

	private CsaOperation toCsaOp(File operationFile) {
		return (CsaOperation) FileTools.read(operationFile).fromInputStream(marshaller::unmarshall);
	}

	private void attachResource(CsaResourceBasedOperation csaOperation, File payloadFile) {
		Resource payload = resourceBuilder.newResource().fromFile(payloadFile);

		csaOperation.setPayload(payload);
	}

}
