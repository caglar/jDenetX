/*
 *    GUI.java
 *    Copyright (C) 2007 University of Waikato, Hamilton, New Zealand
 *    @author Albert Bifet (abifet@cs.waikato.ac.nz)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package tr.gov.ulakbim.jDenetX.gui;

import javax.swing.*;
import java.awt.*;

public class GUI extends JPanel {

    private static final long serialVersionUID = 1L;

    private javax.swing.JTabbedPane panel;

    public GUI() {
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());

        // Create and set up tabs
        panel = new javax.swing.JTabbedPane();
        add(panel, BorderLayout.CENTER);

        // initialize additional panels
        String[] tabs = GUIDefaults.getTabs();
        for (int i = 0; i < tabs.length; i++) {
            try {
                // determine classname
                String[] optionsStr = tabs[i].split(":");
                String classname = optionsStr[0];
                // setup panel
                AbstractTabPanel tabPanel = (AbstractTabPanel) Class.forName(classname).newInstance();
                panel.addTab(tabPanel.getTabTitle(), null, (JPanel) tabPanel, tabPanel.getDescription());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        try {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // Create and set up the window.
                    JFrame frame = new JFrame("MOA Graphical User Interface");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    try {
                        javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    } catch (Exception e) {
                    }

                    GUI gui = new GUI();
                    frame.getContentPane().setLayout(new BorderLayout());
                    frame.getContentPane().add(gui);

                    // Display the window.
                    frame.pack();
                    frame.setVisible(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}