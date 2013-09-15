/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dirbaio.cryptocat.service;

import android.os.Looper;
import org.jivesoftware.smack.util.Base64;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Utils
{
	public static SecureRandom random = new SecureRandom();

	public static String randomString()
	{
		return new BigInteger(130, random).toString(32);
	}

	public static byte[] fromBase64(String base64)
	{
		return Base64.decode(base64);
	}

	public static String toBase64(byte[] binary)
	{
		return Base64.encodeBytes(binary, Base64.DONT_BREAK_LINES);
	}

	public static void assertUiThread()
	{
		if (Looper.getMainLooper().getThread() != Thread.currentThread())
			throw new RuntimeException("Not on UI Thread!");
	}
}
