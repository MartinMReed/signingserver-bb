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
package net.hardisonbrewing.signingserver.service.bbm.lib;

public interface BBMApplication {

    public static final int ACCESS_NOT_REGISTERED = 0;
    public static final int ACCESS_ALLOWED = 1;
    public static final int ACCESS_BLOCKED_BY_USER = 2;
    public static final int ACCESS_BLOCKED_BY_RIM = 3;
    public static final int ACCESS_REREGISTRATION_REQUIRED = 4;
    public static final int ACCESS_NO_DATA_COVERAGE = 5;
    public static final int ACCESS_TEMPORARY_ERROR = 6;
    public static final int ACCESS_BLOCKED_RESET_REQUIRED = 7;
    public static final int ACCESS_REGISTER_WITH_UI_APPLICATION = 9;

    public static final int APP_ENVIRONMENT_TEST = 0;
    public static final int APP_ENVIRONMENT_APPWORLD = 1;

    public static final int INVOKE_PROFILE_BOX_ITEM = 0;

    public boolean isSupported();

    public boolean register();

    public int getAccessErrorCode();

    public void sendDownloadInvitation();
}
