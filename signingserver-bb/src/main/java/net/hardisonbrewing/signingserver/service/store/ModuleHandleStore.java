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
package net.hardisonbrewing.signingserver.service.store;

import net.hardisonbrewing.signingserver.closed.HBCID;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.RuntimeStore;

/**
 * Used by the OptionsProvider to un/register PUSH notifications
 */
public class ModuleHandleStore {

    private static final long UID = HBCID.getUUID( ModuleHandleStore.class );

    public static void put() {

        int moduleHandle = ApplicationDescriptor.currentApplicationDescriptor().getModuleHandle();

        RuntimeStore.getRuntimeStore().remove( UID );
        RuntimeStore.getRuntimeStore().put( UID, new Integer( moduleHandle ) );
    }

    public static int get() {

        Integer integer = (Integer) RuntimeStore.getRuntimeStore().get( UID );
        return integer.intValue();
    }
}
