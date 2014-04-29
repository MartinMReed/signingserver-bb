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
package net.hardisonbrewing.signingserver;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.io.StreamConnection;

import net.hardisonbrewing.signingserver.model.OptionProperties;
import net.hardisonbrewing.signingserver.model.PushNotification;
import net.hardisonbrewing.signingserver.service.icon.IconService;
import net.hardisonbrewing.signingserver.service.network.AbstractRadioStatusListener;
import net.hardisonbrewing.signingserver.service.push.PushPPGService;
import net.hardisonbrewing.signingserver.service.push.PushSIGService;
import net.hardisonbrewing.signingserver.service.store.OptionsStore;
import net.hardisonbrewing.signingserver.service.store.push.PushPPGStatusChangeListener;
import net.hardisonbrewing.signingserver.service.store.push.PushPPGStatusChangeListenerStore;
import net.rim.blackberry.api.push.PushApplication;
import net.rim.blackberry.api.push.PushApplicationDescriptor;
import net.rim.blackberry.api.push.PushApplicationRegistry;
import net.rim.blackberry.api.push.PushApplicationStatus;
import net.rim.device.api.io.http.PushInputStream;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;

import org.apache.commons.threadpool.DefaultThreadPool;
import org.apache.commons.threadpool.ThreadPool;
import org.metova.mobile.util.io.IOUtility;
import org.metova.mobile.util.time.Dates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SigservPushApplication extends Application implements PushApplication, PushPPGStatusChangeListener {

    private static final Logger log = LoggerFactory.getLogger( SigservPushApplication.class );

    private final ThreadPool threadPool = new DefaultThreadPool( 1 );

    private AbstractRadioStatusListener radioStatusListener;
    private TimerTask networkRequiredTimerTask;
    private TimerTask registerServ;

    public SigservPushApplication() {

        PushPPGStatusChangeListenerStore.put( this );

        radioStatusListener = new MyRadioStatusListener();
        addRadioListener( radioStatusListener );
        WLANInfo.addListener( radioStatusListener );
    }

    public static boolean isSupported() {

        return !DeviceInfo.isSimulator();
    }

    public static void mainAutoRegister( String[] args ) {

        if ( !isSupported() ) {
            log.info( "PUSH not supported. Skipping registration." );
            return;
        }

        log.info( "Running auto register PUSH application" );

        if ( !OptionsStore.getBoolean( OptionProperties.PUSH_ENABLED ) ) {
            log.info( "Push notifications disabled. Skipping registration." );
            return;
        }

        mainRegister( args );
    }

    public static void mainRegister( String[] args ) {

        if ( !isSupported() ) {
            log.info( "PUSH not supported. Skipping registration." );
            return;
        }

        log.info( "Registering PUSH application" );

        PushApplicationDescriptor pushApplicationDescriptor = PushPPGService.getPushApplicationDescriptor();
        PushApplicationStatus status = PushApplicationRegistry.getStatus( pushApplicationDescriptor );
        SigservApplication.logEvent( "Status before registration: " + PushSIGService.getStatusText( status.getStatus() ) );

        switch (status.getStatus()) {
            case PushApplicationStatus.STATUS_ACTIVE: {
                SigservApplication.logEvent( "Push status active. Skipping registration." );
                PushPPGService.updateStoredStatus( status.getStatus() );
                break;
            }
            case PushApplicationStatus.STATUS_PENDING: {
                SigservApplication.logEvent( "Push status pending. Skipping registration." );
                PushPPGService.updateStoredStatus( status.getStatus() );
                break;
            }
            case PushApplicationStatus.STATUS_FAILED:
            case PushApplicationStatus.STATUS_NOT_REGISTERED:
            default: {
                SigservApplication.logEvent( "Registering push application" );
                PushApplicationRegistry.registerApplication( pushApplicationDescriptor );
                status = PushApplicationRegistry.getStatus( pushApplicationDescriptor );
                SigservApplication.logEvent( "Status after registration: " + PushSIGService.getStatusText( status.getStatus() ) );
                break;
            }
        }
    }

    public static void mainUnregister( String[] args ) {

        log.info( "Unregistering PUSH application" );

        PushPPGService.unregister();
    }

    public static void mainStartup( String[] args ) {

        log.info( "Running PUSH application" );

        try {
            SigservPushApplication application = new SigservPushApplication();
            application.enterEventDispatcher();
        }
        catch (Exception e) {
            mainAutoRegister( new String[] { SigservApplication.PUSH_REGISTER } );
        }
    }

    public void onMessage( final PushInputStream inputStream, final StreamConnection conn ) {

        log.info( "Recieved new PUSH message" );

        threadPool.invokeLater( new Runnable() {

            public void run() {

                handleMessage( inputStream, conn );
            }
        } );
    }

    public static void handleMessage( PushInputStream inputStream, StreamConnection conn ) {

        log.info( "Reading new PUSH message" );

        PushNotification pushNotification = null;

        try {
            pushNotification = PushSIGService.parse( inputStream );
        }
        catch (Throwable t) {
            log.error( "Exception while parsing new push message", t );
        }
        finally {

            try {
                inputStream.accept();
            }
            catch (Throwable t) {
                // do nothing
            }

            IOUtility.safeClose( inputStream );
            IOUtility.safeClose( conn );
        }

        Hashtable latest = pushNotification == null ? null : (Hashtable) pushNotification.message;
        SigservApplication.updateStoredStatus( latest );
        IconService.updateIcon();

        PushSIGService.confirm( pushNotification == null ? null : pushNotification.pid );
    }

    public void onStatusChange( PushApplicationStatus status ) {

        String errorText = status.getError();
        String statusText = PushSIGService.getStatusText( status.getStatus() );
        String reasonText = PushSIGService.getReasonText( status.getReason() );
        SigservApplication.logEvent( "PushApplicationStatus status[" + statusText + "],  error[" + errorText + "],  reason[" + reasonText + "]" );

        PushPPGService.updateStoredStatus( status.getStatus() );
    }

    public void onStatusChange( byte status ) {

        switch (status) {
            case PushApplicationStatus.STATUS_ACTIVE: {
                startRegisterServTask();
                break;
            }
            case PushApplicationStatus.STATUS_FAILED:
            case PushApplicationStatus.STATUS_NOT_REGISTERED: {
                cancelRegisterServTask();
                PushSIGService.register( false );
                break;
            }
            case PushApplicationStatus.STATUS_PENDING:
            default: {
                break;
            }
        }
    }

    private void cancelNetworkRequiredTask() {

        if ( networkRequiredTimerTask != null ) {
            networkRequiredTimerTask.cancel();
            networkRequiredTimerTask = null;
        }
    }

    private void startNetworkRequiredTask() {

        cancelNetworkRequiredTask();

        Timer timer = new Timer();
        networkRequiredTimerTask = new NetworkRequiredTimerTask();
        timer.schedule( networkRequiredTimerTask, 0, Dates.SECOND * 10 );
    }

    private void cancelRegisterServTask() {

        if ( registerServ != null ) {
            registerServ.cancel();
            registerServ = null;
        }
    }

    private void startRegisterServTask() {

        cancelRegisterServTask();

        Timer timer = new Timer();
        registerServ = new RegisterServTimerTask();
        timer.schedule( registerServ, 0, Dates.DAY );
    }

    public boolean requestClose() {

        log.info( "Closing PUSH application" );

        if ( radioStatusListener != null ) {
            WLANInfo.removeListener( radioStatusListener );
            removeRadioListener( radioStatusListener );
            radioStatusListener = null;
        }

        cancelNetworkRequiredTask();
        cancelRegisterServTask();

        System.exit( 0 );

        return true;
    }

    private final class RegisterServTimerTask extends TimerTask {

        public void run() {

            PushSIGService.register( true );
        }
    }

    private final class NetworkRequiredTimerTask extends TimerTask {

        public void run() {

            log.info( "Checking PUSH registration from task" );

            if ( !PushPPGService.shouldRegister() ) {
                synchronized (radioStatusListener) {
                    log.info( "PUSH registration no longer required, canceling task" );
                    cancelNetworkRequiredTask();
                    return;
                }
            }

            log.info( "Checking coverage for PUSH registration" );

            if ( SigservApplication.hasSufficientCoverage() ) {
                log.info( "Sufficient coverage, running auto PUSH registration" );
                mainAutoRegister( new String[] { SigservApplication.PUSH_REGISTER } );
            }
            else {
                log.info( "Coverage not sufficient, skipping auto PUSH registration" );
            }
        }
    }

    private final class MyRadioStatusListener extends AbstractRadioStatusListener {

        private void checkRegistration() {

            log.info( "Checking PUSH registration" );

            if ( !PushPPGService.shouldRegister() || networkRequiredTimerTask != null ) {
                synchronized (radioStatusListener) {
                    if ( !PushPPGService.shouldRegister() || networkRequiredTimerTask != null ) {
                        log.info( "PUSH registration not required or task already running" );
                        return;
                    }
                }
            }

            log.info( "PUSH registration required, running task" );

            startNetworkRequiredTask();
        }

        public void networkStarted( int networkId, int service ) {

            super.networkStarted( networkId, service );

            if ( ( service & RadioInfo.NETWORK_SERVICE_DATA ) == RadioInfo.NETWORK_SERVICE_DATA ) {
                checkRegistration();
            }
        }

        public void networkServiceChange( int networkId, int service ) {

            super.networkServiceChange( networkId, service );

            if ( ( service & RadioInfo.NETWORK_SERVICE_DATA ) == RadioInfo.NETWORK_SERVICE_DATA ) {
                checkRegistration();
            }
        }

        public void networkStateChange( int state ) {

            super.networkStateChange( state );

            checkRegistration();
        }

        public void networkConnected() {

            super.networkConnected();

            checkRegistration();
        }

        public void networkDisconnected( int reason ) {

            super.networkDisconnected( reason );

            checkRegistration();
        }
    }
}
