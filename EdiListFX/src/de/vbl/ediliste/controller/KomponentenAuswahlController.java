package de.vbl.ediliste.controller;

import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.internal.sessions.remote.SequencingFunctionCall.GetNextValue;

import de.vbl.ediliste.model.Komponente;
import de.vbl.ediliste.model.Partner;
import de.vbl.ediliste.model.PartnerSystem;

public class KomponentenAuswahlController {
	public static enum KomponentenTyp { SENDER, RECEIVER };
	private static final String APPL_TITLE = "EdiListe";
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";
    
    @FXML private ResourceBundle resources;

    @FXML private ComboBox<Partner> partnerCB;
    @FXML private ComboBox<PartnerSystem> systemCB;
    @FXML private ComboBox<Komponente> komponenteCB;
    
    @FXML private Button btnOK;
    @FXML private Button btnNewSystem;
    @FXML private Button btnNewPartner;
    @FXML private Button btnNewKomponente;

    @FXML private Label lbFehlertext;
    @FXML private Label lbSenderReseiver;

	private EntityManager em; 

	private ObjectProperty<Komponente> komponente;
	
	private ObservableList<Partner>       partnerList;     //  = FXCollections.observableArrayList();
	private ObservableList<PartnerSystem> systemList;      //  = FXCollections.observableArrayList(); 
	private ObservableList<Komponente>    komponentenList; //  = FXCollections.observableArrayList(); 

	public Komponente selectedKomponenten() {
		return komponente.get();
	}
	
	public void setKomponente(KomponentenTyp typ, Komponente param) {

		if (typ == KomponentenTyp.SENDER)
			lbSenderReseiver.setText("Sender");
		else
			lbSenderReseiver.setText("Empfänger");

		komponente.set(param);
		if (param != null) {
			
			partnerCB.getSelectionModel().select(param.getPartnerSystem().getPartner());
			systemCB.getSelectionModel().select(param.getPartnerSystem());
			komponenteCB.getSelectionModel().select(komponente.get());
//			komponenteIsSelected.set(true);
		}
	}
	
	/* ------------------------------------------------------------------------
     * initialize() is the controllers "main"-method 
     * it is called after loading "....fxml" 
     * ----------------------------------------------------------------------*/
    @FXML
    void initialize() {
    	System.out.println(getClass().getName()+".initialize called");
    	
    	komponente 	= new SimpleObjectProperty<>();
  	    partnerList     = FXCollections.observableArrayList();
		systemList      = FXCollections.observableArrayList();
		komponentenList = FXCollections.observableArrayList();
    	
    	checkFieldFromView();
        setupEntityManager();

        readPartnerData();
		setupBindings();
		partnerCB.setItems(partnerList);
		systemCB.setItems(systemList);
		komponenteCB.setItems(komponentenList);
    }
    
    private void readPartnerData() {
    	// alle Partner lesen
    	TypedQuery<Partner> tq = em.createQuery(
    			"SELECT partner FROM Partner partner ORDER BY partner.name",Partner.class);
    	List<Partner> ediList = tq.getResultList();
    	for (Partner partner : ediList) {
    		partnerList.add(partner);
    	}
    }
    
    private void readSystemData(Long partnerId) {
    	systemList.clear();
		TypedQuery<PartnerSystem> tq = em.createQuery(
				"SELECT system FROM PartnerSystem system ORDER BY system.name", 
				PartnerSystem.class);
		List<PartnerSystem> ediList = tq.getResultList();
		for (PartnerSystem ediSystem : ediList) {
			if (partnerId == ediSystem.getPartner().getId()) {
				systemList.add(ediSystem);
			}
		}
    }

    private void readKomponentenData(Long systemId) {
    	komponentenList.clear();
		TypedQuery<Komponente> tq = em.createQuery(
				"SELECT komponente FROM Komponente komponente ORDER BY komponente.name", 
				Komponente.class);
		List<Komponente> resultList = tq.getResultList();
		for (Komponente komponente : resultList) {
			if (systemId == komponente.getPartnerSystem().getId()) {
				komponentenList.add(komponente);
			}
		}
		System.out.println(getClass().getName()+".getKomponentenData "+
				"("+resultList.size()+ " von "+komponentenList.size()+")");
    }
    
