package tcp_file_project;

import java.io.File;
import java.io.IOException;

public class ClientTCP {
    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Syntax: ClientTCP <Command> (<FileName>)");
            return;
        }
        ClientTCP clientTCP = new ClientTCP();
        clientTCP.performCommand(args[0], args[1]);
    }

    private void performCommand(String command, String fileName) {
        try {
            switch(command) {
                // TODO: Add the rest of the commands here.
                case "del" -> deleteFile(fileName);
                default -> System.out.println("Not a valid command.");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFile(String fileName) throws IOException {
        File file = new File(fileName);
    }
}
