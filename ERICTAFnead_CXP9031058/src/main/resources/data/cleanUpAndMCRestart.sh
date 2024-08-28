#!/bin/bash

#Clean up part...
tar -cvf /var/opt/ericsson/nms_umts_cms_nead_seg/fc_bkup.tar /var/opt/ericsson/nms_umts_cms_nead_seg/fc/
cd /var/opt/ericsson/nms_umts_cms_nead_seg/fc/
rm -rf *.*
cd

#Restart NEAD MC...
/opt/ericsson/bin/smtool -coldrestart cms_nead_seg -reason=planned -reasontext=restart
/opt/ericsson/bin/smtool progress
