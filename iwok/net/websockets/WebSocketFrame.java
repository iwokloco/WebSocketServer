package iwok.net.websockets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class WebSocketFrame {

	
	public static final byte SINGLE_FRAME_UNMASKED_MESSAGE = (byte) 0x81;
	
	
    /**
     * Write a A single-frame unmasked text message
     * 
     * @param os
     * @param strText
     * @throws IOException 
     */
    public static void doWrite(OutputStream os, String strText) throws IOException {
        byte[] textBytes = strText.getBytes("UTF-8");
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        
        /* Add start of Frame */
        bao.write(SINGLE_FRAME_UNMASKED_MESSAGE);
        bao.write((byte) textBytes.length);
        bao.write(textBytes);

        bao.flush();
        bao.close();

        os.write(bao.toByteArray(), 0, bao.size());
        os.flush();
    }
    
     
    /**
     * Write a A single-frame unmasked text message
     * 
     * @param os
     * @param strText
     * @throws IOException 
     */
    public static void doClose( OutputStream os, String strText) throws IOException {
        byte[] textBytes = strText.getBytes("UTF-8");
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        
        /* Add start of Frame */
        bao.write(SINGLE_FRAME_UNMASKED_MESSAGE );
        bao.write((byte) textBytes.length);
        bao.write(textBytes);

        bao.flush();
        bao.close();

        os.write(bao.toByteArray(), 0, bao.size());
        os.flush();
    }
	
}
