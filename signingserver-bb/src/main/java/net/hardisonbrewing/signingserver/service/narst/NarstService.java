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

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.hardisonbrewing.signingserver.model.JAD;
import net.hardisonbrewing.signingserver.model.SigningAuthority;
import net.hardisonbrewing.signingserver.service.Files;
import net.hardisonbrewing.signingserver.service.Properties;
import net.hardisonbrewing.signingserver.service.store.narst.CSKStore;
import net.hardisonbrewing.signingserver.service.store.narst.DBStore;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.picker.FilePicker;

import org.metova.mobile.util.io.IOUtility;
import org.metova.mobile.util.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NarstService {

    private static final Logger log = LoggerFactory.getLogger( NarstService.class );

    public static JAD requestJADFile() throws Exception {

        JAD jad = new JAD();
        jad.filePath = Files.requestPropertiesFile( jad, "jad" );
        return jad;
    }

    public static JAD requestCodFilePath() throws Exception {

        FilePicker filePicker = FilePicker.getInstance();
        filePicker.setFilter( ".cod" );
        String filePath = filePicker.show();

        if ( filePath == null ) {
            return null;
        }

        int lastIndexOf = filePath.lastIndexOf( '/' );
        String codFilename = filePath.substring( lastIndexOf + 1 );
        String directory = filePath.substring( 0, lastIndexOf );

        String[] filePaths = getAllCods( directory );

        JAD jad = new JAD();
        jad.filePath = directory + "/" + getJadFilenameFromCod( codFilename );

        for (int i = 0; i < filePaths.length; i++) {
            String urlKey = JAD.COD_URL;
            if ( i > 0 ) {
                urlKey += "-" + i;
            }
            jad.setProperty( urlKey, filePaths[i] );
        }

        return jad;
    }

    private static String getJadFilenameFromCod( String filename ) {

        int lastIndexOf = filename.lastIndexOf( '-' );
        if ( lastIndexOf == -1 ) {
            lastIndexOf = filename.lastIndexOf( '.' );
        }
        return filename.substring( 0, lastIndexOf );
    }

    public static String[] getAllCods( String directory ) throws Exception {

        FileConnection fileConnection = null;

        try {

            fileConnection = (FileConnection) Connector.open( directory );

            if ( !fileConnection.exists() || !fileConnection.isDirectory() ) {
                return null;
            }

            Vector files = new Vector();

            Enumeration enumerator = fileConnection.list( "*.cod", false );
            while (enumerator.hasMoreElements()) {
                files.addElement( enumerator.nextElement() );
            }

            String[] _files = new String[files.size()];
            files.copyInto( _files );
            return _files;
        }
        finally {
            IOUtility.safeClose( fileConnection );
        }
    }

    public static SigningAuthority[] requestAuthorities() throws Exception {

        Properties properties = new Properties();
        String filePath = Files.requestPropertiesFile( properties, "db" );

        if ( filePath == null ) {
            return null;
        }

        Vector signingAuthorities = new Vector();

        Enumeration enumerator = properties.keys();
        while (enumerator.hasMoreElements()) {

            String key = (String) enumerator.nextElement();
            String value = properties.getProperty( key );

            int indexOf = value.indexOf( "http" );

            String url = value.substring( indexOf );
            url = Text.replaceAll( url, "\\:", ":" );

            SigningAuthority signingAuthority = new SigningAuthority();
            signingAuthority.key = key;
            signingAuthority.clientId = Long.parseLong( value.substring( 0, indexOf ) );
            signingAuthority.url = url;
            signingAuthorities.addElement( signingAuthority );
        }

        SigningAuthority[] _signingAuthorities = new SigningAuthority[signingAuthorities.size()];
        signingAuthorities.copyInto( _signingAuthorities );
        return _signingAuthorities;
    }

    public static void loadCSKFile() {

        Properties properties = loadFile( "csk" );

        if ( properties == null ) {
            return;
        }

        CSKStore.put( properties );
    }

    public static SigningAuthority[] loadDBFile() {

        SigningAuthority[] signingAuthorities;

        try {
            signingAuthorities = requestAuthorities();
        }
        catch (Exception e) {
            log.error( "Exception loading the signing authorities file" );
            Dialog.inform( Files.LOAD_FILE_FAIL );
            return null;
        }

        DBStore.put( signingAuthorities );

        return signingAuthorities;
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