    private void setupBindings() {
    	btnOK.disableProperty().bind(Bindings.isNull(komponente));
    	
    	btnNewSystem.disableProperty().bind(Bindings.isNull(partnerCB.getSelectionModel().selectedItemProperty()));
    	systemCB.disableProperty().bind(Bindings.isNull(partnerCB.getSelectionModel().selectedItemProperty()));
    	btnNewKomponente.disableProperty().bind(Bindings.isNull(systemCB.getSelectionModel().selectedItemProperty()));
    	komponenteCB.disableProperty().bind(Bindings.isNull(systemCB.getSelectionModel().selectedItemProperty()));
    	
    	
    	partnerCB.setCellFactory(new Callback<ListView<Partner>,ListCell<Partner>> () {
    		@Override
    		public ListCell<Partner> call(ListView<Partner> param) {
    			return new ListCell<Partner>() {
    				@Override
    				protected void updateItem(Partner partner, boolean empty) {
    					super.updateItem(partner, empty);
    					if(partner != null) {
    						setText(partner.getName());
    					}
    					else setText("-");
    				}
    			};
    		}
    	});
    	partnerCB.setButtonCell(new ListCell<Partner> () {
    		@Override
    		protected void updateItem(Partner partner, boolean empty) {
    			super.updateItem(partner, empty);
    			setText( (empty) ? "?" : partner.getName());
    		}    		
    	});
    	partnerCB.getSelectionModel().selectedItemProperty().addListener(
    			new ChangeListener<Partner>() {
    				@Override
    				public void changed (
    						ObservableValue<? extends Partner> observable,
    							Partner oldPartner, Partner newPartner) {
    					readSystemData( (newPartner==null) ? -1L : newPartner.getId());
    					komponenteCB.getSelectionModel().select(null);
    				}
    			}
    	);
    	
    	systemCB.setCellFactory(new Callback<ListView<PartnerSystem>,ListCell<PartnerSystem>> () {
    		@Override
    		public ListCell<PartnerSystem> call(ListView<PartnerSystem> param) {
    			return new ListCell<PartnerSystem>() {
    				@Override
    				protected void updateItem(PartnerSystem system, boolean empty) {
    					super.updateItem(system, empty);
    					if(system != null) {
    						setText(system.getName());
    					}
    					else setText("-");
    				}
    			};
    		}
    	});
    	systemCB.setButtonCell(new ListCell<PartnerSystem> () {
    		@Override
    		protected void updateItem(PartnerSystem system, boolean empty) {
    			super.updateItem(system, empty);
    			System.out.println(getClass().getName()+".updateItem " + ((system==null)?"s":system.getName()));
    			setText( (empty) ? "?" : system.getName());
    		}    		
    	});
    	systemCB.getSelectionModel().selectedItemProperty().addListener(
    			new ChangeListener<PartnerSystem>() {
    				@Override
    				public void changed (
    						ObservableValue<? extends PartnerSystem> observable,
    							PartnerSystem oldSystem, PartnerSystem newSystem) {
    					readKomponentenData( (newSystem==null) ? -1L : newSystem.getId());
    				}
    			}
    	);

    	komponenteCB.setCellFactory(new Callback<ListView<Komponente>,ListCell<Komponente>> () {
    		@Override
    		public ListCell<Komponente> call(ListView<Komponente> param) {
    			return new ListCell<Komponente>() {
    				@Override
    				protected void updateItem(Komponente komponente, boolean empty) {
    					super.updateItem(komponente, empty);
    					if(komponente != null) {
    						setText(komponente.getName());
    					}
    					else setText("-");
    				}
    			};
    		}
    	});
    	komponenteCB.setButtonCell(new ListCell<Komponente> () {
    		@Override
    		protected void updateItem(Komponente komponente, boolean empty) {
    			super.updateItem(komponente, empty);
    			System.out.println(getClass().getName()+".updateItem " + ((komponente==null)?"k":komponente.getName()));
    			setText( (empty) ? "?" : komponente.getName());
    		}    		
    	});
    	komponenteCB.getSelectionModel().selectedItemProperty().addListener(
    			new ChangeListener<Komponente>() {
    				@Override
    				public void changed (
    						ObservableValue<? extends Komponente> observable,
    							Komponente oldKomponente, Komponente newKomponente) {
//    					komponenteIsSelected.set(newKomponente!=null);
    					komponente.set(newKomponente);
    					System.out.println(getClass().getName() + ".changed " + newKomponente);
    				}
    			}
    	);
    }	
    	
    private void setupEntityManager() {
    	EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    	em = factory.createEntityManager();
    }
    
    @FXML
    void okPressed(ActionEvent event) {
		unbind();
		close(event);
    }

    @FXML
    void escapePressed(ActionEvent event) {
		unbind();
    	close(event);
    }
    
    @FXML
    void newPartnerPressed(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	String name = "";
    	while(true) {
    		name = Dialogs.showInputDialog(stage,
    				"Bezeichnung des Partners:",
    				"Neuen Partner anlegen",APPL_TITLE,name);
    		if (name == null)	
    			break;
    		if (name.length() < 3) {
    			Dialogs.showInformationDialog(stage, 
    					"Bitte einen Namen mit mind. 3 Zeichen eingeben",
    					"Korrektur erforderlich",APPL_TITLE);
    			continue;
    		}
    		if (partnerNameExist(name)) {
    			Dialogs.showInformationDialog(stage, 
    					"Ein Partner mit diesem Namen ist bereits vorhanden",
    					"Duplikatsprüfung",APPL_TITLE);
    			continue;
    		}
    		Partner partner = new Partner(name);
    		try {
    			em.getTransaction().begin();
    			em.persist(partner);
    			em.getTransaction().commit();
    		} catch (RuntimeException e) {
    			Dialogs.showErrorDialog(stage,
    					"Fehler beim speichern des Partners",
    					"Datenbankfehler",APPL_TITLE,e);
    			continue;
    		}
    		partnerList.add(partner);
    		partnerCB.getSelectionModel().select(partner);
    		break;
    	}	
    }
    
