package main;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

import javafx.stage.FileChooser;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import main.HydraGame;
import main.HydraNode;


/*
 * This is my container class for the Hydra displayed on screen.
 * This wrapper class accesses a number of protected methods in HydraNode since we are still in the same package.
 */
@SuppressWarnings("restriction")
public class Hydra {
    private HydraNode body = new HydraNode(true);
    protected HydraGame gameInstance;

    public Hydra(HydraGame instance) {
        body.gameInstance = instance;
        gameInstance = instance;
        generteRandomHydra();
    }

    //Just for debugging
    public void print() {
        System.out.println(body.toString());
    }

    /**
     * This helps with drawing the tree quite a lot. This returns the number of nodes at each layer in the Hydra.
     * So, retVal.get(0) is always 1 because only the body is on layer one.
     * So, retVal.get(1) would be 4 if and only if the body has 4 children.
     */
    public ArrayList<Integer> numberOfNodesAtEachIndex() {
        ArrayList<HydraNode> allNodes = body.getAllNodes();
        ArrayList<Integer> retVal = new ArrayList<Integer>();
        //Initialize retVal
        for (int i = 0; i < allNodes.size(); i++) {
            retVal.add(0);
        }
        //Increment the layers accordingly based on allNodes
        for (HydraNode h : allNodes) {
            retVal.set(h.getHeightInTree(), retVal.get(h.getHeightInTree()) + 1);
        }
        //We don't care about layers with no nodes on them so we remove them
        retVal.removeAll(Collections.singleton(0));

        return retVal;
    }

    public void drawLines() {
        body.drawLines();
    }

    public void randomChop() {
        //Get all nodes and remove any nodes that can't be chopped
        ArrayList<HydraNode> nodes = body.getAllNodes();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            if (!nodes.get(i).canBeCopped()) {
                nodes.remove(i);
            }
        }

        //Without this we can run into issues when the game is won because the random throws an error on r.netInt(0)
        if (nodes.size() == 0) {
            return;
        }

        //Pick a node and call chop
        Random r = new Random();
        nodes.get(r.nextInt(nodes.size())).chop();
    }

    private String exportString() {
        return body.exportString();
    }

    //Send the Hydra to a file provided by the user
    public void exportHydra() {
        try {
            FileChooser getFile = new FileChooser();
            File f = getFile.showSaveDialog(null);
            PrintWriter pw = new PrintWriter(f);
            pw.write(exportString());
            pw.close();
        } catch (Exception e) {
            Alert exportIssue = new Alert(AlertType.ERROR);
            exportIssue.setContentText("File not saved.");
            exportIssue.setHeaderText("An ERROR occured when exporting this Hydra.");
            exportIssue.show();
            System.out.println("ERROR: File not exported.");
        }

    }

    //Import Hydra from user file
    public void importHydra() {
        try {
            FileChooser getFile = new FileChooser();
            File f = getFile.showOpenDialog(null);
            Scanner s = new Scanner(f);
            String data = s.nextLine();
            s.close();
            generateHydraFromString(data);
        } catch (Exception e) {
            Alert importIssue = new Alert(AlertType.ERROR);
            importIssue.setContentText("Nothing imported.");
            importIssue.setHeaderText("An ERROR occured when importing.");
            importIssue.show();
            System.out.println("ERROR: Nothing imported.");
        }

    }

    /*
     * Move Hydra string from String (char[]) to int[]
     * Also, Alert the user of an invalid file if non-numerics are found.
     *
     * Finally, start recursive call over created int[]
     */
    private void generateHydraFromString(String data) {
        int[] nums = new int[data.length()];
        try {
            for (int i = 0; i < nums.length; i++) {
                nums[i] = Integer.parseInt(data.substring(i, i + 1));
                System.out.print(" " + nums[i]);
            }
            System.out.println();
        } catch (NumberFormatException e) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("Error: Invalid File Imported");
            a.setContentText("Invalid Hydra Import File. Nothing has changed.");
            a.show();
            return;
        }

        body.children.clear();
        body.generateHydraFromIntArray(nums, 0, 1, 0);
        gameInstance.drawGame();
    }


    //Pretty straight forward. Randomly add five nodes to a new Hydra
    private void generteRandomHydra() {
        //Remove existing children
        this.body.children.clear();

        int childrenToAdd = 5;
        Random r = new Random();

        //Attached at least 1 (or at most 5) children to the body
        //We cannot have a body with zero children. It's impossible.
        int numberOfNodesConnectedToBody = r.nextInt(5) + 1;
        for (int i = 0; i < numberOfNodesConnectedToBody; i++) {
            body.addChild(new HydraNode());
        }

        //figure out how many children we have left to add
        childrenToAdd -= numberOfNodesConnectedToBody;

        //Add the rest of them
        while (childrenToAdd > 0) {
            //Randomly choose any node (including the body!)
            ArrayList<HydraNode> allnodes = body.getAllNodes();
            int index = r.nextInt(allnodes.size());
            allnodes.get(index).addChild(new HydraNode());

            childrenToAdd--;
        }
    }

    //The game is one when all the heads are gone!
    public boolean gameWon() {
        return body.children.size() == 0;
    }

    //Just a getter
    public HydraNode getBody() {
        return this.body;
    }
}
