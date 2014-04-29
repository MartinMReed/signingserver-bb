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

import net.hardisonbrewing.signingserver.closed.HBCID;
import net.hardisonbrewing.signingserver.model.SigningAuthority;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

public class DBStore {

    private static final long UID = HBCID.getUUID( DBStore.class );

    public static void put( SigningAuthority[] signingAuthorities ) {

        if ( signingAuthorities == null ) {
            PersistentStore.destroyPersistentObject( UID );
            return;
        }

        PersistentObject persistentObject = PersistentStore.getPersistentObject( UID );
        persistentObject.setContents( signingAuthorities );
        persistentObject.commit();
    }

    public static SigningAuthority[] get() {

        try {
            PersistentObject persistentObject = PersistentStore.getPersistentObject( UID );
            return (SigningAuthority[]) persistentObject.getContents();
        }
        catch (Exception e) {
            PersistentStore.destroyPersistentObject( UID );
            return null;
        }
    }
}
