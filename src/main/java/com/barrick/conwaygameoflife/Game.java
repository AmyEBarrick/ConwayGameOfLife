/*
 *  Copyright (c) 2020, Amy Barrick
 */

package com.barrick.conwaygameoflife;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author Amy Barrick
 */
public class Game extends JPanel implements ActionListener {
    static final int SIZE = 100;
    static final int GUI_SIZE = 5;
    boolean[][] board = new boolean[SIZE][SIZE];
    boolean[][] previous = new boolean[SIZE][SIZE];
    final List<Rectangle> activity;
    int steps = 0;
    Timer timer;
    
    public Game() {
        super();
        activity = new ArrayList<>();
        Random random = new Random();
        int toAdd = (int)(SIZE * SIZE * 0.5);
        int added = 0;
        while(added < toAdd) {
            int x = random.nextInt(SIZE);
            int y = random.nextInt(SIZE);
            if(board[x][y] == false) {
                board[x][y] = true;
                added++;
            }
        }
    }

    public void run() {
        if(timer == null) {
            timer = new Timer(500, this);
        }
        timer.start();
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        steps++;
        boolean[][] next = step();
        boolean allDead = true;
        boolean same = true;
        boolean samePrev = true;
        for(int i=0; i < next.length; i++) {
            for(int j=0; j < board[i].length; j++) {
                if(next[i][j]) {
                    allDead = false;
                }
                if(next[i][j] != board[i][j]) {
                    same = false;
                }
                if(next[i][j] != previous[i][j]) {
                    samePrev = false;
                }
            }
        }
        activity.clear();
        if(allDead) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "All dead in "+steps+" step(s)");
        } else if (same || samePrev) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Stable in "+steps+" step(s)");
        } else {
            //  collect all Horizontal (height=1) boxes
            int i = 0;
            int j = 0;
            while(i < board.length && j < board[0].length) {
                if(next[i][j] != board[i][j] || next[i][j] != previous[i][j]) {
                    final int startI = i;
                    while(i < board.length && (next[i][j] != board[i][j] || next[i][j] != previous[i][j]))
                        i++;
                    activity.add(new Rectangle(startI, j, i-startI, 1));
                }
                if(i >= board.length-1) {
                    j++;
                    i = 0;
                } else
                    i++;
            }
            //  merge adjacent boxes
            mergeActivity();
            final List<Rectangle> toRemove = new ArrayList<>();
            activity.forEach((rect) -> {
                boolean stable = true;
                for(int x=rect.x; x < rect.x + rect.width && stable; x++)
                    for(int y=rect.y; y < rect.y + rect.height && stable; y++)
                        stable = next[x][y] == previous[x][y];
                if (stable) {
                    toRemove.add(rect);
                }
            });
            activity.removeAll(toRemove);
        }
        previous = board;
        board = next;
        super.repaint();
    }
    
    private boolean[][] step() {
        boolean[][] next = new boolean[SIZE][SIZE];

        for(int i=0; i < board.length; i++) {
            for(int j=0; j < board[i].length; j++) {
                int surrounding = countSurroundingAlive(i,j);
                if(surrounding == 2) {
                    //  ALIVE (true) with 2 or 3 surrounding lives
                    next[i][j] = board[i][j];
                } else if(surrounding == 3) {
                    //  Any with exactly 3 surrounding lives (even if currently dead)
                    next[i][j] = true;
                }
            }
        }
        
        return next;
    }

    private int countSurroundingAlive(int x, int y) {
        int alive = 0;

        for(int i = -1; i <= 1; i++) {
            for(int  j = -1; j <= 1; j++) {
                if((i != 0 || j != 0) &&
                   x+i >= 0 && x+i < board.length &&
                   y+j >= 0 && y+j < board[x+i].length &&
                   board[x+i][y+j]) {
                    alive++;
                }
            }
        }
        
        return alive;
    }
    
    private void mergeActivity() {
        boolean merged = true;
        while(merged) {
            merged = false;
            for(int i=0; i< activity.size(); i++) {
                Rectangle mergeWith = activity.get(i);
                for(int j=i+1; j< activity.size(); j++) {
                    if(shouldMerge(mergeWith, activity.get(j))) {
                        mergeWith = mergeWith.union(activity.get(j));
                        activity.remove(j);
                        j--;
                        merged = true;
                    }
                }
                if(! mergeWith.equals(activity.get(i)))
                    activity.set(i, mergeWith);
            }
        }
    }
    
    private boolean shouldMerge(Rectangle a, Rectangle b) {
        if(a.width >= SIZE/10 || b.width >= SIZE/10 ||
           a.height >= SIZE/10 || b.height >= SIZE/10)
            return false;
        if(a.intersects(b))
            return true;
        if((a.x <= b.x && a.x + a.width >= b.x) ||
           (b.x <= a.x && b.x + b.width >= a.x))
            return (a.y == b.y+b.height+1 || b.y == a.y+a.height+1);
        if((a.y <= b.y && a.y + a.height >= b.y) ||
           (b.y <= a.y && b.y + b.height >= a.y))
            return (a.x == b.x+b.width+1 || b.x == a.x+a.width+1);
        return false;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.white);
        g.fillRect(0, 0, board.length*GUI_SIZE, board[0].length*GUI_SIZE);

        g.setColor(new Color(128, 255, 255));
        activity.forEach((rect) -> {
            g.fillRect(rect.x*GUI_SIZE, rect.y*GUI_SIZE,
                       rect.width*GUI_SIZE, rect.height*GUI_SIZE);
        });
        g.setColor(Color.black);
        for(int i=0; i < board.length; i++) {
            for(int j=0; j < board[i].length; j++) {
                if(board[i][j]) {
                    g.fillRect(i*GUI_SIZE, j*GUI_SIZE, GUI_SIZE, GUI_SIZE);
                }
            }
        }
        
        g.setColor(Color.green);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString(String.valueOf(steps), 15, 25);
    }
}
