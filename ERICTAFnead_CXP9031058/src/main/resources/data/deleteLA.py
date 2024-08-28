import sys
from com.ericsson.nms.umts.ranos.cms.moscript import MibAccess
from com.ericsson.nms.umts.ranos.cms.moscript import MoScriptException

try :
    mib = MibAccess.create()

    print "Have to run the create-LA.py script first so that MO exist..."

    result = mib.getAttributes("SubNetwork=ONRM_ROOT_MO_R,SubNetwork=RNC01,MeContext=RNC01,ManagedElement=1,RncFunction=1,LocationArea=python")
    print "Check if Mo is created = ", result.toString()

    print "Going to delete MO now"

    moName = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=RNC01,MeContext=RNC01,ManagedElement=1,RncFunction=1,LocationArea=python"
    mib.deleteMo(moName)
except MoScriptException, ex :
    # This exception was thrown by the MIB access support
    #
    print "Exception :",ex
    sys.exit(-1)
except:
    # This exception was thrown as a result of some other problem
    # in the script
    # Rollback a started transaction (if needed)
    mib.rollbackTransaction();
    print "Python exception:", sys.exc_info()[0], sys.exc_info()[1]
    traceBack = sys.exc_info()[2]
    print traceBack.dumpStack()
    sys.exit(-2)
