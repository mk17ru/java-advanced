module info.kgeorgiy.ja.kozlov.student {
    requires transitive info.kgeorgiy.java.advanced.student;
    requires junit;

    exports info.kgeorgiy.ja.kozlov.student;

    opens info.kgeorgiy.ja.kozlov.student to junit;
}