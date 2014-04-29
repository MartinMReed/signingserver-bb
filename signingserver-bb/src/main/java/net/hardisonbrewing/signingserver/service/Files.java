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
package net.hardisonbrewing.signingserver.service;

import java.io.InputStream;

import javax.microedition.io.Connector;

import net.rim.device.api.ui.picker.FilePicker;

import org.metova.mobile.util.io.IOUtility;

public class Files {

    public static final String LOAD_FILE_FAIL = "Whoooopsie! There was a problem loading the file. Fail =(";

    public static Properties requestPropertiesFile( String ext ) throws Exception {

        Properties properties = new Properties();
        requestPropertiesFile( properties, ext );
        return properties;
    }

    public static String requestPropertiesFile( Propertieseque properties, String ext ) throws Exception {

        FilePicker filePicker = FilePicker.getInstance();
        filePicker.setFilter( "." + ext.toLowerCase() );
        String filePath = filePicker.show();

        if ( filePath == null ) {
            return filePath;
        }

        InputStream inputStream = null;
        try {
            inputStream = Connector.openInputStream( filePath );
            properties.load( inputStream );
            return filePath;
        }
        finally {
            IOUtility.safeClose( inputStream );
        }
    }
}
