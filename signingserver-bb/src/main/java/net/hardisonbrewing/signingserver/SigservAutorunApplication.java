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

import net.hardisonbrewing.signingserver.service.OptionsProvider;
import net.hardisonbrewing.signingserver.service.SigStatusService;
import net.hardisonbrewing.signingserver.service.icon.IconService;
import net.hardisonbrewing.signingserver.service.network.NetworkReadyListener;
import net.hardisonbrewing.signingserver.service.push.PushPPGService;
import net.hardisonbrewing.signingserver.service.store.ModuleHandleStore;
import net.rim.blackberry.api.options.OptionsManager;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.WLANInfo;

import org.apache.commons.threadpool.DefaultThreadPool;
import org.apache.commons.threadpool.ThreadPool;
import org.metova.mobile.util.time.Dates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SigservAutorunApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger( SigservAutorunApplication.class );

    private static final int STATE_READY = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_RAN = 2;

    private static final Object lock = new Object();
    private static int runState = STATE_READY;

    private TimerTask networkRequiredTimerTask;
    private NetworkReadyListener networkReadyListener;

    public static void mainAutoRunOnStartup( String[] args ) {

        log.info( "Running auto run application" );

        ModuleHandleStore.put();
        IconService.updateIcon();
        OptionsManager.registerOptionsProvider( new OptionsProvider() );

        ThreadPool threadPool = new DefaultThreadPool( 1 );
        threadPool.invokeLater( new NetworkRequiredTask() );
    }

    private static void downloadStatus() throws Exception {

        log.info( "Downloading SigStatus from server" );

        Hashtable status = SigStatusService.downloadStatus();
        SigservApplication.updateStoredStatus( status );
        IconService.updateIcon();
    }

    private static final void startup() throws Exception {

        boolean runStartup = false;

        if ( runState == STATE_READY ) {
            synchronized (lock) {
                if ( runState == STATE_READY ) {
                    runState = STATE_RUNNING;
                    runStartup = true;
                }
            }
        }

        if ( !runStartup ) {
            return;
        }

        try {
            runStartup();
        }
        catch (Exception e) {
            runState = STATE_READY;
        }
    }

    private static final void runStartup() throws Exception {

        log.info( "Running startup" );

        if ( SigservPushApplication.isSupported() ) {
            try {
                PushPPGService.registerOnStartup();
            }
            catch (Exception e) {
                log.error( "Exception trying to register PUSH notifications", e );
            }
        }

        try {
            SigservBBMApplication.registerBBMConnect();
        }
        catch (Exception e) {
            log.error( "Exception trying to register BBM", e );
        }

        downloadStatus();

        runState = STATE_RAN;
    }

    private static final class NetworkRequiredTask implements Runnable {

        public void run() {

            SigservApplication.waitForStatup();

            log.info( "Checking coverage for startup" );

            if ( SigservApplication.hasSufficientCoverage() ) {
                log.info( "Sufficient coverage, running startup" );
                try {
                    startup();
                }
                catch (Exception e) {
                    log.error( "Exception while running network required startup" );
                }
            }
            else {
                log.info( "Coverage not sufficient, skipping startup" );
            }

            if ( runState != STATE_RAN ) {

                log.info( "Startup not completed, delaying startup" );

                final SigservAutorunApplication application = new SigservAutorunApplication();
                application.invokeLater( new Runnable() {

                    public void run() {

                        application.bootloader();
                    }
                } );
                application.enterEventDispatcher();
            }
        }
    }

    private void bootloader() {

        networkReadyListener = new MyNetworkReadyListener();
        addRadioListener( networkReadyListener );
        WLANInfo.addListener( networkReadyListener );

        Timer timer = new Timer();
        networkRequiredTimerTask = new NetworkRequiredTimerTask();
        timer.schedule( networkRequiredTimerTask, Dates.SECOND * 15, Dates.SECOND * 15 );
    }

    public boolean requestClose() {

        log.info( "Closing auto run application" );

        if ( networkRequiredTimerTask != null ) {
            networkRequiredTimerTask.cancel();
            networkRequiredTimerTask = null;
        }

        if ( networkReadyListener != null ) {
            WLANInfo.removeListener( networkReadyListener );
            removeRadioListener( networkReadyListener );
            networkReadyListener = null;
        }

        System.exit( 0 );

        return true;
    }

    private final class NetworkRequiredTimerTask extends TimerTask {

        public void run() {

            if ( runState != STATE_RAN && SigservApplication.hasSufficientCoverage() ) {
                log.info( "Coverage is sufficient and startup is not completed, running startup" );
                try {
                    startup();
                }
                catch (Exception e) {
                    // do nothing
                }
            }

            if ( runState == STATE_RAN ) {
                requestClose();
            }
        }
    }

    private final class MyNetworkReadyListener extends NetworkReadyListener {

        protected void onNetworkReady() throws Exception {

            if ( runState != STATE_RAN ) {
                log.info( "Network is ready and startup is not completed, running startup" );
                startup();
            }

            if ( runState == STATE_RAN ) {
                requestClose();
            }
        }
    }
}
