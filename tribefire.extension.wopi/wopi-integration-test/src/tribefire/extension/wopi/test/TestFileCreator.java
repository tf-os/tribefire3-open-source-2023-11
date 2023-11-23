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
package tribefire.extension.wopi.test;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.RandomTools;

/**
 * Simple utility class to create dummy test files with a particular size
 * 
 *
 */
public class TestFileCreator {

	private static final Logger logger = Logger.getLogger(TestFileCreator.class);

	public static File createPptx(int sizeInMegaByte) {
		OutputStream out = null;
		XMLSlideShow pptx = null;

		File file = FileTools.createNewTempFile(RandomTools.getRandom32CharactersHexString(true) + ".pptx");

		try {
			pptx = new XMLSlideShow();

			pptx.createSlide();

			long totalSize = sizeInMegaByte * 1024 * 1024;
			List<File> images = createBmp(totalSize);

			for (File image : images) {

				InputStream is = null;
				try {
					is = new BufferedInputStream(new FileInputStream(image));

					pptx.addPicture(is, PictureType.BMP);

				} finally {
					IOTools.closeCloseable(is, logger);
				}
			}

			out = new BufferedOutputStream(new FileOutputStream(file));
			pptx.write(out);
		} catch (Exception e) {
			throw Exceptions.unchecked(e);
		} finally {
			IOTools.closeCloseable(pptx, logger);
			IOTools.closeCloseable(out, logger);
		}

		return file;
	}

	public static File createXlsx(int sizeInMegaByte) {
		OutputStream out = null;
		XSSFWorkbook workbook = null;

		File file = FileTools.createNewTempFile(RandomTools.getRandom32CharactersHexString(true) + ".xlsx");

		try {

			workbook = new XSSFWorkbook();

			Sheet sheet = workbook.createSheet("test");

			long totalSize = sizeInMegaByte * 1024 * 1024;
			List<File> images = createBmp(totalSize);

			for (File image : images) {

				InputStream is = null;
				try {

					is = new BufferedInputStream(new FileInputStream(image));

					final CreationHelper helper = workbook.getCreationHelper();
					final Drawing<?> drawing = sheet.createDrawingPatriarch();
					final ClientAnchor anchor = helper.createClientAnchor();
					anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

					// it is a bmp image but png setting seems to work
					final int pictureIndex = workbook.addPicture(IOUtils.toByteArray(is), Workbook.PICTURE_TYPE_PNG);

					anchor.setCol1(0);
					anchor.setRow1(1);
					anchor.setRow2(2);
					anchor.setCol2(1);
					final Picture pict = drawing.createPicture(anchor, pictureIndex);
					pict.resize();

				} finally {
					IOTools.closeCloseable(is, logger);
				}

			}

			out = new BufferedOutputStream(new FileOutputStream(file));

			workbook.write(out);
		} catch (Exception e) {
			throw Exceptions.unchecked(e);
		} finally {
			IOTools.closeCloseable(workbook, logger);
			IOTools.closeCloseable(out, logger);
		}

		return file;

	}

	public static File createDocx(int sizeInMegaByte) {
		OutputStream out = null;
		XWPFDocument doc = null;

		File file = FileTools.createNewTempFile(RandomTools.getRandom32CharactersHexString(true) + ".docx");

		try {

			long totalSize = sizeInMegaByte * 1024 * 1024;
			List<File> images = createBmp(totalSize);

			long size = images.stream().map(f -> f.length()).mapToLong(Long::longValue).sum();
			logger.info(() -> "created: '" + images.size() + "' tmp images with size: '" + size + "'");

			doc = new XWPFDocument();

			XWPFParagraph paragraph = doc.createParagraph();
			XWPFRun run = paragraph.createRun();

			paragraph = doc.createParagraph();
			run = paragraph.createRun();
			run.setText("docx - but too big");

			run = paragraph.createRun();

			run = paragraph.createRun();

			for (File image : images) {

				InputStream is = null;
				try {
					String imgFile = image.getAbsolutePath();
					is = new FileInputStream(imgFile);
					run.addPicture(is, XWPFDocument.PICTURE_TYPE_BMP, imgFile, Units.toEMU(50), Units.toEMU(50));
					run = paragraph.createRun();
				} finally {
					IOTools.closeCloseable(is, logger);

				}
			}

			out = new BufferedOutputStream(new FileOutputStream(file));

			doc.write(out);

			images.forEach(image -> {
				FileTools.deleteFile(image);
			});

		} catch (Exception e) {
			throw Exceptions.unchecked(e);
		} finally {
			IOTools.closeCloseable(out, logger);
			IOTools.closeCloseable(doc, logger);
		}

		return file;

	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private static List<File> createBmp(long totalSize) {
		List<File> files = new ArrayList<>();
		long actualSize = 0;

		while (actualSize < totalSize) {
			BufferedImage img = map(1500, 1000);
			File file = saveBmp(img);
			files.add(file);
			actualSize = actualSize + file.length();
		}

		return files;
	}

	private static BufferedImage map(int sizeX, int sizeY) {
		final BufferedImage res = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				Random r = new Random();
				int nextInt = r.nextInt(0xFFFFFF);
				res.setRGB(x, y, nextInt);
			}
		}
		return res;
	}

	private static File saveBmp(final BufferedImage bi) {
		try {
			File file = FileTools.createNewTempFile(RandomTools.getRandom32CharactersHexString(true), "bmp");

			RenderedImage rendImage = bi;
			ImageIO.write(rendImage, "bmp", file);
			return file;
		} catch (IOException e) {
			throw Exceptions.unchecked(e);
		}
	}

	// -----------------------------------------------------------------------
	// MAIN
	// -----------------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		// File testFileDocx = createDocx(50);
		// File testFileXlsx = createXlsx(20);
		File testFilePptx = createPptx(5);

		// logger.info(() -> "--> " + testFileDocx.getAbsolutePath());
		// logger.info(() -> "--> " + testFileXlsx.getAbsolutePath());
		logger.info(() -> "--> " + testFilePptx.getAbsolutePath());
	}

}
