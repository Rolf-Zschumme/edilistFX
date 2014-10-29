package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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

import de.vbl.ediliste.model.DokuLink.DokuStatus;

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
	
	public SVNRepository open () {
		try {
			SVNURL url = SVNURL.parseURIEncoded(this.location);
			svn_repository = SVNRepositoryFactory.create(url);
		} catch (SVNException e) {
			e.printStackTrace();
		}
		ISVNAuthenticationManager authManager = SVNWCUtil
				.createDefaultAuthenticationManager(
						this.benutzer, this.getPasswort());
		svn_repository.setAuthenticationManager(authManager);
		try {
			svn_repository.testConnection();
		} catch (SVNException e) {
			e.printStackTrace();
		}
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
	
	public final Task<DokuLink> findTestEntries(String name, String firstLevel, ObservableList<DokuLink> dokuLinkList, Label lbHinweis) {
		lbHinweis.setText("Einträge werden gelesen. Bitte warten...");
		dokuLinkList.clear();
		Task<DokuLink> findWorker = createfindWorkerTest0(name, firstLevel, this);

		findWorker.valueProperty().addListener(new ChangeListener<DokuLink>() {
			public void changed(ObservableValue<? extends DokuLink> observable, DokuLink oldValue, DokuLink newValue) {
				if (newValue != null) {
					dokuLinkList.add(newValue);
				} else {	
					lbHinweis.setText(getErgebnisText(dokuLinkList.size()));
				}	
			}
		});
		
		Thread thread = new Thread(findWorker);
		thread.setDaemon(true);
		thread.start();
		
		return findWorker;
	}

	private final static  Task<DokuLink> createfindWorkerTest0(String name, String firstLevel, Repository repository) 
	{
		return new Task<DokuLink>() {
			@Override
			protected DokuLink call() throws Exception {
				int startpfadlength = repository.getStartPfad().length();
				
				DokuLink dok = null; 
				SVNURL url = null;
				SVNURL root = null;
				try {
					url  = SVNURL.parseURIEncoded(repository.location);
					root = null;
				} catch (SVNException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Calendar cal = Calendar.getInstance(); Date createdDate;
				String docName = name + "_Testdokument.docx";
				String path = "/ddd/cccc/eeee/eeee/hhhh/VH_3205";
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
					simulateTime();
					dok = new DokuLink();
					dok.setName(entry.getName().substring(0,7) + "00" + i + entry.getName().substring(6));
					dok.setPfad(path.substring(startpfadlength));
					dok.setRevision(entry.getRevision());
					dok.setVorhaben(vorhaben);
					dok.setStatus(status);
//					dok.setDatum(LocalDateTime.ofInstant(entry.getDate().toInstant(), ZoneId.systemDefault()));
					dok.setDatum(entry.getDate());
					updateValue(dok);
//					dokuLinkList.add(dok);
				}
				return null;
			}
			
		};
	}

	public final Task<ObservableList<DokuLink>> findTest2Entries(String name, String firstLevel, Label lbHinweis) {
		lbHinweis.setText("Einträge werden gelesen. Bitte warten...");
//		dokuLinkList.clear();
//		private ObservableList<DokuLink> dokuLinkList = FXCollections.observableArrayList();		
		Task<ObservableList<DokuLink>> findWorker = createfindWorker2(name, firstLevel, this);

		Thread thread = new Thread(findWorker);
		thread.setDaemon(true);
		thread.start();
		return findWorker;
	}
	
	private final static  Task<ObservableList<DokuLink>> createfindWorker2(String name, String firstLevel, Repository repository) 
	{
		return new Task<ObservableList<DokuLink>>() {
			@Override
			protected ObservableList<DokuLink> call() throws Exception {
				final ObservableList<DokuLink> dokuLinkList = FXCollections.observableArrayList();		
				int startpfadlength = repository.getStartPfad().length();
				
				DokuLink dok = null; 
				SVNURL url = null;
				SVNURL root = null;
				try {
					url  = SVNURL.parseURIEncoded(repository.location);
					root = null;
				} catch (SVNException e) {
					e.printStackTrace();
				}
				Calendar cal = Calendar.getInstance(); Date createdDate;
				String docName = name + "_Testdokument.docx";
				String path = "/ddd/cccc/eeee/eeee/hhhh/VH_3205";
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
					simulateTime();
					dok = new DokuLink();
					dok.setName(entry.getName().substring(0,7) + "00" + i + entry.getName().substring(6));
					dok.setPfad(path.substring(startpfadlength));
					dok.setRevision(entry.getRevision());
					dok.setVorhaben(vorhaben);
					dok.setStatus(status);
					dok.setDatum(entry.getDate());
					dokuLinkList.add(dok);
				}
				return dokuLinkList;
			}
			
		};
	}
    
	public final Task<String> findTest3Entries(String name, String firstLevel,	ObservableList<DokuLink> dokuLinkList, 
																									  Label lbHinweis) {
		lbHinweis.setText("Einträge werden gelesen. Bitte warten...");
		dokuLinkList.clear();
		Task<String> findWorker = createfindWorker3(name, firstLevel, this, dokuLinkList);

		Thread thread = new Thread(findWorker);
		thread.setDaemon(true);
		thread.start();
		return findWorker;
	}
	
	private final static  Task<String> createfindWorker3(String name, String firstLevel, Repository repository,
																							ObservableList<DokuLink> dokuLinkList) 
	{
		return new Task<String>() {
			@Override
			protected String call() throws Exception {
				int startpfadlength = repository.getStartPfad().length();
				
				DokuLink dok = null; 
				SVNURL url = null;
				SVNURL root = null;
				try {
					url  = SVNURL.parseURIEncoded(repository.location);
					root = null;
				} catch (SVNException e) {
					e.printStackTrace();
				}
				Calendar cal = Calendar.getInstance(); Date createdDate;
				String docName = name + "_Testdokument.docx";
				String path = "/ddd/cccc/eeee/eeee/hhhh/VH_3205";
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
//					simulateTime();
					dok = new DokuLink();
					dok.setName(entry.getName().substring(0,7) + "00" + i + entry.getName().substring(6));
					dok.setPfad(path.substring(startpfadlength));
					dok.setRevision(entry.getRevision());
					dok.setVorhaben(vorhaben);
					dok.setStatus(status);
					dok.setDatum(entry.getDate());
					dokuLinkList.add(dok);
				}
				return getErgebnisText(dokuLinkList.size());
			}
			
		};
	}

	private static void simulateTime() throws InterruptedException {
//		Random rnd = new Random(System.currentTimeMillis());
//		long millis = rnd.nextInt(250);
		Thread.sleep(60);
	}
	
	private static String getErgebnisText(int size) {
		if (size > 1)
			return "Es wurden " + size + " Eintrag gefunden";
		else if (size == 1)
			return "Es wurde 1 Eintrag gefunden";
		else
			return "Es wurde kein Eintrag gefunden";
	}

    public final Task<String> findEntries(String name, String firstLevel, ObservableList<DokuLink> dokuLinkList, Label lbHinweis) {
		if (svn_repository == null) {
			throw new RuntimeException("Repository ist nicht göffnet");
		}
		lbHinweis.setText("Einträge werden gelesen. Bitte warten...");
		dokuLinkList.clear();
		int startPfadLaenge = this.getStartPfad().length(); 
		
		Task<String> findWorker = createfindWorker(name, firstLevel, startPfadLaenge, dokuLinkList);		

		Thread thread = new Thread(findWorker);
		thread.setDaemon(true);
		thread.start();
		return findWorker;
	}
    
    private Task<String> createfindWorker(String name, String firstLevel,
			int startPfadLaenge, ObservableList<DokuLink> dokuLinkList) {
    	
		return new Task<String>() {
			@Override
			protected String call() throws Exception {
				try {
					findEntries(svn_repository, startPfad + firstLevel, 
							startPfadLaenge, name.toLowerCase(), dokuLinkList);
				} catch (SVNException e) {
					e.printStackTrace();
				}
				return getErgebnisText(dokuLinkList.size());
			}
		};	
	}

	private static void findEntries(SVNRepository 		 	 svn_repo, 
									String 		  		     path, 
									int                  	 startpfadlength,
									String 			 	     name, 
									Collection<DokuLink>     dokuLinkList     ) throws SVNException 
	{
		@SuppressWarnings("unchecked")
		Collection<SVNDirEntry> entries = svn_repo.getDir(path, -1, null,(Collection<?>) null);
		Iterator<SVNDirEntry> iterator = entries.iterator();
		
		while (iterator.hasNext()) {
			SVNDirEntry entry = iterator.next();
			String eName = entry.getName();
			if (entry.getKind() == SVNNodeKind.FILE &&
			    eName.toLowerCase().contains(name) &&
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
				dok.setPfad(path.substring(startpfadlength));
				dok.setRevision(entry.getRevision());
				dok.setVorhaben(vorhaben);
				dok.setStatus(status);
				dok.setDatum(entry.getDate());
				dokuLinkList.add(dok);
			}
			if (entry.getKind() == SVNNodeKind.DIR) {
				findEntries(svn_repo, path + "/" + eName, startpfadlength, name, dokuLinkList);
			}
			
		}
	}
}