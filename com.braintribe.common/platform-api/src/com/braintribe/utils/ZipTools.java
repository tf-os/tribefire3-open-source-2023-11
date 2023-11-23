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
package com.braintribe.utils;

import static java.util.Objects.requireNonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.utils.io.FlushingOutputStreamWriterBuilder;
import com.braintribe.utils.io.WriterBuilder;
import com.braintribe.utils.io.ZipEntryWriter;
import com.braintribe.utils.stream.HardLimitInputStream;

/**
 * @author peter.gazdik
 */
public class ZipTools {

	// These are ridiculous high numbers, but we don't want to be too restrictive here, do we?
	private static int maxEntries = 100000;
	private static long maxUnzippedEntryLength = Numbers.GIGABYTE;
	private static long maxUnzippedTotalLength = Numbers.GIGABYTE * 100;
	private static double thresholdRatio = 10000d;

	static {
		String maxEntriesString = System.getProperty("TRIBEFIRE_TOOLS_ZIP_MAX_ENTRIES");
		if (!StringTools.isBlank(maxEntriesString)) {
			maxEntries = Integer.parseInt(maxEntriesString);
		}
		String maxEntryLengthString = System.getProperty("TRIBEFIRE_TOOLS_ZIP_MAX_ENTRY_LENGTH");
		if (!StringTools.isBlank(maxEntryLengthString)) {
			maxUnzippedEntryLength = Long.parseLong(maxEntryLengthString);
		}
		String maxTotalLengthString = System.getProperty("TRIBEFIRE_TOOLS_ZIP_MAX_TOTAL_LENGTH");
		if (!StringTools.isBlank(maxTotalLengthString)) {
			maxUnzippedTotalLength = Long.parseLong(maxTotalLengthString);
		}
		String thresholdRatioString = System.getProperty("TRIBEFIRE_TOOLS_ZIP_THRESHOLD_RATIO");
		if (!StringTools.isBlank(thresholdRatioString)) {
			thresholdRatio = Double.parseDouble(thresholdRatioString);
		}
	}

	/**
	 * Convenient way for writing a zip file directly by writing individual entries. The advantage is the client code doesn't have to deal with any
	 * {@link InputStream}s and can enjoy the convenience and power of our beloved {@link WriterBuilder} API when writing the entries.
	 *
	 * <pre>
	 *  File zipFile = ...;
	 *  ZipTools.writeZip(zipFile, zew -> writeZip(zew, fileContents));
	 *
	 *  // writes all files inside the zip archive
	 *  private void writeZip(ZipEntryWriter zew, String[] fileContents) {
	 *    for (int i = 0; i &lt; fileContents.length; i++) {
	 *      String fileName = file + i + ".txt";
	 *      zew.writeZipEntry(fileName, wb -> wb.withCharset("UTF-8").string(fileContents[i]));
	 *    }
	 *  }
	 * </pre>
	 */
	public static void writeZip(File zipFile, Consumer<ZipEntryWriter> zipContentWriter) {
		FileTools.write(zipFile).usingOutputStream(os -> writeZipTo(os, zipContentWriter));
	}

	/**
	 * Wraps given {@link OutputStream} to a {@link ZipEntryWriter} and passes it to given consumer (which then writes the zip's content).
	 * <p>
	 * NOTE that given stream is NOT CLOSED by this method.
	 */
	public static void writeZipTo(OutputStream os, Consumer<ZipEntryWriter> zipContentWriter) throws IOException {
		ZipOutputStream out = new ZipOutputStream(os);

		ZipEntryWriter zew = new ZipEntryWriterImpl(out);
		zipContentWriter.accept(zew);
		out.finish();
	}

	private static class ZipEntryWriterImpl implements ZipEntryWriter {
		private final ZipOutputStream out;

		public ZipEntryWriterImpl(ZipOutputStream out) {
			this.out = out;
		}

		@Override
		public ZipOutputStream getZipOutputStream() {
			return out;
		}

		@Override
		public void writeZipEntry(String zipEntryName, Consumer<WriterBuilder<?>> entryContentWriter) {
			try {
				ZipEntry e = new ZipEntry(zipEntryName);
				out.putNextEntry(e);

				WriterBuilder<?> entryWriterBuilder = new FlushingOutputStreamWriterBuilder(out, "ZipEntry " + zipEntryName);
				entryContentWriter.accept(entryWriterBuilder);

				out.closeEntry();

			} catch (IOException e) {
				throw Exceptions.unchecked(e, "Error while writing zip entry: " + zipEntryName);
			}
		}
	}

	public static void unzip(File zipFile, File targetDir) {
		unzip(zipFile, targetDir, null);
	}

	public static void unzip(File zipFile, File defaultTargetDir, Function<String, File> targetMapper) {
		requireNonNull(zipFile, "zipFile must not be null.");

		try (InputStream fis = new FileInputStream(zipFile)) {
			unzip(fis, defaultTargetDir, targetMapper, zipFile.getAbsolutePath());

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while unpacking zip: " + zipFile.getAbsolutePath());
		}
	}

	public static void unzip(InputStream in, File targetDir) {
		unzip(in, targetDir, null);
	}

	public static void unzip(InputStream in, File targetDir, Function<String, File> mapper) {
		unzip(in, targetDir, mapper, null);
	}

