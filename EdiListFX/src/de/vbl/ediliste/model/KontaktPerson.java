package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.TABLE;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Entity implementation class for Entity: KontaktPerson
 *
 */
@Entity 

public class KontaktPerson implements Serializable {
	
	private long id;
	private String idStr;
	private String name;
	private static final long serialVersionUID = 1L;
	public KontaktPerson() {
		super();
	}   
	@Id    
	@GeneratedValue(strategy = TABLE)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}   
	public String getIdStr() {
		return this.idStr;
	}

	public void setIdStr(String idStr) {
		this.idStr = idStr;
	}   
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
   
}
