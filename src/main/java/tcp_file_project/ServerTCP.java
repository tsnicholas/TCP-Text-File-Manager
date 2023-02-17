package tcp_file_project;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerTCP {
    public static void main(String[] args) throws IOException {
        if (args.length != 1){
            System.out.println("Usage: ServerTCP <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);

        ServerSocketChannel listenChannel = ServerSocketChannel.open();

        listenChannel.bind(new InetSocketAddress(port));

        while(true){
            SocketChannel serveChannel = listenChannel.accept();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            serveChannel.read(buffer);
            buffer.flip();
            byte[] bytes = buffer.array();
            String messageToRead = new String(bytes);
            System.out.println(messageToRead);
            buffer.rewind();

            ServerTCP server = new ServerTCP();
            server.performCommand(messageToRead);

            serveChannel.write(buffer);
            serveChannel.close();
        }

    }

    private void performCommand(String messageToRead) {

        try {
            String command = String.valueOf(messageToRead.charAt(0));
            String fileName = messageToRead.substring(1);
            switch(command) {
                case "d" -> deleteFile(fileName);
                default -> System.out.println("Not a valid command.");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    private void deleteFile(String fileName) {
        File file = new File(fileName);
    }
}
