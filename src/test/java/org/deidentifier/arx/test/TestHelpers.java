package org.deidentifier.arx.test;

import java.nio.file.Paths;

/**
 * COPYRIGHT REAL LIFE SCIENCES INC
 * Created by nathanartz on 5/17/18.
 *
 *
 * This is a general utility class that has helpful methods that
 * can be used in any test
 */
public class TestHelpers {
    public static final String TEST_FIXTURE_DIRECTORY = "/data3";

    /**
     *
     * @param testFixture - builds a full path to the resource file, whether it exists or not
     *                    test fixtures are assumed to be in the resources/data folder
     * @return
     */
    public static String getTestFixturePath(String testFixture) {
        return Paths.get(getTestFixtureDirectory(), testFixture).toString();
    }

    public static String getTestFixtureDirectory() {
        return TestHelpers.class.getResource(TEST_FIXTURE_DIRECTORY).getFile();
    }
}
