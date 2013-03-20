# Common DexGuard configuration for debug versions and release versions.
# Copyright (c) 2012 Saikoa / Itsana BVBA

# Keep all public API.
-keep public class * {
    public protected *;
}

-include dexguard-common.pro
