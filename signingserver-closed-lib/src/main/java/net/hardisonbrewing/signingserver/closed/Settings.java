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
package net.hardisonbrewing.signingserver.closed;

public class Settings {

    public static class SigStatusInfo {

        public static final int VERSION;
        public static final String URL;
    }

    public static class BISPushInfo {

        public static final int VERSION;

        public static final String REGISTER_URL;
        public static final String CONFIRM_URL;

        public static final String URL;
        public static final String APPID;
        public static final int PORT;
    }

    public static class BBMInfo {

        public static final String UUID;

        public static final int SESSION_VERSION;
        public static final String SESSION_URL;
    }
}
