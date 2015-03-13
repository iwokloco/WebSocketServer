package iwok.net.test;

import java.util.StringTokenizer;

public class TestTokenizer {

	public static void main(String[] args) {

		String headerLine = "Sec-WebSocket-Key: x3JJHMbDL1EzLkh9GBhXDw==";
		String property = null;
		String value = null;
		StringTokenizer token = new StringTokenizer(headerLine, ":");
		while(token.hasMoreTokens()){
			property = token.nextToken();
			value = token.nextToken().trim();
		}
		System.out.println(property);
		System.out.println(value);
	}

}
