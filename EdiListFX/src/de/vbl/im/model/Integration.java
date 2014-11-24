package de.vbl.im.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.TypedQuery;
import javax.persistence.Transient;

import de.vbl.im.model.Konfiguration;

import javax.persistence.JoinColumn;
import javax.persistence.GeneratedValue;

import static javax.persistence.GenerationType.IDENTITY;
import static javax.persistence.CascadeType.ALL;

@Entity
public class Integration { 
	public static final int IN_NR_MIN_LEN = 2;
	public static final String FORMAT_INNR = "%03d-%02d";
	
	private long id;
	private IntegerProperty inNr;
	private StringProperty bezeichnung;
	private StringProperty beschreibung;
	private StringProperty seitDatum;
	private StringProperty bisDatum;
	private InKomponente inKomponente;
	private Collection<InEmpfaenger> inEmpfaenger;
	private String laeDatum; 
	private String laeUser;
	
	private StringProperty senderName;
	private StringProperty inSzenarioName;
	private StringProperty konfigurationName;
	private Intervall intervall;
	private Konfiguration konfiguration;
	
	// ========================================================================
	public Integration() {
		inNr = new SimpleIntegerProperty();
		bezeichnung = new SimpleStringProperty();
		beschreibung = new SimpleStringProperty();
		inEmpfaenger = new ArrayList<InEmpfaenger>();
		seitDatum = new SimpleStringProperty();
		bisDatum = new SimpleStringProperty();
		senderName = new SimpleStringProperty();
		inSzenarioName = new SimpleStringProperty("");
		konfigurationName = new SimpleStringProperty("");
	}
	
//	public Integration(final Konfiguration konfig) {
//		this();
//		setKonfiguration(konfig);
//	}

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
	public IntegerProperty inNrProperty() {
		return inNr;
	}

	public int getInNr() {
		return inNr.get();
	}

	public void setInNr(int param) {
		inNr.set(param);
	}
	
	public final int getMaxInNr (final EntityManager em, int startIS) {
//		try {
//			Query query = em.createQuery("SELECT MAX(i.inNr) FROM Integration i");
//			return (int) query.getSingleResult();
//		} catch (Exception e) {
//			throw(e);
//		}
    	TypedQuery<Integration> tq = em.createQuery(
				"SELECT i FROM Integration i", Integration.class);
//    	tq.setParameter("s", startIS * 100);
		List<Integration> aktuList = tq.getResultList();
		int maxInNr = 0;
		for(Integration i : aktuList ) {
			if (i.inNr.getValue() / 100 > startIS) {
				break;
			}
			int inNr = i.inNr.getValue() % 100;
	    	if (inNr > maxInNr) maxInNr = inNr;
		}
		return maxInNr;
		
	}

	public StringExpression inNrStrExp() {
		int isNr = inNr.get();
		return (Bindings.format("%03d-%02d", isNr / 100, isNr % 100));
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
	 
	@ManyToOne
	public InKomponente getInKomponente() {
		return inKomponente;
	}

	public void setInKomponente(InKomponente kompo) {
		if (inKomponente != null) {
			senderName.unbind();
		}
		inKomponente = kompo;
		if (kompo != null) {
			senderName.bind(kompo.fullnameProperty());
		}
	}

	@OneToMany(mappedBy = "integration", cascade = ALL)
	public Collection<InEmpfaenger> getInEmpfaenger() {
		return inEmpfaenger;
	}

	public void setInEmpfaenger(Collection<InEmpfaenger> param) {
		this.inEmpfaenger = param;
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

	public StringProperty konfigurationNameProperty () {
		return konfigurationName;
	}
	
	public StringProperty inSzenarioNameProperty () {
		return inSzenarioName;
	}

	// Return : <InSzenario> - <Sender> - <Geschäftsobjekt> 
    @Transient
    public static String autobezeichnung(Konfiguration konfiguration,
    							  InKomponente sender,
    							  GeschaeftsObjekt gObjekt) {
    	String inSzenarioName = "<InSzenario>";
    	String senderName = "<Sender>";
    	String gObjektName = "<Geschäftsobjekt>";
    	if (konfiguration != null) {
    		if (konfiguration.getInSzenario() != null) {
    			if (konfiguration.getInSzenario().getName() != null)
    				inSzenarioName = konfiguration.getInSzenario().getName();
    		}
    	}	
    	if (sender != null) {
    		senderName = sender.getFullname();
    	}
    	if (gObjekt != null) {
    		gObjektName = gObjekt.getName();
    	}
    	return inSzenarioName + TrennStr() + senderName + TrennStr() + gObjektName;
    }
    
    // TrennStr for autoBezeichung between InSzenario, Sender and GeschaeftsObjekt    
	private static String TrennStr() {
		return "  |  ";
	}

	@ManyToOne
	public Intervall getIntervall() {
	    return intervall;
	}

	public void setIntervall(Intervall param) {
	    this.intervall = param;
	}

	@ManyToOne
	@JoinColumn(name = "konfiguration_id", referencedColumnName = "ID")
	public Konfiguration getKonfiguration() {
	    return konfiguration;
	}

	public void setKonfiguration(Konfiguration param) {
		if (konfiguration != null) {
			konfigurationName.unbind();
			inSzenarioName.unbind();
		}
		konfiguration = param;
		if (konfiguration != null) {
			konfigurationName.bind(konfiguration.nameProperty());
			inSzenarioName.bind(konfiguration.inSzenarioNameProperty());
		}
	}
	
	
}
