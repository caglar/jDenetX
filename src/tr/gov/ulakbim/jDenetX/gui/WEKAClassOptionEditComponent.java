/*
 *    WEKAClassOptionEditComponent.java
 *    Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 *    @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 *    @author FracPete (fracpete at waikato dot ac dot nz)
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

import tr.gov.ulakbim.jDenetX.options.Option;
import tr.gov.ulakbim.jDenetX.options.WEKAClassOption;
import weka.core.Utils;
import weka.gui.GenericObjectEditor;
import weka.gui.GenericObjectEditor.GOEPanel;
import weka.gui.PropertyDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WEKAClassOptionEditComponent
        extends JPanel
        implements OptionEditComponent {

    private static final long serialVersionUID = 1L;

    protected WEKAClassOption editedOption;

    protected JTextField textField = new JTextField();

    protected JButton editButton = new JButton("Edit");

    public WEKAClassOptionEditComponent(WEKAClassOption option) {
        this.editedOption = option;
        this.textField.setEditable(false);
        setLayout(new BorderLayout());
        add(this.textField, BorderLayout.CENTER);
        add(this.editButton, BorderLayout.EAST);
        this.editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                editObject();
            }
        });
        setEditState(this.editedOption.getValueAsCLIString());
    }

    public void applyState() {
        this.editedOption.setValueViaCLIString(this.textField.getText());
    }

    public Option getEditedOption() {
        return this.editedOption;
    }

    public void setEditState(String cliString) {
        this.textField.setText(cliString);
    }

    public void editObject() {
        final GenericObjectEditor goe = new GenericObjectEditor(true);
        goe.setClassType(editedOption.getRequiredType());
        try {
            String[] options = Utils.splitOptions(editedOption.getValueAsCLIString());
            String classname = options[0];
            options[0] = "";
            Object obj = Class.forName(classname).newInstance();
            if (obj instanceof weka.core.OptionHandler)
                ((weka.core.OptionHandler) obj).setOptions(options);
            goe.setValue(obj);
            ((GOEPanel) goe.getCustomEditor()).addOkListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object obj = goe.getValue();
                    String s = obj.getClass().getName();
                    if (obj instanceof weka.core.OptionHandler)
                        s += " " + Utils.joinOptions(((weka.core.OptionHandler) obj).getOptions());
                    setEditState(s.trim());
                }
            });
            PropertyDialog dialog;
            if (PropertyDialog.getParentDialog(this) != null)
                dialog = new PropertyDialog(PropertyDialog.getParentDialog(this), goe);
            else
                dialog = new PropertyDialog(PropertyDialog.getParentFrame(this), goe);
            dialog.setModal(true);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
