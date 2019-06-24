package org.akvo;

import org.akvo.caddisfly.misc.MiscTest;
import org.akvo.caddisfly.test.CbtTest;
import org.akvo.caddisfly.test.SwatchSelectTest;
import org.akvo.caddisfly.ui.IntroTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({MiscTest.class,
        CbtTest.class,
        SwatchSelectTest.class,
        IntroTest.class})
public class UserInterfaceSuite {
}
