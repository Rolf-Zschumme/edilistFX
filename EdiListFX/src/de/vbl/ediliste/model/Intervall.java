package de.vbl.ediliste.model;

import java.io.Serializable;
import java.lang.Long;
import java.lang.String;
import javax.persistence.*;
import static javax.persistence.GenerationType.IDENTITY;

/**
 * Entity implementation class for Entity: Intervall
 *
 */
@Entity

public class Intervall implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7384250256056710054L;
	private long id;
	private String name;

	public Intervall() {
		super();
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
   
}
