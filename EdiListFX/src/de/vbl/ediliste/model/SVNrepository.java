package de.vbl.ediliste.model;

import java.io.Serializable;
import java.lang.String;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: SVNrepository
 *
 */
@Entity
@Table(name = "SVN_Repostitory")
public class SVNrepository implements Serializable {

	   
	@Id
	private long ID;
	private String name;
	private String url;
	private String user;
	private String passwort;
	private static final long serialVersionUID = 1L;

	public SVNrepository() {
		super();
	}   
	public long getID() {
		return this.ID;
	}

	public void setID(long ID) {
		this.ID = ID;
	}   
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}   
	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}   
	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}   
	public String getPasswort() {
		return this.passwort;
	}

	public void setPasswort(String passwort) {
		this.passwort = passwort;
	}
   
}
