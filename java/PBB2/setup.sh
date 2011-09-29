#!/bin/bash

# setup.sh - A script to set up the correct environment
# for ProteinBoxBot.jar
propertiesFile=bot.properties
if [ -f bot.properties ]
then 
    echo "ProteinBoxBot has already been configured. Delete"
    echo "bot.properties file to restart installation."
    exit 1
fi

echo "=== ProteinBoxBot Installation ==="
echo 
echo -n "Enter the username this bot will operate from: "
read wpUsername
echo -n "Enter the password for $wpUsername: "
read wpPassword
echo
pymol="n"
echo -n "Do you have PyMOL installed and want to upload images? (y/N) "
read pymol
if [ "$pymol" = "y" ]
then 
    echo -n "Enter the desired commons.wikimedia.org username: "
    read cwUsername
    echo -n "Enter the password for $cwUsername: "
    read cwPassword
    echo
    echo -n "Enter the full path to the PyMOL binary/executable: "
    read pymolBin
else
    cwUsername=""
    cwPassword=""
    pymolBin=""
fi

function write_properties {
cat << EOF
# Bot Name
name = ProteinBoxBot

# Boolean options
dryRun = false
useCache = false
strictChecking = true
verbose = true
debug = false

# Locations
cacheLocation = /tmp/pbb
logs = /tmp/pbb/logs

# Wikipedia credentials
username = $wpUsername
password = $wpPassword

# Wikimedia Commons credentials (to upload rendered images)
commonsUsername = $cwUsername
commonsPassword = $cwPassword

# Template information
templatePrefix = Template:PBB/
templateName = GNF_Protein_box
api_root = http://en.wikipedia.org/w/
commonsRoot = http://commons.wikimedia.org/w/

# Miscellaneous
loggerLevel = Info

# PDB image rendering
pymol = $pymolBin
EOF
}

write_properties > $propertiesFile