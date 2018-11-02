package com.huawei.vcenterpluginui.utils;

import com.huawei.vcenterpluginui.exception.VersionNotSupportException;
import org.junit.Test;

public class ConnectedVimTest {
    @Test(expected = VersionNotSupportException.class)
    public void testCheckVersionCompatiblea() {
        String version = "5";
        ConnectedVim.checkVersionCompatible(version);
    }

    @Test(expected = VersionNotSupportException.class)
    public void testCheckVersionCompatibleb() {
        String version = "5.99";
        ConnectedVim.checkVersionCompatible(version);
    }

    @Test(expected = VersionNotSupportException.class)
    public void testCheckVersionCompatible1() {
        String version = "6.4.99";
        ConnectedVim.checkVersionCompatible(version);
    }

    @Test(expected = VersionNotSupportException.class)
    public void testCheckVersionCompatible2() {
        String version = "6.4";
        ConnectedVim.checkVersionCompatible(version);
    }

    @Test
    public void testCheckVersionCompatible3() {
        String version = "6.5";
        ConnectedVim.checkVersionCompatible(version);
    }

    @Test
    public void testCheckVersionCompatible4() {
        String version = "6.5.0";
        ConnectedVim.checkVersionCompatible(version);
    }

    @Test
    public void testCheckVersionCompatible4a() {
        String version = "6.5.99";
        ConnectedVim.checkVersionCompatible(version);
    }

    @Test
    public void testCheckVersionCompatible5() {
        String version = "6.6";
        ConnectedVim.checkVersionCompatible(version);
    }

    @Test
    public void testCheckVersionCompatible6() {
        String version = "6.6.0";
        ConnectedVim.checkVersionCompatible(version);
    }

    @Test
    public void testCheckVersionCompatible7() {
        String version = "7";
        ConnectedVim.checkVersionCompatible(version);
    }

    @Test
    public void testCheckVersionCompatible8() {
        String version = "7.0.0";
        ConnectedVim.checkVersionCompatible(version);
    }
}
