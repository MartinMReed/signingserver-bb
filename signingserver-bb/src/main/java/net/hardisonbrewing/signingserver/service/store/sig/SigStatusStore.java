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

import java.util.Enumeration;
import java.util.Hashtable;

import net.hardisonbrewing.signingserver.closed.HBCID;
import net.hardisonbrewing.signingserver.model.SigStatus;
import net.rim.device.api.system.RuntimeStore;

public class SigStatusStore {

    private static final long UID = HBCID.getUUID( SigStatusStore.class );

    public static void put( Hashtable status ) {

        RuntimeStore.getRuntimeStore().remove( UID );

        if ( status == null ) {
            return;
        }

        Hashtable hashtable = get();
        if ( hashtable == null ) {
            RuntimeStore.getRuntimeStore().put( UID, status );
            return;
        }

        Enumeration enumerator = status.keys();
        while (enumerator.hasMoreElements()) {
            String key = (String) enumerator.nextElement();
            hashtable.put( key, hashtable.get( key ) );
        }
    }

    public static Hashtable get() {

        return (Hashtable) RuntimeStore.getRuntimeStore().get( UID );
    }

    public static boolean isSuccess() {

        Hashtable hashtable = get();
        return isSuccess( hashtable );
    }

    public static boolean isSuccess( Hashtable hashtable ) {

        if ( hashtable == null ) {
            return false;
        }

        Enumeration enumerator = hashtable.elements();
        while (enumerator.hasMoreElements()) {
            SigStatus status = (SigStatus) enumerator.nextElement();
            if ( !status.success ) {
                return false;
            }
        }

        return true;
    }
}
