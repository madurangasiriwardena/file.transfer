package file.transfer;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class FileEncryptDecrypt {
	public static void main(String[] args) throws GeneralSecurityException, IOException {
		/*
		 * encrypt parameters public key input file AES encrypted file input
		 * file output file
		 * 
		 * decrypt parameters private key input file AES encrypted file input
		 * file output file
		 * 
		 * keygen parameters public key file private key file
		 */
		if(args.length == 1 && args[0].equals("-h")){
			System.out.println("To generate keys");
			System.out.println("-k <File to save public key> <File to save private key>");
			System.out.println("\nTo receive a file");
			System.out.println("-r <Private key input file> <Location to save downloaded file> <Listening port>");
			System.out.println("\nTo send a file");
			System.out.println("-s <Public key input file> <File to send> <Host to connect> <Port to connect>");

		}else if (args.length == 3 && args[0].equals("-k")) {
			String publicKey = args[1];
			String privateKey = args[2];
			GenerateRSAKeys grk = new GenerateRSAKeys();
			grk.GenerateRSAKeysCall(publicKey, privateKey);

		} else if (args.length == 4 && args[0].equals("-r")) {
			
				String privateKeyInputFile = args[1];
				String folder = args[2];
				String port = args[3];
				Receiver receiver = new Receiver();
				receiver.procedure(folder, privateKeyInputFile, port);
			

		} else if (args.length == 5 && args[0].equals("-s")) {
				String publicKeyInputFile = args[1];
				String inputFile = args[2];
				String host = args[3];
				String port = args[4];
				Sender sender = new Sender();
				sender.procedure(publicKeyInputFile, inputFile, host, port);
		} else {
			System.out.println("Illeagal arguments");
		}
	}
}