	public static void unzip(InputStream in, File defaultTargetDir, Function<String, File> targetMapper, String context) {
		FileTools.ensureFolderExists(requireNonNull(defaultTargetDir, "The targetDir must not be null."));

		try {

			processZipSafely(in, e -> {

				String slashedPathName = e.getName();

				File targetFile = new File(defaultTargetDir, slashedPathName);

				if (!FileTools.isInSubDirectory(defaultTargetDir, targetFile)) {
					throw new RuntimeException("The target file " + targetFile.getAbsolutePath() + " is not within the target folder "
							+ defaultTargetDir.getAbsolutePath() + " (entry name: " + slashedPathName + "). This is not allowed.");
				}

				if (targetMapper != null) {
					targetFile = targetMapper.apply(slashedPathName);
				}

				if (targetFile == null) {
					targetFile = new File(defaultTargetDir, slashedPathName);
				}

				if (e.isDirectory()) {
					// create directory because it maybe empty and it would be an information loss otherwise
					targetFile.mkdirs();
				} else {
					targetFile.getParentFile().mkdirs();

					try (OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile))) {
						IOTools.transferBytes(e.getInputStream(), out, IOTools.BUFFER_SUPPLIER_64K);
					} catch (IOException ioe) {
						throw new UncheckedIOException(ioe);
					}
				}

			}, maxEntries, maxUnzippedTotalLength, maxUnzippedEntryLength, thresholdRatio);

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while unpacking zip " + context);
		}
	}

	/**
	 * Unzips the content of the provided InputStream and calls a Consumer for each entry in the ZIP file. If also applied thresholds on the ZIP
	 * entries to prevent ZIP bomb attacks by
	 *
	 * - limiting the total number of entries in the ZIP file
	 *
	 * - limiting the uncompressed size of individual entries
	 *
	 * - limiting the total uncompresssed size
	 *
	 * - limiting the overall compression ratio (any ratio above the threshold is suspicious)
	 *
	 *
	 *
	 * @param inputStream
	 *            The input stream that contains the ZIP file. This method will take care that the input stream gets closed.
	 * @param entryProcessor
	 *            The actual processor of individual entries
	 * @param maxEntries
	 *            The maximum number of allowed entries
	 * @param maxUnzippedEntryLength
	 *            The maximum (uncompressed) size of an individual entry
	 * @param thresholdRatio
	 *            The maximum compression ratio
	 * @throws UncheckedIOException
	 *             Thrown when there is an underlying IOException
	 * @throws IndexOutOfBoundsException
	 *             When a uncompressed entry is exceeding the maxUnzippedEntryLength
	 */
	public static void processZipSafely(InputStream inputStream, Consumer<LimitedZipEntry> entryProcessor, int maxEntries,
			long maxUnzippedTotalLength, long maxUnzippedEntryLength, double thresholdRatio) throws UncheckedIOException {

		int totalEntries = 0;
		long totalBytesRead = 0;

		try (ZipInputStream zin = new ZipInputStream(new BufferedInputStream(inputStream))) {
			ZipEntry zipEntry = null;

			while ((zipEntry = zin.getNextEntry()) != null) {

				totalEntries++;
				if (totalEntries > maxEntries) {
					throw new RuntimeException("The number of total entries " + maxEntries + " has reached the maximum.");
				}

				LimitedZipEntry limitedZipEntry = new LimitedZipEntry(zipEntry, zin, maxUnzippedEntryLength);
				entryProcessor.accept(limitedZipEntry);

				long compressedSize = zipEntry.getCompressedSize();
				long bytesReadPerEntry = limitedZipEntry.getBytesRead();
				if (compressedSize > 0) {
					double compressionRatio = bytesReadPerEntry / compressedSize;
					if (compressionRatio > thresholdRatio) {
						throw new RuntimeException(
								"The compression ratio " + compressionRatio + " is higher than the maximum threshold " + compressionRatio);
					}
				}

				totalBytesRead += bytesReadPerEntry;
				if (totalBytesRead > maxUnzippedTotalLength) {
					throw new RuntimeException(
							"The total bytes read " + totalBytesRead + " is higher than the maximum threshold " + maxUnzippedTotalLength);
				}

				zin.closeEntry();
			}
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	public static class LimitedZipEntry {

		ZipEntry delegate;
		private final ZipInputStream zin;
		private final long maxUnzippedLength;
		private HardLimitInputStream hardLimitInputStream;

		public LimitedZipEntry(ZipEntry delegate, ZipInputStream zin, long maxUnzippedEntryLength) {
			this.delegate = delegate;
			this.zin = zin;
			this.maxUnzippedLength = maxUnzippedEntryLength;
		}

		public HardLimitInputStream getInputStream() {
			hardLimitInputStream = new HardLimitInputStream(zin, false, maxUnzippedLength) {
				@Override
				public void close() throws IOException {
					// Ignore close method as we are not supposed to close the InputStream per entry
					// but at the end when the ZIP file is processed.
				}
			};
			return hardLimitInputStream;
		}

		public long getBytesRead() {
			if (hardLimitInputStream != null) {
				return hardLimitInputStream.getCount();
			}
			return 0l;
		}
		public String getName() {
			return delegate.getName();
		}
		public boolean isDirectory() {
			return delegate.isDirectory();
		}

		@Override
		public String toString() {
			return delegate.getName();
		}
	}
}
