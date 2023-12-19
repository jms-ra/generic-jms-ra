/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms.util;

public class Strings {

    public static boolean compare(final String me, final String you) {
        // If both null or intern equals
        if (me == you) {
            return true;
        }
        // if me null and you are not
        if (me == null && you != null) {
            return false;
        }
        // me will not be null, test for equality
        return me.equals(you);
    }

    public static boolean compare(final char[] me, final char[] you) {
        // If both null or intern equals
        if (me == you) {
            return true;
        }
        // if me null and you are not
        if ((me == null && you != null) || (me != null && you ==  null)) {
            return false;
        }
        // me will not be null, test for equality
        return new String(me).equals(new String(you));
    }

    public static char[] toCharArray(final String string) {
        if (string == null) {
            return null;
        }
        return string.toCharArray();
    }

    public static String fromCharArray(final char[] array) {
        if (array == null) {
            return null;
        }
        return new String(array);
    }
}
