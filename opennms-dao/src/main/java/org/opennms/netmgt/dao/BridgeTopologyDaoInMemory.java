/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.dao.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;

public class BridgeTopologyDaoInMemory implements BridgeTopologyDao {
    volatile Set<BroadcastDomain> m_domains = new HashSet<BroadcastDomain>();

    @Override
    public synchronized void save(BroadcastDomain domain) {
        m_domains.add(domain);
    }

    @Override
    public synchronized void load(BridgeBridgeLinkDao bridgeBridgeLinkDao,BridgeMacLinkDao bridgeMacLinkDao) {
        m_domains=getAllPersisted(bridgeBridgeLinkDao, bridgeMacLinkDao);
    }

    @Override
    public List<SharedSegment> getBridgeNodeSharedSegments(BridgeBridgeLinkDao bridgeBridgeLinkDao,BridgeMacLinkDao bridgeMacLinkDao, int nodeid) {
        List<SharedSegment> segments = new ArrayList<SharedSegment>();
        /*
        for (BroadcastDomain domain: getAllPersisted(bridgeBridgeLinkDao, bridgeMacLinkDao)) {
            System.out.println("parsing domain:" + domain);
            System.out.println("parsing domain with nodes:" + domain.getBridgeNodesOnDomain());
            System.out.println("parsing domain with macs:" + domain.getMacsOnDomain());
            if (domain.getBridgeNodesOnDomain().contains(nodeid)) {
                System.out.println("got domain with nodeid:" + nodeid);
                for (SharedSegment segment: domain.getTopology()) {
                    System.out.println("parsing segment:" + segment);
                    System.out.println("parsing segment with nodes:" + segment.getBridgeIdsOnSegment());
                    System.out.println("parsing segment with macs:" + segment.getMacsOnSegment());
                    if (segment.getBridgeIdsOnSegment().contains(nodeid)) {
                        segments.add(segment);
                        System.out.println("added segment:" + segment);
                    }
                }
            }
        }*/
        Set<Integer> designated = new HashSet<Integer>();
BRIDGELINK:        for (BridgeBridgeLink link : bridgeBridgeLinkDao.findByNodeId(nodeid)) {
            for (SharedSegment segment : segments) {
                if (segment.containsPort(link.getNode().getId(),
                                         link.getBridgePort())
                     || segment.containsPort(link.getDesignatedNode().getId(),
                                             link.getDesignatedPort())) {
                    segment.add(link);
                    designated.add(link.getDesignatedNode().getId());
                    continue BRIDGELINK;
                }
            }
            SharedSegment segment = new SharedSegment();
            segment.add(link);
            segment.setDesignatedBridge(link.getDesignatedNode().getId());
            segments.add(segment);
        }
        
        designated.add(nodeid);
        for (Integer curNodeId: designated) {
DBRIDGELINK:        for (BridgeBridgeLink link : bridgeBridgeLinkDao.findByDesignatedNodeId(curNodeId)) {
            for (SharedSegment segment : segments) {
                if (segment.containsPort(link.getNode().getId(),
                                         link.getBridgePort())
                     || segment.containsPort(link.getDesignatedNode().getId(),
                                             link.getDesignatedPort())) {
                    segment.add(link);
                    continue DBRIDGELINK;
                }
            }
            SharedSegment segment = new SharedSegment();
            segment.add(link);
            segment.setDesignatedBridge(link.getDesignatedNode().getId());
            segments.add(segment);
        }
        }

MACLINK:        for (BridgeMacLink link : bridgeMacLinkDao.findByNodeId(nodeid)) {
            link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
            for (SharedSegment segment : segments) {
                if (segment.containsMac(link.getMacAddress())
                        || segment.containsPort(link.getNode().getId(),
                                                link.getBridgePort())) {
                    segment.add(link);
                    continue MACLINK;
                }
            }
            SharedSegment segment = new SharedSegment();
            segment.add(link);
            segment.setDesignatedBridge(link.getNode().getId());
            segments.add(segment);
        }

        return segments;
    }
    
    @Override
    public SharedSegment getHostNodeSharedSegment(BridgeBridgeLinkDao bridgeBridgeLinkDao, BridgeMacLinkDao bridgeMacLinkDao, String mac) {
        
        List<BridgeMacLink> links = bridgeMacLinkDao.findByMacAddress(mac);
        if (links.size() == 0 )
            return new SharedSegment();
        BridgeMacLink link = links.get(0);
        for (SharedSegment segment: getBridgeNodeSharedSegments(bridgeBridgeLinkDao, bridgeMacLinkDao, link.getNode().getId()) ) {
            if (segment.containsPort(link.getNode().getId(), link.getBridgePort())) {
                return segment;
            }
        }
        return new SharedSegment();
    }

