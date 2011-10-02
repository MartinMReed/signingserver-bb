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
