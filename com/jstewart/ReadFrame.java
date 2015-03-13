/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstewart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 *
 * Portions from ZAPROXY
 * 
 * @author J. Stewart  
 */
public class ReadFrame {
    private int OPCODE_CLOSE = 8;
    private ByteBuffer currentFrame;
    private boolean isMasked;
    private byte[] mask = new byte[4];
    private int maskIndex = 0;
    private int payloadLength;
    /**
     * By default, there are 7 bits to indicate the payload length. If the
     * length can not be shown with 7 bits, the payload length is set to
     * 126. Then the next 16 bits interpreted as unsigned integer is the
     * payload length.
     */
    private static final int PAYLOAD_LENGTH_16 = 126;
    /**
     * If the 7 bits represent the value 127, then the next 64 bits
     * interpreted as an unsigned integer is the payload length. (the most
     * significant bit MUST be 0)
     */
    private static final int PAYLOAD_LENGTH_63 = 127;

    /**
     * Given the first byte of a frame and a channel,
     * this method reads from the second byte until the end of the frame.
     * 
     * @param in
     * @param frameHeader
     * @throws IOException
     */
    public String readFrame(InputStream in, byte frameHeader) throws IOException {
        int opcode = (frameHeader & 0x0F); // last 4 bits represent opcode
        if ( opcode == OPCODE_CLOSE ) {
            return null;
        }
        
        // most significant bit is FIN flag & 0x1
        //isFinished = (frameHeader >> 7 & 0x1) == 1;

        // currentFrame buffer is filled by read()
        currentFrame = ByteBuffer.allocate(4096);
        currentFrame.put(frameHeader);

        byte payloadByte = read(in);
        isMasked = (payloadByte >> 7 & 0x1) == 1; // most significant bit is MASK flag
        payloadLength = (payloadByte & 0x7F);

        /*
         * Multiple bytes for payload length are submitted in network byte
         * order: most significant byte first
         */
        if (payloadLength < PAYLOAD_LENGTH_16) {
            // payload length is between 0-125 bytes
        } else {
            int bytesToRetrieve = 0;

            // we have got PAYLOAD_LENGTH_16 or PAYLOAD_LENGTH_63
            if (payloadLength == PAYLOAD_LENGTH_16) {
                // payload length is between 126-65535 bytes represented by 2 bytes.
                bytesToRetrieve = 2;
            } else if (payloadLength == PAYLOAD_LENGTH_63) {
                // payload length is between 65536-2^63 bytes represented by 8 bytes
                // (most significant bit must be zero)
                bytesToRetrieve = 8;
            }

            byte[] extendedPayloadLength = read(in, bytesToRetrieve);

            payloadLength = 0;
            for (int i = 0; i < bytesToRetrieve; i++) {
                byte extendedPayload = extendedPayloadLength[i];

                // shift previous bits left and add next byte
                payloadLength = (payloadLength << 8) | (extendedPayload & 0xFF);
            }
        }

        // now we know the payload length
        // we are able to determine frame length

        int bytesToReadForPayload = payloadLength;

        if (isMasked) {
            // read 4 bytes mask
            mask = read(in, 4);
        }

        byte[] payload = read( in, bytesToReadForPayload );

        if (isMasked) {
            for (int i = 0; i < payload.length; i++) {
                // unmask payload by XOR it continuously with frame mask
                payload[i] = (byte) (payload[i] ^ mask[maskIndex]);
                maskIndex = (maskIndex + 1) % 4;
            }
        }

        return new String( payload );

    }

    /**
     * Read only one byte from input stream.
     * 
     * @param in
     * @return
     * @throws IOException
     */
    private byte read(InputStream in) throws IOException {
        byte[] buffer = read(in, 1);
        return buffer[0];
    }

    /**
     * Reads given length from the given stream.
     * 
     * @param in
     * @param length Determines how much bytes should be read from the given channel.
     * @return ByteBuffer read for reading (i.e.: already flipped).
     * @throws IOException
     */
    private byte[] read(InputStream in, int length) throws IOException {
        byte[] buffer = new byte[length];

        int bytesRead = 0;
        do {
            bytesRead += in.read(buffer);
        } while (length != bytesRead);

        int freeSpace = currentFrame.capacity() - currentFrame.position();
        if (freeSpace < bytesRead) {
            currentFrame = reallocate(currentFrame, currentFrame.position() + bytesRead);
        }

        // add bytes to current frame and reset to be able to read again
        currentFrame.put(buffer);

        return buffer;
    }

    /**
     * Resizes a given ByteBuffer to a new size.
     * 
     * @param src ByteBuffer to get resized
     * @param newSize size in bytes for the resized buffer
     */
    public ByteBuffer reallocate(ByteBuffer src, int newSize) {
        int srcPos = src.position();
        if (srcPos > 0) {
            src.flip();
        }

        ByteBuffer dest = ByteBuffer.allocate(newSize);
        dest.put(src);
        dest.position(srcPos);

        return dest;
    }

 
}
