package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.ArrayList;
import java.util.Collection;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import javax.persistence.JoinColumn;
import static javax.persistence.CascadeType.ALL;

@Entity
public class EdiEintrag {
	public static final int EDI_NR_MIN_LEN = 3;
	public static final String FORMAT_EDINR = " %03d";
	private long id;
	private IntegerProperty ediNr;
	private StringProperty bezeichnung;
	private StringProperty beschreibung;
	private StringProperty seitDatum;
	private StringProperty bisDatum;
	private Konfiguration konfiguration;
	private EdiKomponente ediKomponente;
	private Collection<EdiEmpfaenger> ediEmpfaenger;
	private String laeDatum; 
	private String laeUser;
	
	private StringProperty senderName;
	private StringProperty integrationName;
	

	public EdiEintrag() {
		ediNr = new SimpleIntegerProperty();
		bezeichnung = new SimpleStringProperty();
		beschreibung = new SimpleStringProperty();
		ediEmpfaenger = new ArrayList<EdiEmpfaenger>();
		seitDatum = new SimpleStringProperty();
		bisDatum = new SimpleStringProperty();
		senderName = new SimpleStringProperty();
		integrationName = new SimpleStringProperty("");
	}	

	// ------------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = IDENTITY)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	// ------------------------------------------------------------------------
	public IntegerProperty ediNrProperty() {
		return ediNr;
	}

	public Integer getEdiNr() {
		return ediNr.get();
	}

	public void setEdiNr(Integer param) {
		ediNr.set(param);
	}
	
	public String getEdiNrStr() {
		String ret = Integer.toString(getEdiNr());
		while(ret.length()<EDI_NR_MIN_LEN) ret = "0"+ ret;
		return ret;
	}

	// ------------------------------------------------------------------------
	public StringProperty bezeichnungProperty() {
		return bezeichnung;
	}

	public String getBezeichnung() {
		return bezeichnung.get();
	}

	public void setBezeichnung(String param) {
		bezeichnung.set(param);
	}

	// ------------------------------------------------------------------------
	public StringProperty beschreibungProperty() {
		return beschreibung;
	}
	public String getBeschreibung() {
		return beschreibung.get();
	}
	
	public void setBeschreibung(String param) {
		beschreibung.set(param);
	}
	
	// ------------------------------------------------------------------------
	public StringProperty senderNameProperty() {
		return senderName;
	}

	 public void setSenderName(String param) {
		 senderName.set(param);
	 }
	 
	// ------------------------------------------------------------------------
	@ManyToOne(cascade = ALL)
	@JoinColumn(referencedColumnName = "id")
	public Konfiguration getKonfiguration() {
		return konfiguration; 
	}

	public void setKonfiguration(Konfiguration param) {
		this.konfiguration = param;
		if (param != null) {
			this.integrationName.unbind();
			this.integrationName.bind(param.integrationNameProperty());
		}	
	}

	@ManyToOne
	public EdiKomponente getEdiKomponente() {
		return ediKomponente;
	}

	public void setEdiKomponente(EdiKomponente kompo) {
		if (ediKomponente != null) {
			senderName.unbind();
		}
		ediKomponente = kompo;
		if (kompo != null) {
			senderName.bind(kompo.fullnameProperty());
		}	
	}

	@OneToMany(mappedBy = "ediEintrag", cascade = ALL)
	public Collection<EdiEmpfaenger> getEdiEmpfaenger() {
		return ediEmpfaenger;
	}

	public void setEdiEmpfaenger(Collection<EdiEmpfaenger> param) {
		this.ediEmpfaenger = param;
	}
 
	public StringProperty seitDatumProperty() {
		return seitDatum;
	}
	public String getSeitDatum() {
		return seitDatum.get();
	}
	public void setSeitDatum(String param) {
		seitDatum.set(param);
	}

	public StringProperty bisDatumProperty() {
		return bisDatum;
	}
	public String getBisDatum() {
		return bisDatum.get();
	}
	public void setBisDatum(String param) {
		bisDatum.set(param);
	}
	
	public String getLaeDatum() {
		return laeDatum;
	}

	public void setLaeDatum(String laeDatum) {
		this.laeDatum = laeDatum;
	}
	
	public String getLaeUser() {
		return laeUser;
	}

