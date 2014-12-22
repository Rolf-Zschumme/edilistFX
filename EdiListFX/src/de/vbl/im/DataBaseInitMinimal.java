package de.vbl.im;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import de.vbl.im.model.Integration;
import de.vbl.im.model.InKomponente;
import de.vbl.im.model.InPartner;
import de.vbl.im.model.InSystem;
import de.vbl.im.model.GeschaeftsObjekt;
import de.vbl.im.model.InSzenario;
import de.vbl.im.model.Konfiguration;
import de.vbl.im.model.Repository;

public class DataBaseInitMinimal {
	private static final String PERSISTENCE_UNIT_NAME = "IntegrationManager";
	private static EntityManager em = null;

	public static void main(String[] args) {
		
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		em = factory.createEntityManager();
		EntityTransaction ta = null;

		// read the existing entries and write to console
 		Query q = em.createQuery("select a from InSzenario a");
		@SuppressWarnings("unchecked")
		List<InSzenario> anbindungsList = q.getResultList();
		for (InSzenario anbindung : anbindungsList) {
		   System.out.println(anbindung);
		}
		System.out.println("Anzahl Anbindungen: " + anbindungsList.size());
		
		try {
			ta = em.getTransaction();
			
			ta.begin();

			generateRealObjekts();
			generateGeschaeftobjekte();
			generateSzenarios();
			generateRepository();
			generateIntegration();

			if (ta.isActive()) {
				System.out.println("Transaction vor commit isActive=TRUE");
			}	
			ta.commit();
			System.out.println("Daten erfolgreich in DB eingetragen");
						
		} catch (RuntimeException e) {
			if (ta != null && ta.isActive()) {
				System.out.println("Rollback wird durchgeführt");
			}
			e.printStackTrace();
		}
		finally {
			
			q = em.createQuery("SELECT e FROM Integration e ORDER BY e.inNr");
			@SuppressWarnings("unchecked")
			List<Integration> resultList = q.getResultList();
			for (Integration el : resultList) {
			   System.out.println(el);
			}
			System.out.println("Size: " + resultList.size());
			System.out.println("DatabaseInit beendet");
			em.close();
		}	
			
			
	}
	
	private static void generateRepository() {
		Repository repro1 = new Repository();
		
		repro1.setId(1L);
		repro1.setName("QS-Akte");
		repro1.setLocation("svn://itneu/SE-Akten");
		repro1.setStartPfad("/03_QS-Akte");
		repro1.setBenutzer("ad");
		repro1.setPasswort("****");
		em.persist(repro1);
		System.out.println("Repository für " + repro1.getName() + " angelegt.");
	}


//	private static String toString(Integer i, Integer minlen) {
//		String ret = Integer.toString(i);
//		while(ret.length() < minlen)
//			ret = "0" + ret;
//		return ret;
//	}
	
	private static void generateRealObjekts() {
		InPartner partner = null;
		InSystem system = null;

		// 1. neuer Partner mit seinen Systemen und Komponenten anlegen  

		partner = new InPartner("VBL");
		em.persist(partner);
			em.persist(system = newSystem(partner,"SAP CRM"));
					em.persist(newKomponente(system,"ANW"));
			em.persist(system = newSystem(partner,"Dateiablage"));
					em.persist(newKomponente(system,"FB-AGS"));

		// 2. neuer Partner mit seinen Systemen und Komponenten anlegen
					
		partner = new InPartner("Beteiligte");
		em.persist(partner);
			em.persist(system = newSystem(partner,"Internet"));
					em.persist(newKomponente(system,"ftp-Client"));
			em.persist(system = newSystem(partner,"DOI/Testa-Netz"));
					em.persist(newKomponente(system,"ftp-Client"));
			em.persist(system = newSystem(partner,"Mail-Netz"));
					em.persist(newKomponente(system,"Mail-Client"));
			
	}
	
	// Integrationsszenarien und Konfigurationen
	
	private static void generateSzenarios() {
		InSzenario inSzenario = null;

		inSzenario = newInSzenario("ANW Meldungsverarbeitung");
		em.persist(inSzenario);
		em.persist(newKonfiguration("CS_ANW_MLD__Meldungseingang"));
		em.persist(newKonfiguration("CS_ANW_MLD__Meldungsausgang"));

		inSzenario = newInSzenario("LST Zahlungenaufträge");
		em.persist(inSzenario);
		em.persist(newKonfiguration("CS_LSTG_Mitteilung_Leistungstraeger__DPAG_Rentenservice"));
		em.persist(newKonfiguration("CS_LSTG_Zahlungsanweisung__DPAG_Rentenservice"));
		
		inSzenario = newInSzenario("CRM Beschäftspartner-Replikation");
		em.persist(inSzenario);
		em.persist(newKonfiguration("CS_ZGP_Replikation__CRM__to__ERP"));
		em.persist(newKonfiguration("CS_ZGP_Replikation__ERP__to__CRM"));
		
		inSzenario = newInSzenario("Materialbeschaffung (MM)");
		em.persist(inSzenario);
		em.persist(newKonfiguration("CS_e-Procurement_Integration_Lieferanten"));
		
		em.persist(newKonfiguration("CS_ISIV_Koexistenz"));
		em.persist(newKonfiguration("CS_e-Gov_Portal_Integration"));
		em.persist(newKonfiguration("CS_FS-CD_eAvis_Beitragseingang__RZ_Arbeitgeber"));
		em.persist(newKonfiguration("CS_RC_SAS_BNP__Filetransfer"));
	}

	private static void generateIntegration() {
//		Integration inte = new Integration();
//		inte.setInNr(1);
//		inte.setBezeichnung("Rima-Meldungen via-Internet");
	}

	private static void generateGeschaeftobjekte() {
		int anzGO = 0;
		em.persist(new GeschaeftsObjekt("ANW-Meldungen"));			++anzGO;
		em.persist(new GeschaeftsObjekt("ANW-Meldungsdokumente"));	++anzGO;
		System.out.println(anzGO + " Geschaeftobjekte() angelegt");
	}
	
	private static InSzenario newInSzenario (String inSzenarioName) {
		InSzenario inSzenario = new InSzenario();
		inSzenario.setName(inSzenarioName);
		return inSzenario;
	}

	private static Konfiguration newKonfiguration(String konfigName) 
	{
		System.out.print("Konfiguration anlegen mit Name \"" + konfigName + "\"");
		
		Konfiguration konfiguration = new Konfiguration();
		konfiguration.setName(konfigName);
		return konfiguration;
	}
	
	private static InSystem newSystem(InPartner partner, String name) 
	{
		InSystem system = new InSystem();
		system.setName(name);
		system.setinPartner(partner);
		return system;
	}

	private static InKomponente newKomponente(	InSystem system, 
												String name		) 
	{
		System.out.println("Komponente anlegen für \"" + system.getName() +
												"\" mit Name \"" + name + "\"");
		InKomponente k = new InKomponente(name,system);
		return k;
	}
	
	
}
