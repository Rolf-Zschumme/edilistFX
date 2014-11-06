package de.vbl.im.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import de.vbl.im.model.DokuLink.DokuStatus;

import javax.persistence.Access;

import static javax.persistence.AccessType.PROPERTY;

/**
 * Entity implementation class for Entity: SVN_Repository
 *
 */
@Entity  
 
@Access(PROPERTY)
public class Repository {
//	public static enum DokuArt { FSPEZ, TSPEZ, xSPEZ }; 

	private long id;
	private StringProperty name;
	private String location;
	private String startPfad;
	private String benutzer;
	private String passwort;
	@Transient
	private SVNRepository svn_repository = null;
	
	public Repository() {
		name = new SimpleStringProperty();
		DAVRepositoryFactory.setup();
	}   

	public Repository(String name, EntityManager em) {
		this();
		TypedQuery<Repository> tq = em.createQuery(
				"SELECT r FROM Repository r WHERE r.name = :n", Repository.class);
		tq.setParameter("n", name);
		
		List<Repository> repoList = tq.getResultList();
		if (repoList.size() == 0) {
			System.out.println("Repository " +  name + "nicht vorhanden");
			throw new IllegalArgumentException("Repository '" + name + "' nicht DB eingetragen");
		}
		if (repoList.size() > 1) {
			System.out.println("HINWEIS: Repository " + name + " nicht eindeutig");
		}

		Repository aktRepo = repoList.get(0);
		this.id        = aktRepo.id;
		this.name      = aktRepo.name;
		this.location  = aktRepo.location;
		this.startPfad = aktRepo.startPfad;
		this.benutzer  = aktRepo.benutzer; 
		
	}
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public StringProperty nameProperty() {
		return name;
	}
	
	public String getName() {
		return name.getValueSafe();
	}

	public void setName(String n) {
		name.set(n);
	}   
	public String getLocation() {
		return this.location;
	}

	public void setLocation(String url) {
		this.location = url;
	}   

	public String getStartPfad() {
		return startPfad;
	}
	public void setStartPfad(String pfad) {
		this.startPfad = pfad;
	}

	public String getBenutzer() {
		return this.benutzer;
	}

	public void setBenutzer(String benutzer) {
		this.benutzer = benutzer;
	}   
	private String getPasswort() {
		return this.passwort;
	}
 
	public void setPasswort(String passwort) {
		this.passwort = passwort;
	}
	
	public SVNRepository open () throws SVNException {
		SVNURL url = SVNURL.parseURIEncoded(this.location);
		try {
			svn_repository = SVNRepositoryFactory.create(url);
		} catch (SVNException e) {
			e.printStackTrace();
		}
		ISVNAuthenticationManager authManager = SVNWCUtil
				.createDefaultAuthenticationManager(
						this.benutzer, this.getPasswort());
		svn_repository.setAuthenticationManager(authManager);
		svn_repository.testConnection();
		return svn_repository;
	}
	
	public ByteArrayOutputStream getFileStream (String filePath, int revision) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (revision == 0) revision = -1;
		SVNProperties fileProperties = new SVNProperties();
		try {
			svn_repository.getFile(filePath, -1, fileProperties, baos);
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return baos;
	}
	
//	public final Task<?> findTestEntries(String name, String firstLevel,	ObservableList<DokuLink> dokuLinkList, 
//																									  Label lbHinweis) {
//		dokuLinkList.clear();
//		Task<?> findWorker = createTESTfindWorker(name, firstLevel, this, dokuLinkList);
//
//		Thread thread = new Thread(findWorker);
//		thread.setDaemon(true);
//		thread.start();
//		return findWorker; //  findWorker;
//	}
	
	private final static Task<Object> createTESTfindWorker(String name, String firstLevel, int startPfadLaenge,
																							ObservableList<DokuLink> dokuLinkList) 
	{
		return new Task<Object>() {
			@Override
			protected Object call() throws Exception {
				int anz = 0;
				DokuLink dok = null; 
				SVNURL url = null;
				SVNURL root = null;
				Calendar cal = Calendar.getInstance(); Date createdDate;
				String docName = name + "_Testdokument.docx";
				String path = "/VerzeichnisROOT/Ebene1/SubVerzeichnis/VH_3205";
				cal.set(2013,7,1,12,58,30);			
				createdDate = cal.getTime();
				
				SVNDirEntry entry = new SVNDirEntry(url, root, docName,  null, 32000L, false, 1001, createdDate, "Author");
				String vorhaben = "     ";
				DokuLink.DokuStatus status = DokuStatus.OHNE_VORHABEN;
				if (path.contains("abgenommen")) {
					vorhaben = " abgen.";
					status = DokuStatus.ABGENOMMEN;
				} else {	
					int vh_startpos = path.lastIndexOf("/VH_");
					if (vh_startpos > 0) {
						vorhaben = path.substring(vh_startpos + 1, vh_startpos + 8);
					}
					status = DokuStatus.NUR_VORHABEN;
				}
				for (int i=1; i<=8; ++i) {
					++anz;
					dok = new DokuLink();
					dok.setName(entry.getName().substring(0,7) + "00" + i + entry.getName().substring(6));
					dok.setPfad(path.substring(startPfadLaenge));
					dok.setRevision(entry.getRevision());
					dok.setVorhaben(vorhaben);
					dok.setStatus(status);
					dok.setDatum(entry.getDate());
					dokuLinkList.add(dok);
				}
				return getErgebnisText(dokuLinkList.size(),anz);
			}
			
		};
	}

