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
import net.hardisonbrewing.signingserver.model.OptionProperties;
import net.hardisonbrewing.signingserver.service.narst.Signer;
import net.hardisonbrewing.signingserver.service.store.OptionsStore;
import net.rim.device.api.system.RuntimeStore;

public class NarstKeyStoreManager {

    private static final long UID = HBCID.getUUID( NarstKeyStoreManager.class );

    public static void reset() {

        Signer.reset();
        RuntimeStore.getRuntimeStore().remove( UID );
    }

    public static NarstKeyStore getStore() {

        NarstKeyStore keyStore = (NarstKeyStore) RuntimeStore.getRuntimeStore().get( UID );
        if ( keyStore == null ) {
            if ( OptionsStore.getBoolean( OptionProperties.DEVICE_KEY_STORE ) ) {
                keyStore = new NarstDeviceKeyStore();
            }
            else {
                keyStore = new NarstPersistentKeyStore();
            }
            RuntimeStore.getRuntimeStore().put( UID, keyStore );
        }
        return keyStore;
    }
}
