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
        Hashtable hashtable = null;

        try {
            hashtable = (Hashtable) persistentObject.getContents();
        }
        catch (Exception e) {
            PersistentStore.destroyPersistentObject( UID );
        }

        if ( hashtable == null ) {
            hashtable = new NarstKeyHashtable();
            persistentObject.setContents( hashtable );
            persistentObject.commit();
        }

        return hashtable;
    }
}
