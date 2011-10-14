/*
 *    ClassificationTabPanel.java
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

import java.awt.*;

public class ClassificationTabPanel extends AbstractTabPanel {

    private static final long serialVersionUID = 1L;

    protected TaskManagerPanel taskManagerPanel;

    protected PreviewPanel previewPanel;

    public ClassificationTabPanel() {
        this.taskManagerPanel = new TaskManagerPanel();
        this.previewPanel = new PreviewPanel();
        this.taskManagerPanel.setPreviewPanel(this.previewPanel);
        setLayout(new BorderLayout());
        add(this.taskManagerPanel, BorderLayout.NORTH);
        add(this.previewPanel, BorderLayout.CENTER);
    }

    //returns the string to display as title of the tab
    public String getTabTitle() {
        return "Classification";
    }

    //a short description (can be used as tool tip) of the tab, or contributor, etc.
    public String getDescription() {
        return "MOA Classification";
    }

}



