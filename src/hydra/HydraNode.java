package hydra;

import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Ellipse;
import main.CSS;
import main.HydraGame;
import javafx.scene.shape.Line;
/*
 * HydraNode is the basis of my Hydra data structure.
 * It's responsible for:
 * 	1.) Child/parent data
 * 	2.) The Ellipse class that displays on screen
 * 	3.) It handles its' own clicking functionality
 * 	4.) It chops itself
 */
@SuppressWarnings({"restriction", "serial"})
public final class HydraNode extends ActionEvent implements Cloneable, EventHandler<MouseEvent>{
	/*A list of all children a given node my have.*/
	protected ArrayList<HydraNode> children = new ArrayList<HydraNode>();
	
	/**This boolean is a fail-safe that ensures we never try to add the body of our hydra as a child to another node.**/
	private boolean isHydraBody = false;
	
	/*Just keeping track of our parent (null if this node is the body)*/
	private HydraNode parent = null;
	
	/*What display's on screen*/
	private Ellipse graphicalNode = new Ellipse();
	
	/*Just the game instance. Used to tell the game to re-draw and such*/
	protected HydraGame gameInstance;
	
	/*Book keeping that helps with drawing the tree dynamically*/
	private int heightInTree = -1;
	
	public HydraNode(boolean isHydraBody){
		this.isHydraBody = isHydraBody;
		if(isHydraBody){
			heightInTree = 0;
		}
		graphicalNode.setOnMouseClicked(this);
	}
	
	//If no boolean is given for isHydraBody, we assume it to be false
	public HydraNode(){
		graphicalNode.setOnMouseClicked(this);
	}
	
	public boolean isHydraBody(){
		return isHydraBody;
	}
	
	/*Add a given node if and only if it is not a hydra body*/
	protected void addChild(HydraNode n){
		if(!n.isHydraBody()){
			//Give the child everything it needs to know
			this.children.add(n);
			n.parent = this;
			n.gameInstance = gameInstance;
			n.heightInTree = this.heightInTree + 1;
		}else{
			throw new UnsupportedOperationException("HydraNode cannot have a body as a child.");
		}
	}
	
	/*Returns true if this node can be chopped (Not a body and has no children)*/
	public boolean canBeCopped(){
		return !isHydraBody && children.size() == 0;
	}
	
	/**If the node can be chopped, do so. Otherwise, yell at the user.**/
	protected void chop(){
		if(canBeCopped()){
			if(parent.isHydraBody()){
				//No re-growth occurs for nodes connected to the body
				parent.deleteChild(this);
			}else{
				parent.deleteChild(this);
				//To re-grow, we clone the parent subtree and add it to its' respective parent n times
				for(int i = 0; i < this.gameInstance.copiesSpinner.getValue(); i++){
					parent.parent.addChild(parent.clone());
				}
				//If a node is being chopped it no longer has a parent (Orphan)
				//This helps with debugging
				this.parent = null;
			}
		}else{
			Alert cannotBeChoppedWarning = new Alert(AlertType.WARNING);
			cannotBeChoppedWarning.setContentText("Be careful where you click!");
			cannotBeChoppedWarning.setHeaderText("WARNING: Only nodes without children can be chopped.");
			cannotBeChoppedWarning.show();
		}
	}

	private void deleteChild(HydraNode n){
		children.remove(n);
	}
	
	
	//This isn't very pretty but it helps with resolving relationship issues (Like Dr. Phil for HydraNodes!)
	//I pass the ** to my helper method so that if the body has two children and one of those children has one child,
	/**
	 * 
	 * You get something like this: (I'm taking some indentation inspiration from HTML)
	 * Me(Body)
	 * **CHILD
	 * ****ME
	 * ****CHild</me>
	 * **Child</me>
	 * 
	 */
	@Override
	public String toString(){
		return toString("**");
	}
	//See above comments. The delimiter is doubled each time we go up in the tree.
	private String toString(String delimetter){
		String retVal = "ME";
		if(isHydraBody){
			retVal += "(Body)";
		}
		
		for(HydraNode c: children){
			retVal += "\n" + delimetter +  "-CHILD";
			retVal += "\n    -" + c.toString(delimetter + delimetter);
		}
		retVal += "<me>";
		return retVal;
	}

	//This is what empowers the re-growth feature.
	@Override
	public HydraNode clone(){
		//Make our shallow copy to keep primitives.
		HydraNode retVal = (HydraNode) super.clone();
		
		//Make copies of our children
		retVal.children = new ArrayList<HydraNode>();
		for(HydraNode n: this.children){
			retVal.addChild(n.clone());
		}
		
		//Make sure our clone doesn't point at the same things on screen.
		retVal.graphicalNode = new Ellipse();
		
		//make sure I don't get called if my clone is clicked (We don't care)
		retVal.graphicalNode.setOnMouseClicked(retVal);
		
		return retVal;
	}
	
	//Just a getter.
	public int getHeightInTree(){
		return isHydraBody? 0: this.heightInTree;
	}
	
	public Ellipse getGraphicNode(){
		return this.graphicalNode;
	}
	
	//Recursively build a list of all nodes in our tree.
	public ArrayList<HydraNode> getAllNodes(){
		ArrayList<HydraNode> retVal = new ArrayList<HydraNode>();
			retVal.add(this);
			for(HydraNode n: children){
				retVal.addAll(n.getAllNodes());
			}
		return retVal;
	}
	
	//If I'm clicked, call chop and tell the game to update accordingly.
	@Override
	public void handle(MouseEvent event) {
		chop();
		gameInstance.drawGame();
	}
	
	//Recursively draw relationship lines in the game
	protected void drawLines(){
		if(!isHydraBody()){
			Line line = new Line();
			line.setStroke(CSS.hydraRelationshipColor);
			line.setStartX(parent.graphicalNode.getCenterX());
			line.setStartY(parent.graphicalNode.getCenterY());
			line.setEndX(this.graphicalNode.getCenterX());
			line.setEndY(this.graphicalNode.getCenterY());
			this.gameInstance.addGraphicalNode(line);
		}
		for(HydraNode n: children){
			n.drawLines();
		}
	}

	//Generate String that can be imported/exported for current subTree
	public String exportString(){
		String retVal = "";
		if (isHydraBody){
			retVal += "" + this.children.size();
		}
		for(HydraNode n: children){
			retVal += n.children.size();
		}
		for(HydraNode n: children){
			retVal += n.exportString();
		}
		return retVal;
	}

	/*
	 * Recursive call to re-create an imported Hydra
	 * nums: values imported from text file
	 * rootIndex: where "this" is located in the array.
	 * startIndex: where the first child is located
	 * */
	protected void generateHydraFromIntArray(int[] nums, int rootIndex, int startIndex, int sum) {
		//Loop through my (this) entry in the array
		for(int i = 0; i < nums[rootIndex];i++){
			//Add a child of mine
			HydraNode importedNode = new HydraNode();
			this.addChild(importedNode);
			//Some book-keeping
			sum += nums[i];
			//Don't make a call to a child if it has no children
			if(nums[startIndex + i] == 0){
				continue;
			}
			//Make a call to the children to add their possible children
			importedNode.generateHydraFromIntArray(nums, startIndex + i, sum + 1, sum);
		}
	}
}
