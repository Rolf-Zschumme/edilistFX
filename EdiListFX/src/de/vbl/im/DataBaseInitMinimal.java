package de.vbl.im;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import de.vbl.im.model.Integration;
import de.vbl.im.model.EdiKomponente;
import de.vbl.im.model.EdiPartner;
import de.vbl.im.model.EdiSystem;
import de.vbl.im.model.GeschaeftsObjekt;
import de.vbl.im.model.Iszenario;
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
 		Query q = em.createQuery("select a from Iszenario a");
		@SuppressWarnings("unchecked")
		List<Iszenario> anbindungsList = q.getResultList();
		for (Iszenario anbindung : anbindungsList) {
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
			generateEdiEintraege();

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
			
			q = em.createQuery("SELECT e FROM Integration e ORDER BY e.ediNr");
			@SuppressWarnings("unchecked")
			List<Integration> ediList = q.getResultList();
			for (Integration el : ediList) {
			   System.out.println(el);
			}
			System.out.println("Size: " + ediList.size());
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
		repro1.setBenutzer("adelfinop");
		repro1.setPasswort("16WKGE");
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
		EdiPartner partner = null;
		EdiSystem system = null;

		// 1. neuer Partner mit seinen Systemen und Komponenten anlegen  

		partner = new EdiPartner("VBL");
		em.persist(partner);
			em.persist(system = newSystem(partner,"SAP CRM"));
					em.persist(newKomponente(system,"ANW"));
			em.persist(system = newSystem(partner,"Dateiablage"));
					em.persist(newKomponente(system,"FB-AGS"));

		// 2. neuer Partner mit seinen Systemen und Komponenten anlegen
					
		partner = new EdiPartner("Beteiligte");
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
		Iszenario iszenario = null;

		iszenario = newISzenario("ANW Meldungsverarbeitung");
		em.persist(iszenario);
		em.persist(newKonfiguration(iszenario, "CS_ANW_MLD__Meldungseingang"));
		em.persist(newKonfiguration(iszenario, "CS_ANW_MLD__Meldungsausgang"));

		iszenario = newISzenario("LST Zahlungenaufträge");
		em.persist(iszenario);
		em.persist(newKonfiguration(iszenario, "CS_LSTG_Mitteilung_Leistungstraeger__DPAG_Rentenservice"));
		em.persist(newKonfiguration(iszenario, "CS_LSTG_Zahlungsanweisung__DPAG_Rentenservice"));
		
		iszenario = newISzenario("CRM Beschäftspartner-Replikation");
		em.persist(iszenario);
		em.persist(newKonfiguration(iszenario, "CS_ZGP_Replikation__CRM__to__ERP"));
		em.persist(newKonfiguration(iszenario, "CS_ZGP_Replikation__ERP__to__CRM"));
		
		iszenario = newISzenario("Materialbeschaffung (MM)");
		em.persist(iszenario);
		em.persist(newKonfiguration(iszenario, "CS_e-Procurement_Integration_Lieferanten"));
		
		em.persist(newKonfiguration(null, "CS_ISIV_Koexistenz"));
		em.persist(newKonfiguration(null, "CS_e-Gov_Portal_Integration"));
		em.persist(newKonfiguration(null, "CS_FS-CD_eAvis_Beitragseingang__RZ_Arbeitgeber"));
		em.persist(newKonfiguration(null, "CS_RC_SAS_BNP__Filetransfer"));
	}

	private static void generateEdiEintraege() {
//		Integration edi = new Integration();
//		edi.setEdiNr(1);
//		edi.setBezeichnung("Rima-Meldungen via-Internet");
	}

	private static void generateGeschaeftobjekte() {
		int anzGO = 0;
		em.persist(new GeschaeftsObjekt("ANW-Meldungen"));			++anzGO;
		em.persist(new GeschaeftsObjekt("ANW-Meldungsdokumente"));	++anzGO;
		System.out.println(anzGO + " Geschaeftobjekte() angelegt");
	}
	
	private static Iszenario newISzenario (String iszenarioName) {
		Iszenario iszenario = new Iszenario();
		iszenario.setName(iszenarioName);
		return iszenario;
	}

	private static Konfiguration newKonfiguration( Iszenario iszenario,	String konfigName) 
	{
		System.out.print("Edi_Konfiguration anlegen ");
		if (iszenario != null) {
			System.out.print("für \"" + iszenario.getName() + "\" ");
		}
		System.out.println("mit Name \"" + konfigName + "\"");
		
		Konfiguration konfiguration = new Konfiguration();
		konfiguration.setName(konfigName);
		konfiguration.setIszenario(iszenario);
		return konfiguration;
	}
	
	private static EdiSystem newSystem(EdiPartner partner, String name) 
	{
		EdiSystem system = new EdiSystem();
		system.setName(name);
		system.setEdiPartner(partner);
		return system;
	}

	private static EdiKomponente newKomponente(	EdiSystem system, 
												String name		) 
	{
		System.out.println("Edi_Komponente anlegen für \"" + system.getName() +
												"\" mit Name \"" + name + "\"");
		EdiKomponente k = new EdiKomponente(name,system);
		return k;
	}
	
	
}
