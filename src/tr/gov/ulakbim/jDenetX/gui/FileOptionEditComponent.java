/*
 *    FileOptionEditComponent.java
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

import tr.gov.ulakbim.jDenetX.options.FileOption;
import tr.gov.ulakbim.jDenetX.options.Option;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FileOptionEditComponent extends JPanel implements
        OptionEditComponent {

    private static final long serialVersionUID = 1L;

    protected FileOption editedOption;

    protected JTextField textField = new JTextField();

    protected JButton browseButton = new JButton("Browse");

    public FileOptionEditComponent(FileOption option) {
        this.editedOption = option;
        setLayout(new BorderLayout());
        add(this.textField, BorderLayout.CENTER);
        add(this.browseButton, BorderLayout.EAST);
        this.browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                browseForFile();
            }
        });
        setEditState(this.editedOption.getValueAsCLIString());
    }

    public void applyState() {
        this.editedOption.setValueViaCLIString(this.textField.getText()
                .length() > 0 ? this.textField.getText() : null);
    }

    public Option getEditedOption() {
        return this.editedOption;
    }

    public void setEditState(String cliString) {
        this.textField.setText(cliString);
    }

    public void browseForFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(true);
        String extension = this.editedOption.getDefaultFileExtension();
        if (extension != null) {
            fileChooser.addChoosableFileFilter(new FileExtensionFilter(
                    extension));
        }
        fileChooser.setSelectedFile(new File(this.textField.getText()));
        if (this.editedOption.isOutputFile()) {
            if (fileChooser.showSaveDialog(this.browseButton) == JFileChooser.APPROVE_OPTION) {
                File chosenFile = fileChooser.getSelectedFile();
                String fileName = chosenFile.getPath();
                if (!chosenFile.exists()) {
                    if ((extension != null) && !fileName.endsWith(extension)) {
                        fileName = fileName + "." + extension;
                    }
                }
                this.textField.setText(fileName);
            }
        } else {
            if (fileChooser.showOpenDialog(this.browseButton) == JFileChooser.APPROVE_OPTION) {
                this.textField.setText(fileChooser.getSelectedFile().getPath());
            }
        }
    }

}
