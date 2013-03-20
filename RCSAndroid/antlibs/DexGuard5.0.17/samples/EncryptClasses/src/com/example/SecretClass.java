/*
 * Sample application to illustrate class encryption with DexGuard.
 *
 * Copyright (c) 2012 Saikoa / Itsana BVBA
 */
package com.example;

/**
 * Sample class that contains sensitive algorithms or implementations that need
 * to be hidden.
 */
public class SecretClass
{
    public String getMessage()
    {
        return Math.random() > 0.5 ?
            "Hello world!" :
            "Hello Android!";
    }
}
