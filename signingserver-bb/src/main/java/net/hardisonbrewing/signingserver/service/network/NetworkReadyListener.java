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
