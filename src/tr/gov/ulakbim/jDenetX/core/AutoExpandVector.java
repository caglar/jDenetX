/*
 *    AutoExpandVector.java
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
package tr.gov.ulakbim.jDenetX.core;

import tr.gov.ulakbim.jDenetX.AbstractMOAObject;
import tr.gov.ulakbim.jDenetX.MOAObject;

import java.util.ArrayList;
import java.util.Collection;

public class AutoExpandVector<T> extends ArrayList<T> implements MOAObject {

    private static final long serialVersionUID = 1L;

    public AutoExpandVector() {
        super(0);
    }

    @Override
    public void add(int pos, T obj) {
        if (pos > size()) {
            while (pos > size()) {
                add(null);
            }
            trimToSize();
        }
        super.add(pos, obj);
    }

    @Override
    public T get(int pos) {
        return ((pos >= 0) && (pos < size())) ? super.get(pos) : null;
    }

    @Override
    public T set(int pos, T obj) {
        if (pos >= size()) {
            add(pos, obj);
            return null;
        }
        return super.set(pos, obj);
    }

    @Override
    public boolean add(T arg0) {
        boolean result = super.add(arg0);
        trimToSize();
        return result;
    }

    @Override
    public boolean addAll(Collection<? extends T> arg0) {
        boolean result = super.addAll(arg0);
        trimToSize();
        return result;
    }

    @Override
    public boolean addAll(int arg0, Collection<? extends T> arg1) {
        boolean result = super.addAll(arg0, arg1);
        trimToSize();
        return result;
    }

    @Override
    public void clear() {
        super.clear();
        trimToSize();
    }

    @Override
    public T remove(int arg0) {
        T result = super.remove(arg0);
        trimToSize();
        return result;
    }

    @Override
    public boolean remove(Object arg0) {
        boolean result = super.remove(arg0);
        trimToSize();
        return result;
    }

    @Override
    protected void removeRange(int arg0, int arg1) {
        super.removeRange(arg0, arg1);
        trimToSize();
    }

    public MOAObject copy() {
        return AbstractMOAObject.copy(this);
    }

    public int measureByteSize() {
        return AbstractMOAObject.measureByteSize(this);
    }

    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub

    }

}
