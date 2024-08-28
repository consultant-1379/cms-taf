/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.LOG_FILE_PATH;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.CACHE_FIELD_CONSISTENCY_STATE;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.CACHE_FIELD_MO_NAME;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.CACHE_SECTION_CLUSTER;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.CACHE_SECTION_DELIM;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.CACHE_SECTION_MASTER;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.CACHE_SECTION_PROXY;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.CACHE_SECTION_PROXY_UNSYNC;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.CACHE_SECTION_RBSLOCALCELL;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.CACHE_SECTION_UNMANAGED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.COMMAND_CACHE_REVIEW;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.oss.cms.test.model.CachedMo;
import com.ericsson.oss.cms.test.util.GetHostUsers;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author egokdag
 */
@Operator(context = Context.CLI)
public class SnadCacheCliOperator implements SnadCacheOperator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CLICommandHelper cliCommandHelper = GetHostUsers.getCLICommandHelper();

    private final Host host = HostGroup.getOssmaster();

    private final User rootUser = new User(host.getUser(UserType.ADMIN), host.getPass(UserType.ADMIN), UserType.ADMIN);

    private final RemoteObjectHandler remoteObjectHandler = new RemoteObjectHandler(host, rootUser);

    private Map<Fdn, CachedMo> clusterCache = new HashMap<Fdn, CachedMo>();

    private Map<Fdn, CachedMo> masterCache = new HashMap<Fdn, CachedMo>();

    private Map<Fdn, CachedMo> proxyCache = new HashMap<Fdn, CachedMo>();

    private Map<Fdn, CachedMo> unmanagedCache = new HashMap<Fdn, CachedMo>();

    private Map<Fdn, CachedMo> proxyUnsyncedCache = new HashMap<Fdn, CachedMo>();

    private Map<Fdn, CachedMo> rbsLocalCellCache = new HashMap<Fdn, CachedMo>();

    @Override
    public void executeCacheReview() {
        final String snadCacheReviewCommandOutput = cliCommandHelper.simpleExec(COMMAND_CACHE_REVIEW);
        final String remoteFilePath = getSnadCacheFilePathFrom(snadCacheReviewCommandOutput);
        final String localFilePath = copyRemoteFileToLocal(remoteFilePath);

        parseReviewCacheOutput(localFilePath);

        removeRemoteFile(remoteFilePath);
    }

    private String getSnadCacheFilePathFrom(final String snadCacheReviewCommandOutput) {
        final Pattern cacheFilePattern = Pattern.compile(".*" + LOG_FILE_PATH + ".*");
        final Matcher cacheFilePatternMatcher = cacheFilePattern.matcher(snadCacheReviewCommandOutput);

        if (cacheFilePatternMatcher.find()) {
            return cacheFilePatternMatcher.group();
        }
        return null;
    }

    private void parseReviewCacheOutput(final String localFilePath) {
        Map<Fdn, CachedMo> tempMap = new HashMap<Fdn, CachedMo>();

        final File file = new File(localFilePath);
        String currentLine = null;
        try (final ReversedLinesFileReader reader = new ReversedLinesFileReader(file)) {
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.startsWith(CACHE_FIELD_CONSISTENCY_STATE)) {
                    final CachedMo mo = parseMo(currentLine, reader);
                    tempMap.put(mo.getFdn(), mo);
                } else if (currentLine.startsWith(CACHE_SECTION_DELIM)) {
                    storeCacheBySectionHeader(tempMap, reader.readLine());
                    tempMap = new HashMap<>();
                }
            }
        } catch (final IOException ex) {
            logger.error("file operation got exception.", ex);
        } finally {
            file.delete();
        }

    }

    private String copyRemoteFileToLocal(final String filePath_reverse) {
        final String DEFAULT_TEMPFILE_PATH = System.getProperty("java.io.tmpdir");
        final String dir = DEFAULT_TEMPFILE_PATH + filePath_reverse.substring(0, 39);
        final String localFileLocation = DEFAULT_TEMPFILE_PATH + filePath_reverse;
        final File directory = new File(dir);
        boolean success = false;
        if (!directory.exists()) {
            success = directory.mkdirs();
            logger.info("Directory created successfully");
        } else {
            logger.info("Directory available already");
        }
        if (!success) {
            logger.info("Directory not exist and failed to create");
        }
        remoteObjectHandler.copyRemoteFileToLocal(filePath_reverse, localFileLocation);
        return localFileLocation;
    }

    private CachedMo parseMo(String currentLine, final ReversedLinesFileReader reader) throws IOException {
        final String consistencyState = currentLine.substring(CACHE_FIELD_CONSISTENCY_STATE.length());
        final String globalId = reader.readLine();
        currentLine = reader.readLine();
        final Fdn moFdn = new Fdn(currentLine.substring(CACHE_FIELD_MO_NAME.length()));

        return new CachedMo(moFdn, globalId, consistencyState);
    }

    private void storeCacheBySectionHeader(final Map<Fdn, CachedMo> tempCache, final String line) {
        switch (line) {
            case CACHE_SECTION_RBSLOCALCELL:
                rbsLocalCellCache = tempCache;
                break;
            case CACHE_SECTION_PROXY_UNSYNC:
                proxyUnsyncedCache = tempCache;
                break;
            case CACHE_SECTION_UNMANAGED:
                unmanagedCache = tempCache;
                break;
            case CACHE_SECTION_PROXY:
                proxyCache = tempCache;
                break;
            case CACHE_SECTION_MASTER:
                masterCache = tempCache;
                break;
            case CACHE_SECTION_CLUSTER:
                clusterCache = tempCache;
                break;
        }
    }

    @Override
    public boolean isInCache(final Fdn moFdn) {
        return isInMasterCache(moFdn) || isInProxyCache(moFdn) || isInUnmanagedCache(moFdn) || isInProxyUnsyncedCache(moFdn) || isInRbsLocalCellCache(moFdn)
                || isInClusterCache(moFdn);
    }

    @Override
    public boolean isInMasterCache(final Fdn masterMoFdn) {
        return masterCache.containsKey(masterMoFdn);
    }

    @Override
    public boolean isInProxyCache(final Fdn proxyMoFdn) {
        return proxyCache.containsKey(proxyMoFdn);
    }

    @Override
    public boolean isInClusterCache(final Fdn moFdn) {
        return clusterCache.containsKey(moFdn);
    }

    @Override
    public boolean isInUnmanagedCache(final Fdn moFdn) {
        return unmanagedCache.containsKey(moFdn);
    }

    @Override
    public boolean isInProxyUnsyncedCache(final Fdn moFdn) {
        return proxyUnsyncedCache.containsKey(moFdn);
    }

    @Override
    public boolean isInRbsLocalCellCache(final Fdn moFdn) {
        return rbsLocalCellCache.containsKey(moFdn);
    }

    @Override
    public String getMasterConsistencyState(final Fdn masterMoFdn) {
        CachedMo mo = masterCache.get(masterMoFdn);
        if (mo == null) {
            mo = unmanagedCache.get(masterMoFdn);
        }

        if (mo != null) {
            return mo.getConsistencyState();
        }

        logger.info("Master MO: " + masterMoFdn + " is not in SNAD cache");
        return null;
    }

    private void removeRemoteFile(final String filePath) {
        remoteObjectHandler.deleteRemoteFile(filePath);
    }

    @Override
    public List<Fdn> getFdnsNotInCache(final List<Fdn> fdns) {
        final List<Fdn> notInCache = new ArrayList<Fdn>();
        for (final Fdn mo : fdns) {
            logger.debug("Looking for MO in the Cache: " + mo.getFdn());
            if (!isInCache(mo)) {
                notInCache.add(mo);
            }
        }
        return notInCache;
    }

    @Override
    public String reportMosNotInCache(final List<Fdn> fdns) {
        final StringBuilder builder = new StringBuilder();
        for (final Fdn fdn : fdns) {
            builder.append(fdn.getFdn() + "\n");
        }
        return builder.toString();
    }
}
