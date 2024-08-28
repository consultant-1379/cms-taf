#!/usr/bin/expect

spawn /opt/Sentinel/bin/lslic -df CXC4012178 O18
expect "Sentinel RMS Development Kit 8.6.2.0053 License Addition/Deletion Utility
  Copyright (C) 2015 SafeNet, Inc.

This will delete license(s) from the server, do you want to continue? (Y/N):"
send "Y\r"


interact
