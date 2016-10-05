package com.eaglesakura.android.service;

import com.eaglesakura.android.AndroidSupportTestCase;
import com.eaglesakura.android.cmdservice.BuildConfig;

import org.robolectric.annotation.Config;

@Config(constants = BuildConfig.class, packageName = BuildConfig.APPLICATION_ID, sdk = 21)
public abstract class UnitTestCase extends AndroidSupportTestCase {

}
