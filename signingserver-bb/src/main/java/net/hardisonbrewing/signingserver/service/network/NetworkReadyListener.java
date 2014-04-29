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
package net.hardisonbrewing.signingserver.service.network;

import net.hardisonbrewing.signingserver.SigservApplication;

import org.apache.commons.threadpool.DefaultThreadPool;
import org.apache.commons.threadpool.ThreadPool;

public abstract class NetworkReadyListener extends AbstractRadioStatusListener {

    private static final int STATE_READY = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_RAN = 2;

    private final Object lock = new Object();
    private int runState;

    /**
     * This method runs on the ThreadPool
     * @return
     */
    protected abstract void onNetworkReady() throws Exception;

    private void networkReady() {

        boolean runNetworkReady = false;

        if ( runState == STATE_READY ) {
            synchronized (lock) {
                if ( runState == STATE_READY ) {
                    runState = STATE_RUNNING;
                    runNetworkReady = true;
                }
            }
        }

        if ( runNetworkReady ) {
            try {
                runNetworkReady();
            }
            catch (Exception e) {
                runState = STATE_READY;
            }
        }
    }

    private void runNetworkReady() {

        ThreadPool threadPool = new DefaultThreadPool( 1 );
        threadPool.invokeLater( new Runnable() {

            public void run() {

                try {
                    onNetworkReady();
                    runState = STATE_RAN;
                }
                catch (Exception e) {
                    runState = STATE_READY;
                }
            }
        } );
    }

    public void networkStarted( int networkId, int service ) {

        super.networkStarted( networkId, service );

        if ( SigservApplication.hasSufficientCoverage() ) {
            networkReady();
        }
    }

    public void networkServiceChange( int networkId, int service ) {

        super.networkServiceChange( networkId, service );

        if ( SigservApplication.hasSufficientCoverage() ) {
            networkReady();
        }
    }

    public void networkStateChange( int state ) {

        super.networkStateChange( state );

        if ( SigservApplication.hasSufficientCoverage() ) {
            networkReady();
        }
    }

    public void signalLevel( int level ) {

        super.signalLevel( level );

        if ( SigservApplication.hasSufficientCoverage() ) {
            networkReady();
        }
    }

    public void networkConnected() {

        super.networkConnected();

        if ( SigservApplication.hasSufficientCoverage() ) {
            networkReady();
        }
    }
}
