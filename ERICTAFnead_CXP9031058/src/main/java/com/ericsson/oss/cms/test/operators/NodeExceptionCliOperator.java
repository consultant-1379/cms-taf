/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.Map;

import com.ericsson.cifwk.taf.handlers.netsim.*;
import com.ericsson.cifwk.taf.handlers.netsim.commands.*;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;

/**
 * @author eeitky
 */
public class NodeExceptionCliOperator implements NodeExceptionOperator {

    private final NetSimCommandHandler netsimCmdHandler = NetSimCommandHandler.getInstance();

    @Override
    public boolean createIDLException(final NetworkElement networkElement, final String exceptionName, final String idlcommand, final String condition) {
        final String neType = networkElement.getType();
        final NewExceptionCommand newExceptionCmd = NetSimCommands.newException(exceptionName);
        final SetLanguageCommand setLanguageCmd = NetSimCommands.setLanguage("netsimidl");
        final SetPriorityCommand setPriorityCmd = NetSimCommands.setPriority(1);
        final SetNetypeCommand setNetypeCmd = NetSimCommands.setNetype(neType);
        final SetCommandsCommand setCommandsCmd = NetSimCommands.setCommands(idlcommand);
        final SetConditionCommand setConditionCmd = NetSimCommands.setCondition(condition);
        final SetActionCommand setActionCmd = NetSimCommands.setAction("idl-exception",
                "[{exception_parameters,\"lib=configuration_K_lib|exception=ProcessingFailure|reason=CreatedByTaf\"}]");
        final SetSaveCommand setSaveCmd = NetSimCommands.setSave();
        final Map<NetSimContext, NetSimResult> netsimResultmap = netsimCmdHandler.exec(getOpenCommand(networkElement), newExceptionCmd, setLanguageCmd,
                setPriorityCmd, setNetypeCmd, setCommandsCmd, setConditionCmd, setActionCmd, setSaveCmd);
        return isNetsimCommandResponseOK(netsimResultmap);
    }

    @Override
    public boolean deleteException(final NetworkElement networkElement, final String exceptionName) {
        final SelectCommand selectCmd = NetSimCommands.select(exceptionName);
        final DeleteCommand deleteCmd = NetSimCommands.delete();
        final Map<NetSimContext, NetSimResult> netsimResultmap = netsimCmdHandler.exec(getOpenCommand(networkElement), selectCmd, deleteCmd);
        return isNetsimCommandResponseOK(netsimResultmap);
    }

    @Override
    public boolean activateException(final NetworkElement networkElement, final String exceptionName) {
        final SelectnocallbackCommand selectnocallbackCmd = NetSimCommands.selectnocallback(networkElement.getName());
        final SelectCommand selectCmd = NetSimCommands.select(exceptionName);
        final ExceptionCommand exceptionCmd = NetSimCommands.exception("on");
        final Map<NetSimContext, NetSimResult> netsimResultmap = netsimCmdHandler.exec(getOpenCommand(networkElement), selectnocallbackCmd, selectCmd,
                exceptionCmd);
        return isNetsimCommandResponseOK(netsimResultmap);
    }

    @Override
    public boolean deactivateException(final NetworkElement networkElement, final String exceptionName) {
        final SelectnocallbackCommand selectnocallbackCmd = NetSimCommands.selectnocallback(networkElement.getName());
        final SelectCommand selectCmd = NetSimCommands.select(exceptionName);
        final ExceptionCommand exceptionCmd = NetSimCommands.exception("off");
        final Map<NetSimContext, NetSimResult> netsimResultmap = netsimCmdHandler.exec(getOpenCommand(networkElement), selectnocallbackCmd, selectCmd,
                exceptionCmd);
        return isNetsimCommandResponseOK(netsimResultmap);
    }

    private OpenCommand getOpenCommand(final NetworkElement networkElement) {
        final String simName = networkElement.getSimulationName();
        return NetSimCommands.open(simName);
    }

    private boolean isNetsimCommandResponseOK(final Map<NetSimContext, NetSimResult> netsimResultmap) {
        for (final NetSimContext netsimContext : netsimResultmap.keySet()) {
            final NetSimResult netsimResult = netsimResultmap.get(netsimContext);
            final CommandOutput[] commandOutput = netsimResult.getOutput();
            for (final CommandOutput output : commandOutput) {
                if (!output.getRawOutput().trim().endsWith("OK")) {
                    return false;
                }
            }
        }
        return true;
    }

}
