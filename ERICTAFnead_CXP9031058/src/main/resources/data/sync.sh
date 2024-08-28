#!/bin/sh -x
echo `date`
/opt/ericsson/bin/smtool -offline cms_nead_seg -reason=other -reasontext="restart for sync requirement"
/opt/ericsson/bin/smtool progress
sleep 60
/opt/ericsson/bin/smtool -offline mscnead -reason=other -reasontext="restart for sync requirement"
/opt/ericsson/bin/smtool progress
sleep 60
/opt/ericsson/ddc/util/bin/mibutil -resetgc
/opt/ericsson/bin/smtool -online cms_nead_seg
/opt/ericsson/bin/smtool -online mscnead 
echo `date`
#/opt/ericsson/nms_cif_cs/etc/unsupported/bin/notiRec > /home/eclaral/syncTR.txt