	private static String getErgebnisText(int found, int gelesen) {
		if (found > 1)
			return found + " (von " + gelesen + ") Einträgen selektiert";
		else if (found == 1)
			return "Ein Eintrag (von " + gelesen + ") zutreffend";
		else
			return "Kein einziger Eintrag (von " + gelesen + ") zutreffend";
	}

    public final Task<Object> findEntries(String name, String firstLevel, ObservableList<DokuLink> dokuLinkList, Label lbHinweis) {
//		if (svn_repository == null) {
//			String repoName = this.getName() != null ? this.getName() : "?";  
//			throw new RuntimeException("Repository " + repoName + " ist nicht göffnet");
//		}
		dokuLinkList.clear();
		int startPfadLaenge = this.getStartPfad().length(); 
		Task<Object> findWorker = null;
		if (this.getName().equals("Test-SVN") == true)
			findWorker = createTESTfindWorker(name, firstLevel, startPfadLaenge, dokuLinkList);		
		else	
		    findWorker = createfindWorker    (name, firstLevel, startPfadLaenge, dokuLinkList);		

		Thread thread = new Thread(findWorker);
		thread.setDaemon(true);
		thread.start();
		return findWorker;
	}
    
    private Task<Object> createfindWorker(String name, String firstLevel,
    									  int startPfadLaenge, 
    									  ObservableList<DokuLink> dokuLinkList) {
    	String searchname = name.toLowerCase();
		return new Task<Object>() {
			@Override
			protected Object call() throws Exception {
				int anz = 0;
				try {
					anz = findEntries( startPfad + firstLevel);
				} catch (SVNException e) {
					e.printStackTrace();
				}
				updateMessage(getErgebnisText(dokuLinkList.size(),anz));
				return null; //  ""; // getErgebnisText(dokuLinkList.size(),anz);
			}
			
	    	private int findEntries(String path) throws SVNException 
	    	{
	    		int anzGelesen = 0;
	    		@SuppressWarnings("unchecked")
	    		Collection<SVNDirEntry> entries = svn_repository.getDir(path, -1, null,(Collection<?>) null);
	    		Iterator<SVNDirEntry> iterator = entries.iterator();
	    		
    			updateMessage (path);
	    		while (iterator.hasNext()) {
	    			++anzGelesen;
	    			SVNDirEntry entry = iterator.next();
	    			String eName = entry.getName();
	    			if (entry.getKind() == SVNNodeKind.FILE &&
	    					eName.toLowerCase().contains(searchname) &&
        				   (eName.endsWith(".docx") || eName.endsWith(".doc"))) {
	    				String vorhaben = "     ";
	    				DokuLink.DokuStatus status = DokuStatus.OHNE_VORHABEN;
	    				if (path.contains("abgenommen")) {
	    					vorhaben = " abgen.";
	    					status = DokuStatus.ABGENOMMEN;
	    				} else {	
	    					int vh_startpos = path.lastIndexOf("/VH_");
	    					if (vh_startpos > 0) {
	    						vorhaben = path.substring(vh_startpos + 1, vh_startpos + 8);
	    					}
	    					status = DokuStatus.NUR_VORHABEN;
	    				}
	    				DokuLink dok = new DokuLink();
	    				dok.setName(eName);
	    				dok.setPfad(path.substring(startPfadLaenge));
	    				dok.setRevision(entry.getRevision());
	    				dok.setVorhaben(vorhaben);
	    				dok.setStatus(status);
	    				dok.setDatum(entry.getDate());
	    				dokuLinkList.add(dok);
	    			}
	    			if (entry.getKind() == SVNNodeKind.DIR) {
	    				anzGelesen += findEntries(path + "/" + eName);
	    			}
	    			
	    		}
	    		return anzGelesen;
	    	}
		};
		
	}

    @Override
    public String toString() {
    	String ret = 
    		"\nName    : " + (this.name     == null ? "" : this.name.get()) +  
    		"\nLocation: " + (this.location == null ? "" : this.location  );
    	return ret; 
    }
}