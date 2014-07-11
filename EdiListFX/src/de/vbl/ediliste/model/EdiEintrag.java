package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import static javax.persistence.CascadeType.ALL;

@Entity
public class EdiEintrag {
	public static final int EDI_NR_MIN_LEN = 3;
	public static final String FORMAT_EDINR = " %03d";
	private IntegerProperty ediNr = new SimpleIntegerProperty();
	private StringProperty bezeichnung = new SimpleStringProperty();
	private StringProperty beschreibung = new SimpleStringProperty();
	private StringProperty senderName = new SimpleStringProperty();
	private long id;
	private Konfiguration konfiguration;
	private EdiKomponente ediKomponente;
	private Collection<EdiEmpfaenger> ediEmpfaenger;
	private String seitDatum;
	private String bisDatum;
	private String laeDatum; 
	private String laeUser; 

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
	@ManyToOne
	public Konfiguration getKonfiguration() {
		return konfiguration; 
	}

	public void setKonfiguration(Konfiguration param) {
		this.konfiguration = param;
	}

	@ManyToOne
	public EdiKomponente getEdiKomponente() {
		return ediKomponente;
	}

	public void setEdiKomponente(EdiKomponente kompo) {
		String senderName = (kompo == null) ? "?k?" : kompo.getFullname(); 
   		this.senderName.set(senderName);
		this.ediKomponente = kompo;
	}

	@OneToMany(mappedBy = "ediEintrag", cascade = ALL)
	public Collection<EdiEmpfaenger> getEdiEmpfaenger() {
		return ediEmpfaenger;
	}

	public void setEdiEmpfaenger(Collection<EdiEmpfaenger> param) {
		this.ediEmpfaenger = param;
	}
 
	public String getSeitDatum() {
		return seitDatum;
	}

	public void setSeitDatum(String param) {
		this.seitDatum = param;
	}

	public String getBisDatum() {
		return bisDatum;
	}

	public void setBisDatum(String param) {
		this.bisDatum = param;
	}
	
	public String getLaeDatum() {
		System.out.println("laeDatum ("+ ediNr.getValue() + "," + ediNr.get() + ") ="  + laeDatum);
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

	public boolean equaels (EdiEintrag tEDI) {
		if ( (id == tEDI.id)                          								&&
			 (ediNr.get() == tEDI.ediNr.get())		  							  	&&	
		     (beschreibung.getValueSafe().equals(tEDI.beschreibung.getValueSafe()))	&&
		     (konfiguration == tEDI.konfiguration)                  				&&
		     (ediKomponente == tEDI.ediKomponente)									&&
		     (empfaengerListIsEqual(ediEmpfaenger,tEDI.ediEmpfaenger)) 	) {
		     		return true;
		}
		return false;
	}
	
	public void copy (EdiEintrag source) {
		this.setEdiNr(source.getEdiNr());
		this.setBezeichnung(source.getBezeichnung());
		this.setBeschreibung(source.getBeschreibung());
		this.setSenderName(source.senderName.get());
		
		this.id = source.id;
		this.konfiguration = source.konfiguration;
		this.ediKomponente = source.ediKomponente;
		this.setEdiEmpfaenger(new ArrayList<EdiEmpfaenger>(source.ediEmpfaenger));
		Iterator<EdiEmpfaenger> i = this.ediEmpfaenger.iterator();
		while(i.hasNext())
			i.next().setEdiEintrag(this);
		this.seitDatum = source.seitDatum;
		this.bisDatum = source.bisDatum;
		this.laeDatum = source.laeDatum;
		this.laeUser = source.laeUser;
	}

	private boolean empfaengerListIsEqual( Collection<EdiEmpfaenger> empf1,
										   Collection<EdiEmpfaenger> empf2) {
		if (empf1.size() == empf2.size()) {
			Iterator<EdiEmpfaenger> i1 = empf1.iterator();
			Iterator<EdiEmpfaenger> i2 = empf2.iterator();
			while (i1.hasNext()) {
				EdiEmpfaenger e1 = i1.next();
				EdiEmpfaenger e2 = i2.next();
				if ( !e1.equaels(e2) )
					return false;
			}
			return true;
		}
		return false;
	}
	
    public String bezeichnung() {
    	String intSzeName = "I??";
    	String senderName = "S??";
    	String empf01Name = "E??";
    	String geOb01Name = "G??";
    	if (ediKomponente != null) { 
    		senderName = ediKomponente.getFullname();
    	}
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
    	return intSzeName + "  [" + senderName + "  >>  " + empf01Name + ": " + geOb01Name + "]";
    }
}
