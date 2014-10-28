package de.vbl.ediliste.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.beans.value.ObservableValue;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import de.vbl.ediliste.model.DokuLink;
import de.vbl.ediliste.model.DokuLink.DokuStatus;

/**
 * Entity implementation class for Entity: SVN_Repository
 *
 */
@Entity  
 
public class Repository implements Serializable {
	private static final long serialVersionUID = 1L;
	public static enum DokuArt { FSPEZ, TSPEZ, xSPEZ }; 

	@Id
	private long id;
	@Column(unique = true, nullable = true)
	private String name;
	private String location;
	private String startPfad;
	private String benutzer;
	private String passwort;
	@Transient
	SVNRepository svn_repository = null;
	
	public Repository() {
		super();
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
	
	public void open () {
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
	}
	
	
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}   
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getUser() {
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
	
    public final Collection<DokuLink> findEntries(String name, String firstLevel) {
		if (svn_repository == null) {
			throw new RuntimeException("Repository ist nicht göffnet");
		}
		Collection<DokuLink> dokuLinkList = new ArrayList<DokuLink>();
		
		int startPfadLaenge = startPfad.length();
		System.out.println("Start:" + startPfad + firstLevel);
		try {
			findEntries(svn_repository, startPfad + firstLevel, 
						startPfadLaenge, name.toLowerCase(), dokuLinkList);
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return dokuLinkList;
	}

	public final Task<DokuLink> findTestEntries(String name, String firstLevel, Collection<DokuLink> dokuLinkList) {
		dokuLinkList.clear();
		
		Task<DokuLink> findWorker = createfindWorker(name, firstLevel, this);

		findWorker.valueProperty().addListener(new ChangeListener<DokuLink>() {
			public void changed(ObservableValue<? extends DokuLink> observable, DokuLink oldValue, DokuLink newValue) {
				if (newValue != null) {
					dokuLinkList.add(newValue);
				}	
			}
		}); 
		
		Thread thread = new Thread(findWorker);
		thread.setDaemon(true);
		thread.start();
		
		return findWorker;
	}

	private final static  Task<DokuLink> createfindWorker(String name, String firstLevel, Repository repository) 
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
				for (int i=0; i<5; ++i) {
					simulateTime();
					dok = new DokuLink();
					dok.setName(i + entry.getName());
					dok.setPfad(path.substring(startpfadlength));
					dok.setRevision(entry.getRevision());
					dok.setVorhaben(vorhaben);
					dok.setStatus(status);
					dok.setDatum(LocalDateTime.ofInstant(entry.getDate().toInstant(), ZoneId.systemDefault()));
					updateValue(dok);
//					dokuLinkList.add(dok);
				}
				return null;
			}
			
		};
	}
    

	private static void simulateTime() throws InterruptedException {
//		Random rnd = new Random(System.currentTimeMillis());
//		long millis = rnd.nextInt(250);
		Thread.sleep(50);
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
				dok.setDatum(LocalDateTime.ofInstant(entry.getDate().toInstant(), ZoneId.systemDefault()));
				dokuLinkList.add(dok);
			}
			if (entry.getKind() == SVNNodeKind.DIR) {
				findEntries(svn_repo, path + "/" + eName, startpfadlength, name, dokuLinkList);
			}
			
		}
	}
}