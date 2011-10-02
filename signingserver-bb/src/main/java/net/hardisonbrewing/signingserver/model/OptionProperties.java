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
package net.hardisonbrewing.signingserver.model;

import java.util.Hashtable;

import net.rim.device.api.util.Persistable;

public class OptionProperties extends Hashtable implements Persistable {

    public static final String PUSH_ENABLED = "pushEnabled";
    public static final String BBM_ENABLED = "bbmEnabled";

    public boolean update() {

        boolean updated = false;
        updated |= addIfNotPresent( PUSH_ENABLED, Boolean.TRUE );
        updated |= addIfNotPresent( BBM_ENABLED, Boolean.TRUE );
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