    @Override
    public Set<BroadcastDomain> getAllPersisted(BridgeBridgeLinkDao bridgeBridgeLinkDao,BridgeMacLinkDao bridgeMacLinkDao) {
        List<SharedSegment> segments = new ArrayList<SharedSegment>();

BRIDGELINK:        for (BridgeBridgeLink link : bridgeBridgeLinkDao.findAll()) {
            for (SharedSegment segment : segments) {
                if (segment.containsPort(link.getNode().getId(),
                                         link.getBridgePort())
                     || segment.containsPort(link.getDesignatedNode().getId(),
                                             link.getDesignatedPort())) {
                    segment.add(link);
                    continue BRIDGELINK;
                }
            }
            SharedSegment segment = new SharedSegment();
            segment.add(link);
            segment.setDesignatedBridge(link.getDesignatedNode().getId());
            segments.add(segment);
        }
        
        Map<String,List<BridgeMacLink>> mactobridgeportlist = new HashMap<String, List<BridgeMacLink>>();
        for (BridgeMacLink link : bridgeMacLinkDao.findAll()) {
            link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
            if (!mactobridgeportlist.containsKey(link.getMacAddress()))
                mactobridgeportlist.put(link.getMacAddress(), new ArrayList<BridgeMacLink>());
            mactobridgeportlist.get(link.getMacAddress()).add(link);
        }

        List<BridgeMacLink> forwarders = new ArrayList<BridgeMacLink>();
MAC:        for (String macaddress: mactobridgeportlist.keySet()) {
            System.err.println("parsing mac:" + macaddress);
            List<BridgeMacLink> maclinks = mactobridgeportlist.get(macaddress); 
            SharedSegment segmentfound = null;
MACLINK:    for (BridgeMacLink link : maclinks) {
                // check if I can found a segment
                for (SharedSegment segment : segments) {
                    if (segment.containsPort(link.getNode().getId(),
                                                    link.getBridgePort())) {
                        segmentfound = segment;
                        System.err.println("found segment:" + segmentfound.printTopology());
                        break MACLINK;
                    }
                }
            }
            if (segmentfound != null) {
                boolean forwardermac = false;
                for (BridgePort bp: segmentfound.getBridgePortsOnSegment()) {
                    boolean found = false;
                    for (BridgeMacLink link: maclinks) {
                        if (link.getNode().getId().intValue() == bp.getNode().getId().intValue() &&
                                link.getBridgePort().intValue() == bp.getBridgePort().intValue()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        forwardermac = true;
                        break;
                    }
                }
                System.err.println("forwarder:" + forwardermac);
                             if (forwardermac) {
                    forwarders.addAll(maclinks);
                    continue MAC;
                }
            } else {
                segmentfound = new SharedSegment();
                segments.add(segmentfound);
            }
            for (BridgeMacLink link: maclinks) {
                segmentfound.add(link);
                if (!segmentfound.hasDesignatedBridgeport())
                    segmentfound.setDesignatedBridge(link.getNode().getId());
            }
        }
                
        Set<Set<Integer>> nodelinked = new HashSet<Set<Integer>>();
SHARED:        for (SharedSegment segment: segments) {
            Set<Integer> nodesOnSegment = new HashSet<Integer>(segment.getBridgeIdsOnSegment());
            for (Set<Integer> nodes: nodelinked) {
                for (Integer nodeid: nodesOnSegment) {
                    if (nodes.contains(nodeid)) 
                        continue SHARED;
                }
            }
            nodelinked.add(getNodesOnDomainSet(segments, segment, new HashSet<SharedSegment>(),nodesOnSegment));
        }
        
        Set<BroadcastDomain> domains = new HashSet<BroadcastDomain>();
        for (Set<Integer> nodes : nodelinked) {
            BroadcastDomain domain = new BroadcastDomain();
            for (Integer nodeid: nodes)
                domain.addBridge(new Bridge(nodeid));
            domains.add(domain);
        }
        // Assign the segment to domain and add to single nodes
        for (SharedSegment segment : segments) {
            BroadcastDomain domain = null;
            for (BroadcastDomain cdomain: domains) {
                if (cdomain.containsAtleastOne(segment.getBridgeIdsOnSegment())) {
                    domain = cdomain;
                    break;
                }
            }
            if (domain == null) {
                domain = new BroadcastDomain();
                domains.add(domain);
            }
            domain.loadTopologyEntry(segment);
        }
        for (BroadcastDomain domain: domains)
            domain.loadTopologyRoot();
        
        for (BridgeMacLink forwarder : forwarders) {
            for (BroadcastDomain domain: domains) {
                if (domain.containBridgeId(forwarder.getNode().getId())) {
                    domain.addForwarding(forwarder);
                    break;
                }
            }
        }

        return domains;
    }
    
    private Set<Integer> getNodesOnDomainSet(List<SharedSegment> segments, SharedSegment segment, Set<SharedSegment> parsed, Set<Integer> nodesOnDomain) {
        parsed.add(segment);
MAINLOOP:        for (SharedSegment parsing: segments) {
            if (parsed.contains(parsing))
                continue;
            Set<Integer> nodesOnSegment = parsing.getBridgeIdsOnSegment();
            for (Integer nodeid: nodesOnSegment) {
                if (nodesOnDomain.contains(nodeid)) {
                    nodesOnDomain.addAll(nodesOnSegment);
                    getNodesOnDomainSet(segments, parsing, parsed, nodesOnDomain);
                    break MAINLOOP;
                }
            }
        }
        return nodesOnDomain;
    }

    @Override
    public synchronized void delete(BroadcastDomain domain) {
        m_domains.remove(domain);
    }

    @Override
    public synchronized BroadcastDomain get(int nodeid) {
        for (BroadcastDomain domain: m_domains) {
            if (domain.containBridgeId(nodeid))
                return domain;
        }
        return null;
    }

    public synchronized Set<BroadcastDomain> getAll() {
        return m_domains;
    }

    @Override
    public synchronized void clean() {
        Set<BroadcastDomain> domains = new HashSet<BroadcastDomain>();
        for (BroadcastDomain domain: m_domains) {
            if (domain.isEmpty())
                continue;
            domains.add(domain);
        }
        m_domains = domains;
    }
    

}
