module info.kgeorgiy.ja.kozlov.arrayset {
    requires transitive info.kgeorgiy.java.advanced.arrayset;
    requires junit;

    exports info.kgeorgiy.ja.kozlov.arrayset;

    opens info.kgeorgiy.ja.kozlov.arrayset to junit;
}