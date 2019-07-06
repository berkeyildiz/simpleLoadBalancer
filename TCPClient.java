import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Set;

public class TCPClient {

    public static void main(String argv[]){
        String name;
        Scanner sc = new Scanner(System.in);
        System.out.println("Your name please:");
        name = sc.next();
        new Thread(new Worker(name)).start();
    }
}
class Worker implements Runnable{

    String name;

    public Worker(String name){
        this.name = name;
    }

    public void run(){
        Thread.currentThread().setName(this.name);
        while(!Thread.currentThread().isInterrupted()) {
            System.out.println("Started for "+ this.name);
            Scanner sc = new Scanner(System.in);
            System.out.println(this.name+" Enter 1 for directory listing, 2 for file transfer, 3 for computation 4 for new client 5 for exit");
            int choice = sc.nextInt();
            if (choice==4){
                System.out.println("Your name please:");
                name = sc.next();
                new Thread(new Worker(name)).start();
                System.out.println("New client is created");
                Set<Thread> threads = Thread.getAllStackTraces().keySet();
                for(Thread t : threads){
                    String namet = t.getName();
                    Thread.State state = t.getState();
                    System.out.printf("%-20s \t %s \t \n", namet, state);
                }
                System.out.println(Thread.currentThread().getName());
            }
            else if (choice==5){
                System.out.println("Exiting from the "+ this.name);
                Thread.currentThread().interrupt();
            }
            if(choice==1 || choice==2 || choice==3) {
                try {
                    String requesting = "Client Request";
                    Socket mysocket = new Socket("localhost", 6666);
                    DataOutputStream outToServer = new DataOutputStream(mysocket.getOutputStream());
                    outToServer.writeBytes(requesting + '\n');
                    try {
                        System.out.println("sleeping " + Thread.currentThread().getName());
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Thread sleep interrupted");
                    }
                    String choicetoloader = Integer.toString(choice);
                    outToServer = new DataOutputStream(mysocket.getOutputStream());
                    outToServer.writeBytes(choicetoloader + '\n');

                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(mysocket.getInputStream()));
                    int port = Integer.parseInt(inFromServer.readLine());
                    System.out.println(this.name + " Connecting to: " + port);
                    switch (choice) {

                        case 1:
                            mysocket = new Socket("localhost", port);
                            outToServer = new DataOutputStream(mysocket.getOutputStream());//sending the name
                            outToServer.writeBytes(name + '\n');//sending the name
                            try {
                                Thread.currentThread().sleep(1000);
                            } catch (InterruptedException e) {
                                System.out.println("Thread sleep interrupted");
                            }
                            String dir = "Directory Reading";
                            outToServer = new DataOutputStream(mysocket.getOutputStream());
                            outToServer.writeBytes(dir + '\n');
                            inFromServer = new BufferedReader(new InputStreamReader(mysocket.getInputStream()));
                            String line = null;
                            while ((line = inFromServer.readLine()) != null && mysocket.isConnected()) {
                                System.out.println(line);
                            }
                            break;

                        case 2:
                            mysocket = new Socket("localhost", port);
                            outToServer = new DataOutputStream(mysocket.getOutputStream());//sending the name
                            outToServer.writeBytes(name + '\n');//sending the name
                            try {
                                Thread.currentThread().sleep(1000);
                            } catch (InterruptedException e) {
                                System.out.println("Thread sleep interrupted");
                            }
                            String download = "Download";
                            outToServer = new DataOutputStream(mysocket.getOutputStream());
                            outToServer.writeBytes(download + '\n');

                            BufferedInputStream get = new BufferedInputStream(mysocket.getInputStream());
                            PrintWriter put = new PrintWriter(mysocket.getOutputStream(), true);

                            String f;
                            int u;
                            System.out.println(this.name + " Enter the file name that exist in the Server");
                            DataInputStream dis = new DataInputStream(System.in);
                            f = dis.readLine();
                            put.println(f);
                            System.out.println(this.name + " Enter the name you want to save");
                            String saveas = sc.next();
                            File f1 = new File(saveas);
                            String str = "C:/Users/berke/IdeaProjects/cse334/testdir";//change to local path
                            FileOutputStream fs = new FileOutputStream(new File(str, f1.toString()));
                            byte arr[] = new byte[1024];
                            while ((u = get.read(arr, 0, 1024)) != -1) {
                                fs.write(arr, 0, u);
                            }
                            fs.close();
                            System.out.println(this.name + " completed");
                            break;

                        case 3:
                            mysocket = new Socket("localhost", port);
                            outToServer = new DataOutputStream(mysocket.getOutputStream());//sending the name
                            outToServer.writeBytes(name + '\n');//sending the name
                            try {
                                Thread.currentThread().sleep(1000);
                            } catch (InterruptedException e) {
                                System.out.println("Thread sleep interrupted");
                            }
                            System.out.println(this.name + " Enter the time you want to make the server busy in seconds");

                            String computation = "Computation";
                            outToServer = new DataOutputStream(mysocket.getOutputStream());
                            outToServer.writeBytes(computation + '\n');
                            String time = sc.next();
                            outToServer = new DataOutputStream(mysocket.getOutputStream());
                            outToServer.writeBytes(time + '\n');
                            break;
                    }
                } catch (IOException e) {
                    System.out.println("error connecting to socket");
                }
            }
        }
    }
}




