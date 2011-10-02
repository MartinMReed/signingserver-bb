/**
 * Copyright (c) 2011 Martin M Reed
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.hardisonbrewing.signingserver.service.store.sig;

import java.lang.ref.WeakReference;
import java.util.Vector;

import net.hardisonbrewing.signingserver.closed.HBCID;
import net.rim.device.api.system.RuntimeStore;

public class SigStatusChangeListenerStore {

    private static final long UID = HBCID.getUUID( SigStatusChangeListenerStore.class );

    public static void put( SigStatusChangeListener listener ) {

        Vector vector = (Vector) RuntimeStore.getRuntimeStore().get( UID );

        if ( vector == null ) {
            vector = new Vector();
            RuntimeStore.getRuntimeStore().put( UID, vector );
        }

        vector.addElement( new WeakReference( listener ) );
    }

    public static SigStatusChangeListener[] get() {

        Vector vector = (Vector) RuntimeStore.getRuntimeStore().get( UID );

        if ( vector == null ) {
            return null;
        }

        Vector result = new Vector();

        for (int i = vector.size() - 1; i >= 0; i--) {
            WeakReference weakReference = (WeakReference) vector.elementAt( i );
            SigStatusChangeListener listener = (SigStatusChangeListener) weakReference.get();
            if ( listener == null ) {
                vector.removeElementAt( i );
                continue;
            }
            result.addElement( listener );
        }

        SigStatusChangeListener[] _result = new SigStatusChangeListener[result.size()];
        result.copyInto( _result );
        return _result;
    }
}
