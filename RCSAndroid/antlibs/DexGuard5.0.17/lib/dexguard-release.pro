# DexGuard configuration for release versions.
# Copyright (c) 2012 Saikoa / Itsana BVBA

-optimizationpasses 5

-obfuscationdictionary        dictionary.txt
-classobfuscationdictionary   dictionary.txt
-packageobfuscationdictionary dictionary.txt

-repackageclasses ''
-allowaccessmodification

-include dexguard-common.pro
-include dexguard-assumptions.pro
