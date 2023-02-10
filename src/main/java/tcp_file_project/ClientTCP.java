package tcp_file_project;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ClientTCP {
    public static void main(String[] args) throws IOException {
        if(args.length != 2) {
            System.out.println("Syntax: ClientTCP <ServerIP> <ServerPort>");
            return;
        }
        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);
        // Prompt the user for a message.
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a message: ");
        String msg = scanner.nextLine();
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
        // create a SocketChannel by calling open()
        SocketChannel sc = SocketChannel.open();
        // Initiate a connection request to server.
        // This request will be handled by the accept() on the server side.
        sc.connect(new InetSocketAddress(serverIP, serverPort));
        // Send message to the server side.
        sc.write(buffer);
        // Important: must shut down output when done sending.
        sc.shutdownOutput();
        // Read from the server to receive the response.
        buffer.flip();
        sc.read(buffer);
        sc.close();
        // Display the response.
        buffer.flip();
        byte[] response = buffer.array();
        System.out.println(new String(response));
    }
}
