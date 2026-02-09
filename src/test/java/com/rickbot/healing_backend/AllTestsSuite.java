package com.rickbot.healing_backend;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.IncludeClassNamePatterns;

@Suite
@SelectPackages("com.rickbot.healing_backend")
@IncludeClassNamePatterns({".*Test"})
public class AllTestsSuite {
}
