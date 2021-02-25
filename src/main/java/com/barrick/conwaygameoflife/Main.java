/*
 *  Copyright (c) 2020, Amy Barrick
 */
package com.barrick.conwaygameoflife;

import java.awt.Dimension;
import javax.swing.JFrame;

/**
 *
 * @author amy_e
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JFrame window = new JFrame("Conway's Game of Life");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        Dimension windowSize = new Dimension(Game.SIZE *Game.GUI_SIZE + 15, Game.SIZE *Game.GUI_SIZE + 40);
        window.setMinimumSize(windowSize);
        window.setPreferredSize(windowSize);
        Game game = new Game();
        window.add(game);
        window.setVisible(true);
        game.run();
    }
    
}
