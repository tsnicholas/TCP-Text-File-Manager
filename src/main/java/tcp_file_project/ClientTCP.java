package tcp_file_project;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientTCP {
    private static final String DELETE = "del";

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Syntax: ClientTCP <Command> <FileName>");
            return;
        }
        ClientTCP clientTCP = new ClientTCP();
        clientTCP.performCommand(args[0], args[1]);
    }

    private void performCommand(String command, String fileName) {
        try {
            if (DELETE.equals(command)) {
                deleteFile(fileName);
            } else {
                System.out.println("Not a valid command.");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFile(String fileName) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("10.222.18.133", 4269));
        sc.write(new ByteBuffer[] {ByteBuffer.wrap(DELETE.getBytes()), ByteBuffer.wrap(fileName.getBytes())});
        sc.shutdownOutput();
        ByteBuffer buffer = ByteBuffer.allocate(400);
        sc.read(buffer);
        sc.close();
        printResponse(buffer);
    }

    private void printResponse(ByteBuffer buffer) {
        buffer.flip();
        byte[] response = buffer.array();
        System.out.println(new String(response));
    }
}
