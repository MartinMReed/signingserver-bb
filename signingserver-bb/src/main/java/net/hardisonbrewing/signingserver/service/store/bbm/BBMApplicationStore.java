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
package net.hardisonbrewing.signingserver.service.store.bbm;

import net.hardisonbrewing.signingserver.SigservBBMApplication;
import net.hardisonbrewing.signingserver.closed.HBCID;
import net.rim.device.api.system.RuntimeStore;

public class BBMApplicationStore {

    private static final long UID = HBCID.getUUID( BBMApplicationStore.class );

    public static SigservBBMApplication get() {

        SigservBBMApplication application = (SigservBBMApplication) RuntimeStore.getRuntimeStore().get( UID );

        if ( application == null ) {
            application = new SigservBBMApplication();
            RuntimeStore.getRuntimeStore().put( UID, application );
        }

        return application;
    }
}
