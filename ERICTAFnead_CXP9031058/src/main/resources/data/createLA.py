import sys
import string
from string import upper
from org.python.core import PyArray
from com.ericsson.nms.umts.ranos.cms.moscript import MibAccess
from com.ericsson.nms.umts.ranos.cms.moscript import NameValueList
from com.ericsson.nms.umts.ranos.cms.moscript import MoScriptException
#from com.ericsson.nms.cif.ts.moimpl.RNC_NODE_MODEL_vV_5_3279_Y_1_1.idl import LocationArea


#mib.setAttributes("SubNetwork=ONRM_ROOT_MO_R,SubNetwork=RNC01,MeContext=RNC01,ManagedElement=1,RncFunction=1",nameValueList)
#hsToDchTriggerObj = HsToDchTrigger(0,1,0,1,0);
#nameValueList.set("hsToDchTrigger",hsToDchTriggerObj);


try :
    mib = MibAccess.create()

    attrNames = []
    attrNames.append("lac")
    # Have to get an object to use in the createMO function .......
    # get one from an existing LocationArea ... RNC01 and LocationArea=1 .... should exist.... :
    nameValueList = mib.getAttributes("SubNetwork=ONRM_ROOT_MO_R,SubNetwork=RNC01,MeContext=RNC01,ManagedElement=1,RncFunction=1,LocationArea=1" , attrNames);

    nameValueList.set("lac",55555);

    moType = "LocationArea"
    moName = "python"

    parentFdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=RNC01,MeContext=RNC01,ManagedElement=1,RncFunction=1"
    mib.createMo(parentFdn, moType, moName, nameValueList)

    result = mib.getAttributes("SubNetwork=ONRM_ROOT_MO_R,SubNetwork=RNC01,MeContext=RNC01,ManagedElement=1,RncFunction=1,LocationArea=python")
    print "Check if Mo is created = ", result.toString()

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
