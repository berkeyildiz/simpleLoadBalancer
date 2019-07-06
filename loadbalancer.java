import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Set;

public class loadbalancer{

    public static void main(String[] args) throws Exception {
        String clientSentence;
        String name;
        ServerSocket welcomeSocket = new ServerSocket(6666);
        BufferedReader inFromClient = null;
        ArrayList<TCPServer> allservers = new ArrayList<>();
        int ports = 6667;
        while(true) {
            Socket connectionSocket = welcomeSocket.accept();
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            PrintWriter outToClient = new PrintWriter(connectionSocket.getOutputStream(),true);
            clientSentence = inFromClient.readLine();
            System.out.println(clientSentence);

            if (clientSentence.equals("Server Join")){
                TCPServer sw = new TCPServer(ports);
                allservers.add(sw);
                System.out.println("New server is joining with port number :" + sw.port);
                new Thread(sw).start();
                ports++;
                connectionSocket.close();
            }

            if (clientSentence.equals("Server Goodbye")){
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                int port = Integer.parseInt(inFromClient.readLine());
                System.out.println("New server  with port number :" + port);
                for(int i =0; i<allservers.size(); i++) {
                    if (allservers.get(i).port == port) {
                        allservers.remove(i);
                    }
                }
                connectionSocket.close();
            }

            else if(clientSentence.equals("Client Request")){
                Set<Thread> threads = Thread.getAllStackTraces().keySet();
                for(Thread t : threads){
                    String namet = t.getName();
                    Thread.State state = t.getState();
                    System.out.printf("%-20s \t %s \t \n", namet, state);
                }
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                int choice = Integer.parseInt(inFromClient.readLine());
                int min=3;
                int j = 0;
                for(int i =0; i<allservers.size(); i++) {
                    System.out.println(allservers.get(i).port);
                    if (allservers.get(i).howbusy < min) {
                        min = allservers.get(i).howbusy;
                        j = i;
                    }
                }
                if (min!=0 && min+choice>3){
                    System.out.println("There is no available servers for computation");

                    /*TCPServer sw = new TCPServer(ports);     #I could start a new server here, and send it's port to the client.
                    allservers.add(sw);
                    System.out.println("New server is joining with port number :" + sw.port);
                    new Thread(sw).start();
                    outToClient.println(ports);
                    System.out.println("Load balancer sending port: "+ ports);
                    ports++;*/
                }
                else{
                    String port = Integer.toString(allservers.get(j).port);
                    outToClient.println(port);
                    System.out.println("Load balancer sending port: "+ port);
                }
                connectionSocket.close();
            }
        }
    }
}