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
import net.hardisonbrewing.signingserver.service.Properties;
import net.hardisonbrewing.signingserver.service.narst.Signer;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

public class CSKStore {

    private static final long UID = HBCID.getUUID( CSKStore.class );

    public static void put( Properties properties ) {

        if ( properties == null ) {
            Signer.reset();
            PersistentStore.destroyPersistentObject( UID );
            return;
        }

        PersistentObject persistentObject = PersistentStore.getPersistentObject( UID );
        persistentObject.setContents( properties );
        persistentObject.commit();
    }

    public static Properties get() {

        try {
            PersistentObject persistentObject = PersistentStore.getPersistentObject( UID );
            return (Properties) persistentObject.getContents();
        }
        catch (Exception e) {
            PersistentStore.destroyPersistentObject( UID );
            return null;
        }
    }
}
