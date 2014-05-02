package de.vbl.ediliste.model;

import java.io.Serializable;
import java.lang.String;
import javax.persistence.*;
import static javax.persistence.GenerationType.IDENTITY;
import de.vbl.ediliste.model.EdiEmpfaenger;
import java.util.Collection;

/**
 * Entity implementation class for Entity: GeschaeftsObjekt
 *
 */
@Entity

public class GeschaeftsObjekt implements Serializable {

	
	private long id;
	private String name;
	private static final long serialVersionUID = 1L;
	private Collection<EdiEmpfaenger> ediEmpfaenger;

	public GeschaeftsObjekt() {
		super();
	}   
	public GeschaeftsObjekt(String name) {
		this.name = name;
	}
	@Id    
	@GeneratedValue(strategy = IDENTITY)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}   
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@OneToMany(mappedBy = "geschaeftsObjekt")
	public Collection<EdiEmpfaenger> getEdiEmpfaenger() {
	    return ediEmpfaenger;
	}
	public void setEdiEmpfaenger(Collection<EdiEmpfaenger> param) {
	    this.ediEmpfaenger = param;
	}
   
}