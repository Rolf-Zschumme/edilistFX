package de.vbl.ediliste;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class UseCase02_TestFileContent {

	public static void main(String[] args) {

		String name = "adelfinop";
		String password = "16WKGE";

		String filePath = "/03_QS-Akte/02_xSpez_abgenommen/03_Technische Spezifikation/EDI/Tspez_0111_VBL_PI_ANW_PT_Anerkennung.doc";

		SVNURL url;
		try {
			// url =
			// SVNURL.parseURIDecoded("svn://itneu/Anwendungskomponenten");
			url = SVNURL.parseURIEncoded("svn://itneu/SE-Akten");
			SVNRepository repository = SVNRepositoryFactory.create(url, null);

			ISVNAuthenticationManager authManager = SVNWCUtil
					.createDefaultAuthenticationManager(name, password);
			repository.setAuthenticationManager(authManager);

			SVNNodeKind nodeKind = repository.checkPath(filePath, -1);

			if (nodeKind == SVNNodeKind.NONE) {
				System.err.println("There is no entry at '" + url + "'.");
				System.exit(1);
			} else if (nodeKind == SVNNodeKind.DIR) {
				System.err.println("The entry at '" + url
						+ "' is a directory while a file was expected.");
				System.exit(1);
			}

			SVNProperties fileProperties = new SVNProperties();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			repository.getFile(filePath, -1, fileProperties, baos);

			String mimeType = (String) fileProperties
					.getStringValue(SVNProperty.MIME_TYPE);
			boolean isTextType = SVNProperty.isTextMimeType(mimeType);

			Iterator iterator = fileProperties.nameSet().iterator();
			while (iterator.hasNext()) {
				String propertyName = (String) iterator.next();
				String propertyValue = (String) fileProperties
						.getStringValue(propertyName);
				System.out.println("File property: " + propertyName + "="
						+ propertyValue);
			}

			// if ( isTextType ) {
			// System.out.println( "File contents:" );
			// System.out.println( );
			// try {
			// baos.writeTo( System.out );
			// } catch ( IOException ioe ) {
			// ioe.printStackTrace( );
			// }
			// } else {
			// System.out.println( "Not a text file." );
			// }
			int extPos = filePath.indexOf(".");
			String ext = filePath.substring(extPos);
			try {
				baos.writeTo(new FileOutputStream("tmpfile" + ext));
				Desktop desktop = null;
				if (Desktop.isDesktopSupported()) {
					desktop = Desktop.getDesktop();
				}

				desktop.open(new File("tmpfile" + ext));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("geschafft");
	}

	public static void listEntries(SVNRepository repository, String path)
			throws SVNException {

		Collection entries = repository.getDir(path, -1, null,
				(Collection) null);
		Iterator iterator = entries.iterator();

		while (iterator.hasNext()) {
			SVNDirEntry entry = (SVNDirEntry) iterator.next();

			System.out.println("/" + (path.equals("") ? "" : path + "/")
					+ entry.getName() + " ( type: " + entry.getKind()
					+ ", author: '" + entry.getAuthor() + "'; revision: "
					+ entry.getRevision() + "; date: " + entry.getDate() + ")");
			if (entry.getKind() == SVNNodeKind.DIR) {
				listEntries(repository, (path.equals("")) ? entry.getName()
						: path + "/" + entry.getName());
			}

		}
	}

}
