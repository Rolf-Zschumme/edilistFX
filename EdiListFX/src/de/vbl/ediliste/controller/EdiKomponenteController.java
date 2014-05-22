package de.vbl.ediliste.controller;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.google.common.collect.Table;

import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiEmpfaenger;
import de.vbl.ediliste.model.EdiKomponente;
import de.vbl.ediliste.model.EdiSystem;
import de.vbl.ediliste.view.EdiNrListElement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class EdiKomponenteController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField tfBezeichnung;
    @FXML private TextField taBeschreibung;
    @FXML private TableView<EdiEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEdiNr;
    @FXML private TableColumn<EdiEmpfaenger, String> tcSender;
    @FXML private TableColumn<EdiEmpfaenger, ?> tcDatumBis;
    
	private MainController mainController;
	private Stage primaryStage;
	private String applName;
	private EntityManager em;

    private ObservableList<EdiEmpfaenger> ediKomponenteList = FXCollections.observableArrayList();
	
	public void setInitial (MainController main, 
							Stage stage, 
							String applikationName, 
							EntityManager entityManager) {
		mainController = main;
		primaryStage = stage;
		applName = applikationName;
		em = entityManager;
		System.out.println("EntityManager="+ em);
		
		initialize();
		
		tvVerwendungen.setItems(ediKomponenteList);
//		tcEdiNr.setCellValueFactory(new PropertyValueFactory<EdiEmpfaenger,String>("senderName"));
		tcSender.setCellValueFactory(new PropertyValueFactory<EdiEmpfaenger,String>("senderName"));

		
	}

    public void setSelection( EdiKomponente selKomponente) {
    	tfBezeichnung.setText(selKomponente.getName());
    	taBeschreibung.setText(selKomponente.getBeschreibung());
    	
    	readEdiListeforKomponete(selKomponente);
    	
    }
     
	
	private void readEdiListeforKomponete( EdiKomponente selKomponente) {
		ediKomponenteList.clear();
		
		TypedQuery<EdiEintrag> tq = em.createQuery(
				"SELECT e FROM EdiEintrag e WHERE e.komponente = :k", EdiEintrag.class);
		tq.setParameter("k", selKomponente);
		List<EdiEintrag> ediList = tq.getResultList();
		for(EdiEintrag e : ediList ) {
	    	ediKomponenteList.addAll(e.getEdiEmpfaenger());
		}
	}
	
    
    
    

    @FXML
    void initialize() {
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tcEdiNr != null : "fx:id=\"tcEdiNr\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert taBeschreibung != null : "fx:id=\"taBeschreibung\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    }
}
