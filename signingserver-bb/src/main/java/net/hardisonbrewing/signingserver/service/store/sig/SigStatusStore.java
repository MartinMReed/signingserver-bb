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
