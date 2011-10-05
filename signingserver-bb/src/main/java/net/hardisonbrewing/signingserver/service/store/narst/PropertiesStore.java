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

import net.hardisonbrewing.signingserver.narst.Properties;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

public class PropertiesStore {

    public static void put( long uid, Properties properties ) {

        if ( properties == null ) {
            PersistentStore.destroyPersistentObject( uid );
            return;
        }

        PersistentObject persistentObject = PersistentStore.getPersistentObject( uid );
        persistentObject.setContents( properties );
        persistentObject.commit();
    }

    public static Properties get( long uid ) {

        PersistentObject persistentObject = PersistentStore.getPersistentObject( uid );
        return (Properties) persistentObject.getContents();
    }
}
