
package de.vbl.im.controller.subs;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.controlsfx.dialog.Dialog.Actions;

import de.vbl.im.controller.IMController;
import de.vbl.im.model.DokuLink;
import de.vbl.im.model.Repository;

public class DokumentAuswaehlenController implements Initializable {
	private static final Logger logger = LogManager.getLogger(DokumentAuswaehlenController.class.getName());

	private static Stage primaryStage = null;
    private static String applName = null;
	private static IMController managerController;
    private static EntityManager entityManager = null;
    
    private ObservableList<Repository> reposiList = FXCollections.observableArrayList();
    private ObservableList<DokuLink> dokuLinkList = FXCollections.observableArrayList();
    private ObservableList<String> firstLevelList = FXCollections.observableArrayList();
	/**
	 * injection from 'DokumentAuswaehlen.fxml'
	 */
	@FXML private ComboBox<Repository> cmbRepository; 
	@FXML private ComboBox<String> cmbFirstLevel; 
	@FXML private TextField tfSearch; 
    
    @FXML private TableView<DokuLink> tableDokuLinkAuswahl;
    @FXML private TableColumn<DokuLink, String> tColDokumentVorhaben;
    @FXML private TableColumn<DokuLink, String> tColDokumentName;
    @FXML private TableColumn<DokuLink, String> tColDokumentPfad;
    @FXML private TableColumn<DokuLink, String> tColDokumentRevision;
    @FXML private TableColumn<DokuLink, LocalDateTime> tColDokumentDatum;
    
    @FXML private Label     lbHinweis;
    @FXML private Button    btnSearch;
    @FXML private Button    btnOK;

    private DokuLink selDokuLink = null;
    private BooleanProperty listIsNotSelected = new SimpleBooleanProperty();
    private BooleanProperty repositoryIsOpen = new SimpleBooleanProperty(false);
    
    private Repository aktRepository = null;
    private String aktFirstLevel = null;
    private String searchText = "TSpez_0";
    private Actions retAction = Actions.CLOSE;
    
