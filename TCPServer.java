import java.io.*;
import java.net.*;

public class TCPServer implements Runnable {

	int port;
	ServerSocket welcomeSocket = null;
	Socket joinSocket = null;
	Thread runningThread = null;
	boolean isStopped = false;
	int howbusy = 0;

	public TCPServer(int port){
		this.port=port;
	}

	public TCPServer(){

	}

	public static void main(String[] args) {
		TCPServer t = new TCPServer();
		t.join();
	}

	public void join(){
		openJoinSocket();
		try {
			DataOutputStream outToServer = new DataOutputStream(this.joinSocket.getOutputStream());
			String joinstr = "Server Join";
			outToServer.writeBytes(joinstr + '\n');
		}
		catch(IOException e){
			throw new RuntimeException("Cannot connect",e);
		}
	}

	public void goodbye(){
		openJoinSocket();
		try {
			DataOutputStream outToServer = new DataOutputStream(this.joinSocket.getOutputStream());
			String goodbyestr = "Server Goodbye";
			outToServer.writeBytes(goodbyestr + '\n');
			outToServer = new DataOutputStream(this.joinSocket.getOutputStream());
			String port = Integer.toString(this.port);
			outToServer.writeBytes(port + '\n');
			this.runningThread.interrupt();
		}
		catch(IOException e){
			throw new RuntimeException("Cannot connect",e);
		}
	}

	public void openJoinSocket(){
		try{
			this.joinSocket = new Socket ("localhost", 6666);
		}
		catch(IOException e){
			throw new RuntimeException("Cannot connect",e);
		}
	}

	public synchronized boolean isStopped() {
		return this.isStopped;
	}

	public synchronized void stop(){
		this.isStopped = true;
		try {
			this.welcomeSocket.close();
			goodbye();
		} catch (IOException e) {
			throw new RuntimeException("Error closing server", e);
		}
	}

	public void openServerSocket(){
		try{
			this.welcomeSocket = new ServerSocket(this.port);
		}
		catch(IOException e){
			throw new RuntimeException("Cannot open port",e);
		}
	}

	public void run() {
		synchronized (this) {
			this.runningThread = Thread.currentThread();
			Thread.currentThread().setName(Integer.toString(this.port));
		}
		openServerSocket();
		String clientSentence;
		String name;
		BufferedReader inFromClient = null;
		while (!isStopped) {
			Socket connectionSocket = null;
			try {
				connectionSocket = this.welcomeSocket.accept();
			} catch (IOException e) {
				if (isStopped()) {
					System.out.println("Server Stopped.");
					return;
				}
				throw new RuntimeException("Error accepting client connection", e);
			}
			try {
				inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				name = inFromClient.readLine();
				inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

				PrintWriter outToClient = new PrintWriter(connectionSocket.getOutputStream(), true);
				clientSentence = inFromClient.readLine();
				System.out.println(name + " wants: " + clientSentence);
				if (clientSentence.equals("Directory Reading")) {
					this.howbusy = 1;
					String text = "";
					File curDir = new File(".");
					File[] listOfFiles = curDir.listFiles();
					for (int i = 0; i < listOfFiles.length; i++) {
						if (listOfFiles[i].isFile()) {
							text = "File: ";
							text += listOfFiles[i].getName();
							outToClient.println(text);
						}
						if (listOfFiles[i].isDirectory()) {
							text = "Directory: ";
							text += listOfFiles[i].getName();
							outToClient.println(text);
						}
					}
					connectionSocket.close();
					this.howbusy = 0;
				} else if (clientSentence.equals("Download")) {
					this.howbusy = 2;
					BufferedOutputStream put = new BufferedOutputStream(connectionSocket.getOutputStream());
					BufferedReader st = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
					String s = st.readLine();
					String path = "C:/Users/berke/IdeaProjects/cse334/";//Change to local path
					File f = new File(path + s);
					if (f.isFile()) {
						FileInputStream fis = new FileInputStream(f);

						byte[] buf = new byte[1024];
						int read;
						while ((read = fis.read(buf, 0, 1024)) != -1) {
							put.write(buf, 0, read);
							put.flush();
						}
						System.out.println("Server " +this.port+" : File transferred");

					} else System.out.println("Server " +this.port+" : File doesn't exist");
					connectionSocket.close();
					this.howbusy = 0;
				} else if (clientSentence.equals("Computation")) {
					this.howbusy = 3;
					inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
					int time = Integer.parseInt(inFromClient.readLine());
					System.out.println("Server will be busy for " + time + " seconds");
					try {
						runningThread.sleep(time * 1000);
					}
					catch(InterruptedException  e){
						System.out.println("Thread sleep interrupted");
					}
					System.out.println("Server " +this.port+"  woke up again.");
					connectionSocket.close();
					this.howbusy = 0;
				}
				else{
					outToClient.println("Request is not understood.");
					connectionSocket.close();
				}
			} catch (IOException e) {
				System.out.println("error");
				stop();
			}
		}

	}
}

