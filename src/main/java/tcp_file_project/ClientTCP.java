package tcp_file_project;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ClientTCP {
    private static final String DOWNLOAD = "D";
    private static final String DELETE = "d";
    private static SocketChannel sc;

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Syntax: ClientTCP <IP Address> <Port Number>");
            return;
        }
        try {
            ClientTCP clientTCP = new ClientTCP();
            sc = SocketChannel.open();
            sc.connect(new InetSocketAddress(args[0], Integer.parseInt(args[1])));
            String command = clientTCP.getCommand();
            clientTCP.performCommand(command);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private String getCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your command: ");
        return scanner.nextLine();
    }

    private void performCommand(String command) throws IOException {
        switch(command) {
            case DELETE -> deleteFile();
            case DOWNLOAD -> System.out.println("Command not available yet.");
            default -> System.out.println("Not a valid command.");
        }
    }

    private void deleteFile() throws IOException {
        String output = DELETE + getFileName();
        sc.write(ByteBuffer.wrap(output.getBytes()));
        sc.shutdownOutput();
        ByteBuffer buffer = getServerResponse();
        printResponse(buffer);
    }

    private String getFileName() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the file name: ");
        return scanner.nextLine();
    }

    private ByteBuffer getServerResponse() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(400);
        sc.read(buffer);
        sc.close();
        return buffer;
    }

    private void printResponse(ByteBuffer buffer) {
        buffer.flip();
        byte[] response = buffer.array();
        char responseCode = new String(response).charAt(0);
        switch(responseCode) {
            case 'S' -> System.out.println("Operation Successful.");
            case 'F' -> System.out.println("Operation Failed.");
            default -> System.out.println("Unknown response from server.");
        }
    }
}
