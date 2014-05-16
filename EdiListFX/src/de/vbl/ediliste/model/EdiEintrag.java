package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;
import static javax.persistence.TemporalType.DATE;

import java.util.Collection;
import java.util.Date;
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
import javax.persistence.Temporal;

@Entity
public class EdiEintrag {
	public static final int EDI_NR_MIN_LEN = 3;
	private IntegerProperty ediNr = new SimpleIntegerProperty();
	private StringProperty bezeichnung = new SimpleStringProperty();
	private StringProperty beschreibung = new SimpleStringProperty();
	private StringProperty senderName = new SimpleStringProperty();
	private long id;
	private Konfiguration konfiguration;
	private EdiKomponente ediKomponente;
	private Collection<EdiEmpfaenger> ediEmpfaenger;
	private Date vonDatum;
	private Date bisDatum; 

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

	// public void setSenderName(String param) {
	// senderName.set(param);
	// } 

	// ------------------------------------------------------------------------
	@ManyToOne
	public Konfiguration getEdiSzenario() {
		return konfiguration;
	}

	public void setEdiSzenario(Konfiguration param) {
		this.konfiguration = param;
	}

	@ManyToOne
	public EdiKomponente getKomponente() {
		return ediKomponente;
	}

	public void setKomponente(EdiKomponente param) {
		String senderName = (param == null) ? "" : param.getFullname(); 
   		this.senderNameProperty().set(senderName);
		this.ediKomponente = param;
	}

	@OneToMany(mappedBy = "ediEintrag")
	public Collection<EdiEmpfaenger> getEdiEmpfaenger() {
		return ediEmpfaenger;
	}

	public void setEdiEmpfaenger(Collection<EdiEmpfaenger> param) {
		this.ediEmpfaenger = param;
	}
 
	@Temporal(DATE) 
	public Date getVonDatum() {
		return vonDatum;
	}

	public void setVonDatum(Date param) {
		this.vonDatum = param;
	}

	@Temporal(DATE) 
	public Date getBisDatum() {
		return bisDatum;
	}

	public void setBisDatum(Date param) {
		this.bisDatum = param;
	}
	
	public boolean equaels (EdiEintrag tEDI) {
		if ( (id == tEDI.id)                          								&&
			 (ediNr.get() == tEDI.ediNr.get())		  							  	&&	
		     (beschreibung.getValueSafe().equals(tEDI.beschreibung.getValueSafe()))	&&
		     (konfiguration == tEDI.konfiguration)                  					&&
		     (ediKomponente == tEDI.ediKomponente)									&&
		     (empfaengerListIsEqual(ediEmpfaenger,tEDI.ediEmpfaenger)) 	) {
		     		return true;
		}
		return false;
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
		}
		return true;
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
    	return intSzeName + "  [" + senderName + "  >>  " + empf01Name + ": " + geOb01Name + "]";
    }
}
