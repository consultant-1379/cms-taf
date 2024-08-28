/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.MECONTEXT;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.commands.SendcsnotifCommand.Type;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author emacraj
 */
public class NodeOverFlowNotificationCliOperator implements NodeOverFlowNotificationOperator {

    @Inject
    NodeCliOperator nodeCliOperator;

    @Override
    public boolean generateNodeOverflow(final Fdn activeNodeFdn) {

        final NetworkElement networkElement = nodeCliOperator.getNetworkElement(activeNodeFdn.getNameOfFdnPart(MECONTEXT));
        final NetSimResult cmdResult = networkElement.exec(NetSimCommands.sendcsnotif().setType(Type.OVERFLOWTYPE));
        return nodeCliOperator.checkCmdResult(cmdResult);
    }
}