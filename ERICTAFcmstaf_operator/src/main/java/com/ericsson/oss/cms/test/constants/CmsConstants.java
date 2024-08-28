/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.constants;

/**
 * @author xrajnka
 */
public class CmsConstants {

    public static final String MO_NAME_PREFIX = "TAF_";

    // NETSIM Constants
    public static final String NETSIM_CMD_EXEC_SUCCESS = "OK";

    public class MeContext {

        // MECONTEXT attribute Constants
        public static final String MECONTEXT = "MeContext";

        public static final String GENERATION_COUNTER = "generationCounter";

        public static final String CONN_STATUS = "connectionStatus";

        public static final int CONN_STATUS_CONNECTED = 2;

        public static final int CONN_STATUS_DISCONNECTED = 3;

        public static final int CONN_STATUS_NEVERCONNECTED = 1;

        public static final String SYNCH_STATUS = "mirrorMIBsynchStatus";

        public static final int SYNCH_STATUS_SYNCHRONIZED = 3;

        public static final int SYNCH_STATUS_UNSYNCHRONIZED = 4;

        public static final String NE_TYPE = "neType";

        public static final String IPADDRESS = "ipAddress";

    }

    public class Snad {

        public static final String SNAD_MC = "cms_snad_reg";

        // PED parameters
        public static final String LTE_DISCONNECTED_NODE_TIMEOUT_DURATION = "lteDisconnectedNodeTimeoutDuration";

        // Log Constants
        public static final String LOG_FILE_PATH = "/var/opt/ericsson/nms_umts_cms_snad_reg";

        // MO constants
        public static final String RESERVED_BY = "reservedBy";

        // AutoFix constants
        public static final String MISSINGMASTER_AUTOFIX_ON = "NetworkInformation_MissingMaster_RedundantProxy_TrafficInformation";

        public static final long MAX_TIME_TO_READ_CIF_LOGS = 180000;

        public static final long COLDRESTART_WAIT_TIME = 180 * 1000;

        // UtranRelation Constants
        public static final String ADJACENT_CELL = "adjacentCell";

        public static final String NODE_RELATION_TYPE = "nodeRelationType";

        public static final String INTER_UTRAN_RELATION = "1";

        public class Cache {

            // Commands
            public static final String COMMAND_CACHE_REVIEW = "/opt/ericsson/nms_umts_cms_lib_com/bin/run_moscript /opt/ericsson/nms_umts_cms_lib_com/info/XReviewCache.py";

            public static final String COMMAND_CONSISTENCY_CHECK = "/opt/ericsson/nms_umts_cms_lib_com/bin/run_moscript /opt/ericsson/nms_umts_cms_lib_com/info/ResumeChecker.py";

            // XReviewCache headers
            public static final String CACHE_SECTION_DELIM = "=========================";

            public static final String CACHE_SECTION_CLUSTER = "CLUSTER MOs";

            public static final String CACHE_SECTION_MASTER = "MASTER MOs";

            public static final String CACHE_SECTION_PROXY = "PROXY MOs";

            public static final String CACHE_SECTION_UNMANAGED = "UNMANAGED MOs";

            public static final String CACHE_SECTION_PROXY_UNSYNC = "PROXY MOs IN UnsyncCache";

            public static final String CACHE_SECTION_RBSLOCALCELL = "Rbs LocalCells MOs";

            // Cache File Constants
            public static final String CACHE_FIELD_MO_NAME = "MO Name: ";

            public static final String CACHE_FIELD_CONSISTENCY_STATE = "Consistency state: ";

            // Node State
            public static final String NODE_UNSYNCHRONIZED = "NodeUnsynchronized";

            // MO State
            public static final String CONSISTENT = "CONSISTENT";

            public static final String REDUNDANT_PROXY = "REDUNDANT_PROXY";

            public static final String TRANSIENT_INCONSISTENT = "TRANSIENT_INCONSISTENT";

        }
    }

    public class Nead {

        public static final String AUX_PLUGIN_UNIT_FOR_MIXEDMODE_EPIC_PED = "auxPlugInUnitForMixedModeRadio";

        public static final String BLACKLIST_FILTER_REQUIRED_NEAD = "blacklistFilterRequired_NEAD";
        
        public static final String NEAD_MC = "cms_nead_seg";

        public static final String NEAD_MIB_ADAPTER = "Seg_masterservice_NEAD";

        // Log Constants
        public final static String NOTIFICATION_LOG_FILE_PATH = "/var/opt/ericsson/nms_umts_cms_nead_seg/";

        public final static String NOTIFICATION_LOG_FILE_PATTERN = "Notifications.log.*";

        public final static String EXCEPTION_FILE_PATTERN = "exceptionLogs.log.*";

