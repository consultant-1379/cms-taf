#!/bin/bash

a17B="17B"
a18A="18A"
a19X="19X"
echo $1
echo $2

if [ "$a17B" ==  "$1" ]
then

/opt/Sentinel/bin/lslic -F $2'Control_Node_Management'

elif [ "$a18A" ==  "$1" ] 
then

/opt/Sentinel/bin/lslic -F $2'sentinel_license_Control_Node_Management_2'

elif [ "$a19X" ==  "$1" ] 
then

/opt/Sentinel/bin/lslic -F $2'sentinel_license_Control_Node_Management_3'

fi
