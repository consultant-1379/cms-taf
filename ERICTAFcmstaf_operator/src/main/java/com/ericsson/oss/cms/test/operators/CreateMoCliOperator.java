/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.MO_NAME_PREFIX;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.MECONTEXT;
import static com.ericsson.oss.cms.test.util.AttributeValueConverter.convertDBToNetsimValue;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.CreatemoCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xrajnka
 */
@Operator(context = Context.CLI)
public class CreateMoCliOperator implements CreateMoOperator {

    @Inject
    private SetMoCliOperator setMoCliOperator;

    @Inject
    private NodeCliOperator nodeCliOperator;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Fdn buildMoFdn(final Fdn parentMoFdn, final String moType, final String moName) {
        final String newMoName = MO_NAME_PREFIX + moName;
        return parentMoFdn.newChild(moType, newMoName);
    }

    @Override
    public boolean createMo(final Fdn moFdn, final List<Attribute> attributes) {
        final String[] attributeNames = new String[attributes.size()];
        final String[] attributeValues = new String[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            attributeNames[i] = attributes.get(i).getName();
            attributeValues[i] = convertDBToNetsimValue(attributes.get(i));
        }
        return createMo(moFdn, attributeNames, attributeValues);
    }

    @Override
    public boolean createMo(final Fdn moFdn, final String[] attributes, final String[] attributeValues) {

        final Fdn parentMoFdn = moFdn.getParentFdn();
        final String moType = moFdn.getType();
        final String moName = moFdn.getMoName();

        final String attrString = setMoCliOperator.buildAttributeValuePair(attributes, attributeValues);

        logger.info("Creating MO with the following attributes : " + attrString);

        final NetSimResult cmdResult = createMoInNetsim(parentMoFdn, moType, moName, attrString);

        if (nodeCliOperator.checkCmdResult(cmdResult)) {
            logger.info("Create MO Operation is successful, new MO Fdn : " + moFdn);
            return true;
        } else {
            logger.info("Failed to create MO of type " + moType + " on NETSIM node - " + parentMoFdn.getNameOfFdnPart(MECONTEXT));
            logger.info("NETSIM Command Result: " + cmdResult);
            return false;
        }
    }

    private NetSimResult createMoInNetsim(final Fdn parentMoFdn, final String moType, final String moName, final String attrString) {
        final CreatemoCommand createCmd = NetSimCommands.createmo(parentMoFdn.getLdn(), moType, moName, 1);
        createCmd.setAttributes(attrString);
        final NetworkElement networkElement = nodeCliOperator.getNetworkElement(parentMoFdn.getNameOfFdnPart(MECONTEXT));
        return networkElement.exec(createCmd);
    }
}