# Hydra-Battle

This is a project I worked on in CSC364 (Data Structures & Algorithms). It works with Java generics, cloanable, JavaFX, and much more to create the Hydra Battle!

The goal of the game is to defeat the hydra!

## The Logic

* If a head has no children, it can be chopped.
* If a head has children, it cannot be chopped.
* Any head chopped causes it's parent to duplicate itself and it's children (Minus head chopped) X times (X=2 by default)