        // Notification Types
        public final static String NOTIFICATION_CREATE = ": CN";

        public final static String NOTIFICATION_AVC = "AVCN";

        public final static String NOTIFICATION_DELETE = ": DN";

        public final static String NOTIFICATION_SEQUENCE_DELTA = "SDN";

        public final static String NOTIFICATION_SDN_ADD = "ADD";

        public final static String NOTIFICATION_SDN_REMOVE = "REMOVE";

        public final static String OVERFLOW_EXCEPTION = "An overflow notification has being sent from the NODE";

        // Other Constants
        public static final int MAX_TIME_TO_READ_GEN_COUNTER = 60;

        public static final String XML_REMOTE_PATH = "/var/opt/ericsson/arne/";

        public static final String IMPORT_SCRIPT_PATH = "/opt/ericsson/arne/bin/import.sh";

    }

    public class CifLogMessages {
        public static final String SLEEP = "sleep";

        public static final String LONG_SLEEP = "long sleep 3600sec";

        public static final String RECOVERED = " recovered";

        public static final String DISCONNECTED_NODE_REMOVED = "The DISCONNECTED node has been removed from the cache.";

        public static final String CC_RESUMED = "Nudged the consistency checks due to a resume";

        public static final long SLEEP_WAIT_TIME = 120;

        public static final String UNSYNCHRONIZED = "UNSYNCHRONIZED";

        public static final String SYNCHRONIZED = "SYNCHRONIZED";

        public static final String NEAD_SUBSCRIPTION_TIMEDOUT_MSG = "NEAD SUBSCRIPTION TIMEDOUT";

        public static final String SUCCESSFUL_NEAD_SYNCH = ", now has Synch Status: " + SYNCHRONIZED;

        public static final String SUCCESSFUL_NMA_SYNCH = ":FULL SYNC STOPPED, SUCCESS:";

        public static final String PCI_CONFLICT_NOT_ADDED_IN_MASTER = "cannot be added to Master MO as it already contains  maximum of 5 reported conflicts";

    }

    public class CSTestConstants {
        public static final String UNLIMITED_DEPTH_LEVEL = "-1";

        public final static String CSTESTLA = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice la ";

        public final static String CSTESTLT = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice lt ";

        public final static String CSTESTMI = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice mi ";

        public final static String CSTESTCM = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice cm ";

        public final static String CSTESTDM = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS -ns masterservice dm ";

    }

    public class FdnConstants {
        public static final String MANAGED_ELEMENT = "ManagedElement";

        public static final String RNC_FUNCTION = "RncFunction";

        public static final String RNC_NODE = "RNC";

        public static final String UTRAN_RELATION = "UtranRelation";
    }

    public class NodeLicNeadContants {

        public final static String REMOVE_19X_NODE_LICENSE = "remove19XnodesLicense.sh";

        public final static String REMOVE_18A_NODE_LICENSE = "remove18AnodesLicense.sh";

        public final static String REMOVE_17B_NODE_LICENSE = "remove17BnodesLicense.sh";

        public final static String FILE_17B_LICENSE = "Control_Node_Management";

        public final static String FILE_18A_LICENSE = "sentinel_license_Control_Node_Management_2";

        public final static String FILE_19X_LICENSE = "sentinel_license_Control_Node_Management_3";

        public final static String SCRIPT_REMOTE_PATH = "/home/nmsadm/";

        public final static String CLEANUP_AND_RESTART_MC = "cleanUpAndMCRestart.sh";

        public final static String INSTALL_NODE_LICENSE = "install_Node_License.sh";

        public final static String NODE_LIC_17B_CHECK_CMD = "/opt/ericsson/nms_us_licensing_cli/bin/unsupported/uslicensingcorbaserviceclient.sh CXC4012178";

        public final static String NODE_LIC_18A_CHECK_CMD = "/opt/ericsson/nms_us_licensing_cli/bin/unsupported/uslicensingcorbaserviceclient.sh CXC4012232";

        public final static String NODE_LIC_19X_CHECK_CMD = "/opt/ericsson/nms_us_licensing_cli/bin/unsupported/uslicensingcorbaserviceclient.sh CXC4012386";

        public final static String NODE_LIC_17B = "CXC4012178";

        public final static String NODE_LIC_18A = "CXC4012232";

        public final static String NODE_LIC_19X = "CXC4012386";

        public static final String INSTALL_19X_LIC = INSTALL_NODE_LICENSE + " 19X " + SCRIPT_REMOTE_PATH;

        public static final String INSTALL_18A_LIC = INSTALL_NODE_LICENSE + " 18A " + SCRIPT_REMOTE_PATH;

        public static final String INSTALL_17B_LIC = INSTALL_NODE_LICENSE + " 17B " + SCRIPT_REMOTE_PATH;

    }

}
