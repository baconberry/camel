/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.pubnub;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.pubnub.api.PubNub;
import com.pubnub.api.UserId;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.java.v2.PNConfiguration;
import com.pubnub.internal.java.PubNubForJavaImpl;
import org.apache.camel.BindToRegistry;
import org.apache.camel.test.AvailablePortFinder;
import org.apache.camel.test.junit5.CamelTestSupport;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.pubnub.api.enums.PNHeartbeatNotificationOptions.NONE;

public class PubNubTestBase extends CamelTestSupport {

    private final int port = AvailablePortFinder.getNextAvailable();

    @BindToRegistry("pubnub")
    private PubNub pubnub = createPubNubInstance();

    private WireMockServer wireMockServer = new WireMockServer(options().port(port));

    @Override
    protected void setupResources() {
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @Override
    protected void cleanupResources() {
        wireMockServer.stop();
        pubnub.destroy();
    }

    protected PubNub getPubnub() {
        return pubnub;
    }

    private PubNub createPubNubInstance() {
        PNConfiguration config;
        try {
            config = PNConfiguration.builder(new UserId("myUUID"), "mySubscribeKey")
                    .publishKey("myPublishKey")
                    .secure(false)
                    .origin("localhost" + ":" + port)
                    .logVerbosity(PNLogVerbosity.NONE)
                    .heartbeatNotificationOptions(NONE)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        class MockedTimePubNub extends PubNubForJavaImpl {

            MockedTimePubNub(PNConfiguration initialConfig) {
                super(initialConfig);
            }

            @Override
            public int getTimestamp() {
                return 1337;
            }

            @Override
            public String getVersion() {
                return "suchJava";
            }

        }

        return new MockedTimePubNub(config);
    }
}
