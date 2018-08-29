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

import java.util.Map;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.simple.SimpleTcpParser;
import org.opennms.netmgt.telemetry.listeners.simple.SimpleUdpParser;
import org.opennms.netmgt.telemetry.listeners.smart.SmartUdpListener;
import org.opennms.netmgt.telemetry.listeners.smart.SmartUdpParser;
import org.opennms.netmgt.telemetry.protocols.flow.parser.ipfix.IpfixTcpParser;
import org.opennms.netmgt.telemetry.protocols.flow.parser.ipfix.IpfixUdpParser;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

public class Ipfix implements SmartUdpParser.Factory, SimpleTcpParser.Factory {

    @Override
    public SimpleTcpParser createTcpParser(final String name,
                                           final Map<String, String> parameters,
                                           final AsyncDispatcher<TelemetryMessage> dispatcher) {
        final IpfixTcpParser parser = new IpfixTcpParser(name, dispatcher);

        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(parser);
        wrapper.setPropertyValues(parameters);

        return parser;
    }

    @Override
    public SmartUdpParser createUdpParser(final String name,
                                          final Map<String, String> parameters,
                                          final AsyncDispatcher<TelemetryMessage> dispatcher) {
        final IpfixUdpParser parser = new IpfixUdpParser(name, dispatcher);

        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(parser);
        wrapper.setPropertyValues(parameters);

        return parser;
    }
}
