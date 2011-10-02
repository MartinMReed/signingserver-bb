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
package net.hardisonbrewing.signingserver.service.store.push;

import net.hardisonbrewing.signingserver.closed.HBCID;
import net.rim.blackberry.api.push.PushApplicationStatus;
import net.rim.device.api.system.RuntimeStore;

public class PushPPGStatusStore {

    private static final long UID = HBCID.getUUID( PushPPGStatusStore.class );

    public static void put( byte status ) {

        RuntimeStore.getRuntimeStore().remove( UID );
        RuntimeStore.getRuntimeStore().put( UID, new Byte( status ) );
    }

    public static byte get() {

        Byte _byte = (Byte) RuntimeStore.getRuntimeStore().get( UID );
        if ( _byte == null ) {
            return PushApplicationStatus.STATUS_NOT_REGISTERED;
        }
        return _byte.byteValue();
    }
}
