/*
 * GraphAxes.java
 *
 * Created on 13.04.2010, 10:43:25
 */

package tr.gov.ulakbim.jDenetX.gui.visualization;

import java.awt.*;
import java.text.DecimalFormat;

/**
 * @author jansen
 */
public class GraphAxes extends javax.swing.JPanel {
    private final int x_offset_left = 35;
    private final int x_offset_right = 5;
    private final int y_offset_bottom = 20;
    private final int y_offset_top = 20;

    private int height;
    private int width;

    private double x_resolution; //how many pixels per 1px
    private int processFrequency;

    private double min_value = 0;
    private double max_value = 1;
    private int max_x_value;

    /**
     * Creates new form GraphAxes
     */
    public GraphAxes() {
        initComponents();
    }

    public void setXMaxValue(int max) {
        max_x_value = max;
    }

    public void setXResolution(double resolution) {
        x_resolution = resolution;
    }

    public void setProcessFrequency(int frequency) {
        processFrequency = frequency;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //stream not started yet
        if (processFrequency == 0) return;

        height = getHeight() - y_offset_bottom - y_offset_top;
        width = getWidth() - x_offset_left - x_offset_right;

        //System.out.println(width);

        g.setColor(new Color(236, 233, 216));
        g.fillRect(0, 0, getWidth(), getHeight());

        //draw background
        g.setColor(Color.WHITE);
        g.fillRect(x_offset_left, y_offset_top, width, height);

        g.setFont(new Font("Tahoma", 0, 11));


        xAxis(g);
        yAxis(g);
    }

    private void xAxis(Graphics g) {
        g.setColor(Color.BLACK);

        //x-achsis
        g.drawLine(x_offset_left, calcY(0), width + x_offset_left, calcY(0));

        //x achsis labels
        int w = 100;
        for (int i = 0; w * i < width - x_offset_right; i++) {
            g.drawLine(w * i + x_offset_left, height + y_offset_top, w * i + x_offset_left, height + y_offset_top + 5);

//            int exp = (int)Math.log10(w*i*processFrequency);
//            int multipleof1000 = exp/3;
            String label = Integer.toString((int) (w * i * processFrequency * x_resolution));
//            if(multipleof1000 == 1) label = w*i*processFrequency/1000+"k";

            int str_length = g.getFontMetrics().stringWidth(label);
            g.drawString(label, w * i + x_offset_left - str_length / 2, height + y_offset_top + 18);
        }
    }


    private void yAxis(Graphics g) {
        //y-achsis
        g.setColor(Color.BLACK);
        g.drawLine(x_offset_left, calcY(0), x_offset_left, y_offset_top);

        //center horizontal line
        g.setColor(new Color(220, 220, 220));
        g.drawLine(x_offset_left, height / 2 + y_offset_top, getWidth(), height / 2 + y_offset_top);

        //3 y-achsis markers + labels
        g.setColor(Color.BLACK);
        DecimalFormat d = new DecimalFormat("0.00");
        int digits_y = (int) (Math.log10(max_value)) - 1;
        double upper = Math.ceil(max_value / Math.pow(10, digits_y));
        if (digits_y < 0) upper *= Math.pow(10, digits_y);

        if (Double.isNaN(upper)) upper = 1.0;

        g.drawString(d.format(0.0), 3, height + y_offset_top + 5);
        g.drawString(d.format(upper / 2), 3, height / 2 + y_offset_top + 5);
        g.drawString(d.format(upper), 3, y_offset_top + 5);
        g.drawLine(x_offset_left - 5, height + y_offset_top, x_offset_left, height + y_offset_top);
        g.drawLine(x_offset_left - 5, height / 2 + y_offset_top, x_offset_left, height / 2 + y_offset_top);
        g.drawLine(x_offset_left - 5, y_offset_top, x_offset_left, y_offset_top);

    }

    public void setYMinMaxValues(double min, double max) {
        min_value = min;
        max_value = max;
    }

    public void setMaxXValue(int max) {
        max_x_value = max;
    }

    private int calcY(double value) {
        return (int) (height - (value / max_value) * height) + y_offset_top;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}