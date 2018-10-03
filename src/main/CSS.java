package main;

import javafx.scene.paint.Color;

/*
 * Just a one-stop-shop for changing GUI style.
 */
@SuppressWarnings("restriction")
public final class CSS {
    public static final String hyrdaCanvasAreaStyle = "-fx-background-color: black;";
    public static final String controlButtonAreaStyle = "-fx-background-color: Silver;";

    public static final Color clickableHydraNode = Color.DARKGREEN;
    public static final Color unClickableHydraNode = Color.DARKRED;
    public static final Color hydraRelationshipColor = Color.WHITE;

    //The Hydra gets 80% of the screen while the buttons get 20%
    public static final double hydraCanvasHeightMultiple = .8, controlAreaHeightMultiple = .2;
}
