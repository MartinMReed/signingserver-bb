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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Vector;

import net.hardisonbrewing.signingserver.service.Propertieseque;
import net.rim.device.api.io.LineReader;

import org.metova.mobile.util.text.Text;

public class JAD extends Vector implements Propertieseque {

    public static final String COD_URL = "RIM-COD-URL";
    public static final String COD_SIZE = "RIM-COD-Size";
    public static final String COD_SHA1 = "RIM-COD-SHA1";

    public String filePath;

    public int getCodCount() {

        int cods = 0;
        for (int i = 0; i < size(); i++) {
            Entry entry = (Entry) elementAt( i );
            if ( entry.key.startsWith( COD_URL ) ) {
                cods++;
            }
        }
        return cods;
    }

    public COD getCOD( int index ) {

        String id = index > 0 ? "-" + index : "";
        Entry url = getEntry( COD_URL + id );
        Entry size = getEntry( COD_SIZE + id );
        Entry sha1 = getEntry( COD_SHA1 + id );

        int filename = url.value.lastIndexOf( '/' );

        COD cod = new COD();
        cod.filename = filename == -1 ? url.value : url.value.substring( filename + 1 );
        cod.url = url.value;
        if ( size != null ) {
            cod.size = Long.parseLong( size.value );
        }
        if ( sha1 != null ) {
            cod.sha1 = sha1.value;
        }
        return cod;
    }

    public COD[] getCODs() {

        Vector cods = new Vector();

        for (int i = 0, c = 0; i < size(); i++) {
            Entry entry = (Entry) elementAt( i );
            if ( entry.key.startsWith( COD_URL ) ) {
                COD cod = getCOD( c );
                cods.addElement( cod );
                c++;
            }
        }

        COD[] _cods = new COD[cods.size()];
        cods.copyInto( _cods );
        return _cods;
    }

    public void setProperty( String key, String value ) {

        key = key.trim();
        value = value.trim();

        Entry entry = getEntry( key );
        if ( entry != null ) {
            entry.value = value;
            return;
        }

        entry = new Entry();
        entry.key = key;
        entry.value = value;
        addElement( entry );
    }

    public String getProperty( String key ) {

        Entry entry = getEntry( key );
        return entry == null ? null : entry.value;
    }

    private Entry getEntry( String key ) {

        for (int i = 0; i < size(); i++) {
            Entry entry = (Entry) elementAt( i );
            if ( key.equals( entry.key ) ) {
                return entry;
            }
        }
        return null;
    }

    public void load( InputStream inputStream ) throws IOException {

        LineReader lineReader = new LineReader( inputStream );
        byte[] read;
        while (( read = readLine( lineReader ) ) != null) {
            String line = new String( read, "UTF-8" );
            line = trimComment( line.trim() );
            if ( line == null ) {
                continue;
            }
            int indexOf = line.indexOf( ':' );
            if ( indexOf == -1 ) {
                throw new IllegalStateException( "Malformed JAD file" );
            }
            String key = line.substring( 0, indexOf );
            String value = ( indexOf < line.length() ) ? line.substring( indexOf + 1 ) : "";
            setProperty( key, unescape( value ) );
        }
    }

    private byte[] readLine( LineReader lineReader ) throws IOException {

        try {
            return lineReader.readLine();
        }
        catch (EOFException e) {
            return null;
        }
    }

    private String trimComment( String text ) {

        int indexOf = text.indexOf( '#' );
        if ( indexOf == 0 ) {
            return null;
        }
        if ( indexOf == -1 ) {
            return text;
        }
        while (indexOf != -1 && indexOf - 1 == '\\') {
            indexOf = text.indexOf( '#', indexOf + 1 );
        }
        return text.substring( 0, indexOf );
    }

    public void store( OutputStream outputStream ) throws IOException {

        store( outputStream, null );
    }

    public void store( OutputStream outputStream, String comment ) throws IOException {

        OutputStreamWriter writer = new OutputStreamWriter( outputStream, "UTF-8" );

        if ( comment != null ) {
            writer.write( '#' );
            writer.write( comment );
            writer.write( '\n' );
        }

        String[] keys = keys();
        for (int i = 0; i < keys.length; i++) {
            writer.write( escape( keys[i] ) );
            writer.write( ':' );
            writer.write( escape( getProperty( keys[i] ) ) );
            writer.write( '\n' );
        }

        writer.flush();
    }

    protected String[] keys() {

        String[] keys = new String[size()];

        Enumeration enumerator = elements();
        for (int i = 0; enumerator.hasMoreElements(); i++) {
            Entry entry = (Entry) enumerator.nextElement();
            keys[i] = entry.key;
        }

        return keys;
    }

    private String escape( String text ) {

        text = Text.replaceAll( text, ":", "\\:" );
        text = Text.replaceAll( text, "#", "\\#" );
        return text;
    }

    private String unescape( String text ) {

        text = Text.replaceAll( text, "\\:", ":" );
        text = Text.replaceAll( text, "\\#", "#" );
        return text;
    }

    public static final class Entry {

        public String key;
        public String value;
    }

    public static final class COD {

        public String filename;
        public String url;
        public long size;
        public String sha1;
    }
}
