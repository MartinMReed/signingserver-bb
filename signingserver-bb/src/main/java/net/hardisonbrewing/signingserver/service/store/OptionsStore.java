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
package net.hardisonbrewing.signingserver.service.store;

import net.hardisonbrewing.signingserver.closed.HBCID;
import net.hardisonbrewing.signingserver.model.OptionProperties;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

public class OptionsStore {

    private static final long UID = HBCID.getUUID( OptionsStore.class );

    public static void commit() {

        PersistentObject persistentObject = PersistentStore.getPersistentObject( UID );
        persistentObject.commit();
    }

    public static OptionProperties get() {

        PersistentObject persistentObject = PersistentStore.getPersistentObject( UID );
        OptionProperties optionProperties = null;

        try {
            optionProperties = (OptionProperties) persistentObject.getContents();
        }
        catch (Exception e) {
            PersistentStore.destroyPersistentObject( UID );
        }

        boolean created = false;

        if ( optionProperties == null ) {
            optionProperties = new OptionProperties();
            persistentObject.setContents( optionProperties );
            created = true;
        }

        boolean updated = optionProperties.update();

        if ( created || updated ) {
            persistentObject.commit();
        }

        return optionProperties;
    }

    public static boolean getBoolean( String key ) {

        return get().getBoolean( key );
    }
}
