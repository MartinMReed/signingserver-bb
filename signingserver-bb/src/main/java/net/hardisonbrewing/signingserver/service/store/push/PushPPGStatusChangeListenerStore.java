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
package net.hardisonbrewing.signingserver.service.store.push;

import java.lang.ref.WeakReference;
import java.util.Vector;

import net.hardisonbrewing.signingserver.closed.HBCID;
import net.rim.device.api.system.RuntimeStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushPPGStatusChangeListenerStore {

    private static final Logger log = LoggerFactory.getLogger( PushPPGStatusChangeListenerStore.class );

    private static final long UID = HBCID.getUUID( PushPPGStatusChangeListenerStore.class );

    public static void put( PushPPGStatusChangeListener listener ) {

        Vector vector = (Vector) RuntimeStore.getRuntimeStore().get( UID );

        if ( vector == null ) {
            vector = new Vector();
            RuntimeStore.getRuntimeStore().put( UID, vector );
        }

        vector.addElement( new WeakReference( listener ) );
    }

    public static PushPPGStatusChangeListener[] get() {

        Vector vector = (Vector) RuntimeStore.getRuntimeStore().get( UID );

        if ( vector == null ) {
            return null;
        }

        Vector result = new Vector();

        for (int i = vector.size() - 1; i >= 0; i--) {
            WeakReference weakReference = (WeakReference) vector.elementAt( i );
            PushPPGStatusChangeListener listener = (PushPPGStatusChangeListener) weakReference.get();
            if ( listener == null ) {
                vector.removeElementAt( i );
                continue;
            }
            result.addElement( listener );
        }

        PushPPGStatusChangeListener[] _result = new PushPPGStatusChangeListener[result.size()];
        result.copyInto( _result );
        return _result;
    }

    public static void notifyListeners( byte status ) {

        PushPPGStatusChangeListener[] pushStatusChangeListeners = get();
        if ( pushStatusChangeListeners != null ) {
            log.info( "Notifying PUSH status change listeners" );
            for (int i = 0; i < pushStatusChangeListeners.length; i++) {
                pushStatusChangeListeners[i].onStatusChange( status );
            }
        }
    }
}
