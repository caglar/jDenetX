package tr.gov.ulakbim.jDenetX.core;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Sep 14, 2010
 * Time: 3:52:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class NullObjectRepository implements ObjectRepository {

    @Override
    public Object getObjectNamed(String string) {
        return this;
    }
}
