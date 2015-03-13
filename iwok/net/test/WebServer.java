package iwok.net.test;

import java.io.IOException;
import java.net.ServerSocket;

public class WebServer {

	static int MAX_THREADS = 50;
	static Thread[] threads;
	static SocketHandler[] sockets;
	static int[] freeIDS;
	static int ri; //read index
	static int wi; //write index
	static ServerSocket server;
	static Boolean exit;
	static int nThreads;
	
	public static void freeSocket(int id){
		freeIDS[wi] = id;
		wi++;if(wi==MAX_THREADS) wi=0;
		nThreads--;
		//System.out.println("Free socket"+id);
	}
	
	public static void main(String[] args) {
		server = null;
		exit = false;
		threads = new Thread[MAX_THREADS];
		sockets = new SocketHandler[MAX_THREADS];
		freeIDS = new int[MAX_THREADS];
		for(int i=0;i<MAX_THREADS;i++) {
			sockets[i] = new SocketHandler(i);
			freeIDS[i] = i;
		}
		ri = 0;
		wi = 0;
		nThreads = 0;
	
		try{
			server = new ServerSocket(1702,100);
			
			while(!exit && !server.isClosed()){
				if(nThreads < MAX_THREADS){
					sockets[freeIDS[ri]].acceptSocket(server.accept());
					threads[freeIDS[ri]] = new Thread(sockets[freeIDS[ri]]);
					threads[freeIDS[ri]].start();
					//System.out.println("socket"+sockets[freeIDS[ri]].id + " lock");
					ri++; //System.out.println("ri="+ri); 
					if(ri==MAX_THREADS) ri=0;
					//System.out.println("ri="+ri);
					nThreads++;
				}
			}
			server.close();	
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
