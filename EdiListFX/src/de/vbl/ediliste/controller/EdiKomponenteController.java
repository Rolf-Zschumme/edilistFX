package de.vbl.ediliste.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import de.vbl.ediliste.model.EdiEmpfaenger;
import de.vbl.ediliste.model.EdiKomponente;

public class EdiKomponenteController {
	private final ObjectProperty<EdiKomponente> komponente;
	private final ObservableList<EdiEmpfaenger> ediKomponenteList = FXCollections.observableArrayList();
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField tfBezeichnung;
    @FXML private TextField taBeschreibung;
    @FXML private TableView<EdiEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEdiNr;
    @FXML private TableColumn<EdiEmpfaenger, String> tcSender;
    @FXML private TableColumn<EdiEmpfaenger, ?> tcDatumBis;
    
    public EdiKomponenteController() {
    	this.komponente = new SimpleObjectProperty<>(this, "komponente", null);
    }
	
	public void initialize() {
		System.out.println("EdiKomponenteController.initialize()");
		checkFieldsFromView();
		
		komponente.addListener(new ChangeListener<EdiKomponente>() {
			@Override
			public void changed(ObservableValue<? extends EdiKomponente> ov,
					EdiKomponente oldKomponente, EdiKomponente newKomponente) {
				System.out.println("EdiKomponenteController.ChangeListener(komponente)");
				if (oldKomponente != null) {
					tfBezeichnung.textProperty().unbindBidirectional(oldKomponente.nameProperty());
					
				}
				if (newKomponente != null) {
					tfBezeichnung.textProperty().bindBidirectional(newKomponente.nameProperty());
				}
			}
	
		});
		
//		tvVerwendungen.setItems(ediKomponenteList);
//		tcEdiNr.setCellValueFactory(new PropertyValueFactory<EdiEmpfaenger,String>("ediNr"));
//		tcSender.setCellValueFactory(new PropertyValueFactory<EdiEmpfaenger,String>("senderName"));

		
	}

//    public void setSelection( EdiKomponente selKomponente) {
//    	tfBezeichnung.setText(selKomponente.getName());
//    	taBeschreibung.setText(selKomponente.getBeschreibung());
//    	
//    	readEdiListeforKomponete(selKomponente);
//    	
//    }
//     
//	
//	private void readEdiListeforKomponete( EdiKomponente selKomponente) {
//		ediKomponenteList.clear();
//		
//		TypedQuery<EdiEintrag> tq = em.createQuery(
//				"SELECT e FROM EdiEintrag e WHERE e.komponente = :k", EdiEintrag.class);
//		tq.setParameter("k", selKomponente);
//		List<EdiEintrag> ediList = tq.getResultList();
//		for(EdiEintrag e : ediList ) {
//	    	ediKomponenteList.addAll(e.getEdiEmpfaenger());
//		}
//	}
	
	public final ObjectProperty<EdiKomponente> komponenteProperty() {
		return komponente;
	}
	
	public final EdiKomponente getKomponente() {
		return komponente.get() ;
	}
	
	public final void setKomponente(EdiKomponente komponente) {
		this.komponente.set(komponente);
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
