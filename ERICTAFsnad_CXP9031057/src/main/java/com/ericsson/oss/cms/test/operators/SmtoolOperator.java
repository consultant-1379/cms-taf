package com.ericsson.oss.cms.test.operators;

import java.util.Map;

public interface SmtoolOperator {

    /**
     * Set host
     * 
     * @param name
     *        The name of the Service to use
     * @return
     */
    String host(String name);

    /**
     * Starts all MCs on the host that are online.
     * 
     * @return
     */

    String startMCs();

    /**
     * Stops all MCs on the host, except MCs that are necessary for SM.
     * 
     * @param all
     * @param reason
     *        Reason should be one of upgrade | application | planned| ha-failover| other
     * @param reasontext
     *        Description why mc was stopped.
     * @return
     */
    String stopMC(boolean all, String reason, String reasontext);

    /**
     * @param mc
     *        Starts and onlines the specified MCs.
     *        group - Online all MCs in the specified group.
     * @return
     */
    String online(String[] mc, String group);

    /**
     * @param mc
     * @return
     */
    String offline(String[] mc);

    /**
     * @param mc
     * @param group
     *        offline all MCs in the specified group.
     * @param reason
     *        Reason should be one of upgrade | application | planned| ha-failover| other
     * @param reasontext
     *        Deascription why MC was offlined.
     * @return
     */
    String offline(String[] mc, String group, String reason, String reasontext);

    /**
     * Lists the running states of all MCs
     * 
     * @return
     */
    Map<String, String> list();

    /**
     * Lists the running states of the specified MCs.
     * 
     * @param mc
     *        list of MCs to check.
     * @return
     */
    Map<String, String> list(String[] mc);

    /**
     * @param mc
     * @param group
     *        List all MCs in the specified group
     * @return
     */
    Map<String, String> list(String mc, String group);

    /**
     * Lists all MC (sub)processes/managed entities.
     * 
     * @return Map<mc_name, status>
     */
    Map<String, String> listAllProcesses();

    /**
     * List MC hostnames
     * 
     * @return Map<mc_name, hostname>
     */
    Map<String, String> listbyhost();

    /**
     * List all OSGi type MCs only
     * 
     * @return
     */
    Map<String, String> listOsgi();

    /**
     * List all classic type MCs only
     * 
     * @return
     */
    Map<String, String> listClassic();
    //
    //
    // -list -groups -list -groups will list all group names and their MCs.
    // -list -containers -list -container will list all containers
    // -show ([-container <container> [-verbose]] | Shows information for the specifed container, OSGi type MC or managed entity
    // [-mc <mc> | -container <container> shows details of specifed container and the MCs which belong to it
    // [-me <managed entity>]) -container <container> -verbose shows details of specifed container and the MCs and managed entities which
    // belong to it
    // -mc <mc> shows details of specifed MC and managed entities which belong to it, specified MC must be of type OSGi -me <managed entity>
    // shows details of specifed managed entity
    // -coldrestart ([<mc>...] | Makes a cold restart of the specified MCs.
    // [-all] | -all will cold restart all MCs.
    // [-osgi] | -osgi will cold restart all OSGi type MCs.
    // [-group <groupname>]) -group will cold restart all MCs in the specified group.
    // -reason=<reason> -reason should be one of upgrade|application|planned|ha-failover|other
    // -reasontext=<"reason description"> -reasontext is a text description of the reason in quotes.
    // -warmrestart ([<mc>...] | Makes a warm restart of the specified MCs.
    // [-all] | -all will warm restart all MCs.
    // [-osgi] | -osgi will warm restart all OSGi type MCs.
    // [-classic] | -classic will warm restart all classic type MCs.
    // [-group <groupname>]) -group will warm restart all MCs in the specified group.
    // -reason=<reason> -reason should be one of upgrade|application|planned|ha-failover|other
    // -reasontext=<"reason description"> -reasontext is a text description of the reason in quotes.
    // -cancel Cancels all actions that are ongoing or queued in the SSR server.
    // No complete rollback is made.
    // Actions may be cancelled in an uncomplete state.
    // -sync [off|on|verbose] Sets the CLI's sync mode. Hit CR to stop.
    // If on the CLI will wait for a command to finish.
    // If verbose the progress is listed
    // -progress Lists the progress of all ongoing actions. Hit CR to stop.
    // Self management commands (require SM Core to be started):
    // -config [-osgi | -classic | (<mc/process>|<mc/managed entity>)...] Lists specified configuration parameters for specified MCs,
    // processes or managed entities.
    // [<param>...] If no parameter is specified all parameters are listed.
    // -osgi will list parameters for all OSGi type MCs and Managed Entities only
    // -classic will list parameters for all classic type MCS and processes only
    // If no MC, process or managed entity is specified and neither -osgi nor -classic is not specified, all MCs, processes and managed
    // entities are listed.
    // Short cuts:
    // -config [-l] start Lists parameters related to the start of all MCs.
    // -config [-l] stop Lists parameters related to the stop of all MCs.
    // -l to list process parameters
    // -instrumentation [-osgi | -classic | <mc>...] Lists instrumentation parameters for specified MCs.
    // -osgi will list all OSGi type MCs only
    // -classic will list all classic type MCs only
    // If no MC is specified, and neither -osgi nor -classic is not specified, all MCs are listed.
    // -set <mc/process> <param> Sets one configuration parameter for an MC or process.
    // <value> [<type>] <type> is ignored, exists for backward compatibility
    // -action <mc> <action> <param> Invokes an action on an MC.
    // <value> [<type>] Supported types are "string" "int" "float" and "boolean".
    // Default type is "string".
    // -selftest <mc> <selftestNbr> Performs one or more selftests on an MC.
    // [<selftestNbr>...]
    // Trace command (require SM and SM Core to be started):
    // -trace [<mc> <types> <file>] | Lists, start and stops traces on MCs.
    // [<mc> stop] | Valid trace types between 0-199.
    // [stop] Example: trace MyMC 1-3,5 mytraceFile
    // Name Service command:
    // -nameserv [internal|guibound| List name service and portal content on the internal,
    // external|<port>] guibound, or external name services. Or, specify a
    // different name service port number using <port>.
    // RMI command:
    // -viewrmi [<host>][<port>] View services registered in RMI registry on the given host and port.
    // -viewrmi Default value of host is localhost. Default value of port is 1099.
    // -viewrmi [<host>] Default value of port is 1099
    // -viewrmi [<port>] Default value of host is localhost
    // General commands:
    // -help [command] Print this help or help for a specific command.
    // -quit/exit Quit/exit this CLI.

}
