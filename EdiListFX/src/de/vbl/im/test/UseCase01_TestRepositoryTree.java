package de.vbl.im.test;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javafx.stage.FileChooser;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class UseCase01_TestRepositoryTree {

	public static void main(String[] args) {

		DAVRepositoryFactory.setup();
		
		String name = "adelfinop";
		String password = "16WKGE";

		SVNURL url;
		try {
			// url =
			// SVNURL.parseURIDecoded("svn://itneu/Anwendungskomponenten");

			url = SVNURL.parseURIEncoded("svn://itneu/SE-Akten");
			SVNRepository repository = SVNRepositoryFactory.create(url, null);

			ISVNAuthenticationManager authManager = SVNWCUtil
					.createDefaultAuthenticationManager(name, password);
			repository.setAuthenticationManager(authManager);

			System.out.println("Repository Root: "	+ repository.getRepositoryRoot(true));
			System.out.println("Repository UUID: " 	+ repository.getRepositoryUUID(true));

			SVNNodeKind nodeKind = repository.checkPath("", -1);
			if (nodeKind == SVNNodeKind.NONE) {
				System.err.println("There is no entry at '" + url + "'.");
				System.exit(1);
			} else if (nodeKind == SVNNodeKind.FILE) {
				System.err.println("The entry at '" + url
						+ "' is a file while a directory was expected.");
				System.exit(1);
			}

//			listEntries(repository, "", false); // alles
//			listEntries(
//					repository,
//					"/03_QS-Akte/02_xSpez_abgenommen/03_Technische Spezifikation/EDI/",
//					true);
			
//			findEntries(repository,"/03_QS-Akte","TSpez_0122");  
//			findEntries(repository,"/03_QS-Akte","TSpez_0123");  
			findEntries(repository,"/03_QS-Akte","TSpez_0124");  
//			findEntries(repository,"/03_QS-Akte","TSpez_0125");  
//			findEntries(repository,"/03_QS-Akte","TSpez_0126");  
//			findEntries(repository,"/03_QS-Akte","TSpez_0127");  
//			findEntries(repository,"/03_QS-Akte","TSpez_0239");  
//			findEntries(repository,"/03_QS-Akte","TSpez_0201");  

			String path = "/03_QS-Akte/02_xSpez_abgenommen/03_Technische Spezifikation/LSTG";
			
			copyToDesktop(repository, path, "TSpez_0124_LST-CM-LB_Steuerlauf_ZfA.docx");
			
			
			
			Long latestRevision = repository.getLatestRevision();
			System.out.println("Repository latest revision: " + latestRevision);

		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("geschafft");
	}

	
	

	private static void copyToDesktop(SVNRepository repo , String path, String docName ) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		File file = getDestinationFile(docName);
		if (file != null) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			docName =   path + "/" + docName;		
			try {
				repo.getFile(docName, -1, null, baos);
			} catch (SVNException e1) {
				e1.printStackTrace();
			}
			try {
				baos.close();
				baos.writeTo(fos);
//				String str = new String(baos.toByteArray(),"utf-8");
//				System.out.println(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static String initialFilePath = System.getProperty("user.home");

	private static File getDestinationFile(String initialFileName) {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Name und Ablageort des Dokumentes eingeben");
    	fileChooser.setInitialFileName(initialFileName);
    	fileChooser.getExtensionFilters().add(
    			new FileChooser.ExtensionFilter("Word-Dokument", "*.docx"));
    	File filepath = new File(initialFilePath);
    	if (filepath.isDirectory()==false)
    		initialFilePath = System.getProperty("user.home");
   		fileChooser.setInitialDirectory(new File (initialFilePath));

   		// File file = fileChooser.showSaveDialog(primaryStage);
   		
   		File file = new File(initialFilePath + "/" + initialFileName);
   		System.out.println("File: " + initialFilePath + "  Filename: " + initialFileName);
    	return file;

	}



	private static void findEntries(SVNRepository repository, String path, String name) 
						throws SVNException 
	{
		@SuppressWarnings("unchecked")
		Collection<SVNDirEntry> entries = repository.getDir(path, -1, null,(Collection<?>) null);
		Iterator<SVNDirEntry> iterator = entries.iterator();

		while (iterator.hasNext()) {
			SVNDirEntry entry = iterator.next();
			String eName = entry.getName();
			if (eName.startsWith(name) && eName.endsWith(".docx") ) {
				String vorhaben = "     ";
				int vh_startpos = path.lastIndexOf("/VH_");
				if (vh_startpos > 0) {
					vorhaben = path.substring(vh_startpos + 1, vh_startpos + 8);
				} 
				else if (path.contains("abgenommen")) {
					vorhaben = " abgen.";
					while (eName.length() < 48) eName += " "; 
					System.out.println( vorhaben +
							"\n  Name   : " + eName + 
							"\n  rev.   : " + entry.getRevision() +
							"\n  Datum  : " + entry.getDate() +
							"\n  Größe  : " + entry.getSize() + 
//							"\n  extTarg: " + entry.getExternalTarget()  +
//							"\n  extPUrl: " + entry.getExternalParentUrl()  +
							"\n  extPUrl: " + entry.getRepositoryRoot() +
							"\n  relPath: " + entry.getRelativePath() +
							"\n  RepRoot: " + entry.getRepositoryRoot() +
							"\n  path   : " + path + 
							"\n  url    : " + entry.getURL() +
//											   " Pfad:"  + path +  
							"");
				}
			}
			if (entry.getKind() == SVNNodeKind.DIR) {
				findEntries(repository, path + "/" + eName, name);
			}
			
		}
	}


	public static void listEntries(SVNRepository repository, String path,
			boolean recursive) throws SVNException {

		@SuppressWarnings("unchecked")
		Collection<SVNDirEntry> entries = repository.getDir (path, -1, null,(Collection<?>) null);
		Iterator<SVNDirEntry> iterator = entries.iterator();

		System.out.println( "Pfad:" + (path.equals("") ? "" : path));
		while (iterator.hasNext()) {
			SVNDirEntry entry = iterator.next();

			System.out.println("\t"
					+ entry.getName() + " ( type: " + entry.getKind()
					+ ", author: '" + entry.getAuthor() + "'; revision: "
					+ entry.getRevision() + "; date: " + entry.getDate() + ")");
			if (entry.getKind() == SVNNodeKind.DIR && recursive) {
				listEntries(repository, (path.equals("")) ? entry.getName()
						: path + "/" + entry.getName(), recursive);
			}
		}
	}
}
