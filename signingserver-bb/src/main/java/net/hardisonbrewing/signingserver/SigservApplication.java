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
package net.hardisonbrewing.signingserver;

import java.util.Hashtable;

import net.hardisonbrewing.signingserver.service.icon.IconService;
import net.hardisonbrewing.signingserver.service.store.ModuleHandleStore;
import net.hardisonbrewing.signingserver.service.store.sig.SigStatusChangeListener;
import net.hardisonbrewing.signingserver.service.store.sig.SigStatusChangeListenerStore;
import net.hardisonbrewing.signingserver.service.store.sig.SigStatusStore;
import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.ui.UiApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SigservApplication extends UiApplication {

    private static final Logger log = LoggerFactory.getLogger( SigservApplication.class );

    public static final String AUTO_RUN_ON_STARTUP = "AutoRunOnStartup";
    public static final String BBM_REGISTER = "BBMRegister";
    public static final String PUSH_STARTUP = "PushStartup";
    public static final String PUSH_REGISTER = "PushRegister";
    public static final String PUSH_UNREGISTER = "PushUnregister";
    public static final String UPDATE_ICON_BITMAP = "UpdateIconBitmap";
    public static final String UPDATE_ICON_STATE = "UpdateIconState";

    public static void main( String[] args ) {

        if ( log.isDebugEnabled() ) {
            //HACK: workaround for a LoggerFactory so it will not be stripped out by obfuscation
            log.debug( "Logger: " + org.slf4j.impl.LoggerFactoryBinder.class.getName() );
        }

        ApplicationDescriptor currentApplicationDescriptor = ApplicationDescriptor.currentApplicationDescriptor();
        EventLogger.register( Settings.EVENT_LOG_ID, currentApplicationDescriptor.getModuleName(), EventLogger.VIEWER_STRING );

        if ( args != null ) {
            if ( args.length > 0 ) {
                if ( AUTO_RUN_ON_STARTUP.equals( args[0] ) ) {
                    SigservAutorunApplication.mainAutoRunOnStartup( args );
                    return;
                }
                else if ( BBM_REGISTER.equals( args[0] ) ) {
                    SigservBBMApplication.mainAutoRegister( args );
                    return;
                }
                else if ( PUSH_STARTUP.equals( args[0] ) ) {
                    SigservPushApplication.mainStartup( args );
                    return;
                }
                else if ( PUSH_REGISTER.equals( args[0] ) ) {
                    SigservPushApplication.mainRegister( args );
                    return;
                }
                else if ( PUSH_UNREGISTER.equals( args[0] ) ) {
                    SigservPushApplication.mainUnregister( args );
                    return;
                }
                else if ( UPDATE_ICON_BITMAP.equals( args[0] ) ) {
                    logEvent( "Updating application icon to: " + args[1] );
                    Bitmap bitmap = IconService.getPreferredIcon( args[1] );
                    HomeScreen.updateIcon( bitmap );
                    logEvent( "Finished updating application icon" );
                    return;
                }
                else if ( UPDATE_ICON_STATE.equals( args[0] ) ) {
                    boolean newState = "true".equalsIgnoreCase( args[1] );
                    logEvent( "Updating application state to: " + newState );
                    IconService.setIconNewState( newState );
                    logEvent( "Finished updating application state" );
                    return;
                }
            }
        }

        final SigservApplication application = new SigservApplication();
        application.invokeLater( new Runnable() {

            public void run() {

                application.bootloader();
            }
        } );
        application.enterEventDispatcher();
    }

    public static final void logEvent( String text ) {

        log.info( text );
        EventLogger.logEvent( Settings.EVENT_LOG_ID, text.getBytes() );
    }

    public static final SigservApplication getSigservApplication() {

        return (SigservApplication) Application.getApplication();
    }

    public static final ApplicationDescriptor getSystemApplicationDescriptor() {

        int moduleHandle = ModuleHandleStore.get();
        ApplicationDescriptor[] applicationDescriptors = CodeModuleManager.getApplicationDescriptors( moduleHandle );

        for (int i = 0; i < applicationDescriptors.length; i++) {
            int flags = applicationDescriptors[i].getFlags();
            if ( ( flags & ApplicationDescriptor.FLAG_SYSTEM ) == ApplicationDescriptor.FLAG_SYSTEM ) {
                return applicationDescriptors[i];
            }
        }

        return applicationDescriptors[0];
    }

    public static void updateStoredStatus( Hashtable status ) {

        SigStatusStore.put( status );

        SigStatusChangeListener[] statusChangeListeners = SigStatusChangeListenerStore.get();
        if ( statusChangeListeners != null ) {
            for (int i = 0; i < statusChangeListeners.length; i++) {
                statusChangeListeners[i].onStatusChange( status );
            }
        }
    }

    private void bootloader() {

        pushScreen( new net.hardisonbrewing.signingserver.ui.HomeScreen() );
    }

    public void activate() {

        IconService.setIconNewState( false );
        super.activate();
    }

    public static final boolean hasSufficientCoverage() {

        int[] transportTypes = TransportInfo.getAvailableTransportTypes();
        for (int i = 0; i < transportTypes.length; i++) {
            if ( TransportInfo.hasSufficientCoverage( i ) ) {
                return true;
            }
        }
        return false;
    }

    public static final void waitForStatup() {

        log.info( "Waiting for device startup to finish" );

        while (ApplicationManager.getApplicationManager().inStartup()) {
            try {
                Thread.sleep( 200 );
            }
            catch (InterruptedException e) {
                // do nothing
            }
        }

        log.info( "Device startup has finished" );
    }
}
