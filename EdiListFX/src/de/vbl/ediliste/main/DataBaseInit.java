package de.vbl.ediliste.main;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiKomponente;
import de.vbl.ediliste.model.EdiPartner;
import de.vbl.ediliste.model.EdiSystem;
import de.vbl.ediliste.model.GeschaeftsObjekt;
import de.vbl.ediliste.model.Integration;
import de.vbl.ediliste.model.Konfiguration;

public class DataBaseInit {
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";
	
	
	private static EntityManager em = null;

	public static void main(String[] args) {
		
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		em = factory.createEntityManager();
		EntityTransaction ta = null;

		// read the existing entries and write to console
 		Query q = em.createQuery("select a from Integration a");
		@SuppressWarnings("unchecked")
		List<Integration> anbindungsList = q.getResultList();
		for (Integration anbindung : anbindungsList) {
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
					case 4: 	generateSzenarios();
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
			
			q = em.createQuery("SELECT e FROM EdiEintrag e ORDER BY e.ediNr");
			@SuppressWarnings("unchecked")
			List<EdiEintrag> ediList = q.getResultList();
			for (EdiEintrag el : ediList) {
			   System.out.println(el);
			}
			System.out.println("Size: " + ediList.size());
			System.out.println("DatabaseInit beendet");
			em.close();
		}	
			
			
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

	private static void generateRealObjekts() {
		EdiPartner partner = null;
		EdiSystem system = null;

		// 1. neuer Partner mit seinen Systemen und Komponenten anlegen  

		partner = new EdiPartner("VBL");
		em.persist(partner);
			em.persist(system = newSystem(partner,"SAP CRM"));
					em.persist(newKomponente(system,"ANW"));
					em.persist(newKomponente(system,"ZGP"));
					em.persist(newKomponente(system,"Zulage"));
					em.persist(newKomponente(system,"VA"));
			em.persist(system = newSystem(partner,"SAP ERP"));
					em.persist(newKomponente(system,"Leistung"));
					em.persist(newKomponente(system,"HR"));
					em.persist(newKomponente(system,"FSCD"));
					em.persist(newKomponente(system,"FI/FS-CD/HR"));
					em.persist(newKomponente(system,"IMMO"));
					em.persist(newKomponente(system,"TR"));
			em.persist(system = newSystem(partner,"Host(IBM)"));
					em.persist(newKomponente(system,"IS-VL"));
					em.persist(newKomponente(system,"FV"));
					em.persist(newKomponente(system,"UEV"));
			em.persist(system = newSystem(partner,"APC"));
					em.persist(newKomponente(system,"DÜVA"));
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
	
	// Integrationen und Konfigurationen
	
	private static void generateSzenarios() {
		Integration integration = null;

		integration = newIntegration("ANW Meldungsverarbeitung");
		em.persist(integration);
		em.persist(newKonfiguration(integration, "CS_ANW_MLD__Meldungseingang"));
		em.persist(newKonfiguration(integration, "CS_ANW_MLD__Meldungsausgang"));

		integration = newIntegration("LST Zahlungenaufträge");
		em.persist(integration);
		em.persist(newKonfiguration(integration, "CS_LSTG_Mitteilung_Leistungstraeger__DPAG_Rentenservice"));
		em.persist(newKonfiguration(integration, "CS_LSTG_Zahlungsanweisung__DPAG_Rentenservice"));
		
		integration = newIntegration("CRM Beschäftspartner-Replikation");
		em.persist(integration);
		em.persist(newKonfiguration(integration, "CS_ZGP_Replikation__CRM__to__ERP"));
		em.persist(newKonfiguration(integration, "CS_ZGP_Replikation__ERP__to__CRM"));
		
		integration = newIntegration("FV-Anbindung");
		em.persist(integration);
		em.persist(newKonfiguration(integration, "CS_ISIV_Koexistenz"));

		integration = newIntegration("Materialbeschaffung (MM)");
		em.persist(integration);
		em.persist(newKonfiguration(integration, "CS_e-Procurement_Integration_Lieferanten"));
		
		em.persist(newKonfiguration(null, "CS_e-Gov_Portal_Integration"));
		em.persist(newKonfiguration(null, "CS_FS-CD_eAvis_Beitragseingang__RZ_Arbeitgeber"));
		em.persist(newKonfiguration(null, "CS_RC_SAS_BNP__Filetransfer"));
		
		integration = newIntegration("Zulage-System");
		em.persist(integration);
		em.persist(newKonfiguration(integration, "IS_ZUL_ZUSY_SAP"));
		em.persist(newKonfiguration(integration, "IS_ZUL_ZUSY_KOEX"));
	}
	
	private static void generateGeschaeftobjekte() {
		em.persist(new GeschaeftsObjekt("ZGP-Stammdaten"));
		em.persist(new GeschaeftsObjekt("ZGP-Beziehungen"));
		em.persist(new GeschaeftsObjekt("Zahlungsanweisung"));
		em.persist(new GeschaeftsObjekt("ZA-Protokolle"));
		em.persist(new GeschaeftsObjekt("ANW-Meldungen"));
		em.persist(new GeschaeftsObjekt("ANW-Meldungsdokumente"));
		em.persist(new GeschaeftsObjekt("ZfA-Meldungen"));
		em.persist(new GeschaeftsObjekt("MM-Bestellanfrage"));
		em.persist(new GeschaeftsObjekt("MM-Bestellung"));
		em.persist(new GeschaeftsObjekt("MM-Bestelländerung"));
		em.persist(new GeschaeftsObjekt("MM-Materialstammdaten"));
		em.persist(new GeschaeftsObjekt("MM-Einkaufsinfosätze"));
		System.out.println("12 Geschaeftobjekte() angelegt");
	}
	
	private static Integration newIntegration (String integrationName) {
		Integration integration = new Integration();
		integration.setName(integrationName);
		return integration;
	}

	private static Konfiguration newKonfiguration( Integration integration,	String konfigName) 
	{
		System.out.print("Edi_Konfiguration anlegen ");
		if (integration != null) {
			System.out.print("für \"" + integration.getName() + "\" ");
		}
		System.out.println("mit Name \"" + konfigName + "\"");
		
		Konfiguration konfiguration = new Konfiguration();
		konfiguration.setName(konfigName);
		konfiguration.setIntegration(integration);
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
