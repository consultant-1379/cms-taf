/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.NETSIM_CMD_EXEC_SUCCESS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.MECONTEXT;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.DeletemoCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xmurran
 */
@Operator(context = { Context.CLI })
public class DeleteMoCliOperator implements DeleteMoOperator {

    private final NetSimCommandHandler netSimCmdHandler = NetSimCommandHandler.getInstance();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean deleteMo(final Fdn moFdn) {

        logger.info("Deleting MO : " + moFdn);
        final String meContextFdn = moFdn.getNameOfFdnPart(MECONTEXT);
        final NetworkElement networkElement = netSimCmdHandler.getAllStartedNEs().get(meContextFdn);
        final DeletemoCommand deleteCmd = NetSimCommands.deletemo(moFdn.getLdn());
        final NetSimResult cmdResult = networkElement.exec(deleteCmd);

        return checkCmdResult(cmdResult);
    }

    @Override
    public void deleteMos(final List<Fdn> moFdns) {
        for (final Fdn moFdn : moFdns) {
            deleteMo(moFdn);
        }
    }

    private boolean checkCmdResult(final NetSimResult cmdResult) {

        return cmdResult.getRawOutput().trim().endsWith(NETSIM_CMD_EXEC_SUCCESS);
    }
}