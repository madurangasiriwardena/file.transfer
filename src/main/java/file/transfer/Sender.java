package file.transfer;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.DigestInputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Sender {
	public void sendFile(File file, DataOutputStream dos) throws IOException {
	    if(dos!=null&&file.exists()&&file.isFile())
	    {
	    	
	        FileInputStream input = new FileInputStream(file);
	        dos.writeLong(file.length());
	        System.out.println(file.length());
	        System.out.println(file.getAbsolutePath());
	        int read = 0;
	        
	        byte[] buffer = new byte[1024];
	        
	        while ((read = input.read(buffer)) != -1){
	            dos.write(buffer, 0, read);
	        }
	        dos.flush();
	        input.close();
	        System.out.println("File successfully sent!");
	    }
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException, GeneralSecurityException{
		Sender client = new Sender();
		String publicKeyInputFile = "publickey";
		String fileToSend = "The.Vampire.Diaries.S01E01.Pilot.HDTV.XviD-FQM.avi";
		String host ="127.0.0.1";
		String port = "8989";
		client.procedure(publicKeyInputFile, fileToSend, host, port);
	}
	
	public void procedure(String publicKeyInputFile, String fileToSend, String host, String port) throws UnknownHostException, IOException, GeneralSecurityException{
		String folder = "tmp/";
		File dirBase = new File(folder);
		dirBase.mkdir();
		makeKey();
		File encryptedKey = new File(folder + "encryptedKey");
		File publicKeyFile = new File(publicKeyInputFile);
		saveKey(encryptedKey, publicKeyFile);
		
		File in = new File(fileToSend);
		File out = new File(folder + in.getName());
		encrypt(in, out);
		
		MessageDigest md = MessageDigest.getInstance("MD5");
		DigestInputStream dis = new DigestInputStream(new FileInputStream(in), md);
		File encryptedHash = new File(folder + "encryptedHash");
		byte[] buffer = md.digest();

		dis.close();
		InputStream aa = new ByteArrayInputStream(buffer);
		encrypt(aa, encryptedHash);
		
		Socket socket = null;
//	    String host = "127.0.0.1";

	    socket = new Socket(host, Integer.parseInt(port));
		
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());	
		
		dos.writeUTF(out.getName());
		sendFile(out, dos);
		sendFile(encryptedHash, dos);
		sendFile(encryptedKey, dos);
		
		dos.close();
		socket.close();
		encryptedKey.delete();
		out.delete();
		encryptedHash.delete();
		dirBase.delete();
		
	}
	
public static final int AES_Key_Size = 256;
	
	Cipher pkCipher, aesCipher;
	byte[] aesKey;
	SecretKeySpec aeskeySpec;
	
	/**
	 * Constructor: creates ciphers
	 */
	public Sender() throws GeneralSecurityException {
		// create RSA public key cipher
		pkCipher = Cipher.getInstance("RSA");
	    // create AES shared key cipher
	    aesCipher = Cipher.getInstance("AES");
	}
	
	/**
	 * Creates a new AES key
	 */
	public void makeKey() throws NoSuchAlgorithmException {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
	    kgen.init(AES_Key_Size);
	    SecretKey key = kgen.generateKey();
	    aesKey = key.getEncoded();
	    aeskeySpec = new SecretKeySpec(aesKey, "AES");
	}
	
	/**
	 * Encrypts the AES key to a file using an RSA public key
	 */
	public void saveKey(File out, File publicKeyFile) throws IOException, GeneralSecurityException {
		// read public key to be used to encrypt the AES key
		byte[] encodedKey = new byte[(int)publicKeyFile.length()];
		new FileInputStream(publicKeyFile).read(encodedKey);
		
		// create public key
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PublicKey pk = kf.generatePublic(publicKeySpec);
		
		// write AES key
		pkCipher.init(Cipher.ENCRYPT_MODE, pk);
		CipherOutputStream os = new CipherOutputStream(new FileOutputStream(out), pkCipher);
		os.write(aesKey);
		os.close();
	}
	
	/**
	 * Encrypts and then copies the contents of a given file.
	 */
	public void encrypt(File in, File out) throws IOException, InvalidKeyException {
		aesCipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);
		
		FileInputStream is = new FileInputStream(in);
		CipherOutputStream os = new CipherOutputStream(new FileOutputStream(out), aesCipher);
		
		copy(is, os);
		
		os.close();
	}
	
	public void encrypt(InputStream is, File out) throws IOException, InvalidKeyException {
		aesCipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);
		
//		FileInputStream is = new FileInputStream(in);
		CipherOutputStream os = new CipherOutputStream(new FileOutputStream(out), aesCipher);
		
		copy(is, os);
		
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