    final static String filename = "DokumentAuswaehlen.fxml";
    final static String errtxt = "' was not injected: check FXML-file '" + filename + "'.";
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
        	assert tableDokuLinkAuswahl != null : "fixid='tableDokuLinkAuswahl" + errtxt; 
        	assert cmbRepository 		!= null : "fx:id='cmbRepository" 		+ errtxt;
        	assert cmbFirstLevel 		!= null : "fx:id='cmbFirstLevel" 	 	+ errtxt;
        	assert tfSearch      		!= null : "fx:id='tfsearch"      		+ errtxt;
        	assert btnSearch     		!= null : "fx:id='btnSearch"     		+ errtxt;
        	assert lbHinweis     		!= null : "fx:id='lbHinweis"     		+ errtxt;
        	assert btnOK	     		!= null : "fx:id='btnOK"	     		+ errtxt;
		} catch (AssertionError e) {
			logger.error(e.getMessage(), e);
		}
        
		cmbRepository.setCellFactory((cmbBx) -> {
			return new ListCell<Repository>() {
				@Override
				protected void updateItem(Repository repo, boolean empty) {
					super.updateItem(repo, empty);
					if (repo == null || empty) {
						setText(null);
					} else {
						setText(repo.getName());
					}
				}
			} ;
		} );
		cmbRepository.setCellFactory((cmbBx) -> {
			return new ListCell<Repository>() {
				@Override
				protected void updateItem(Repository repo, boolean empty) {
					super.updateItem(repo, empty);
					if (repo == null || empty) {
						setText(null);
					} else {
						setText(repo.getName());
					}
				}
			} ;
		} );
		cmbRepository.setConverter( new StringConverter<Repository>() {
			@Override
			public String toString(Repository repo) {
				if (repo == null) {
					return null;
				} else {
					return repo.getName();
				}
			}
			@Override
			public Repository fromString(String string) {
				return null; // No conversion fromString needed
			}
		} );
		cmbRepository.valueProperty().addListener((ov, oldValue, newRepository) -> {
			String msg = "";
			aktRepository = newRepository;
			firstLevelList.clear();
			if (newRepository.getName().equals("Test-SVN")) {
				repositoryIsOpen.set(true);
			} else {
				repositoryIsOpen.set(false);
				try {
					aktRepository.open();
					repositoryIsOpen.set(true);
					getFirstLevels();
					cmbFirstLevel.getSelectionModel().select(1);
				} catch (Exception e) {
					msg = "FEHLER beim �ffnen des Repository: " + e.getMessage();
				}
			}
	        lbHinweis.setText(msg);
		});

		cmbFirstLevel.setItems(firstLevelList);
		cmbFirstLevel.valueProperty().addListener((ov, oldValue, newValue) -> {
			lbHinweis.setText("");
			aktFirstLevel = newValue;
		});
		
        tfSearch.textProperty().addListener((ov, oldValue, newValue) ->  {
        	lbHinweis.setText("");
        	searchText = newValue.trim();

        });
        btnSearch.disableProperty().bind(Bindings.not(repositoryIsOpen));
        
		tableDokuLinkAuswahl.setItems(dokuLinkList);
		tColDokumentVorhaben.setCellValueFactory(cellData -> cellData.getValue().vorhabenProperty());
		tColDokumentName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		tColDokumentPfad.setCellValueFactory(cellData -> cellData.getValue().pfadProperty());
		tColDokumentDatum.setCellValueFactory(cellData -> cellData.getValue().datumProperty());
		
		DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);
		
		tColDokumentDatum.setCellFactory(column -> {
			return new TableCell<DokuLink, LocalDateTime>() {
				@Override
				protected void updateItem (LocalDateTime item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(dtf.format(item));
					}
				}
			};
		});
		
		tableDokuLinkAuswahl.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getClickCount()>1) {
					DokuLink selDoku = tableDokuLinkAuswahl.getSelectionModel().getSelectedItem();
					if (selDoku != null && checkSelectionAndExit(selDoku)) {
						close(event);
					}
				}
			}
		});

    	listIsNotSelected.bind(Bindings.isNull(tableDokuLinkAuswahl.getSelectionModel().selectedItemProperty()));
    	
    	btnOK.disableProperty().bind(listIsNotSelected);
    	
    	
    }

    public void start(Stage primaryStage, IMController managerController, EntityManager entityManager) {

    	DokumentAuswaehlenController.primaryStage = primaryStage;
    	DokumentAuswaehlenController.entityManager = entityManager;
    	DokumentAuswaehlenController.managerController = managerController;
		applName = primaryStage.getTitle();
		
		readRepositoriesFromDB(entityManager);
		
		if (reposiList.size() < 1) {
			lbHinweis.setText("Kein Repositry verf�gbar. Bitte zuvor eintragen!");
			lbHinweis.setTextFill(Color.RED);
		} else {
			cmbRepository.getSelectionModel().select(0);
		}
//		btnSearch.disableProperty().bind(Bindings.isEmpty(reposiList));
	}

	private void readRepositoriesFromDB(EntityManager em) {
    	TypedQuery<Repository> tq = entityManager.createQuery(
				"SELECT r FROM Repository r ORDER BY r.name", Repository.class);
		List<Repository> aktuList = tq.getResultList();
		reposiList.retainAll(aktuList);
		reposiList.addAll(aktuList);
		cmbRepository.setItems(reposiList);
	}

	@FXML
	private void searchPressed(ActionEvent event) {
		if (searchText.length() < 0) {
			lbHinweis.setText("Bitte mindesten ein Zeichen eingeben");
			return;
		}
		dokuLinkList.clear();
		primaryStage.getScene().setCursor(Cursor.WAIT);
		try {
			Task<?> worker = aktRepository.findEntries(searchText, aktFirstLevel, dokuLinkList, lbHinweis);
			btnSearch.disableProperty().bind(worker.runningProperty());
			lbHinweis.setAlignment(Pos.CENTER_LEFT);
			lbHinweis.textProperty().bind(worker.messageProperty());
			worker.setOnSucceeded(e -> {
				cleanSearchTask(lbHinweis.getText());
			});
		} catch (Exception e) {
			String msg = "FEHLER: " + e.getMessage();
			e.printStackTrace();
			cleanSearchTask(msg);
			logger.error(msg,e);
		}
	}
	private void cleanSearchTask(String msg) {
		btnSearch.disableProperty().unbind();
		lbHinweis.textProperty().unbind();
		lbHinweis.setAlignment(Pos.CENTER);
		lbHinweis.setText(msg);
		primaryStage.getScene().setCursor(Cursor.DEFAULT);
	}
	
    @FXML
    private void okPressed(ActionEvent event) {
    	if (applName == null && managerController == null && primaryStage == null ) {
    		// just for suppressing "unused" warning;
    	}
		if (checkSelectionAndExit(
				tableDokuLinkAuswahl.getSelectionModel().selectedItemProperty().get() )) {
			close(event);
		}
    }

	private boolean checkSelectionAndExit(DokuLink dokuLink) {
		boolean ret = false;
		Action response = Dialog.Actions.YES;
		if (dokuLink.getPfad().startsWith("/02_xSpez_abgenommen") == false) {
			response = Dialogs.create().owner(primaryStage)
					.title("Hinweis")
					.message("Das gew�hlte Dokument\n\n" + dokuLink.getName() + 
						 "\n ist nicht in \"02_xSpez_abgenommen\" gespeichert." +
						 "\n\nDas bedeutet es gilt nicht als abgenommen !")
					.actions(Dialog.Actions.YES, Dialog.Actions.NO)
					.showWarning();
		}
		if (response == Dialog.Actions.YES) {
			retAction = Actions.OK;
			this.selDokuLink = dokuLink;
			ret = true;
		}
		return ret;
	}

	@FXML
    private void escapePressed(ActionEvent event) {
		retAction = Actions.CANCEL;
    	close(event);
    }
    
    private void close(Event event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	stage.close();
    }
    
    public Actions getResponse () {
    	return retAction;
    }
    
    public DokuLink getSelectedDokuLink () {
    	selDokuLink.setRepository(aktRepository);
    	return selDokuLink;
    }
    
    
	private void getFirstLevels() {
		firstLevelList.add("/01_xSpez_Reviews");
		firstLevelList.add("/02_xSpez_abgenommen");
		firstLevelList.add("");
	}
}
