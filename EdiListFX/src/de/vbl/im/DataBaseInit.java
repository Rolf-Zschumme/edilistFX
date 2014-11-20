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

public class DataBaseInit {
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

			for (int i=2; i<=2 ; ++i) {
				switch(i) {
					case 1: 	generateTestObjekts();
					case 2:		generateRealObjekts();
					case 3: 	generateGeschaeftobjekte();
					case 4: 	generateISzenarios();
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
			
			q = em.createQuery("SELECT e FROM Integration e ORDER BY e.iNr");
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
			EdiPartner partner = new EdiPartner("Test-P"+ pNr);
			em.persist(partner);
			for (int s=1; s<=20; ++s) {
				EdiSystem system = null;
				String sNr = toString(++zSnr,3) + "-" + toString(s,2);
				em.persist(system = newSystem(partner,"Test-S"+ sNr + "vP" + pNr));
				for (int k=1; k<=10; ++k) {
					String kNr = toString(++zKnr,4) + "-" +  toString(k,2);
					em.persist(newKomponente(system,"Test-K"+ kNr + "vS" +sNr+ "P"+ pNr));
				}
			}
		}
	}

	static EdiKomponente k[] = new EdiKomponente[999];
	
	private static void generateRealObjekts() {
		EdiPartner partner = null;
		EdiSystem system = null;

		// 1. neuer Partner mit seinen Systemen und Komponenten anlegen  
		partner = new EdiPartner("VBL");
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
					
		partner = new EdiPartner("Beteiligte");
		em.persist(partner);
			em.persist(system = newSystem(partner,"Internet"));
					em.persist(k[211] = newKomponente(system,"ftp-Client"));
			em.persist(system = newSystem(partner,"DOI/Testa-Netz"));
					em.persist(k[221] = newKomponente(system,"ftp-Client"));
			em.persist(system = newSystem(partner,"Mail-Netz"));
					em.persist(k[221] = newKomponente(system,"Mail-Client"));
			
		// 3. neuer Partner mit seinen Systemen und Komponenten anlegen  
					
		partner = new EdiPartner("Finanzamt");
		em.persist(partner);
			em.persist(system = newSystem(partner,"ZfA-Server"));
					em.persist(newKomponente(system,"MQ: MAV"));
			
		// 4. neuer Partner mit seinen Systemen und Komponenten anlegen  
					
		partner = new EdiPartner("Siteforum");
		em.persist(partner);
			em.persist(system = newSystem(partner,"VBL-Portal"));
					em.persist(newKomponente(system,"FV"));
					
		// 5. neuer Partner mit seinen Systemen und Komponenten anlegen  
					
		partner = new EdiPartner("ZfA");
		em.persist(partner);
			em.persist(system = newSystem(partner,"ZfA-Server"));
					em.persist(newKomponente(system,"MQ: Zulage"));
					em.persist(newKomponente(system,"Familienkasse"));
					
		// 6. neuer Partner mit seinen Systemen und Komponenten anlegen  
					
		partner = new EdiPartner("Postrentendienst");
		em.persist(partner);
			em.persist(system = newSystem(partner,"Post-Server"));
					em.persist(newKomponente(system,"ftps-Servis"));
			em.persist(system = newSystem(partner,"Mail-Netz"));
					em.persist(newKomponente(system,"Mail-Client"));
					
		// 7. neuer Partner mit seinen Systemen und Komponenten anlegen  
					
		partner = new EdiPartner("Hausverwaltungen");
		em.persist(partner);
			em.persist(system = newSystem(partner,"Internet"));
					em.persist(newKomponente(system,"ftp-Client"));

		// 8. neuer Partner mit seinen Systemen und Komponenten anlegen  
		
		partner = new EdiPartner("Bloomberg");
		em.persist(partner);
					
		// 9. neuer Partner mit seinen Systemen und Komponenten anlegen  
		
		partner = new EdiPartner("EMA-Provider");
		em.persist(partner);
								
		// 10. neuer Partner mit seinen Systemen und Komponenten anlegen  
		
		partner = new EdiPartner("Bafin");
		em.persist(partner);

		
	}
	
	// Integrationsszenarios und Konfigurationen
	
	private static void generateISzenarios() {
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
		
		iszenario = newISzenario("FV-Anbindung");
		em.persist(iszenario);
		em.persist(newKonfiguration(iszenario, "CS_ISIV_Koexistenz"));

		iszenario = newISzenario("Materialbeschaffung (MM)");
		em.persist(iszenario);
		em.persist(newKonfiguration(iszenario, "CS_e-Procurement_Integration_Lieferanten"));
		
		em.persist(newKonfiguration(null, "CS_e-Gov_Portal_Integration"));
		em.persist(newKonfiguration(null, "CS_FS-CD_eAvis_Beitragseingang__RZ_Arbeitgeber"));
		em.persist(newKonfiguration(null, "CS_RC_SAS_BNP__Filetransfer"));
		
		iszenario = newISzenario("Zulage-System");
		em.persist(iszenario);
		em.persist(newKonfiguration(iszenario, "IS_ZUL_ZUSY_SAP"));
		em.persist(newKonfiguration(iszenario, "IS_ZUL_ZUSY_KOEX"));
	}
	
//	private static EdiKomponente getKompo(String string) {
//    	TypedQuery<EdiKomponente> tq = em.createQuery(
//			"SELECT k FROM EdiKomponente k WHERE LOWER(k.name) = LOWER(:n)", EdiKomponente.class);
//		List<EdiKomponente> aktuList = tq.getResultList();
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
	
	private static Iszenario newISzenario (String isznearioName) {
		Iszenario iszenario = new Iszenario();
		iszenario.setName(isznearioName);
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
