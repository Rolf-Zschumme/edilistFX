package de.vbl.ediliste.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiEmpfaenger;
import de.vbl.ediliste.model.EdiKomponente;

public class EdiKomponenteController {
	private final ObjectProperty<EdiKomponente> edikomponente;
	private String beschreibung;
	private EntityManager entityManager;
	private final ObservableList<EdiEmpfaenger> ediKomponenteList = FXCollections.observableArrayList();
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private TableView<EdiEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEdiNr;
    @FXML private TableColumn<EdiEmpfaenger, String> tcSender;
    @FXML private TableColumn<EdiEmpfaenger, ?> tcDatumBis;
    
    public EdiKomponenteController() {
    	this.entityManager = null;
    	this.edikomponente = new SimpleObjectProperty<>(this, "edikomponente", null);
    	this.beschreibung = "";
    }
	
	public void initialize() {
		System.out.println("EdiKomponenteController.initialize()");
		checkFieldsFromView();
		
		edikomponente.addListener(new ChangeListener<EdiKomponente>() {
			@Override
			public void changed(ObservableValue<? extends EdiKomponente> ov,
					EdiKomponente oldKomponente, EdiKomponente newKomponente) {
				System.out.println("EdiKomponenteController.ChangeListener(edikomponente):"+ oldKomponente + " " +newKomponente);
				if (oldKomponente != null) {
					ediKomponenteList.clear();
					tfBezeichnung.textProperty().unbindBidirectional(oldKomponente.nameProperty());
				}
				if (newKomponente != null) {
					readEdiListeforKomponete(newKomponente);
					tfBezeichnung.textProperty().bindBidirectional(newKomponente.nameProperty());
					taBeschreibung.setText(newKomponente.getBeschreibung());
					beschreibung = newKomponente.getBeschreibung();
				}
			}
	
		});
		tvVerwendungen.setItems(ediKomponenteList);
		tcEdiNr.setCellValueFactory(new PropertyValueFactory<EdiEmpfaenger, String>("ediNr") );
		tcSender.setCellValueFactory(new PropertyValueFactory<EdiEmpfaenger, String>("senderName"));
		tcEmpfaenger.setCellValueFactory(new PropertyValueFactory<EdiEmpfaenger, String>("empfaengerName"));
	}

	private void readEdiListeforKomponete( EdiKomponente selKomponente) {
		TypedQuery<EdiEintrag> tq = entityManager.createQuery(
				"SELECT e FROM EdiEintrag e WHERE e.komponente = :k", EdiEintrag.class);
		tq.setParameter("k", selKomponente);
		List<EdiEintrag> ediList = tq.getResultList();
		for(EdiEintrag e : ediList ) {
			System.out.println("EDI "+ e.getEdiNrStr() + " " + e.getKomponente());
	    	ediKomponenteList.addAll(e.getEdiEmpfaenger());
		}
	}
	
	public final ObjectProperty<EdiKomponente> komponenteProperty() {
		return edikomponente;
	}
	
	public final EdiKomponente getKomponente() {
		return edikomponente.get() ;
	}
	
	public final void setKomponente(EdiKomponente komponente) {
		this.edikomponente.set(komponente);
	}
    
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
    
    

    @FXML
    void checkFieldsFromView() {
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tcEdiNr != null : "fx:id=\"tcEdiNr\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert taBeschreibung != null : "fx:id=\"taBeschreibung\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    }

}
