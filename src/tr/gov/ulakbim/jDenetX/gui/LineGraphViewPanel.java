/*
 *    LineGraphViewPanel.java
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

import tr.gov.ulakbim.jDenetX.evaluation.LearningCurve;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

public class LineGraphViewPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    static final BasicStroke lineStroke = new BasicStroke(3.0f);

    protected class PlotLine {

        public LearningCurve curve;

        public Color colour;

        public int xAxisIndex;

        public int yAxisIndex;

        public double xMin;

        public double xMax;

        public double yMin;

        public double yMax;

        public float convertX(double x) {
            return (float) ((x - this.xMin) / (this.xMax - this.xMin));
        }

        public float convertY(double y) {
            return (float) ((y - this.yMin) / (this.yMax - this.yMin));
        }

        public Shape getShapeToPlot() {
            GeneralPath path = new GeneralPath();
            if (this.curve.numEntries() > 0) {
                path.moveTo(convertX(this.curve.getMeasurement(0,
                        this.xAxisIndex)), convertY(this.curve.getMeasurement(
                        0, this.yAxisIndex)));
                for (int i = 1; i < this.curve.numEntries(); i++) {
                    path.lineTo(convertX(this.curve.getMeasurement(i,
                            this.xAxisIndex)), convertY(this.curve
                            .getMeasurement(i, this.yAxisIndex)));
                }
            }
            return path;
        }
    }

    protected class PlotPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        @Override
        public void paint(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            Graphics2D g2 = (Graphics2D) g;
            g2.scale(getWidth(), getHeight());
            g2.setStroke(lineStroke);
            for (PlotLine plotLine : LineGraphViewPanel.this.plotLines) {
                g2.setPaint(plotLine.colour);
                g2.draw(plotLine.getShapeToPlot());
            }
        }
    }

    protected class PlotTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return "source";
                case 1:
                    return "colour";
                case 2:
                    return "x-axis";
                case 3:
                    return "y-axis";
                case 4:
                    return "x-min";
                case 5:
                    return "x-max";
                case 6:
                    return "y-min";
                case 7:
                    return "y-max";
            }
            return null;
        }

        public int getColumnCount() {
            return 8;
        }

        public int getRowCount() {
            return LineGraphViewPanel.this.plotLines.size();
        }

        public Object getValueAt(int row, int col) {
            PlotLine plotLine = LineGraphViewPanel.this.plotLines.get(row);
            switch (col) {
                case 0:
                    return plotLine.curve;
                case 1:
                    return plotLine.colour;
                case 2:
                    return plotLine.curve.getMeasurementName(plotLine.xAxisIndex);
                case 3:
                    return plotLine.curve.getMeasurementName(plotLine.yAxisIndex);
                case 4:
                    return plotLine.xMin;
                case 5:
                    return plotLine.xMax;
                case 6:
                    return plotLine.yMin;
                case 7:
                    return plotLine.yMax;
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

    }

    protected List<PlotLine> plotLines = new ArrayList<PlotLine>();

}
