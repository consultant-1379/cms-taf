/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.constants;

/**
 * @author eeimacn
 */
public final class BCGConstants {

    private BCGConstants() {

    }

    public class BCGFile {

        public static final String VENDORNAME = "vendorName";

        public static final String ERICSSON = "Ericsson";

        public static final String ATTRIBUTES = "attributes";

        public static final String MODIFIER = "modifier";

        public static final String BULK_CM_CONFIG_DATA_FILE = "bulkCmConfigDataFile";

        public static final String FILEHEADER = "fileHeader";

        public static final String FILEFOOTER = "fileFooter";

        public static final String CONFIGDATA = "configData";

        public static final String FILEFORMATVERSION = "fileFormatVersion";

        public static final String START = "Start";

        public static final String END = "End";

        public class NameSpace {

            public static final String UN = "un:";

            public static final String XN = "xn:";

            public static final String MOCFILTER_PREFIX = ":";
        }

        public class MOs {

            public static final String UTRANRELATION = "UtranRelation";

            public static final String UTRANCELL = "UtranCell";

            public static final String MECONTEXT = "MeContext";

            public static final String RNCFUNCTION = "RncFunction";

            public static final String MANAGEDELEMENT = "ManagedElement";

            public static final String SUBNETWORK = "SubNetwork";

        }

        public class MOAttributes {
            public static final String ADJACENTCELL = "adjacentCell";

        }
    }

    public class BCGFlags {

        public static final String CMD_EXPORT_FLAG = "-e";

        public static final String CMD_IMPORT = "-i";

        public static final String CMD_ACTIVATE = "-a";

        public static final String CMD_REMOVEPLAN = "-rp";

        public static final String PARAM_DOMAIN_FLAG = "-d";

        public static final String PARAM_PLAN = "-p";

    }

    public class BCGPaths {
        public static final String EXPORTFILEPATH = "/var/opt/ericsson/nms_umts_wran_bcg/files/export/";

        public static final String BCGTOOL = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh";

        public static final String IMPORT = "/var/opt/ericsson/nms_umts_wran_bcg/files/import/";

        public static final String IMPORTLOGS = "/var/opt/ericsson/nms_umts_wran_bcg/files/logs/import";

    }

}