    private boolean partnerNameExist(String name) {
    	for (Partner p : partnerList) {
    		if (name.equalsIgnoreCase(p.getName()))
    			return true;
    	}
		return false;
	}

    
    @FXML
    void newSystemPressed(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	String name = "";
    	Partner aktPartner = partnerCB.getSelectionModel().getSelectedItem();
    	String partnerName = aktPartner.getName();
    	while(true) {
    		name = Dialogs.showInputDialog(stage,
    				"Bezeichnung des neuen Systems:",
    				"Neues System für Partner " + partnerName + " anlegen" ,APPL_TITLE,name);
    		if (name == null)	
    			break;
    		if (name.length() < 3) {
    			Dialogs.showInformationDialog(stage, 
    					"Der System-Name muß mindestens 3 Zeichen lang sein",
    					"Korrektur erforderlich",APPL_TITLE);
    			continue;
    		}
    		if (systemNameExist(name)) {
    			Dialogs.showInformationDialog(stage, 
    					"Für den Partner " + partnerName + " ist bereits ein gleichnamiges System vorhanden",
    					"Duplikatsprüfung",APPL_TITLE);
    			continue;
    		}
    		PartnerSystem partnerSystem = new PartnerSystem(name, aktPartner);
    		try {
    			em.getTransaction().begin();
    			em.persist(partnerSystem);
    			em.getTransaction().commit();
    		} catch (RuntimeException e) {
    			Dialogs.showErrorDialog(stage,
    					"Fehler beim Anlegen des Systems",
    					"Datenbankfehler",APPL_TITLE,e);
    			continue;
    		}
    		systemList.add(partnerSystem);
    		systemCB.getSelectionModel().select(partnerSystem);
    		break;
    	}	
    }

    private boolean systemNameExist(String name) {
    	for (PartnerSystem p : systemList) {
    		if (name.equalsIgnoreCase(p.getName()))
    			return true;
    	}
		return false;
	}
    
    @FXML
    void newKomponentePressed(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	String name = "";
    	PartnerSystem aktSystem = systemCB.getSelectionModel().getSelectedItem();
    	String systemName = aktSystem.getFullname();
    	while(true) {
    		name = Dialogs.showInputDialog(stage,
    				"Bezeichnung der neuen Komponente:",
    				"Neue Komponente für das System " + systemName + " anlegen" ,APPL_TITLE,name);
    		if (name == null)	
    			break;
    		if (name.length() < 3) {
    			Dialogs.showInformationDialog(stage, 
    					"Der System-Name muß mindestens 3 Zeichen lang sein",
    					"Korrektur erforderlich",APPL_TITLE);
    			continue;
    		}
    		if (komponentenNameExist(name)) {
    			Dialogs.showInformationDialog(stage, 
    					"Für den System " + systemName + " ist bereits eine gleichnamige Komponente vorhanden",
    					"Duplikatsprüfung",APPL_TITLE);
    			continue;
    		} 
    		Komponente komponente = new Komponente(name, aktSystem);
    		try {
    			em.getTransaction().begin();
    			em.persist(komponente);
    			em.getTransaction().commit();
    		} catch (RuntimeException e) {
    			Dialogs.showErrorDialog(stage,
    					"Fehler beim Anlegen der Komponente",
    					"Datenbankfehler",APPL_TITLE,e);
    			continue;
    		}
    		komponentenList.add(komponente);
    		komponenteCB.getSelectionModel().select(komponente);
    		break;
    	}	

    }

    private boolean komponentenNameExist(String name) {
    	for (Komponente k : komponentenList) {
    		if (name.equalsIgnoreCase(k.getName()))
    			return true;
    	}
		return false;
	}

	@FXML
    void ff0808(ActionEvent event) {

    }

    private void unbind() {
//    	tfEdiNr.textProperty().unbindBidirectional(ediEintrag.ediNrProperty());
//    	tfKurzbez.textProperty().unbindBidirectional(ediEintrag.kurzBezProperty());
    }
    
    private void close(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	stage.close();
    }
    
    /* *****************************************************************************
	 * 
	 * ****************************************************************************/
    
    private void checkFieldFromView() {
    	assert partnerCB != null : "fx:id=\"partnerCB\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert systemCB != null : "fx:id=\"systemCB\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert komponenteCB != null : "fx:id=\"komponenteCB\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert btnNewSystem != null : "fx:id=\"btnNewSystem\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert btnNewPartner != null : "fx:id=\"btnNewPartner\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert btnNewKomponente != null : "fx:id=\"btnNewKomponente\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert lbFehlertext != null : "fx:id=\"fehlertext\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
	}
}