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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.TypedQuery;

import de.vbl.im.tools.IMconstant;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.GenerationType.IDENTITY;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Entity
public class Integration { 
	private static final Logger logger = LogManager.getLogger(GeschaeftsObjekt.class.getName());
	public static final int IN_NR_MIN_LEN = 2;
	public static final String FORMAT_INNR = "%03d-%02d";
	
	private long id;
	private IntegerProperty inNr;
	private StringProperty bezeichnung;
	private StringProperty beschreibung;
	private StringProperty seitDatum;
	private StringProperty bisDatum;
	private String laeDatum; 
	private String laeUser;
	
	private InSzenario inSzenario;
	private StringProperty inSzenarioName;
	
	private InKomponente inKomponente;
	private StringProperty senderName;
	
	private Collection<InEmpfaenger> inEmpfaenger;
	
	private Konfiguration konfiguration;
	private StringProperty konfigurationName;
	
	private Intervall intervall;
	
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
	
	public static int getMaxInNr (final EntityManager em, int searchIsNr) {
//		try {
//			Query query = em.createQuery("SELECT MAX(i.inNr) FROM Integration i");
//			return (int) query.getSingleResult();
//		} catch (Exception e) {
//			throw(e);
//		}
    	TypedQuery<Integration> tq = em.createQuery(
				"SELECT i FROM Integration i ORDER BY i.inNr", Integration.class);
//    	tq.setParameter("s", startIS * 100);
		List<Integration> aktuList = tq.getResultList();
		int maxInNr = 0;
		for(Integration i : aktuList ) {
			int foundIsNr = i.inNr.getValue() / 100;
			if (foundIsNr < searchIsNr) {
				continue;
			}
			if (foundIsNr > searchIsNr) {
				break;
			}
			int inNr = i.inNr.getValue() % 100;
	    	if (inNr > maxInNr) maxInNr = inNr;
		}
		if (maxInNr >= 99) {
			logger.error("keine neue Integration-Nummer für " + searchIsNr  + " verfuegbar");
		}
		return maxInNr;
		
	}

	public StringExpression inNrStrExp() {
		int isNr = inNr.get();
		return (Bindings.format("%03d-%02d", isNr / 100, isNr % 100));
	}

	// ------------------------------------------------------------------------
	@ManyToOne
	public InSzenario getInSzenario() {
	    return inSzenario;
	}

	public void setInSzenario(InSzenario param) {
		if (inSzenario != null) {
			inSzenarioName.unbind();
		}
	    inSzenario = param;
		if (inSzenario != null) {
			inSzenarioName.bind(inSzenario.nameProperty());
		}
	}

	public StringProperty inSzenarioNameProperty () {
		return inSzenarioName;
	}
	
	// ------------------------------------------------------------------------
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

	public StringProperty senderNameProperty() {
		return senderName;
	}

//	 public void setSenderName(String param) {
//		 senderName.set(param);
//	 }
	 
	// ------------------------------------------------------------------------
	@OneToMany(mappedBy = "integration", cascade = ALL)
	public Collection<InEmpfaenger> getInEmpfaenger() {
		return inEmpfaenger;
	}

	public void setInEmpfaenger(Collection<InEmpfaenger> param) {
		this.inEmpfaenger = param;
	}
 
	// ------------------------------------------------------------------------
	@ManyToOne
	public Intervall getIntervall() {
	    return intervall;
	}

	public void setIntervall(Intervall param) {
	    this.intervall = param;
	}

	// ------------------------------------------------------------------------
	@ManyToOne
//	@JoinColumn(name = "konfiguration_id", referencedColumnName = "ID")
	public Konfiguration getKonfiguration() {
	    return konfiguration;
	}

	public void setKonfiguration(Konfiguration param) {
		if (konfiguration != null) {
			konfigurationName.unbind();
		}
		konfiguration = param;
		if (konfiguration != null) {
			konfigurationName.bind(konfiguration.nameProperty());
		}
	}
	
	public StringProperty konfigurationNameProperty () {
		return konfigurationName;
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
	public StringProperty seitDatumProperty() {
		return seitDatum;
	}
	public String getSeitDatum() {
		return seitDatum.get();
	}
	public void setSeitDatum(String param) {
		seitDatum.set(param);
	}

	// ------------------------------------------------------------------------
	public StringProperty bisDatumProperty() {
		return bisDatum;
	}
	public String getBisDatum() {
		return bisDatum.get();
	}
	public void setBisDatum(String param) {
		bisDatum.set(param);
	}
	
	// ------------------------------------------------------------------------
	public String getLaeDatum() {
		return laeDatum;
	}

	public void setLaeDatum(String laeDatum) {
		this.laeDatum = laeDatum;
	}
	
	// ------------------------------------------------------------------------
	public String getLaeUser() {
		return laeUser;
	}

	public void setLaeUser(String laeUser) {
		this.laeUser = laeUser;
	}

	// ------------------------------------------------------------------------
	// Return : <InSzenario> - <Sender> - <Geschäftsobjekt> 
    // @Transient
    public String autobezeichnung(InSzenario inSzenario,
    							  		 InKomponente sender,
    							  		 GeschaeftsObjekt gObjekt) {
    	String senderName = "<Sender>";
    	String gObjektName = "<Geschäftsobjekt>";
    	if (sender != null) {
    		senderName = sender.getFullname();
    	}
    	if (gObjekt != null) {
    		gObjektName = gObjekt.getName();
    	}
    	return inSzenarioName.getValueSafe() + IMconstant.INBEZ_TRENNUNG + 
    		   senderName + IMconstant.INBEZ_TRENNUNG + 
    		   gObjektName;
    }
    
}
