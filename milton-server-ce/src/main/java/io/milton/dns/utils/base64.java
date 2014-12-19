/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.utils;

import java.io.*;

/**
 * Routines for converting between Strings of base64-encoded data and arrays of
 * binary data.
 *
 * @author Brian Wellington
 */

public class base64 {

private static final String Base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

private
base64() {}

/**
 * Convert binary data to a base64-encoded String
 * @param b An array containing binary data
 * @return A String containing the encoded data
 */
public static String
toString(byte [] b) {
	ByteArrayOutputStream os = new ByteArrayOutputStream();

	for (int i = 0; i < (b.length + 2) / 3; i++) {
		short [] s = new short[3];
		short [] t = new short[4];
		for (int j = 0; j < 3; j++) {
			if ((i * 3 + j) < b.length)
				s[j] = (short) (b[i*3+j] & 0xFF);
			else
				s[j] = -1;
		}
		
		t[0] = (short) (s[0] >> 2);
		if (s[1] == -1)
			t[1] = (short) (((s[0] & 0x3) << 4));
		else
			t[1] = (short) (((s[0] & 0x3) << 4) + (s[1] >> 4));
		if (s[1] == -1)
			t[2] = t[3] = 64;
		else if (s[2] == -1) {
			t[2] = (short) (((s[1] & 0xF) << 2));
			t[3] = 64;
		}
		else {
			t[2] = (short) (((s[1] & 0xF) << 2) + (s[2] >> 6));
			t[3] = (short) (s[2] & 0x3F);
		}
		for (int j = 0; j < 4; j++)
			os.write(Base64.charAt(t[j]));
	}
	return new String(os.toByteArray());
}

/**
 * Formats data into a nicely formatted base64 encoded String
 * @param b An array containing binary data
 * @param lineLength The number of characters per line
 * @param prefix A string prefixing the characters on each line
 * @param addClose Whether to add a close parenthesis or not
 * @return A String representing the formatted output
 */
public static String
formatString(byte [] b, int lineLength, String prefix, boolean addClose) {
	String s = toString(b);
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < s.length(); i += lineLength) {
		sb.append (prefix);
		if (i + lineLength >= s.length()) {
			sb.append(s.substring(i));
			if (addClose)
				sb.append(" )");
		}
		else {
			sb.append(s.substring(i, i + lineLength));
			sb.append("\n");
		}
	}
	return sb.toString();
}


/**
 * Convert a base64-encoded String to binary data
 * @param str A String containing the encoded data
 * @return An array containing the binary data, or null if the string is invalid
 */
public static byte []
fromString(String str) {
	ByteArrayOutputStream bs = new ByteArrayOutputStream();
	byte [] raw = str.getBytes();
	for (int i = 0; i < raw.length; i++) {
		if (!Character.isWhitespace((char)raw[i]))
			bs.write(raw[i]);
	}
	byte [] in = bs.toByteArray();
	if (in.length % 4 != 0) {
		return null;
	}

	bs.reset();
	DataOutputStream ds = new DataOutputStream(bs);

	for (int i = 0; i < (in.length + 3) / 4; i++) {
		short [] s = new short[4];
		short [] t = new short[3];

		for (int j = 0; j < 4; j++)
			s[j] = (short) Base64.indexOf(in[i*4+j]);

		t[0] = (short) ((s[0] << 2) + (s[1] >> 4));
		if (s[2] == 64) {
			t[1] = t[2] = (short) (-1);
			if ((s[1] & 0xF) != 0)
				return null;
		}
		else if (s[3] == 64) {
			t[1] = (short) (((s[1] << 4) + (s[2] >> 2)) & 0xFF);
			t[2] = (short) (-1);
			if ((s[2] & 0x3) != 0)
				return null;
		}
		else {
			t[1] = (short) (((s[1] << 4) + (s[2] >> 2)) & 0xFF);
			t[2] = (short) (((s[2] << 6) + s[3]) & 0xFF);
		}

		try {
			for (int j = 0; j < 3; j++)
				if (t[j] >= 0)
					ds.writeByte(t[j]);
		}
		catch (IOException e) {
		}
	}
	return bs.toByteArray();
}

}
