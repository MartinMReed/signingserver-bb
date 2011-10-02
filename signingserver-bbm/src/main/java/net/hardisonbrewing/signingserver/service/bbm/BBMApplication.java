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
package net.hardisonbrewing.signingserver.service.bbm;

import net.hardisonbrewing.signingserver.closed.Settings.BBMInfo;
import net.rim.blackberry.api.bbm.platform.BBMPlatformApplication;
import net.rim.blackberry.api.bbm.platform.BBMPlatformContext;
import net.rim.blackberry.api.bbm.platform.BBMPlatformContextListener;
import net.rim.blackberry.api.bbm.platform.BBMPlatformManager;
import net.rim.blackberry.api.bbm.platform.service.MessagingService;

public final class BBMApplication extends BBMPlatformApplication implements net.hardisonbrewing.signingserver.service.bbm.lib.BBMApplication {

    private BBMPlatformContext context;

    public BBMApplication() {

        super( BBMInfo.UUID );
    }

    public boolean isSupported() {

        return true;
    }

    public boolean register() {

        context = BBMPlatformManager.register( this );

        if ( context == null ) {
            return false;
        }

        context.setListener( new MyBBMPlatformContextListener() );
        return context.getAccessErrorCode() == BBMPlatformContext.ACCESS_ALLOWED;
    }

    public int getAccessErrorCode() {

        if ( context == null ) {
            return ACCESS_NOT_REGISTERED;
        }
        return context.getAccessErrorCode();
    }

    public void sendDownloadInvitation() {

        MessagingService messagingService = context.getMessagingService();
        messagingService.sendDownloadInvitation();
    }

    private static class MyBBMPlatformContextListener extends BBMPlatformContextListener {

        public void accessChanged( boolean isAccessAllowed, int accessErrorCode ) {

            // do nothing
        }
    }
}
