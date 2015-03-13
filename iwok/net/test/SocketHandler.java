package iwok.net.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import com.jstewart.ReadFrame;

import iwok.net.websockets.WebSocketFrame;
import iwok.net.websockets.WebSocketKey;

public class SocketHandler implements Runnable{

	public Socket socket;
	public int id;
	public BufferedReader in;
	public PrintWriter out;
	public StringBuffer request;
	public StringBuffer response;
	public Boolean closeSocket = false;
	public String websocketkey = null;
	public String websocketAcceptkey = null;
	
	public BufferedInputStream bufferIn;
	public BufferedOutputStream bufferOut;
	
	public SocketHandler(int id){
		this.id = id;
	}
	
	public void acceptSocket(Socket socket){
		this.socket = socket;
		this.request = new StringBuffer();
		this.response = new StringBuffer();
		this.closeSocket = false;
		this.websocketkey = null;
		this.websocketAcceptkey = null;
		try{
			this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.out = new PrintWriter(socket.getOutputStream());
			this.bufferIn = new BufferedInputStream(socket.getInputStream());
			this.bufferOut = new BufferedOutputStream(socket.getOutputStream());
		}catch(IOException e){e.printStackTrace();}		
	} 
	
	@Override
	public void run() {
		String headerLine = null;
		String headerProperty = null;
		String headerValue = null;
		StringTokenizer token = null;
		try{		
			while(this.in.ready()){
				headerLine = in.readLine();
				token = new StringTokenizer(headerLine, ":");
				while(token.hasMoreTokens()){
					headerProperty = token.nextToken();
					if(token.hasMoreTokens()){
						headerValue = token.nextToken();
						headerValue = headerValue.trim();
					}
				}

				if(headerProperty.equals("Sec-WebSocket-Key")){
					this.websocketkey = headerValue;
					this.websocketAcceptkey = WebSocketKey.generateAcceptKey(this.websocketkey);
				}
					
				request.append(headerLine);
				request.append("\n");
			}
				
			
	        String strHeaders = "HTTP/1.1 101 Switching Protocols\r\n"
	                + "Upgrade: websocket\r\n"
	                + "Connection: Upgrade\r\n"
	                + "Sec-WebSocket-Accept: " + this.websocketAcceptkey + "\r\n"
	                + "\r\n";				
            out.write(strHeaders);
            out.flush();
                    

            
            while(!this.closeSocket){
            	byte[] frameHeader = new byte[1];
                bufferIn.read( frameHeader );
                String strPayloadData = new ReadFrame().readFrame( bufferIn, frameHeader[ 0] ); // Read frame.  This also handles masked payloads           	
                System.out.println(strPayloadData);	
                
                WebSocketFrame.doWrite(bufferOut, "User"+this.id+": " + strPayloadData);
                
            }
            
              
			this.bufferIn.close();
			this.bufferOut.close();
			this.in.close();
			this.out.close();
			this.socket.close();
			WebServer.freeSocket(this.id);
		}catch(IOException e){
			e.printStackTrace();
			WebServer.freeSocket(this.id);
		}
	}
}

/*System.out.println("\nREQUEST");                
System.out.println(request.toString());                
System.out.println("\nRESPONSE");                
System.out.println(strHeaders);*/