/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tr.gov.ulakbim.jDenetX.clusterers.clustree.util;

/**
 * This is an interface for classes that are to be given along with every data
 * point inserted in the tree. The tree has to inform the implementation of
 * this <code>Budget</code> interface of every operation it does, and ask at the
 * places where it can stop prematurely if it should. This models the arrival of
 * a new data point in the stream, before there was enough time to insert the
 * current one.
 *
 * @author sanchez
 */
public interface Budget {

    /**
     * A function for the tree to ask if there is budget(time) left.
     *
     * @return A <code>boolean</code> that is <code>true</code> if the tree
     *         should go on, <code>false</code> otherwise.
     */
    public boolean hasMoreTime();

    /**
     * Inform the <code>Budget</code> class that an integer addition has been
     * performed by the tree.
     */
    public void integerAddition();

    /**
     * Inform the <code>Budget</code> that a certain number of integer additions
     * have been done.
     *
     * @param number the number of additions done.
     */
    public void integerAddition(int number);

    /**
     * Inform the <code>Budget</code> class that a double addition has been
     * performed by the tree.
     */
    public void doubleAddition();

    /**
     * Inform the <code>Budget</code> that a certain number of double additions
     * have been performed.
     *
     * @param number the number of additions done.
     */
    public void doubleAddition(int number);

    /**
     * Inform the <code>Budget</code> class that a integer multiplicaton has
     * been performed by the tree.
     */
    public void integerMultiplication();

    /**
     * Inform the <code>Budget</code> that a certain number of integer
     * multiplications have been performed.
     *
     * @param number the number of multiplication done.
     */
    public void integerMultiplication(int number);

    /**
     * Inform the <code>Budget</code> class that a double multiplicaton has
     * been performed by the tree.
     */
    public void doubleMultiplication();

    /**
     * Inform the <code>Budget</code> that a certain number of double
     * multiplications have been performed.
     *
     * @param number the number of multiplications done.
     */
    public void doubleMultiplication(int number);

    /**
     * Inform the <code>Budget</code> class that a integer division has
     * been performed by the tree.
     */
    public void integerDivision();

    /**
     * Inform the <code>Budget</code> that a certain number of integer divisions
     * have been performed.
     *
     * @param number the number of division done.
     */
    public void integerDivision(int number);

    /**
     * Inform the <code>Budget</code> class that a double division has
     * been performed by the tree.
     */
    public void doubleDivision();

    /**
     * Inform the <code>Budget</code> that a certain number of double divisions
     * have been performed.
     *
     * @param number the number of divisions done.
     */
    public void doubleDivision(int number);
}
