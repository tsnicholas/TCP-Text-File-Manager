package tcp_file_project;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class TCPProcessor implements TCP {
    private static final String END_CODE = "~END~";

    public String getMessageData(SocketChannel sc) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(MAX_TRANSFER_SIZE);
        sc.read(buffer);
        buffer.flip();
        return new String(buffer.array()).replace("\0", "");
    }

    public String transferFile(SocketChannel sc, File file) throws IOException {
        char[] readData = new char[MAX_TRANSFER_SIZE];
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while(reader.read(readData, 0, MAX_TRANSFER_SIZE) != -1) {
            sc.write(ByteBuffer.wrap(new String(readData).getBytes()));
            // Reinitialize readData to avoid repeating data that has already been transferred.
            readData = new char[MAX_TRANSFER_SIZE];
        }
        sc.write(ByteBuffer.wrap(END_CODE.getBytes()));
        return getMessageData(sc);
    }

    public void receiveFile(SocketChannel sc, File file) throws IOException {
        while(true) {
            String data = getMessageData(sc);
            if(!data.equals(END_CODE)) {
                writeToFile(file, data);
            } else {
                sc.write(ByteBuffer.wrap(SUCCESS.getBytes()));
                break;
            }
        }
    }

    private void writeToFile(File file, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
        writer.write(content);
        writer.close();
    }
}
