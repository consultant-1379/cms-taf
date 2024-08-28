/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.CONSISTENT;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.TRANSIENT_INCONSISTENT;
import static java.util.Collections.emptyList;

import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.SimpleFilterBuilder;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.cshandler.model.Filter;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author egokdag
 */
public class GeranFrequencyCLIOperator implements GeranFrequencyOperator {

    private static final String GERAN_FREQ_GROUP = "GeranFreqGroup";

    private static final String GERA_NETWORK = "GeraNetwork";

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private SnadApiOperator snadOperator;

    @Inject
    private CreateMoCliOperator createMoCliOperator;

    @Inject
    private SetMoCliOperator setMoCliOperator;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    @Override
    public Fdn getSharedGeranFreqGroupFdn(final String nodeType, final String freqGroupType, final String parentType, final String proxyType) {

        final List<Fdn> syncedNodes = nodeCliOperator.getPercentageOfSyncedNodes(nodeType, 100, null);
        final Filter geranFrequencyChildFilter = SimpleFilterBuilder.builder().type(proxyType).build();
        final Filter geranFreqGroupChildFilter = SimpleFilterBuilder.builder().type(freqGroupType).build();

        for (final Fdn syncedNode : syncedNodes) {

            final List<Fdn> geranFreqGroupFdns = getGeranFreqGroups(parentType, geranFrequencyChildFilter, geranFreqGroupChildFilter, syncedNode);

            for (final Fdn geranFreqGroupFdn : geranFreqGroupFdns) {
                final Fdn masterExternalGsmFreqGroup = snadOperator.getMasterForProxy(geranFreqGroupFdn);
                if (isConsistent(masterExternalGsmFreqGroup) && hasMultipleProxies(masterExternalGsmFreqGroup)) {
                    return geranFreqGroupFdn;
                }
            }

        }

        return null;
    }

    private List<Fdn> getGeranFreqGroups(
            final String parentType,
            final Filter geranFrequencyChildFilter,
            final Filter geranFreqGroupChildFilter,
            final Fdn syncedNode) {

        switch (parentType) {
            case GERA_NETWORK:
                return getNewModelGeranFreqGroups(syncedNode, geranFrequencyChildFilter, geranFreqGroupChildFilter);

            case GERAN_FREQ_GROUP:
                return getOldModelGeranFreqGroups(syncedNode, geranFrequencyChildFilter, geranFreqGroupChildFilter);

            default:
                return emptyList();
        }
    }

    private List<Fdn> getNewModelGeranFreqGroups(final Fdn syncedNode, final Filter geranFrequencyChildFilter, final Filter geranFreqGroupChildFilter) {
        final String newModelGeranFreqLevel = "4";
        final List<Fdn> newGeranFrequencyList = csHandler.getChildMos(syncedNode, newModelGeranFreqLevel, geranFrequencyChildFilter);

        if (!newGeranFrequencyList.isEmpty()) {
            final List<Fdn> geranFreqGroupFdns = csHandler.getChildMos(syncedNode, geranFreqGroupChildFilter);
            return geranFreqGroupFdns;
        }

        return emptyList();
    }

    private List<Fdn> getOldModelGeranFreqGroups(final Fdn syncedNode, final Filter geranFrequencyChildFilter, final Filter geranFreqGroupChildFilter) {
        final String oldModelGeranFreqLevel = "5";

        if (getNewModelGeranFreqGroups(syncedNode, geranFrequencyChildFilter, geranFreqGroupChildFilter).isEmpty()) {
            final List<Fdn> oldGeranFrequencyList = csHandler.getChildMos(syncedNode, oldModelGeranFreqLevel, geranFrequencyChildFilter);
            if (!oldGeranFrequencyList.isEmpty()) {
                final List<Fdn> geranFreqGroupFdns = csHandler.getChildMos(syncedNode, geranFreqGroupChildFilter);
                return geranFreqGroupFdns;
            }
        }

        return emptyList();
    }

    private boolean hasMultipleProxies(final Fdn externalGsmFreqGroup) {
        return snadOperator.getProxiesForMaster(externalGsmFreqGroup).size() > 1;
    }

    private boolean isConsistent(final Fdn externalGsmFreqGroup) {
        return snadOperator.getMasterState(externalGsmFreqGroup).equals(CONSISTENT);
    }

    @Override
    public Fdn buildMoFdn(final String parentType, final Fdn sharedGeranFreqGroupFdn, final String proxyType, final String testId) {

        switch (parentType) {
            case GERA_NETWORK:
                return createMoCliOperator.buildMoFdn(sharedGeranFreqGroupFdn.getParentFdn(), proxyType, testId);

            case GERAN_FREQ_GROUP:
                return createMoCliOperator.buildMoFdn(sharedGeranFreqGroupFdn, proxyType, testId);

            default:
                return null;
        }
    }

    @Override
    public String[] addMoRefData(final String parentType, final String[] proxyAttrNames, final String proxyGeranGroupRefAttr) {

        switch (parentType) {
            case GERA_NETWORK:
                return setMoCliOperator.addMoRefData(proxyAttrNames, proxyGeranGroupRefAttr);

            case GERAN_FREQ_GROUP:
                return proxyAttrNames;

            default:
                return null;

        }
    }

    @Override
    public String[] addMoRefData(final String parentType, final String[] proxyAttrValues, final String ldn, final String proxyGeranGroupRefAttrType) {

        switch (parentType) {
            case GERA_NETWORK:
                return setMoCliOperator.addMoRefData(proxyAttrValues, ldn, proxyGeranGroupRefAttrType);

            case GERAN_FREQ_GROUP:
                return proxyAttrValues;

            default:
                return null;

        }
    }

    @Override
    public String getProxyStateAfterCC(final Fdn proxyMoFdn) {

        String proxyState = snadOperator.getProxyState(proxyMoFdn);

        while (proxyState.equals(TRANSIENT_INCONSISTENT)) {
            proxyState = snadOperator.getProxyState(proxyMoFdn);
        }

        return proxyState;
    }
}