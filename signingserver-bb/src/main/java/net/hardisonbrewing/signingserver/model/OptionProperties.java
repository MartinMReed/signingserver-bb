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
package net.hardisonbrewing.signingserver.model;

import java.util.Hashtable;

import net.rim.device.api.util.Persistable;

public class OptionProperties extends Hashtable implements Persistable {

    public static final String PUSH_ENABLED = "pushEnabled";
    public static final String BBM_ENABLED = "bbmEnabled";
    public static final String DEVICE_KEY_STORE = "deviceKeyStore";

    public boolean update() {

        boolean updated = false;
        updated |= addIfNotPresent( PUSH_ENABLED, Boolean.TRUE );
        updated |= addIfNotPresent( BBM_ENABLED, Boolean.TRUE );
        updated |= addIfNotPresent( DEVICE_KEY_STORE, Boolean.TRUE );
        return updated;
    }

    public boolean addIfNotPresent( String key, Object value ) {

        if ( !containsKey( key ) ) {
            put( key, value );
            return true;
        }
        return false;
    }

    public boolean getBoolean( String key ) {

        return ( (Boolean) get( key ) ).booleanValue();
    }
}
