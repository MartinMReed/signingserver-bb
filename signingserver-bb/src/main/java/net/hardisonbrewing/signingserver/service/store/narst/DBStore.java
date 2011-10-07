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

        PersistentObject persistentObject = PersistentStore.getPersistentObject( UID );
        return (SigningAuthority[]) persistentObject.getContents();
    }
}
