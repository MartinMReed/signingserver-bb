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

import javax.microedition.content.ContentHandler;
import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;

import net.hardisonbrewing.signingserver.model.OptionProperties;
import net.hardisonbrewing.signingserver.service.AppWorldInfo;
import net.hardisonbrewing.signingserver.service.bbm.lib.BBMApplication;
import net.hardisonbrewing.signingserver.service.icon.IconService;
import net.hardisonbrewing.signingserver.service.store.OptionsStore;
import net.hardisonbrewing.signingserver.service.store.bbm.BBMApplicationStore;
import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SigservBBMApplication {

    private static final Logger log = LoggerFactory.getLogger( SigservBBMApplication.class );

    private BBMApplication application;

    public SigservBBMApplication() {

        application = (BBMApplication) RuntimeStore.getRuntimeStore().get( Settings.BBM_STORE );
    }

    public static void mainAutoRegister( String[] args ) {

        if ( !OptionsStore.getBoolean( OptionProperties.BBM_ENABLED ) ) {
            log.info( "BBM disabled. Skipping registration." );
            return;
        }

        final AutoRegisterApplication application = new AutoRegisterApplication();
        application.invokeLater( new Runnable() {

            public void run() {

                application.register();
                System.exit( 0 );
            }
        } );
        application.enterEventDispatcher();
    }

    public static int registerBBMConnect() throws ApplicationManagerException {

        ApplicationDescriptor systemApplicationDescriptor = SigservApplication.getSystemApplicationDescriptor();
        String[] arguments = new String[] { SigservApplication.BBM_REGISTER };
        ApplicationDescriptor applicationDescriptor = new ApplicationDescriptor( systemApplicationDescriptor, arguments );
        return ApplicationManager.getApplicationManager().runApplication( applicationDescriptor );
    }

    public boolean shouldRegisterOnUi() {

        return application.getAccessErrorCode() == BBMApplication.ACCESS_REGISTER_WITH_UI_APPLICATION;
    }

    public boolean isSupported() {

        return application != null;
    }

    public synchronized boolean isRegistered() {

        return application.getAccessErrorCode() == BBMApplication.ACCESS_ALLOWED;
    }

    public synchronized boolean register() {

        if ( isRegistered() ) {
            log.error( "BBM already registered. Skipping registration." );
            return true;
        }

        boolean registered = application.register();

        if ( !registered ) {
            int accessErrorCode = application.getAccessErrorCode();
            log.error( "Unable to register for BBM. Access status[" + accessErrorCode + "]" );
            if ( accessErrorCode == BBMApplication.ACCESS_REGISTER_WITH_UI_APPLICATION ) {
                IconService.setIconNewState( true );
                return registered;
            }
        }
        else {
            IconService.setIconNewState( false );
        }

        return registered;
    }

    public void sendDownloadInvitation() {

        application.sendDownloadInvitation();
    }

    public boolean isRegistered( boolean ask ) {

        boolean registered = isRegistered();
        if ( !registered && ask ) {
            String message = "This feature requires a BBM connection. Would you like to connect now?";
            int result = Dialog.ask( Dialog.D_YES_NO, message, Dialog.YES );
            if ( result != Dialog.YES ) {
                return false;
            }
            return register();
        }
        return registered;
    }

    public static void invokeAppWorld() {

        boolean invoked = false;

        try {
            Invocation invocation = new Invocation( null, null, "net.rim.bb.appworld.Content", true, ContentHandler.ACTION_OPEN );
            invocation.setArgs( new String[] { AppWorldInfo.BBM_ID } );

            Registry registry = Registry.getRegistry( UiApplication.getUiApplication().getClass().getName() );
            registry.invoke( invocation );

            Invocation response = registry.getResponse( true );
            invoked = response.getStatus() == Invocation.OK;
        }
        catch (Throwable t) {
            log.error( "Unable to launch AppWorld BBM page", t );
        }

        if ( !invoked ) {
            BrowserSession browser = Browser.getDefaultSession();
            browser.displayPage( AppWorldInfo.CONTENT_URL + AppWorldInfo.BBM_ID );
        }
    }

    private static final class AutoRegisterApplication extends Application {

        private void register() {

            SigservBBMApplication application = BBMApplicationStore.get();
            if ( application.isSupported() && !application.isRegistered() ) {
                application.register();
            }
        }
    }
}
