package com.ericsson.oss.cms.test.operators;

/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.MECONTEXT;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.CreatemoCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.cms.test.util.Lists;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

@Operator(context = Context.CLI)
public class ProxyCRUDCLIOperator implements ProxyCRUDOperator {

    private static final Logger logger = Logger.getLogger(ProxyCRUDCLIOperator.class);

    private final CSHandler cs = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final NetSimCommandHandler netsim = NetSimCommandHandler.getInstance();

    @Override
    public Fdn createProxy(final String proxyType, final String parentType) {

        final Fdn parent = this.getParent(parentType);
        logger.debug("Chosen parent: " + parent);

        final Fdn proxyFdn = parent.newChild(proxyType, "TAF");
        logger.debug("New proxy fdn: " + proxyFdn);

        createMoUsingNetsim(proxyType, parent);
        return proxyFdn;
    }

    private Fdn getParent(final String parentType) {
        final List<Fdn> possibleParents = this.cs.getByType(parentType);
        for (final Iterator<Fdn> it = possibleParents.iterator(); it.hasNext();) {
            final Fdn fdn = it.next();
            if (!fdn.hasParent(MECONTEXT)) {
                it.remove();
            }
        }
        return Lists.randomItem(possibleParents);
    }

    private void createMoUsingNetsim(final String proxyType, final Fdn parent) {
        final CreatemoCommand createCmd = NetSimCommands.createmo(parent.getLdn(), proxyType, "TAF", 1);

        final NeGroup startedNodes = netsim.getAllStartedNEs();
        final NetworkElement parentNe = startedNodes.get(parent.getNameOfFdnPart(MECONTEXT));

        final NetSimResult cmdResult = parentNe.exec(createCmd);
        logger.debug(cmdResult.getRawOutput());
    }
}