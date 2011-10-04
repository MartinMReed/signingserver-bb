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

import java.lang.ref.WeakReference;

import net.hardisonbrewing.signingserver.SigservPushApplication;
import net.hardisonbrewing.signingserver.model.OptionProperties;
import net.hardisonbrewing.signingserver.service.push.PushPPGService;
import net.hardisonbrewing.signingserver.service.push.PushSIGService;
import net.hardisonbrewing.signingserver.service.store.OptionsStore;
import net.hardisonbrewing.signingserver.service.store.narst.NarstKeyStoreManager;
import net.hardisonbrewing.signingserver.service.store.push.PushPPGStatusChangeListener;
import net.hardisonbrewing.signingserver.service.store.push.PushPPGStatusChangeListenerStore;
import net.hardisonbrewing.signingserver.service.store.push.PushPPGStatusStore;
import net.rim.blackberry.api.push.PushApplicationStatus;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptionsProvider implements net.rim.blackberry.api.options.OptionsProvider {

    private static final Logger log = LoggerFactory.getLogger( OptionsProvider.class );

    private WeakReference mainScreen;

    public String getTitle() {

        return "Signing Server Status";
    }

    public void populateMainScreen( MainScreen mainScreen ) {

        this.mainScreen = new WeakReference( mainScreen );

        OptionProperties optionProperties = OptionsStore.get();

        PushStatusField pushStatusField = new PushStatusField();
        PushPPGStatusChangeListenerStore.put( pushStatusField );
        mainScreen.add( pushStatusField );

        boolean pushEnabled = optionProperties.getBoolean( OptionProperties.PUSH_ENABLED );
        CheckboxField pushEnabledField = new CheckboxField( "Enable push at startup", pushEnabled, Field.USE_ALL_WIDTH );
        pushEnabledField.setCookie( OptionProperties.PUSH_ENABLED );
        mainScreen.add( pushEnabledField );

        if ( !SigservPushApplication.isSupported() ) {
            pushEnabledField.setEditable( false );
        }

        boolean bbmEnabled = optionProperties.getBoolean( OptionProperties.BBM_ENABLED );
        CheckboxField bbmEnabledField = new CheckboxField( "Enable BBM at startup", bbmEnabled, Field.USE_ALL_WIDTH );
        bbmEnabledField.setCookie( OptionProperties.BBM_ENABLED );
        mainScreen.add( bbmEnabledField );

        boolean deviceKeyStore = optionProperties.getBoolean( OptionProperties.DEVICE_KEY_STORE );
        CheckboxField deviceKeyStoreField = new CheckboxField( "Use the device key store", deviceKeyStore, Field.USE_ALL_WIDTH );
        deviceKeyStoreField.setCookie( OptionProperties.DEVICE_KEY_STORE );
        mainScreen.add( deviceKeyStoreField );

        if ( SigservPushApplication.isSupported() ) {
            PushMenuItem pushMenuItem = new PushMenuItem();
            PushPPGStatusChangeListenerStore.put( pushMenuItem );
            mainScreen.addMenuItem( pushMenuItem );
        }
    }

    public void save() {

        MainScreen mainScreen = (MainScreen) this.mainScreen.get();

        OptionProperties optionProperties = OptionsStore.get();
        boolean pushEnabled = optionProperties.getBoolean( OptionProperties.PUSH_ENABLED );
        boolean deviceKeyStore = optionProperties.getBoolean( OptionProperties.DEVICE_KEY_STORE );

        int fieldCount = mainScreen.getFieldCount();
        for (int i = 0; i < fieldCount; i++) {
            Field field = mainScreen.getField( i );
            Object cookie = field.getCookie();
            if ( field instanceof CheckboxField ) {
                CheckboxField checkboxField = (CheckboxField) field;
                optionProperties.put( cookie, new Boolean( checkboxField.getChecked() ) );
            }
        }

        OptionsStore.commit();

        boolean _pushEnabled = optionProperties.getBoolean( OptionProperties.PUSH_ENABLED );
        boolean _deviceKeyStore = optionProperties.getBoolean( OptionProperties.DEVICE_KEY_STORE );

        if ( pushEnabled != _pushEnabled ) {
            try {
                PushPPGService.register( _pushEnabled );
            }
            catch (ApplicationManagerException e) {
                log.error( "Exception while trying " + ( _pushEnabled ? "register" : "unregister" ) + " push notifications" );
            }
        }

        if ( deviceKeyStore != _deviceKeyStore ) {
            NarstKeyStoreManager.reset();
        }
    }

    private static final class PushMenuItem extends MenuItem implements PushPPGStatusChangeListener {

        private boolean running;

        public PushMenuItem() {

            super( "", 0, 0 );

            updateText( PushPPGStatusStore.get() );
        }

        private void updateText( byte status ) {

            String text;
            switch (status) {
                case PushApplicationStatus.STATUS_ACTIVE: {
                    text = "Unregister Push";
                    break;
                }
                case PushApplicationStatus.STATUS_PENDING: {
                    text = "Cancel Push";
                    break;
                }
                default: {
                    text = "Register Push";
                    break;
                }
            }
            setText( text );
        }

        public void onStatusChange( byte status ) {

            running = false;

            updateText( status );
        }

        public void run() {

            if ( running ) {
                return;
            }

            running = true;

            try {

                byte status = PushPPGStatusStore.get();

                boolean register;
                String text;
                switch (status) {
                    case PushApplicationStatus.STATUS_ACTIVE: {
                        text = "Unregistering Push";
                        register = false;
                        break;
                    }
                    case PushApplicationStatus.STATUS_PENDING: {
                        text = "Canceling Push";
                        register = false;
                        break;
                    }
                    default: {
                        text = "Registering Push";
                        register = true;
                        break;
                    }
                }

                try {
                    setText( text + Characters.HORIZONTAL_ELLIPSIS );
                    PushPPGService.register( register );
                }
                catch (Exception e) {
                    log.error( "Exception while trying " + ( register ? "register" : "unregister" ) + " push notifications" );
                    updateText( status );
                    throw e;
                }
            }
            catch (Exception e) {
                running = false;
            }
        }
    }

    private static final class PushStatusField extends LabelField implements PushPPGStatusChangeListener {

        public PushStatusField() {

            updateText( PushPPGStatusStore.get() );
        }

        private void updateText( byte status ) {

            setText( "Push status: " + PushSIGService.getStatusHumanText( status ) );
        }

        public void onStatusChange( final byte status ) {

            Screen screen = getScreen();
            if ( screen == null ) {
                return;
            }

            Application application = screen.getApplication();
            if ( application == null ) {
                return;
            }

            application.invokeAndWait( new Runnable() {

                public void run() {

                    updateText( status );
                }
            } );
        }
    }
}
