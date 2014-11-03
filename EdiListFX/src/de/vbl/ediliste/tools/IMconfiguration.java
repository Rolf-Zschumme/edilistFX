package de.vbl.ediliste.tools;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import de.vbl.ediliste.model.Repository;

public class IMconfiguration {

	public SVNRepository readRepository (String name, EntityManager em) throws SVNException {
		
		TypedQuery<Repository> tq = em.createQuery(
				"SELECT r FROM Repository r WHERE r.name = :n", Repository.class);
		tq.setParameter("n", name);
		
		List<Repository> repoList = tq.getResultList();
		if (repoList.size() != 1) {
			return null;
		}
		DAVRepositoryFactory.setup();

		Repository aktRepo = repoList.get(0);
		
		SVNURL url = SVNURL.parseURIEncoded(aktRepo.getLocation());
		SVNRepository repository = SVNRepositoryFactory.create(url);

//		ISVNAuthenticationManager authManager = SVNWCUtil
//				.createDefaultAuthenticationManager(aktRepo.getUser(), 
//												    aktRepo.getPasswort());
//		repository.setAuthenticationManager(authManager);

		return repository;
	}
}
