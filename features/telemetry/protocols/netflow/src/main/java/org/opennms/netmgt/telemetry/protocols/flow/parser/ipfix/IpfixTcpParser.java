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

package org.opennms.netmgt.telemetry.protocols.flow.parser.ipfix;

import java.net.InetSocketAddress;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.api.parser.TcpParser;
import org.opennms.netmgt.telemetry.protocols.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.flow.parser.ParserBase;
import org.opennms.netmgt.telemetry.protocols.flow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.flow.parser.netflow9.proto.Header;
import org.opennms.netmgt.telemetry.protocols.flow.parser.netflow9.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.flow.parser.session.TcpSession;

import io.netty.channel.EventLoopGroup;

public class IpfixTcpParser extends ParserBase implements TcpParser {

    public IpfixTcpParser(final String name,
                          final AsyncDispatcher<TelemetryMessage> dispatcher) {
        super(Protocol.IPFIX, name, dispatcher);
    }

    @Override
    public void start(final EventLoopGroup eventLoopGroup) {
    }

    @Override
    public void stop() {}

    @Override
    public Handler accept(final InetSocketAddress remoteAddress,
                          final InetSocketAddress localAddress) {
        final TcpSession session = new TcpSession();

        return buffer -> {
            final Header header = new Header(BufferUtils.slice(buffer, Header.SIZE));
            final Packet packet = new Packet(session, header, buffer);

            this.transmit(packet, remoteAddress);
        };
    }
}
