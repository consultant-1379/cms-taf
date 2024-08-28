/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.MECONTEXT;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.KertayleCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author ecolhar
 */
public class NetsimKertayleCliOperator implements NetsimKertayleOperator {

    @Inject
    private NodeCliOperator nodeCliOperator;

    private static final String NETSIMUSERNAME = "netsim";

    private static final String NETSIMPWORD = "netsim";

    private static final User NETSIMUSER = new User(NETSIMUSERNAME, NETSIMPWORD, UserType.OPER);

    private final RemoteFileHandler remoteFileHandler = new RemoteFileHandler(HostGroup.getAllNetsims().get(0), NETSIMUSER);

    private static final String SCRIPTFILENAME = "kertayleScript";

    private static final String REMOTEPATH = "/usr/tmp/netsimscripts/" + SCRIPTFILENAME;

    private static final String DEFAULT_TEMPFILE_PATH = System.getProperty("java.io.tmpdir");

    @Override
    public boolean createMo(final Fdn moFdn, final List<String> kertayleAttrs) throws IOException {

        final File kertayleScript = createMoKertayleScript(moFdn, kertayleAttrs);

        final Fdn parentMoFdn = moFdn.getParentFdn();
        final NetworkElement networkElement = nodeCliOperator.getNetworkElement(parentMoFdn.getNameOfFdnPart(MECONTEXT));

        return runScript(kertayleScript, networkElement);
    }

    private File createMoKertayleScript(final Fdn moFdn, final List<String> kertayleAttrs) throws IOException {

        final File kertayleScript = File.createTempFile(SCRIPTFILENAME, "", new File(DEFAULT_TEMPFILE_PATH));
        kertayleScript.deleteOnExit();

        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(kertayleScript))) {

            final List<String> CREATEHEADER = Arrays.asList("CREATE", "(");
            final String CREATEFOOTER = ")";
            final String INDENT = "     ";
            final List<String> lines = new ArrayList<String>(CREATEHEADER);

            final String parentLdn = moFdn.getParentFdn().getLdn().toString();
            lines.add(INDENT + "parent \"" + parentLdn + "\"");
            final String identity = moFdn.getMoName();
            lines.add(INDENT + "identity " + identity);
            final String moType = moFdn.getType();
            lines.add(INDENT + "moType " + moType);
            final int noOfAttributes = kertayleAttrs.size();
            lines.add(INDENT + "nrOfAttributes " + noOfAttributes);
            for (final String attribute : kertayleAttrs) {
                lines.add(INDENT + attribute);
            }
            lines.add(CREATEFOOTER);

            for (final String line : lines) {
                writer.write(line + '\n');
            }
        }
        return kertayleScript;
    }

    private boolean runScript(final File kertayleScript, final NetworkElement networkElement) {

        remoteFileHandler.copyLocalFileToRemote(kertayleScript.getName(), REMOTEPATH, DEFAULT_TEMPFILE_PATH);

        final KertayleCommand command = NetSimCommands.kertayle();
        command.setFile(REMOTEPATH);
        final NetSimResult result = networkElement.exec(command);

        remoteFileHandler.deleteRemoteFile(REMOTEPATH);

        return nodeCliOperator.checkCmdResult(result);
    }
}
