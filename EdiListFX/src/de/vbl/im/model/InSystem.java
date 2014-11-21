package de.vbl.im.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Collection;
import java.util.Set;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import de.vbl.im.tools.IMconstant;
import javax.persistence.JoinTable;

@Entity
public class InSystem {
	private long id;
	private StringProperty name;
	private InPartner inPartner;
	private String beschreibung;
	private ObservableList<InKomponente> inKomponente;
	private Set<Ansprechpartner> ansprechpartner;
	
	private StringProperty fullname;
	private IntegerProperty anzKomponenten;

	public InSystem() {
		name = new SimpleStringProperty();
		fullname = new SimpleStringProperty();
		anzKomponenten = new SimpleIntegerProperty();
	}

	public InSystem(String name, InPartner inPartner) {
		this();
		this.name.setValue(name);
		this.setinPartner(inPartner);
		if (inPartner.getInSystem() != null) {
			inPartner.getInSystem().add(this);
		}
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
	public StringProperty nameProperty() {
		return name;
	}

	public String getName() {
		return name.get();
	}
	
	public String getNameSafe() {
		return name.getValueSafe();
	}

	public void setName(String param) {
		name.set(param);
		
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	@Transient
	public StringProperty fullnameProperty() {
		return fullname;
	}
	public String getFullname () {
		return fullname.get();
	}

//	public String getFullname() {
//		String partnerName = inPartner == null ? "-?-" : inPartner.getName();
//		return partnerName + ASCIItoStr(42) + name.get();   // 151
//		return fullname.get();
//	}
	
	@ManyToOne
	public InPartner getinPartner() {
		return inPartner;
	}

	public void setinPartner(InPartner param) {
		if (inPartner != null) {
			fullname.unbind();
			inPartner.getInSystem().remove(this);
		}
		inPartner = param;
		fullname.bind(Bindings.concat(inPartner.nameProperty(), IMconstant.KOMPO_TRENNUNG, this.name));
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	@OneToMany(mappedBy = "inSystem")
	public Collection<InKomponente> getInKomponente() {
		return inKomponente;
	}

	public void setInKomponente(Collection<InKomponente> komponenten) {
		if (inKomponente != null) {
			anzKomponenten.unbind();
		}
		inKomponente = FXCollections.observableArrayList(komponenten);
		anzKomponenten.bind(Bindings.size(inKomponente));

//		anzKomponenten.addListener(new ChangeListener<Number>() {
//			@Override
//			public void changed(ObservableValue<? extends Number> observable,
//					Number oldValue, Number newValue) {
//				System.out.println("------->   Value changed from " + oldValue + " to " + newValue + " for " + observable.toString());
//			}
//		} );
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	@Transient
	public IntegerProperty anzKomponentenProperty() {
		return anzKomponenten;
	}

//	@Transient
//	public String getPartnerName() {
//		return inPartner.getName();
//	}
//
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	@ManyToMany
	@JoinTable
	public Set<Ansprechpartner> getAnsprechpartner() {
	    return ansprechpartner;
	}

	public void setAnsprechpartner(Set<Ansprechpartner> param) {
	    this.ansprechpartner = param;
	}
}
