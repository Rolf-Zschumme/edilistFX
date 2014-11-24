package de.vbl.im.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.vbl.im.model.InSzenario;
import de.vbl.im.tools.IMconstant;

public class TestDatabase {
	private static final Logger logger = LogManager.getLogger(TestDatabase.class.getName()); 
	
	private static EntityManager em = null;
	
	
	public static void main(String[] args) {
		logger.info("Main");
		
		TestDatabase tDB = new TestDatabase();
		tDB.testgetMaxIsNr();
		
	}
	
	private void testgetMaxIsNr() {
		InSzenario is = new InSzenario();
		int i = is.getMaxIsNr(em);
		logger.info("Hoechste IsNr = " + i);
	}

	public TestDatabase() {
		em = setupDataBase();
	}

	private static EntityManager setupDataBase() {
		EntityManagerFactory factory = null;
		try 
		{
			factory = Persistence.createEntityManagerFactory(IMconstant.PERSISTENCE_UNIT_NAME);
		} catch (RuntimeException e) {
			logger.throwing(e);
		}
		EntityManager em = factory.createEntityManager();
		logger.info("ok");
		return em;
	}

}

