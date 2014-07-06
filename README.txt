Building and running the code

To run the program you will need to replace the  local_policy.jar and US_export_policy.jar in JAVA_HOME/jre/lib/security with the files that can be downloaded from
http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html

Go to the project folder
run the command
	mvn clean install
It will create a jar file file.transfer-1.0-jar-with-dependencies.jar in the target folder.

To generate the the RSA key pair run the command
java -jar file.transfer-1.0-jar-with-dependencies.jar -k <File to save public key> <File to save private key>

To receive the files and decrypt run the command
java -jar file.transfer-1.0-jar-with-dependencies.jar -r <Private key input file> <Location to save downloaded file> <Listening port>

To encrypt and send the file run the following command
java -jar file.transfer-1.0-jar-with-dependencies.jar -s <Public key input file> <File to send> <Host to connect> <Port to connect>
