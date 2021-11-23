package info.kgeorgiy.ja.kozlov.implementor;

import info.kgeorgiy.java.advanced.implementor.*;
import info.kgeorgiy.java.advanced.implementor.full.interfaces.Interfaces;
import org.junit.Test;

import java.nio.file.Path;

public class CustomTestImpl extends AdvancedJarImplementorTest {
    @Test
    public void test00_nothing() throws ImplerException {
        Implementor i = new Implementor();
        i.main(new String[]{"-jar", "java.util.List", "./dir/test.jar"});
    }
}