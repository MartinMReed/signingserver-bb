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

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;

import net.hardisonbrewing.signingserver.SigservApplication;
import net.hardisonbrewing.signingserver.closed.Settings.SigStatusInfo;
import net.hardisonbrewing.signingserver.model.SigStatus;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;

import org.json.javame.JSONArray;
import org.json.javame.JSONException;
import org.json.javame.JSONObject;
import org.metova.mobile.util.io.IOUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SigStatusService {

    private static final Logger log = LoggerFactory.getLogger( SigStatusService.class );

    public static Hashtable downloadStatus() throws Exception {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append( SigStatusInfo.URL );
        stringBuffer.append( "?v=" );
        stringBuffer.append( SigStatusInfo.VERSION );

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setAttemptsLimit( 10 );
        ConnectionDescriptor connectionDescriptor = connectionFactory.getConnection( stringBuffer.toString() );

        if ( connectionDescriptor == null ) {
            SigservApplication.logEvent( "Unable to obtain ConnectionDescriptor to download status" );
            throw new IOException( "Unable to obtain ConnectionDescriptor to download status" );
        }

        HttpConnection connection = null;
        InputStream inputStream = null;

        try {
            connection = (HttpConnection) connectionDescriptor.getConnection();
            inputStream = connection.openInputStream();
            return parse( inputStream );
        }
        finally {
            IOUtility.safeClose( inputStream );
            IOUtility.safeClose( connection );
        }
    }

    public static Hashtable parse( InputStream inputStream ) throws IOException, JSONException {

        String response = IOUtility.getInputStreamAsString( inputStream );
        JSONObject status = new JSONObject( response );
        return parse( status );
    }

    public static Hashtable parse( JSONObject status ) throws JSONException {

        int version = status.getInt( "version" );
        if ( version != SigStatusInfo.VERSION ) {
            log.error( "Version does not match" );
            return null;
        }

        JSONArray sigs = status.getJSONArray( "sigs" );
        int length = sigs.length();

        if ( length == 0 ) {
            return null;
        }

        return parse( sigs );
    }

    public static Hashtable parse( JSONArray sigs ) throws JSONException {

        int length = sigs.length();

        if ( length == 0 ) {
            return null;
        }

        Hashtable result = new Hashtable();

        for (int i = 0; i < length; i++) {

            JSONObject sig = sigs.getJSONObject( i );

            SigStatus _status = new SigStatus();
            _status.sig = sig.getString( "sig" );
            _status.success = sig.getBoolean( "success" );
            _status.repeat = sig.getInt( "repeat" );
            _status.speed = sig.getDouble( "speed" );
            _status.aspeed = sig.getDouble( "aspeed" );
            _status.date = sig.getLong( "date" );
            result.put( _status.sig, _status );

            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append( "Received sig[" );
            stringBuffer.append( _status.sig );
            stringBuffer.append( "], success[" );
            stringBuffer.append( _status.success );
            stringBuffer.append( "], repeat[" );
            stringBuffer.append( _status.repeat );
            stringBuffer.append( "], speed[" );
            stringBuffer.append( _status.speed );
            stringBuffer.append( "], aspeed[" );
            stringBuffer.append( _status.aspeed );
            stringBuffer.append( "], date[" );
            stringBuffer.append( _status.date );
            stringBuffer.append( "]" );
            SigservApplication.logEvent( stringBuffer.toString() );
        }

        return result;
    }
}