	public void setLaeUser(String laeUser) {
		this.laeUser = laeUser;
	}

//	public boolean equaels (EdiEintrag tEDI) {
//		if ( (id == tEDI.id)                          								&&
//			 (ediNr.get() == tEDI.ediNr.get())		  							  	&&	
//		     (beschreibung.getValueSafe().equals(tEDI.beschreibung.getValueSafe()))	&&
//		     (konfiguration == tEDI.konfiguration)                  				&&
//		     (ediKomponente == tEDI.ediKomponente)									&&
//		     (seitDatum == tEDI.seitDatum || (seitDatum != null && seitDatum.equals(tEDI.bisDatum))) &&
//		     (bisDatum == tEDI.bisDatum || (bisDatum != null && bisDatum.equals(tEDI.bisDatum))) &&
//		     (empfaengerListIsEqual(ediEmpfaenger,tEDI.ediEmpfaenger)) 	) {
//		     		return true;
//		}
//		return false;
//	}
	
//	private boolean empfaengerListIsEqual( Collection<EdiEmpfaenger> empf1,
//			Collection<EdiEmpfaenger> empf2) {
//		if (empf1.size() == empf2.size()) {
//			Iterator<EdiEmpfaenger> i1 = empf1.iterator();
//			Iterator<EdiEmpfaenger> i2 = empf2.iterator();
//			while (i1.hasNext()) {
//				EdiEmpfaenger e1 = i1.next();
//				EdiEmpfaenger e2 = i2.next();
//				if ( !e1.equaels(e2) )
//					return false;
//			}
//			return true;
//		}
//		return false;
//	}
	
//	public void copy (EdiEintrag source) {
//		this.id = source.id;
//		this.setEdiNr(source.getEdiNr());
//		this.setBezeichnung(source.getBezeichnung());
//		this.setBeschreibung(source.getBeschreibung());
//		this.setEdiKomponente(source.ediKomponente);
//		this.setKonfiguration(source.konfiguration);
//
//		Iterator<EdiEmpfaenger> is = source.ediEmpfaenger.iterator();
//		while(is.hasNext()) {
//			EdiEmpfaenger e = is.next();
//			System.out.println("Copy: S-Empf-Id="+ e.getId() +" S-Edi-Id="+ e.getEdiEintrag().getId() + " " + e.getKomponente().getFullname()); 
//		}
//		
//		if (source.ediEmpfaenger == null) {
//			this.setEdiEmpfaenger(null);
//		} else {
//			
//			this.ediEmpfaenger.clear();
//			for (EdiEmpfaenger e : source.ediEmpfaenger) {
//				this.ediEmpfaenger.add(e);
//			}
//		}
//
//		if (this.ediEmpfaenger == null) {
//			System.out.println("Copy: T-Empf   = null");
//		}
//		else {
//			Iterator<EdiEmpfaenger> it = this.ediEmpfaenger.iterator();
//			while(it.hasNext()) {
//				EdiEmpfaenger e = it.next();
//				System.out.println("Copy: T-Empf-Id="+ e.getId() +" T-Edi-Id="+ e.getEdiEintrag().getId()+ " " + e.getKomponente().getFullname()); 
//			}	
//		}
//		
//		this.setSeitDatum(source.seitDatum.get());
//		this.setBisDatum(source.bisDatum.get());
//		this.laeDatum = source.laeDatum;
//		this.laeUser = source.laeUser;
//		}
	
	public StringProperty intregrationName () {
		return integrationName;
	}

    public String autoBezeichnung() {
    	String intSzeName = "I??";
    	String empf01Name = "E??";
    	String geOb01Name = "G??";
    	if (ediEmpfaenger.size() > 0) {
    		EdiEmpfaenger e01 = ediEmpfaenger.iterator().next();
    		if (e01.getKomponente() != null) {
    			empf01Name = e01.getKomponente().getFullname();
    		}
    		if (e01.getGeschaeftsObjekt() != null) {
    			geOb01Name = e01.getGeschaeftsObjekt().getName();
    		}
    	}
    	if (konfiguration != null) {
    		if (konfiguration.getIntegration() != null) {
    			if (konfiguration.getIntegration().getName() != null)
    				intSzeName = konfiguration.getIntegration().getName();
    		}
    	}	
    	return intSzeName + "  [" + senderName.get() + "  >>  " + empf01Name + ": " + geOb01Name + "]";
    }
}
