package de.vbl.im;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import de.vbl.im.model.InKomponente;
import de.vbl.im.model.InPartner;
import de.vbl.im.model.InSystem;
import de.vbl.im.model.GeschaeftsObjekt;
import de.vbl.im.model.InSzenario;
import de.vbl.im.model.Konfiguration;
import de.vbl.im.model.Repository;

public class DataBaseInit {
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

			for (int i=2; i<=2 ; ++i) {
				switch(i) {
					case 1: 	generateTestObjekts();
					case 2:		generateRealObjekts();
					case 3: 	generateGeschaeftobjekte();
					case 4: 	generateInSzenarios();
					case 5:     generateRepository();
				}
			}

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
			
//			q = em.createQuery("SELECT e FROM Integration e ORDER BY e.inNr");
//			@SuppressWarnings("unchecked")
//			List<Integration> resultList = q.getResultList();
//			for (Integration el : resultList) {
//			   System.out.println(el);
//			}
//			System.out.println("Size: " + resultList.size());
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
		
		Repository repro2 = new Repository();
		repro2.setId(2L);
		repro2.setName("Test-SVN");
		repro2.setLocation("svn://Test-Server/");
		repro2.setStartPfad("/");
		repro2.setBenutzer("Tester");
		repro2.setPasswort("******");
		em.persist(repro2);
		System.out.println("Repository für " + repro2.getName() + " angelegt.");
	}


	private static String toString(Integer i, Integer minlen) {
		String ret = Integer.toString(i);
		while(ret.length() < minlen)
			ret = "0" + ret;
		return ret;
	}
	
	private static void generateTestObjekts() {
		int zSnr = 0;
		int zKnr = 0;
		for (int p=1; p<=50; ++p) {
			String pNr = toString(p,2);
			InPartner partner = new InPartner("Test-P"+ pNr);
			em.persist(partner);
			for (int s=1; s<=20; ++s) {
				InSystem system = null;
				String sNr = toString(++zSnr,3) + "-" + toString(s,2);
				em.persist(system = newSystem(partner,"Test-S"+ sNr + "vP" + pNr));
				for (int k=1; k<=10; ++k) {
					String kNr = toString(++zKnr,4) + "-" +  toString(k,2);
					em.persist(newKomponente(system,"Test-K"+ kNr + "vS" +sNr+ "P"+ pNr));
				}
			}
		}
	}

	static InKomponente k[] = new InKomponente[999];
	
	private static void generateRealObjekts() {
		InPartner partner = null;
		InSystem system = null;

		// 1. neuer Partner mit seinen Systemen und Komponenten anlegen  
		partner = new InPartner("VBL");
		em.persist(partner);
			em.persist(system = newSystem(partner,"SAP CRM"));
					em.persist(k[111] = newKomponente(system,"ANW"));
					em.persist(k[112] = newKomponente(system,"ZGP"));
					em.persist(k[113] = newKomponente(system,"Zulage"));
					em.persist(k[114] = newKomponente(system,"VA"));
			em.persist(system = newSystem(partner,"SAP ERP"));
					em.persist(k[121] = newKomponente(system,"Leistung"));
					em.persist(k[122] = newKomponente(system,"HR"));
					em.persist(k[123] = newKomponente(system,"FSCD"));
					em.persist(k[124] = newKomponente(system,"FI/FS-CD/HR"));
					em.persist(k[125] = newKomponente(system,"IMMO"));
					em.persist(k[126] = newKomponente(system,"TR"));
			em.persist(system = newSystem(partner,"Host(IBM)"));
					em.persist(k[131] = newKomponente(system,"IS-VL"));
					em.persist(k[132] = newKomponente(system,"FV"));
					em.persist(k[133] = newKomponente(system,"UEV"));
			em.persist(system = newSystem(partner,"APC"));
					em.persist(k[141] = newKomponente(system,"DÜVA"));
			em.persist(system = newSystem(partner,"Dateiablage"));
					em.persist(k[151] = newKomponente(system,"FB-AGS"));

		// 2. neuer Partner mit seinen Systemen und Komponenten anlegen
					
		partner = new InPartner("Beteiligte");
		em.persist(partner);
			em.persist(system = newSystem(partner,"Internet"));
					em.persist(k[211] = newKomponente(system,"ftp-Client"));
			em.persist(system = newSystem(partner,"DOI/Testa-Netz"));
					em.persist(k[221] = newKomponente(system,"ftp-Client"));
			em.persist(system = newSystem(partner,"Mail-Netz"));
					em.persist(k[221] = newKomponente(system,"Mail-Client"));
			
		// 3. neuer Partner mit seinen Systemen und Komponenten anlegen  
					
		partner = new InPartner("Finanzamt");
		em.persist(partner);
			em.persist(system = newSystem(partner,"ZfA-Server"));
					em.persist(newKomponente(system,"MQ: MAV"));
			
		// 4. neuer Partner mit seinen Systemen und Komponenten anlegen  
					
		partner = new InPartner("Siteforum");
		em.persist(partner);
			em.persist(system = newSystem(partner,"VBL-Portal"));
					em.persist(newKomponente(system,"FV"));
					
		// 5. neuer Partner mit seinen Systemen und Komponenten anlegen  
					
		partner = new InPartner("ZfA");
		em.persist(partner);
			em.persist(system = newSystem(partner,"ZfA-Server"));
					em.persist(newKomponente(system,"MQ: Zulage"));
					em.persist(newKomponente(system,"Familienkasse"));
					
		// 6. neuer Partner mit seinen Systemen und Komponenten anlegen  
					
		partner = new InPartner("Postrentendienst");
		em.persist(partner);
			em.persist(system = newSystem(partner,"Post-Server"));
					em.persist(newKomponente(system,"ftps-Servis"));
			em.persist(system = newSystem(partner,"Mail-Netz"));
					em.persist(newKomponente(system,"Mail-Client"));
					
		// 7. neuer Partner mit seinen Systemen und Komponenten anlegen  
					
		partner = new InPartner("Hausverwaltungen");
		em.persist(partner);
			em.persist(system = newSystem(partner,"Internet"));
					em.persist(newKomponente(system,"ftp-Client"));

		// 8. neuer Partner mit seinen Systemen und Komponenten anlegen  
		
		partner = new InPartner("Bloomberg");
		em.persist(partner);
					
		// 9. neuer Partner mit seinen Systemen und Komponenten anlegen  
		
		partner = new InPartner("EMA-Provider");
		em.persist(partner);
								
		// 10. neuer Partner mit seinen Systemen und Komponenten anlegen  
		
		partner = new InPartner("Bafin");
		em.persist(partner);

		
	}
	
	// Integrationsszenarios und Konfigurationen
	
	private static void generateInSzenarios() {
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
		
		inSzenario = newInSzenario("FV-Anbindung");
		em.persist(inSzenario);
		em.persist(newKonfiguration("CS_ISIV_Koexistenz"));

		inSzenario = newInSzenario("Materialbeschaffung (MM)");
		em.persist(inSzenario);
		em.persist(newKonfiguration("CS_e-Procurement_Integration_Lieferanten"));
		
		em.persist(newKonfiguration("CS_e-Gov_Portal_Integration"));
		em.persist(newKonfiguration("CS_FS-CD_eAvis_Beitragseingang__RZ_Arbeitgeber"));
		em.persist(newKonfiguration("CS_RC_SAS_BNP__Filetransfer"));
		
		inSzenario = newInSzenario("Zulage-System");
		em.persist(inSzenario);
		em.persist(newKonfiguration("IS_ZUL_ZUSY_SAP"));
		em.persist(newKonfiguration("IS_ZUL_ZUSY_KOEX"));
	}
	
