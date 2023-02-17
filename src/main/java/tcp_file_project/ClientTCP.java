package tcp_file_project;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ClientTCP {
    private static final String DOWNLOAD = "dL";
    private static final String DELETE = "del";

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Syntax: ClientTCP <Command>");
            return;
        }
        ClientTCP clientTCP = new ClientTCP();
        clientTCP.performCommand(args[0]);
    }

    private void performCommand(String command) {
        try {
            switch(command) {
                case DELETE -> deleteFile();
                case DOWNLOAD -> System.out.println("Command not available yet.");
                default -> System.out.println("Not a valid command.");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFile() throws IOException {
        String fileName = promptUserForFileName();
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("10.222.18.133", 4269));
        sc.write(new ByteBuffer[] {ByteBuffer.wrap(DELETE.getBytes()), ByteBuffer.wrap(fileName.getBytes())});
        sc.shutdownOutput();
        // Get response from server side.
        ByteBuffer buffer = ByteBuffer.allocate(400);
        sc.read(buffer);
        sc.close();
        printResponse(buffer);
    }

    private String promptUserForFileName() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the file name: ");
        return scanner.nextLine();
    }

    private void printResponse(ByteBuffer buffer) {
        buffer.flip();
        byte[] response = buffer.array();
        System.out.println(new String(response));
    }
}
