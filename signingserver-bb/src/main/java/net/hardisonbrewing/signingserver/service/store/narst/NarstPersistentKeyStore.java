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
package net.hardisonbrewing.signingserver.service.store.narst;

import java.util.Hashtable;

import net.hardisonbrewing.signingserver.closed.HBCID;
import net.hardisonbrewing.signingserver.model.NarstKeyHashtable;
import net.rim.device.api.crypto.PrivateKey;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

public class NarstPersistentKeyStore implements NarstKeyStore {

    private static final long UID = HBCID.getUUID( NarstPersistentKeyStore.class );

    public void put( String clientId, PrivateKey privateKey ) {

        Hashtable hashtable = load();
        hashtable.put( clientId, privateKey );

        PersistentObject persistentObject = PersistentStore.getPersistentObject( UID );
        persistentObject.commit();
    }

    public PrivateKey get( String clientId ) {

        Hashtable hashtable = load();
        return (PrivateKey) hashtable.get( clientId );
    }

    private Hashtable load() {

        PersistentObject persistentObject = PersistentStore.getPersistentObject( UID );
        Hashtable hashtable = (Hashtable) persistentObject.getContents();

        if ( hashtable == null ) {
            hashtable = new NarstKeyHashtable();
            persistentObject.setContents( hashtable );
            persistentObject.commit();
        }

        return hashtable;
    }
}