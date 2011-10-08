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
