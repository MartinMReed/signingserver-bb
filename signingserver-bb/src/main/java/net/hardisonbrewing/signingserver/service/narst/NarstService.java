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
package net.hardisonbrewing.signingserver.service.narst;

import net.hardisonbrewing.signingserver.service.Files;
import net.hardisonbrewing.signingserver.service.Properties;
import net.hardisonbrewing.signingserver.service.store.narst.CSKStore;
import net.hardisonbrewing.signingserver.service.store.narst.DBStore;
import net.rim.device.api.ui.component.Dialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NarstService {

    private static final Logger log = LoggerFactory.getLogger( NarstService.class );

    public static void loadCSKFile() {

        Properties properties = loadFile( "csk" );

        if ( properties == null ) {
            return;
        }

        CSKStore.put( properties );
    }

    public static void loadDBFile() {

        Properties properties = loadFile( "db" );

        if ( properties == null ) {
            return;
        }

        DBStore.put( properties );
    }

    public static Properties loadFile( String ext ) {

        try {
            return Files.requestPropertiesFile( ext );
        }
        catch (Exception e) {
            log.error( "Exception loading " + ext.toUpperCase() + " file" );
            Dialog.inform( Files.LOAD_FILE_FAIL );
            return null;
        }
    }
}
