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
