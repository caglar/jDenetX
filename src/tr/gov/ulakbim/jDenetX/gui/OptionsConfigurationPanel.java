/*
 *    OptionsConfigurationPanel.java
 *    Copyright (C) 2007 University of Waikato, Hamilton, New Zealand
 *    @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
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

import tr.gov.ulakbim.jDenetX.classifiers.HoeffdingTree;
import tr.gov.ulakbim.jDenetX.options.Option;
import tr.gov.ulakbim.jDenetX.options.OptionHandler;
import tr.gov.ulakbim.jDenetX.options.Options;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

public class OptionsConfigurationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public static final int FIXED_PANEL_WIDTH = 400;

    public static final int MAX_PANEL_HEIGHT = 300;

    protected Options options;

    protected List<OptionEditComponent> editComponents = new LinkedList<OptionEditComponent>();

    protected JButton helpButton = new JButton("Help");

    protected JButton resetButton = new JButton("Reset to defaults");

    public OptionsConfigurationPanel(String purposeString, Options options) {
        this.options = options;
        setLayout(new BorderLayout());
        if (purposeString != null) {
            JTextArea purposeTextArea = new JTextArea(purposeString, 3, 0);
            purposeTextArea.setEditable(false);
            purposeTextArea.setLineWrap(true);
            purposeTextArea.setWrapStyleWord(true);
            purposeTextArea.setEnabled(false);
            purposeTextArea.setBorder(BorderFactory
                    .createTitledBorder("Purpose"));
            purposeTextArea.setBackground(getBackground());
            add(purposeTextArea, BorderLayout.NORTH);
        }
        JPanel optionsPanel = createLabelledOptionComponentListPanel(options
                .getOptionArray(), this.editComponents);
        JScrollPane scrollPane = new JScrollPane(optionsPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        int optionPanelHeight = (int) optionsPanel.getPreferredSize()
                .getHeight();
        int scrollPaneHeight = (int) scrollPane.getPreferredSize().getHeight();
        scrollPane.setPreferredSize(new Dimension(FIXED_PANEL_WIDTH,
                scrollPaneHeight > MAX_PANEL_HEIGHT ? MAX_PANEL_HEIGHT
                        : scrollPaneHeight));
        optionsPanel.setPreferredSize(new Dimension(0, optionPanelHeight));
        add(scrollPane, BorderLayout.CENTER);
        JPanel lowerButtons = new JPanel();
        lowerButtons.setLayout(new FlowLayout());
        lowerButtons.add(this.helpButton);
        lowerButtons.add(this.resetButton);
        add(lowerButtons, BorderLayout.SOUTH);
        this.helpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                showHelpDialog();
            }
        });
        this.resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                resetToDefaults();
            }
        });
    }

    public static boolean showEditOptionsDialog(Component parent, String title,
                                                OptionHandler optionHandler) {
        OptionsConfigurationPanel panel = new OptionsConfigurationPanel(
                optionHandler.getPurposeString(), optionHandler.getOptions());
        if (JOptionPane.showOptionDialog(parent, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                null, null) == JOptionPane.OK_OPTION) {
            panel.applyChanges();
            return true;
        }
        return false;
    }

    public String getHelpText() {
        return this.options.getHelpString();
    }

    public void showHelpDialog() {
        JTextArea helpTextArea = new JTextArea(getHelpText(), 20, 80);
        helpTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        helpTextArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(helpTextArea),
                "Options Help", JOptionPane.INFORMATION_MESSAGE);
    }

    public void resetToDefaults() {
        for (OptionEditComponent editor : this.editComponents) {
            editor.setEditState(editor.getEditedOption().getDefaultCLIString());
        }
    }

    public void applyChanges() {
        for (OptionEditComponent editor : this.editComponents) {
            try {
                editor.applyState();
            } catch (Exception ex) {
                GUIUtils.showExceptionDialog(this, "Problem with option "
                        + editor.getEditedOption().getName(), ex);
            }
        }
    }

    protected static JPanel createLabelledOptionComponentListPanel(
            Option[] options, List<OptionEditComponent> editComponents) {
        JPanel panel = new JPanel();
        if ((options != null) && (options.length > 0)) {
            GridBagLayout gbLayout = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            panel.setLayout(gbLayout);
            for (int i = 0; i < options.length; i++) {
                JLabel label = new JLabel(options[i].getName());
                label.setToolTipText(options[i].getPurpose());
                gbc.gridx = 0;
                gbc.fill = GridBagConstraints.NONE;
                gbc.anchor = GridBagConstraints.EAST;
                gbc.weightx = 0;
                gbc.insets = new Insets(5, 5, 5, 5);
                gbLayout.setConstraints(label, gbc);
                panel.add(label);
                JComponent editor = options[i].getEditComponent();
                label.setLabelFor(editor);
                if (editComponents != null) {
                    editComponents.add((OptionEditComponent) editor);
                }
                gbc.gridx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.weightx = 1;
                gbc.insets = new Insets(5, 5, 5, 5);
                gbLayout.setConstraints(editor, gbc);
                panel.add(editor);
            }
        } else {
            panel.add(new JLabel("No options."));
        }
        return panel;
    }

    private static void createAndShowGUI() {

        // Create and set up the window.
        JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create and set up the content pane.
        Options options = new HoeffdingTree().getOptions();
        JPanel panel = new OptionsConfigurationPanel(null, options);
        // createLabelledOptionComponentListPanel(options
        // .getOptionArray(), null);
        panel.setOpaque(true); // content panes must be opaque
        frame.setContentPane(panel);

        // Display the window.
        frame.pack();
        // frame.setSize(400, 400);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createAndShowGUI();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
