package org.akvo

import org.akvo.caddisfly.instruction.CbtInstructions
import org.akvo.caddisfly.test.CbtTest
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Enclosed::class)
@Suite.SuiteClasses(CbtInstructions::class, CbtTest::class)
class CbtSuite
