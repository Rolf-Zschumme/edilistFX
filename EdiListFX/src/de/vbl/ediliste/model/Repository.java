package de.vbl.ediliste.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Column;
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
		DAVRepositoryFactory.setup();

		Repository aktRepo = repoList.get(0);
		this.id        = aktRepo.id;
		this.name      = aktRepo.name;
		this.location  = aktRepo.location;
		this.startPfad = aktRepo.startPfad;
		this.benutzer  = aktRepo.benutzer; 
		
		try {
			SVNURL url = SVNURL.parseURIEncoded(this.location);
			svn_repository = SVNRepositoryFactory.create(url);
		} catch (SVNException e) {
			e.printStackTrace();
		}
		ISVNAuthenticationManager authManager = SVNWCUtil
				.createDefaultAuthenticationManager(
						this.benutzer, aktRepo.getPasswort());
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
	
    public Collection<DokuLink> findEntries(String name, String firstLevel) {
		if (svn_repository == null) {
			throw new RuntimeException("Repository is null");
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