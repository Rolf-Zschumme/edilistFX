
package de.vbl.im.model;

import static javax.persistence.GenerationType.IDENTITY;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import de.vbl.im.model.Ansprechpartner;

import java.util.Collection;

import javax.persistence.ManyToMany;

@Entity
public class EdiKomponente {
	private long id;
	private StringProperty name;
	private String beschreibung;
	private EdiSystem ediSystem;
	private Collection<Ansprechpartner> ansprechpartner;
	
	private StringProperty fullname;  // transient

	public EdiKomponente() {
		name = new SimpleStringProperty();
		fullname = new SimpleStringProperty();
	}

	public EdiKomponente(String name, EdiSystem system) {
		this();
		setName(name);
		setEdiSystem(system);
		if (system.getEdiKomponente() != null)
			system.getEdiKomponente().add(this);
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
		return name.getValueSafe();
	}

	public void setName(String param) {
		name.set(param);
	}

	// ------------------------------------------------------------------------
	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String param) {
		beschreibung = param;
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	@ManyToOne
	@JoinColumn(name = "ediSystem_id", referencedColumnName = "id")
	public EdiSystem getEdiSystem() {
		return ediSystem;
	}

	public void setEdiSystem(EdiSystem param) {
		if (ediSystem != null) {
			fullname.unbind();
		}
		this.ediSystem = param;
		fullname.bind(Bindings.concat(ediSystem.fullnameProperty(), trennung(),name));
	}
	private String trennung() {
		return " � ";  // halbgeviertstrich Alt+0150
//		return " � ";
	}
//	private String ASCIItoStr(int a) {
//		byte[] b = { (byte) a };
//		String ret = new String(b);
//		return " " + ret + " ";
//	}

	// *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   *   * 
	public StringProperty fullnameProperty() {
		return fullname;
	}
	
	public String getFullname() {
		return fullname.get();
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	@ManyToMany
	public Collection<Ansprechpartner> getAnsprechpartner() {
	    return ansprechpartner;
	}

	public void setAnsprechpartner(Collection<Ansprechpartner> param) {
	    this.ansprechpartner = param;
	}
}
