/**
 * Copyright (c) 2011 Martin M Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
