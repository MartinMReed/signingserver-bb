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
package net.hardisonbrewing.signingserver.service;

import java.io.InputStream;

import javax.microedition.io.Connector;

import net.hardisonbrewing.signingserver.narst.Properties;
import net.rim.device.api.ui.picker.FilePicker;

import org.metova.mobile.util.io.IOUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Files {

    private static final Logger log = LoggerFactory.getLogger( Files.class );

    public static Properties requestPropertiesFile( String ext ) throws Exception {

        FilePicker filePicker = FilePicker.getInstance();
        filePicker.setFilter( "." + ext.toLowerCase() );
        String filePath = filePicker.show();

        if ( filePath == null ) {
            return null;
        }

        InputStream inputStream = null;
        try {
            inputStream = Connector.openInputStream( filePath );
            Properties properties = new Properties();
            properties.load( inputStream );
            return properties;
        }
        finally {
            IOUtility.safeClose( inputStream );
        }
    }
}
