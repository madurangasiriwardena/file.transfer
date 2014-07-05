package file.transfer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.DigestInputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;

public class Receiver {
	
public static final int AES_Key_Size = 256;
	
	Cipher pkCipher, aesCipher;
	byte[] aesKey;
	SecretKeySpec aeskeySpec;
	public static void main(String[] args) throws IOException, GeneralSecurityException{
		Receiver server = new Receiver();
		String savePath = "/home/maduranga/Desktop/";
		String privateKeyFile = "/home/maduranga/Desktop/privatekey";
		String port = "8989";
		server.procedure(savePath, privateKeyFile, port);
		
	}
	
	public void procedure(String savePath, String privateKeyFile, String port) throws IOException, GeneralSecurityException{
		if(!savePath.endsWith("/")){
			savePath += "/";
		}
		
		ServerSocket server_socket = new ServerSocket(Integer.parseInt(port));

		Socket socket = server_socket.accept();
		
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		String folder = savePath+"tmp/";
		File dirBase = new File(folder);
		dirBase.mkdir();
		
		String fileName = dis.readUTF();
		File in = new File(folder+fileName);
		readFile(in, dis);
		
		File encryptedHash = new File(folder+"encryptedHash");
		readFile(encryptedHash, dis);

		File encryptedKey = new File(folder+"encryptedKey");
		readFile(encryptedKey, dis);

		dis.close();
		server_socket.close();
		socket.close();
		
		File privateKey = new File(privateKeyFile);
		loadKey(encryptedKey, privateKey);
		File out = new File(savePath+fileName);
		System.out.println("decrypting the file");
		decrypt(in, out);
		System.out.println("file decrepted successfully");
		File hash = new File(folder+"hash"); 
		decrypt(encryptedHash, hash);

		MessageDigest md = MessageDigest.getInstance("MD5");
		DigestInputStream digis = new DigestInputStream(new FileInputStream(out), md);
		byte[] buffer = md.digest();
		byte[] buffer2 = new byte[(int) hash.length()];

		FileInputStream output = new FileInputStream(hash);
		output.read(buffer2);
		output.close();
		digis.close();

		if(Arrays.equals(buffer, buffer2)){
			System.out.println("file downloaded successfully");
		}else{
			System.out.println("error while downloading");
			out.delete();
		}
		
		in.delete();
		encryptedKey.delete();
		encryptedHash.delete();
		hash.delete();
		dirBase.delete();
	}
	
	public void readFile(File file, DataInputStream dis) throws IOException{
		FileOutputStream output = new FileOutputStream(file);
		long len = dis.readLong();
        System.out.println(file.getName());
        int read = 0;
        long counter =0;
        byte[] buffer;
        if(len<1024){
        	buffer = new byte[(int) len];
        }else{
        	buffer = new byte[1024];
        }
       
        while ((read = dis.read(buffer)) != -1){
            output.write(buffer, 0, read);
            counter+= read;
            if(len-counter<1024 && len-counter!=0){
            	buffer = new byte[(int) (len-counter)];
            }
            else if(len-counter==0){
            	break;
            }
        }
        output.close();
        
        System.out.println("File successfully received!");
				
	}
	
	public Receiver() throws GeneralSecurityException {
		// create RSA public key cipher
		pkCipher = Cipher.getInstance("RSA");
	    // create AES shared key cipher
	    aesCipher = Cipher.getInstance("AES");
	}
	

	/**
	 * Decrypts an AES key from a file using an RSA private key
	 */
	public void loadKey(File in, File privateKeyFile) throws GeneralSecurityException, IOException {
		// read private key to be used to decrypt the AES key
		byte[] encodedKey = new byte[(int)privateKeyFile.length()];
		new FileInputStream(privateKeyFile).read(encodedKey);
		
		// create private key
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedKey);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PrivateKey pk = kf.generatePrivate(privateKeySpec);
		
		// read AES key
		pkCipher.init(Cipher.DECRYPT_MODE, pk);
		aesKey = new byte[AES_Key_Size/8];
		CipherInputStream is = new CipherInputStream(new FileInputStream(in), pkCipher);
		is.read(aesKey);
		aeskeySpec = new SecretKeySpec(aesKey, "AES");
	}
	
	/**
	 * Decrypts and then copies the contents of a given file.
	 */
	public void decrypt(File in, File out) throws IOException, InvalidKeyException {
		aesCipher.init(Cipher.DECRYPT_MODE, aeskeySpec);
		
		CipherInputStream is = new CipherInputStream(new FileInputStream(in), aesCipher);
		FileOutputStream os = new FileOutputStream(out);
		
		copy(is, os);
		
		is.close();
		os.close();
	}
	
	/**
	 * Copies a stream.
	 */
	private void copy(InputStream is, OutputStream os) throws IOException {
		int i;
		byte[] b = new byte[1024];
		while((i=is.read(b))!=-1) {
			os.write(b, 0, i);
		}
	}
}
