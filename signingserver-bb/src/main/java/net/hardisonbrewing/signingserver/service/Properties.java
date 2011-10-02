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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import net.rim.device.api.io.LineReader;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.StringComparator;

import org.metova.mobile.util.text.Text;

public class Properties extends Hashtable {

    public void setProperty( String key, String value ) {

        put( key, value );
    }

    public String getProperty( String key ) {

        return (String) get( key );
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
            int indexOf = line.indexOf( '=' );
            if ( indexOf == -1 ) {
                throw new IllegalStateException( "Malformed PROPERTIES file" );
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

        String[] keys = sortedKeys();
        for (int i = 0; i < keys.length; i++) {
            writer.write( escape( keys[i] ) );
            writer.write( '=' );
            writer.write( escape( getProperty( keys[i] ) ) );
            writer.write( '\n' );
        }

        writer.flush();
    }

    protected String[] sortedKeys() {

        String[] keys = new String[size()];

        Enumeration enumerator = keys();
        for (int i = 0; enumerator.hasMoreElements(); i++) {
            keys[i] = (String) enumerator.nextElement();
        }

        Arrays.sort( keys, StringComparator.getInstance( true ) );

        return keys;
    }

    private String escape( String text ) {

        text = Text.replaceAll( text, "=", "\\=" );
        text = Text.replaceAll( text, "#", "\\#" );
        return text;
    }

    private String unescape( String text ) {

        text = Text.replaceAll( text, "\\=", "=" );
        text = Text.replaceAll( text, "\\#", "#" );
        return text;
    }
}
