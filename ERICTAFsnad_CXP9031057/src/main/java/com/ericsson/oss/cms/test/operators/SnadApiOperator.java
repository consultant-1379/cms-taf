package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.CifLogMessages.RECOVERED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.CifLogMessages.SLEEP;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.CONSISTENT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cif.logs.domain.CIFLogType;
import com.ericsson.oss.cif.logs.domain.CriteriaBuilder;
import com.ericsson.oss.cif.logs.domain.CriteriaBuilder.Qualifier;
import com.ericsson.oss.cms.test.util.GetHostUsers;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.SimpleFilterBuilder;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.cshandler.model.Filter;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.smhandler.SmtoolHandler;

/**
 * Base implementation of the {@link SnadOperator} API.
 * 
 * @author edunsea
 */
@Operator(context = { Context.API, Context.CLI })
@Singleton
public class SnadApiOperator implements SnadOperator {

    @Inject
    private MoCliOperator moCliOperator;

    private final Logger logger = Logger.getLogger(this.getClass());

    private static final String CONSISTENTENCY_STATE = "Consistency state:";

    private static final String ZERO_ROWS_AFFECTED = "(0 rows affected)";

    private final SmtoolHandler smtool = new SmtoolHandler(HostGroup.getOssmaster());

    private final CLICommandHelper handler = GetHostUsers.getCLICommandHelper();

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private static final String SNAD_MC = "cms_snad_reg";

    private static final String MO_FDN = "MoFDN";

    private static final String ACTION_GET_MASTER = "getMaster";

    private static final String ACTION_GET_PROXY = "getProxy";

    private static final String ACTION_GET_MASTER_FOR_PROXY = "getMasterForProxy";

    private static final String ACTION_GET_PROXIES_FOR_MASTER = "getProxiesForMaster";

    private static final String ACTION_GET_CLUSTER_MEMBERS = "getClusterMembers";
   
    private static final String ACTION_CHECK = "check";

    @Inject
    CIFLogCliOperator cifLogCliOperator;

    @Override
    public String getMCStatus() {
        return smtool.getMCStatus(SNAD_MC);
    }

    @Override
    public String getMaster(final Fdn fdn) {
        return smtool.action(SNAD_MC, ACTION_GET_MASTER, MO_FDN, fdn.getFdn());
    }

    @Override
    public String getProxy(final Fdn fdn) {
        return smtool.action(SNAD_MC, ACTION_GET_PROXY, MO_FDN, fdn.getFdn());
    }

    @Override
    public String getMasterState(final Fdn fdn) {
        final String stdout = this.getMaster(fdn);
        return parseMoState(stdout);
    }

    @Override
    public String getProxyState(final Fdn fdn) {
        final String stdout = this.getProxy(fdn);
        return parseMoState(stdout);
    }

    private String parseMoState(final String actionOutput) {
        if (actionOutput.contains(CONSISTENTENCY_STATE)) {
            return parseMoStateFromNewActionOutput(actionOutput);
        } else {
            return parseMoStateFromOldActionOutput(actionOutput);
        }
    }

    private String parseMoStateFromNewActionOutput(final String actionOutput) {
        final String consistencyState = actionOutput.split("\\n")[2].trim();
        final String state = consistencyState.split(": ")[1];

        return state.toUpperCase();
    }

    private String parseMoStateFromOldActionOutput(final String actionOutput) {
        final String state = actionOutput.split("\\n")[0].trim();
        return state;
    }

    @Override
    public Fdn getMasterForProxy(final Fdn fdn) {
        final String stdout = smtool.action(SNAD_MC, ACTION_GET_MASTER_FOR_PROXY, MO_FDN, fdn.getFdn());
        final List<Fdn> masters = splitNewLineAndStripBrackets(stdout);
        return masters.isEmpty() ? null : masters.get(0);
    }

    @Override
    public List<Fdn> getProxiesForMaster(final Fdn fdn) {
        final String stdout = smtool.action(SNAD_MC, ACTION_GET_PROXIES_FOR_MASTER, MO_FDN, fdn.getFdn());
        return splitNewLineAndStripBrackets(stdout);
    }

    @Override
    public List<Fdn> getClusterMembers(final Fdn fdn) {
        final String stdout = smtool.action(SNAD_MC, ACTION_GET_CLUSTER_MEMBERS, MO_FDN, fdn.getFdn());
        return splitNewLine(stdout);
    }

