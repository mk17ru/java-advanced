module info.kgeorgiy.ja.kozlov.walk {
    requires transitive info.kgeorgiy.java.advanced.walk;
    requires junit;

    exports info.kgeorgiy.ja.kozlov.walk;

    opens info.kgeorgiy.ja.kozlov.walk to junit;
}