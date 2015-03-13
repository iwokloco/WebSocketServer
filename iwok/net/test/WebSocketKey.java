package iwok.net.test;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class WebSocketKey {

	public static void main(String[] args) {
		String returnBase = null;
		String websocketkey = "x3JJHMbDL1EzLkh9GBhXDw==";
		String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		
		String msg = websocketkey+magicString;
		
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("SHA-1");
			messageDigest.update(msg.getBytes("iso-8859-1"), 0, msg.length());
			byte[] sha1hash = messageDigest.digest();
			returnBase = base64(sha1hash);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(returnBase);
		
	}
	
	
    public static String base64(byte[] input) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException{
        Class<?> c = Class.forName("sun.misc.BASE64Encoder");
        java.lang.reflect.Method m = c.getMethod("encode", new Class<?>[]{byte[].class});
        String s = (String) m.invoke(c.newInstance(), input);
        return s;
    }

     
	
	

}