    @Override
    public boolean areProxiesConsistent(final List<Fdn> proxyMos) {
        for (final Fdn proxyMo : proxyMos) {
            final String proxyState = getProxyState(proxyMo);

            if (!proxyState.equals(CONSISTENT)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Splits the given String around new line from output of Snad smtool actions.
     * 
     * @param stdout
     *        Output from smtool action.
     * @return List of FDNs.
     */
    private List<Fdn> splitNewLine(final String stdout) {
        final String[] lines = stdout.split("\\n");
        final List<Fdn> fdns = new ArrayList<Fdn>();

        for (final String line : lines) {
            fdns.add(new Fdn(line));
        }
        return fdns;
    }

    /**
     * Splits the given String around new line, then strips opening and closing square brackets from output of Snad smtool actions.
     * 
     * @param stdout
     *        Output from smtool action.
     * @return List of FDNs without leading or trailing brackets.
     */
    private List<Fdn> splitNewLineAndStripBrackets(final String stdout) {
        final String[] lines = stdout.trim().split("\\n");
        final List<Fdn> fdns = new ArrayList<Fdn>();

        for (String line : lines) {

            line = StringUtils.strip(line.trim(), "[]");
            if (!line.isEmpty()) {
                fdns.add(new Fdn(line));
            }
        }
        return fdns;
    }

    @Override
    public void waitForCCToComplete() {
        final String getLongSleepCmd = buildGetLongSleepLogCommand();

        String stdout = "";
        do {
            waitFor(30000);
            stdout = handler.simpleExec(getLongSleepCmd);
        } while (stdout.contains(ZERO_ROWS_AFFECTED));
    }

    private String buildGetLongSleepLogCommand() {
        final String time = handler.simpleExec("date +'%h %d %Y %I:%M%p'");

        return "isql -Usa -Psybase11 -Dlvlogdb -w2240 -s#<< EOF\n" + "SELECT * FROM Logs WHERE time_stamp>'" + time + "' AND "
                + "application_name='cms_snad_reg' AND " + "additional_info LIKE 'Consistency Check going for long sleep 3600sec'\n" + "go\n" + "EOF\n";
    }

    @Override
    public void waitFor(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (final InterruptedException e) {
        }
    }

    @Override
    public boolean waitForSleep(final long startTime, final long timeOut) {

        final CriteriaBuilder criteria = new CriteriaBuilder("CMS")
                .withApplication_name(Qualifier.EQUALS, SNAD_MC)
                .withAdditional_info(Qualifier.CONTAINS, SLEEP)
                .withTimeRange(startTime, startTime + timeOut);

        final List<CIFLogItem> cifLogEntries = cifLogCliOperator.waitForCifLogs(criteria, timeOut);

        return !cifLogEntries.isEmpty();
    }

    @Override
    public Boolean hasSubNetworkRecovered(final String rootMO, final long startTime) {
        final Set<CIFLogType> logtypes = new HashSet<CIFLogType>();
        logtypes.add(CIFLogType.SYSTEM_EVENT_LOG);
        final Set<String> nes = new HashSet<String>();
        nes.add(rootMO);
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder("CMS")
                .withApplication_name(Qualifier.CONTAINS, SNAD_MC)
                .withLogTypes(logtypes)
                .withMutlipleResource(Qualifier.CONTAINS, nes)
                .withTimeRange(startTime, System.currentTimeMillis())
                .withAdditional_info(Qualifier.CONTAINS, rootMO + RECOVERED);
        return !cifLogCliOperator.getCifLogs(criteriaBuilder).isEmpty();

    }

    public Fdn getMoFdnWithAttributeValue(final String moName, final String attName, final String attValue) {
        final Filter moFilter = SimpleFilterBuilder.builder().attr(attName).equalTo(attValue).build();
        final List<Fdn> listOfMoFdns = csHandler.getByType(moName, moFilter);

        if (listOfMoFdns.isEmpty()) {
            return null;
        }
        return listOfMoFdns.get(0);
    }

    @Override
    public boolean isConflictExist(final Fdn fdn) {
        boolean isConflict = false;
        final String conflictValue = csHandler.getAttributeValue(fdn, "pciConflict").trim();
        if (!(conflictValue.isEmpty() || conflictValue.equals("0") || conflictValue.equals("1") || conflictValue.equals("<UndefinedValue>"))) {
            isConflict = true;
        }

        return isConflict;

    }

    @Override
    public List<Fdn> getZeroConflictMasterProxyFromSyncedNodes(final List<Fdn> activeNodeFdns, final String moType, final int proxyCount) {
        List<Fdn> activeMoFdns = new ArrayList<Fdn>();
        List<Fdn> zeroConflictMasterProxy = new ArrayList<Fdn>();
        for (final Fdn activeNodeFdn : activeNodeFdns) {
            activeMoFdns = moCliOperator.getChildrenMoFdns(activeNodeFdn, moType);
            logger.info("Find a Master and " + proxyCount + " Proxy MOs with zero conflict from node " + activeNodeFdn);
            zeroConflictMasterProxy = getNoConflictMasterAndProxy(activeMoFdns, proxyCount);
            if (zeroConflictMasterProxy != null && !zeroConflictMasterProxy.isEmpty()) {
                return zeroConflictMasterProxy;
            }
            logger.info("No Master and Proxy found with zero conflicts from node [" + activeNodeFdn + "]");
        }
        logger.info("Failed to find Master Mo and Proxy Mo with zero conflicts from all the synced nodes" + zeroConflictMasterProxy);
        return null;
    }

    @Override
    public List<Fdn> getNoConflictMasterAndProxy(final List<Fdn> activeMoFdns, final int proxyCount) {

        final List<Fdn> noConflictMasterProxy = new ArrayList<Fdn>();
        final List<Fdn> noConflictProxy = new ArrayList<Fdn>();
        boolean master_conflictValue, proxy_conflictValue;

        for (final Fdn masterMo : activeMoFdns) {
            master_conflictValue = isConflictExist(masterMo);
            if (!master_conflictValue) {
                logger.info("Master found with zero conflict [" + masterMo + "]");
                final List<Fdn> proxyMos = getProxiesForMaster(masterMo);
                if (proxyMos.size() >= proxyCount) {
                    for (int i = 0; i < proxyMos.size(); i++) {
                        proxy_conflictValue = isConflictExist(proxyMos.get(i));
                        if (!proxy_conflictValue) {
                            noConflictProxy.add(proxyMos.get(i));
                            if (noConflictProxy.size() == proxyCount) {
                                // Adding Master in the first index
                                noConflictMasterProxy.add(masterMo);
                                // Adding proxies from the second index
                                noConflictMasterProxy.addAll(noConflictProxy);
                                logger.info("Expected Proxy/Proxies found from Master [" + noConflictProxy.toString() + "]");
                                return noConflictMasterProxy;
                            }
                        }
                    }
                }
            }
            if (!noConflictProxy.isEmpty()) {
                noConflictProxy.removeAll(noConflictProxy);
            }
            logger.info("Expected Proxy/Proxies NOT found from Master [" + masterMo + "]");
        }
        return null;
    }

    @Override
    public Map<String, String> getPlmIdentity(final Fdn masterMoFdn, final Fdn activeNodeFdn) {

        final Map<String, String> plmIdentity = new HashMap<String, String>();
        plmIdentity.put("cellId", csHandler.getAttributeValue(masterMoFdn, "cellId"));

        final Fdn eNodeBFunMOFdn = moCliOperator.getChildMoFdn(activeNodeFdn, "ENodeBFunction");
        plmIdentity.put("eNBId", csHandler.getAttributeValue(eNodeBFunMOFdn, "eNBId"));

        final String eNodeBPlmnId = csHandler.getAttributeValue(eNodeBFunMOFdn, "eNodeBPlmnId");

        plmIdentity.put("mcc", eNodeBPlmnId.substring(29, 32));
        plmIdentity.put("mnc", eNodeBPlmnId.substring(42, 44));
        plmIdentity.put("mncLength", eNodeBPlmnId.substring(60, 61));

        return plmIdentity;

    }

    @Override
    public String setPciCellValues(final Map<String, String> plmIdentity, final int cellId, final int inputCount) {

        String pciConflict = "'";
        final String cell_Id[] = new String[inputCount];
        final String conflict_values[] = new String[inputCount];

        for (int i = 0; i < inputCount; i++) {
            cell_Id[i] = Integer.toString(cellId + i);
            conflict_values[i] = "enbId:" + plmIdentity.get("eNBId") + ",cellId:" + cell_Id[i] + ",mcc:" + plmIdentity.get("mcc") + ",mnc:"
                    + plmIdentity.get("mnc") + ",mncLength:" + plmIdentity.get("mncLength");
            pciConflict += conflict_values[i] + " ";
        }
        pciConflict = pciConflict.trim() + "'";
        return pciConflict;

    }

    @Override
    public String setPciDetCellValues(final Map<String, String> plmIdentity, final int cellId, final int inputCount) {

        String pciConflict = "'";
        final String conflict_values[] = new String[inputCount];

        for (int i = 0; i < inputCount; i++) {
            conflict_values[i] = "enbId:" + plmIdentity.get("eNBId") + ",cellId:" + cellId + ",mcc:" + plmIdentity.get("mcc") + ",mnc:"
                    + plmIdentity.get("mnc") + ",mncLength:" + plmIdentity.get("mncLength");
            pciConflict += conflict_values[i] + " ";
        }
        pciConflict = pciConflict.trim() + "'";
        return pciConflict;

    }

    @Override
    public String setPciConflictValues(final String... conflictVals) {
        String conflictValue = "'";
        for (final String conflictVal : conflictVals) {
            conflictValue += conflictVal + " ";
        }
        conflictValue = conflictValue.trim() + "'";

        return conflictValue;

    }
   
   @Override
    public void performCheckonFDN(final Fdn moFdn) {
        smtool.action(SNAD_MC, ACTION_CHECK, MO_FDN, moFdn.getFdn());
    }

}
