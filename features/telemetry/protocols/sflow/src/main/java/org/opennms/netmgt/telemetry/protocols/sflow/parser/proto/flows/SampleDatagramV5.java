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

package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Array;

import com.google.common.base.MoreObjects;

// struct sample_datagram_v5 {
//    address agent_address;          /* IP address of sampling agent,
//                                      sFlowAgentAddress. */
//    unsigned int sub_agent_id;     /* Used to distinguishing between datagram
//                                      streams from separate agent sub entities
//                                      within an device. */
//    unsigned int sequence_number;  /* Incremented with each sample datagram
//                                      generated by a sub-agent within an
//                                      agent. */
//    unsigned int uptime;           /* Current time (in milliseconds since device
//                                      last booted). Should be set as close to
//                                      datagram transmission time as possible.
//                                      Note: While a sub-agents should try and
//                                            track the global sysUptime value
//                                            a receiver of sFlow packets must
//                                            not assume that values are
//                                            synchronised between sub-agents. */
//    sample_record samples<>;        /* An array of sample records */
// };

public class SampleDatagramV5 {
    public final Address agent_address;
    public final long sub_agent_id;
    public final long sequence_number;
    public final long uptime;
    public final Array<SampleRecord> samples;

    public SampleDatagramV5(final ByteBuffer buffer) throws InvalidPacketException {
        this.agent_address = new Address(buffer);
        this.sub_agent_id = BufferUtils.uint32(buffer);
        this.sequence_number = BufferUtils.uint32(buffer);
        this.uptime = BufferUtils.uint32(buffer);
        this.samples = new Array(buffer, Optional.empty(), SampleRecord::new);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("agent_address", this.agent_address)
                .add("sub_agent_id", this.sub_agent_id)
                .add("sequence_number", this.sequence_number)
                .add("uptime", this.uptime)
                .add("samples", this.samples)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("agent_address");
        this.agent_address.writeBson(bsonWriter);
        bsonWriter.writeInt64("sub_agent_id", this.sub_agent_id);
        bsonWriter.writeInt64("sequence_number", this.sequence_number);
        bsonWriter.writeInt64("uptime", this.uptime);
        bsonWriter.writeStartArray("samples");
        for (final SampleRecord sampleRecord : this.samples) {
            sampleRecord.writeBson(bsonWriter);
        }
        bsonWriter.writeEndArray();
        bsonWriter.writeEndDocument();
    }
}
