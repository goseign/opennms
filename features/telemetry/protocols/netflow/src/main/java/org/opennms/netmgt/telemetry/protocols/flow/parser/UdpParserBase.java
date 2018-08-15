/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.telemetry.protocols.flow.parser;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.api.parser.UdpParser;
import org.opennms.netmgt.telemetry.protocols.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.flow.parser.ie.RecordProvider;
import org.opennms.netmgt.telemetry.protocols.flow.parser.netflow9.proto.Header;
import org.opennms.netmgt.telemetry.protocols.flow.parser.netflow9.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.flow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.flow.parser.session.UdpSessionManager;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;

public abstract class UdpParserBase extends ParserBase implements UdpParser {
    public final static long HOUSEKEEPING_INTERVAL = 60000;

    private UdpSessionManager sessionManager;

    private ScheduledFuture<?> housekeepingFuture;
    private Duration templateTimeout = Duration.ofMinutes(30);

    public UdpParserBase(Protocol protocol, String name, AsyncDispatcher<TelemetryMessage> dispatcher) {
        super(protocol, name, dispatcher);
    }

    protected abstract RecordProvider parse(final Session session, final ByteBuffer buffer) throws Exception;

    @Override
    public final void parse(final ByteBuffer buffer, final InetSocketAddress remoteAddress, final InetSocketAddress localAddress) throws Exception {
        final Session session = this.sessionManager.getSession(remoteAddress, localAddress);
        this.transmit(this.parse(session, buffer), remoteAddress);
    }

    @Override
    public void start(final EventLoopGroup eventLoopGroup) {
        this.sessionManager = new UdpSessionManager(this.templateTimeout);
        this.housekeepingFuture = eventLoopGroup.scheduleAtFixedRate(this.sessionManager::doHousekeeping,
                HOUSEKEEPING_INTERVAL,
                HOUSEKEEPING_INTERVAL,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        this.housekeepingFuture.cancel(false);
    }

    public Duration getTemplateTimeout() {
        return this.templateTimeout;
    }

    public void setTemplateTimeout(final Duration templateTimeout) {
        this.templateTimeout = templateTimeout;
    }
}
