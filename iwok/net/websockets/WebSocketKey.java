package iwok.net.websockets;

import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;

public class WebSocketKey {

	public static String MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	
	public static String generateAcceptKey(String websocketkey){
		try{
			String key = websocketkey+MAGIC_STRING;
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
			messageDigest.update(key.getBytes("iso-8859-1"), 0, key.length());
			return  base64(messageDigest.digest());
		}catch(Exception e){
			return "";
		}
	}

    public static String base64(byte[] input) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException{
        Class<?> c = Class.forName("sun.misc.BASE64Encoder");
        java.lang.reflect.Method m = c.getMethod("encode", new Class<?>[]{byte[].class});
        String s = (String) m.invoke(c.newInstance(), input);
        return s;
    }		
}
