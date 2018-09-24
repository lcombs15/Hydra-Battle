package main;
import java.util.ArrayList;
import hydra.Hydra;
import hydra.HydraNode;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.shape.Line;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.shape.Ellipse;
import javafx.stage.Screen;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.layout.RowConstraints;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;

/*
 * Main game class
 */
@SuppressWarnings("restriction")
public class HydraGame extends Application implements EventHandler<ActionEvent>{
	//Make screen size information available
	private Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
	private Double ScreenWidth = primaryScreenBounds.getWidth();
	private Double ScreenHeight = primaryScreenBounds.getHeight();
	
	//Wrapper and two main display areas
	private VBox wrapper = new VBox();
	private GridPane controlButtonArea = new GridPane();
	private Pane hydraCanvasArea = new Pane();
	
	//HUD controls
	private Button chopButton, exportButton, importButton;
	public Spinner<Integer> copiesSpinner;
	
	//Hydra instance
	private Hydra theHydra = new Hydra(this);
	
	public static void main(String[] args) {
		Application.launch("Hydra Battle");
	}

	@Override
	public void start(Stage primaryStage){
		//Setup GUI sizing
		wrapper.minHeightProperty().bind(primaryStage.heightProperty());
		wrapper.minWidthProperty().bind(primaryStage.widthProperty());
		wrapper.getChildren().addAll(hydraCanvasArea,controlButtonArea);
			hydraCanvasArea.prefHeightProperty().bind(wrapper.heightProperty().multiply(.80));
			controlButtonArea.prefHeightProperty().bind(wrapper.heightProperty().multiply(.20));
			hydraCanvasArea.prefWidthProperty().bind(wrapper.widthProperty());
			controlButtonArea.prefWidthProperty().bind(wrapper.widthProperty());
		
		//Finalize GUI structure
		Scene mainScene = new Scene(wrapper);
			mainScene.setOnKeyTyped(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent e) {
			        switch (e.getCharacter()) {
	                    case "c":    theHydra.randomChop();
	                    			drawGame();
	                    			break;
	                    case "+":  copiesSpinner.getValueFactory().increment(1); break;
	                    case "-":  copiesSpinner.getValueFactory().increment(-1); break;
	                    case "e":  theHydra.exportHydra(); break;
	                    case "i":  theHydra.importHydra(); break;
					default:
						break;
	                }
				}
			});
		primaryStage.setScene(mainScene);
		primaryStage.setMinWidth(ScreenWidth);
		primaryStage.setMinHeight(ScreenHeight);
		primaryStage.setTitle("Hydra Battle");
		primaryStage.show();
		
		/*Grid --> Scene --> Stage*/
		
		//make game maximized and update all elements inside.
		primaryStage.setMaximized(true);
		//Set background colors and such
		hydraCanvasArea.setStyle(CSS.hyrdaCanvasAreaStyle);
		controlButtonArea.setStyle(CSS.controlButtonAreaStyle);
		
		drawGame();
		setupHUD();
		greetUser();
	}

	private void greetUser() {
		Alert greeting = new Alert(AlertType.INFORMATION);
		greeting.setHeaderText("Welcome!");
		greeting.setContentText("Try to defeat the Hydra!\nInstructions:\n1.) Green nodes are clickable (Red is not)\n2.) You can click on the HUD below or you can use your keyboard (c,+,-,e,i).\n HINT: Holding down c can be quite helpful.\n Good Luck, Warrior!");
		greeting.show();
	}

	private void setupHUD() {
		//Setup HUD spacing
		controlButtonArea.setPadding( new Insets(ScreenHeight*CSS.controlAreaHeightMultiple*.1));
		controlButtonArea.setHgap(this.ScreenWidth/10);
		controlButtonArea.getRowConstraints().add(new RowConstraints(this.ScreenHeight*CSS.controlAreaHeightMultiple*.45));
		controlButtonArea.getRowConstraints().add(new RowConstraints());
		
		//<setup Controls>
		chopButton = new Button("Chop! (c)");
			controlButtonArea.add(chopButton, 0, 0);
			chopButton.setStyle("-fx-padding: " + (ScreenHeight*CSS.controlAreaHeightMultiple*.4) + "px; -fx-margin-left: " + (ScreenHeight*(1-CSS.controlAreaHeightMultiple)*.4) + "px;");
			GridPane.setRowSpan(chopButton, GridPane.REMAINING);
			chopButton.setOnAction(this);
		
		copiesSpinner = new Spinner<Integer>();
			copiesSpinner.setValueFactory(new IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 2, 1));
			controlButtonArea.add(copiesSpinner,1,1);
			GridPane.setValignment(copiesSpinner, VPos.TOP);
		
		Label copiesLabel = new Label("Set number of copies(+/-):");
			controlButtonArea.add(copiesLabel,1,0);
			GridPane.setValignment(copiesLabel, VPos.BOTTOM);
			GridPane.setHalignment(copiesLabel, HPos.CENTER);
			
		exportButton = new Button("Export (e)");
			exportButton.setOnAction(this);
			controlButtonArea.add(this.exportButton,2,0);	
			
		importButton = new Button("Import (i)");
			importButton.setOnAction(this);
			controlButtonArea.add(this.importButton,2,1);
		//<Setup Controls>					
	}

	public void drawGame() {
		//This method gets called whenever we remove a node, so, we clear the screen to start.
		hydraCanvasArea.getChildren().clear();
		
		//<Book Keeping>
		ArrayList<HydraNode> allNodes = theHydra.getBody().getAllNodes();
		ArrayList<Integer> levels = theHydra.numberOfNodesAtEachIndex();
		int currentDrawHeight = 0;
		//</Book Keeping>
		//Draw one layer of the Hydra at a time until we have an empty layer.
		do{
			int nodesDrawn = 0;
			//Determine dynamic node sizing that changes based on # of nodes and screen size.
			double nodeWidth = determineNodeWidthRadius(levels.get(currentDrawHeight));
			double nodeHeight = determineNodeHeightRadius(levels.size());
			double distanceInbetween =( 1 - (((nodeWidth * 2) / ScreenWidth)*levels.get(currentDrawHeight))) / (levels.get(currentDrawHeight) + 1);
			distanceInbetween = distanceInbetween * this.ScreenWidth;
				
				for(HydraNode n: allNodes){
					//If the HydraNode n is on our current level
					if(n.getHeightInTree() == currentDrawHeight){
						//Configure a Nodes' Ellipse
						Ellipse e = n.getGraphicNode();
							e.setRadiusX(nodeWidth);
							e.setRadiusY(nodeHeight);
							hydraCanvasArea.getChildren().add(e);
							if(n.canBeCopped()){
								e.setFill(CSS.clickableHydraNode);
							}else{
								e.setFill(CSS.unClickableHydraNode);
							}
							
							//The first node drawn is always special
							if(nodesDrawn == 0){
								e.setCenterX(distanceInbetween + e.getRadiusX());
							}else{
								e.setCenterX((((distanceInbetween) * (nodesDrawn+1))) + (((nodesDrawn*2)+1)*(e.getRadiusX())));
							}
						
							if(currentDrawHeight == 0){
								e.setCenterY(hydraCanvasArea.getHeight());
							}else{
								e.setCenterY((this.ScreenHeight*CSS.hydraCanvasHeightMultiple) - ((currentDrawHeight + 1) * ((this.ScreenHeight*CSS.hydraCanvasHeightMultiple)/(levels.size() + 1))));
							}
						nodesDrawn++;
					}
				}
			currentDrawHeight++;
		}while(currentDrawHeight < levels.size());
		
		//Draw the relationship lines
		this.theHydra.drawLines();
		
		//See if the game is won!
		checkForGameWon();		
	}

	//Don't ever let the nodes be taller than 20% of the display
	private double determineNodeHeightRadius(int size) {
		double retVal = (ScreenHeight* CSS.hydraCanvasHeightMultiple) / ((size + 2)*2);
		
		if(retVal > ScreenHeight*.05){
			return this.ScreenHeight*.05;
		}
		
		return retVal;
	}

	//Congratulate the user on a job well done
	private void checkForGameWon() {
		if(theHydra.gameWon()){
			Alert gameWon = new Alert(AlertType.INFORMATION);
			gameWon.setContentText("Congratulations!");
			gameWon.setHeaderText("Hydra Defeated!");
			gameWon.show();
			theHydra = new Hydra(this);
			drawGame();
		}
	}
	
	//Outside classes can pass in things to be drawn on screen, like relationship lines.
	public void addGraphicalNode(Node n){
		this.hydraCanvasArea.getChildren().add(n);
		//If we are adding a relationship line, make sure it goes behind the Hydra heads.
		if(n instanceof Line){
			n.toBack();
		}
	}
	
	//Just an off-site calculation to help break up complicated graphics calculations
	private double determineNodeWidthRadius(int numNodes){
		double retVal = ScreenWidth / ((numNodes + 1)*2);
						
		if(retVal > (ScreenWidth * .075)){
			return ScreenWidth * .075;
		}
		
		return retVal;
	}

	//Determine which button was clicked and handle it accordingly
	@Override
	public void handle(ActionEvent e) {
		if (((Button) e.getSource()).equals(this.chopButton)){
			theHydra.randomChop();
			this.drawGame();
		}else if (((Button) e.getSource()).equals(this.exportButton)){
			theHydra.exportHydra();
		}else if (((Button) e.getSource()).equals(this.importButton)){
			theHydra.importHydra();
			this.drawGame();
		}
	}
}
