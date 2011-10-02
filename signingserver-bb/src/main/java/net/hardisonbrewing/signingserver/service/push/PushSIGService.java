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
package net.hardisonbrewing.signingserver.service.push;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.HttpConnection;

import net.hardisonbrewing.signingserver.closed.Settings.BISPushInfo;
import net.hardisonbrewing.signingserver.model.PushNotification;
import net.hardisonbrewing.signingserver.service.SigStatusService;
import net.hardisonbrewing.signingserver.service.icon.IconService;
import net.hardisonbrewing.signingserver.service.store.push.PushSIGStatusStore;
import net.rim.blackberry.api.push.PushApplicationStatus;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.system.DeviceInfo;

import org.json.javame.JSONException;
import org.json.javame.JSONObject;
import org.metova.mobile.util.io.IOUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushSIGService {

    private static final Logger log = LoggerFactory.getLogger( PushSIGService.class );

    public static void register( boolean active ) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append( BISPushInfo.REGISTER_URL );
        stringBuffer.append( "?pin=" );
        stringBuffer.append( Integer.toHexString( DeviceInfo.getDeviceId() ) );
        stringBuffer.append( "&active=" );
        stringBuffer.append( active );

        log.info( stringBuffer.toString() );

        ConnectionFactory connectionFactory = new ConnectionFactory();
        ConnectionDescriptor connectionDescriptor = connectionFactory.getConnection( stringBuffer.toString() );

        if ( connectionDescriptor == null ) {
            log.error( "Unable to create connection. Push registration not completed." );
            return;
        }

        HttpConnection connection = null;

        try {
            connection = (HttpConnection) connectionDescriptor.getConnection();
            int responseCode = connection.getResponseCode();
            if ( responseCode >= 200 && responseCode < 300 ) {
                log.error( "Push registration completed." );
                PushSIGStatusStore.put( active );
                IconService.updateIcon();
            }
            else {
                log.error( "Made connection with server but error[" + responseCode + "] was returned. Push registration not completed." );
            }
        }
        catch (IOException e) {
            log.error( "Exception caught while registering with server. Push registration not completed." );
        }
        finally {
            IOUtility.safeClose( connection );
        }
    }

    public static void confirm( String pid ) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append( BISPushInfo.CONFIRM_URL );
        stringBuffer.append( "?pin=" );
        stringBuffer.append( Integer.toHexString( DeviceInfo.getDeviceId() ) );
        stringBuffer.append( "&pid=" );
        stringBuffer.append( pid );

        log.info( stringBuffer.toString() );

        ConnectionFactory connectionFactory = new ConnectionFactory();
        ConnectionDescriptor connectionDescriptor = connectionFactory.getConnection( stringBuffer.toString() );

        if ( connectionDescriptor == null ) {
            log.error( "Unable to create connection. Push confirmation not completed." );
            return;
        }

        HttpConnection connection = null;

        try {
            connection = (HttpConnection) connectionDescriptor.getConnection();
            int responseCode = connection.getResponseCode();
            if ( responseCode >= 200 && responseCode < 300 ) {
                log.error( "Push confirmation completed." );
            }
            else {
                log.error( "Made connection with server but error[" + responseCode + "] was returned. Push confirmation not completed." );
            }
        }
        catch (IOException e) {
            log.error( "Exception caught while confirming with server. Push confirmation not completed." );
        }
        finally {
            IOUtility.safeClose( connection );
        }
    }

    public static PushNotification parse( InputStream inputStream ) throws IOException, JSONException {

        String response = IOUtility.getInputStreamAsString( inputStream );

        log.info( response );

        JSONObject notification = new JSONObject( response );
        int version = notification.getInt( "version" );
        if ( version != BISPushInfo.VERSION ) {
            log.error( "Version does not match" );
            return null;
        }

        JSONObject message = notification.getJSONObject( "message" );

        PushNotification pushNotification = new PushNotification();
        pushNotification.pid = notification.getString( "pid" );
        pushNotification.date = notification.getLong( "date" );
        pushNotification.message = SigStatusService.parse( message );
        return pushNotification;
    }

    public static String getStatusText( byte status ) {

        switch (status) {
            case PushApplicationStatus.STATUS_ACTIVE: {
                return "STATUS_ACTIVE";
            }
            case PushApplicationStatus.STATUS_FAILED: {
                return "STATUS_FAILED";
            }
            case PushApplicationStatus.STATUS_NOT_REGISTERED: {
                return "STATUS_NOT_REGISTERED";
            }
            case PushApplicationStatus.STATUS_PENDING: {
                return "STATUS_PENDING";
            }
            default: {
                return null;
            }
        }
    }

    public static String getStatusHumanText( byte status ) {

        switch (status) {
            case PushApplicationStatus.STATUS_ACTIVE: {
                return "Active";
            }
            case PushApplicationStatus.STATUS_FAILED: {
                return "Failed";
            }
            case PushApplicationStatus.STATUS_PENDING: {
                return "Pending";
            }
            case PushApplicationStatus.STATUS_NOT_REGISTERED:
            default: {
                return "Not Registered";
            }
        }
    }

    public static String getReasonText( byte reason ) {

        switch (reason) {
            case PushApplicationStatus.REASON_API_CALL: {
                return "REASON_API_CALL";
            }
            case PushApplicationStatus.REASON_INVALID_PARAMETERS: {
                return "REASON_INVALID_PARAMETERS";
            }
            case PushApplicationStatus.REASON_NETWORK_ERROR: {
                return "REASON_NETWORK_ERROR";
            }
            case PushApplicationStatus.REASON_REJECTED_BY_SERVER: {
                return "REASON_REJECTED_BY_SERVER";
            }
            case PushApplicationStatus.REASON_SIM_CHANGE: {
                return "REASON_SIM_CHANGE";
            }
            default: {
                return null;
            }
        }
    }
}
