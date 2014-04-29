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
