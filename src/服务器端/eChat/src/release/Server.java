package release;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
	static int port = 7179;
	static int thread_init = 10;
	static int thread_max = 10000;
	static int queue_size = 10000;
	static volatile Map<Integer,PrintWriter> printer_map = new ConcurrentHashMap<>();
	static Random random = new Random();
	static SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
	public static void main(String[] args) throws Exception {
		ThreadPoolExecutor executor = new ThreadPoolExecutor(thread_init,thread_max,1,TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queue_size));
		ServerSocket listenSocket = new ServerSocket(port);
		listenSocket.setPerformancePreferences(0, 2, 1);
		System.out.println("thread init num:" + String.valueOf(thread_init));
		System.out.println("thread max num:" + String.valueOf(thread_max));
		System.out.println("queue size:" + String.valueOf(queue_size));
		System.out.println("server start at port:"+String.valueOf(port));
		while(true) {
			Socket clientSocket = listenSocket.accept();
			System.out.println("Connect " + 
			clientSocket.getInetAddress()+" "+print_time());
			Integer uid_t;
			while(true) {
				uid_t = random.nextInt();
				if(!printer_map.containsKey(uid_t))break;
			}
			final Integer uid=uid_t;
			InputStream inStream = clientSocket.getInputStream();
			OutputStream outStream = clientSocket.getOutputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inStream,"UTF-8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(outStream,"UTF-8"));
			printer_map.put(uid, out);
			send_all(new MQ(true,"","",out));
			executor.execute(new Runnable() {
				String user;
				String input;
				public void run() {
					try {
						while(true) {
							user = in.readLine();
							input = in.readLine();
							if(input==null||user==null) {
								System.out.println("Disconnect " + 
							clientSocket.getInetAddress() + " " + print_time());
								printer_map.remove(uid);
								send_all(new MQ(true,"","",out));
								return;
							}
							System.out.print("Message ");
							System.out.print(user+":");
							System.out.println(input+" "+
							clientSocket.getInetAddress() + " " + print_time());
							MQ mq = new MQ(false,user,input,out);
							send_all(mq);
						}
					} catch (Exception e) {
						System.out.println("Disconnect "+
					clientSocket.getInetAddress() + " " + print_time());
						printer_map.remove(uid);
						send_all(new MQ(true,"","",out));
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	static void send_all(MQ mq) {
		int num = printer_map.size();
		Iterator<Map.Entry<Integer, PrintWriter>> it = printer_map.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<Integer, PrintWriter> entry = it.next();
			PrintWriter e = entry.getValue();
			try {
				if(!mq.ifnum) {
					if(e.equals(mq.out))continue;
					e.println(mq.user);
					e.println(mq.message);
					e.flush();
					//System.out.println("send "+ mq.user + ":" + mq.message);
				}else {
					e.println("connect_num");
					e.println(num);
					e.flush();
					//System.out.println("send connect_num " + String.valueOf(printer_list.size()));
				}
			}catch(Exception err) {
				it.remove();
				err.printStackTrace();
			}
		}
	}
	
	static String print_time() {
		return dateFormat.format(new Date());
	}
}

class MQ{
	boolean ifnum;
	String user;
	String message;
	PrintWriter out;
	MQ(boolean ifnum,String user,String message,PrintWriter out){
		this.ifnum = ifnum;
		this.user = user;
		this.message = message;
		this.out = out;
	}
}