//	private static InKomponente getKompo(String string) {
//    	TypedQuery<InKomponente> tq = em.createQuery(
//			"SELECT k FROM InKomponente k WHERE LOWER(k.name) = LOWER(:n)", InKomponente.class);
//		List<InKomponente> aktuList = tq.getResultList();
//		tq.setParameter("n", string);
//		if (aktuList.size()!=1) {
//			System.out.println("Komponente " + string + " nicht gefunden");
//			return null;
//		}
//		return aktuList.get(0);
//	}

	static GeschaeftsObjekt g[] = new GeschaeftsObjekt[20];
	
	private static void generateGeschaeftobjekte() {
		int anzGO = 0;
		em.persist(g[1]  = new GeschaeftsObjekt("ZGP-Stammdaten"));   		++anzGO;
		em.persist(g[2]  = new GeschaeftsObjekt("ZGP-Beziehungen"));		++anzGO;
		em.persist(g[3]  = new GeschaeftsObjekt("Zahlungsanweisung"));		++anzGO;
		em.persist(g[4]  = new GeschaeftsObjekt("ZA-Protokolle"));			++anzGO;
		em.persist(g[5]  = new GeschaeftsObjekt("ANW-Meldungen"));			++anzGO;
		em.persist(g[6]  = new GeschaeftsObjekt("ANW-Meldungsdokumente"));	++anzGO;
		em.persist(g[7]  = new GeschaeftsObjekt("ZfA-Meldungen"));			++anzGO;
		em.persist(g[8]  = new GeschaeftsObjekt("MM-Bestellanfrage"));		++anzGO;
		em.persist(g[9]  = new GeschaeftsObjekt("MM-Bestellung"));			++anzGO;
		em.persist(g[10] = new GeschaeftsObjekt("MM-Bestelländerung"));		++anzGO;
		em.persist(g[11] = new GeschaeftsObjekt("MM-Materialstammdaten"));	++anzGO;
		em.persist(g[12] = new GeschaeftsObjekt("MM-Einkaufsinfosätze"));	++anzGO;
		System.out.println(anzGO + " Geschaeftobjekte() angelegt");
	}
	
	private static InSzenario newInSzenario (String isznearioName) {
		InSzenario inSzenario = new InSzenario();
		inSzenario.setName(isznearioName);
		inSzenario.setIsNr(inSzenario.getMaxIsNr(em)+1);
		return inSzenario;
	}

	private static Konfiguration newKonfiguration( String konfigName) 
	{
		System.out.println("Konfiguration anlegen mit Name \"" + konfigName + "\"");
		
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
