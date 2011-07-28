#!/bin/bash
DATE="`date +%j`"
cp PBBGeneral.db PBBGeneral.db.bk.$DATE
echo "Backed up PBB database to PBBGeneral.db.bk.%DATE"
