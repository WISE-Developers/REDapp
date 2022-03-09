/***********************************************************************
 * REDapp - KmzWriter.java
 * Copyright (C) 2015-2019 The REDapp Development Team
 * Homepage: http://redapp.org
 * 
 * REDapp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * REDapp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REDapp. If not see <http://www.gnu.org/licenses/>. 
 **********************************************************************/

package ca.redapp.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.util.IOUtils;

import de.micromata.opengis.kml.v_2_2_0.Kml;

/**
 * Write data to a KMZ file.
 * 
 */
public class KmzWriter {
	public static abstract class DataSource {
		protected String archivedFileName;

		public abstract void writeToStream(ZipOutputStream zipOutputStream) throws IOException;
	}

	/**
	 * The data source for any file that needs to be added to the KMZ file.
	 */
	public static final class FileDataSource extends DataSource {
		private File sourceFile;

		public FileDataSource(File sourceFile, String archivedFileName) {
			this.sourceFile = sourceFile;
			this.archivedFileName = archivedFileName;
		}

		@Override
		public void writeToStream(ZipOutputStream zipOutputStream) throws IOException {
			if (!sourceFile.exists()) {
				throw new IllegalArgumentException("File referenced in parameter [" + sourceFile.getAbsolutePath() + "] does not exist");
			}

			try (FileInputStream fis = new FileInputStream(sourceFile)) {
				ZipEntry entry = new ZipEntry("files/" + archivedFileName);
				zipOutputStream.putNextEntry(entry);

				IOUtils.copy(fis, zipOutputStream);
			} catch (FileNotFoundException e) {
				REDappLogger.error("Error exporting KMZ", e);
			}
		}
	}

	/**
	 * The data source for the KML file that is required to be added to a KMZ file.
	 * 
	 */
	public static final class KMLDataSource extends DataSource {
		private Kml kml;

		public KMLDataSource(Kml kml, String archivedFileName) {
			this.kml = kml;
			this.archivedFileName = archivedFileName;
		}

		@Override
		public void writeToStream(ZipOutputStream zipOutputStream) throws IOException {
			ZipEntry entry = new ZipEntry(archivedFileName);
			zipOutputStream.putNextEntry(entry);

			kml.marshal(zipOutputStream);
		}
	}

	/**
	 * Write the data to a KMZ file. There must be a KML file to add to it and any number of files.
	 * 
	 * @param os the stream to write the data to
	 * @param kmlDataSource the KML file to write to the stream
	 * @param files a list of files to add to the stream
	 */
	public static void packageAsKMZ(OutputStream os, DataSource kmlDataSource, FileDataSource ... files) {
		ZipOutputStream stream = null;
		boolean isExceptionThrown = false;

		try {
			if (files == null)
				files = Collections.emptyList().toArray(new FileDataSource[] { });
			stream = new ZipOutputStream(new BufferedOutputStream(os));
			kmlDataSource.writeToStream(stream);
			stream.putNextEntry(new ZipEntry("files/"));
			for (FileDataSource file : files) {
				file.writeToStream(stream);
			}
			stream.flush();
			stream.close();
		}
		catch (IOException e) {
			isExceptionThrown = true;
		}
		catch (RuntimeException e) {
			isExceptionThrown = true;
		}
		catch (Exception e) {
			isExceptionThrown = true;
		}
		catch (Error e) {
			isExceptionThrown = true;
		}
		finally {
			if (isExceptionThrown) {
				try {
					if (stream != null)
						stream.close();
				}
				catch (IOException e) {
				}
			}
		}
	}
}
