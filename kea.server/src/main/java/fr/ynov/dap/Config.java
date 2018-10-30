package fr.ynov.dap;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
/**
 * Contains all configuration needed to call the Google API.
 * @author Antoine
 *
 */
@Component
//TODO kea by Djer Prendre en compte les retours de Checkstyle ! 
public class Config {
    //TODO kea by Djer On évite de "pré-remplir" des variables avec des "chaines magiques", on utilise des constantes
    //TODO kea by Djer Par "habitude" on place les constantes en premier
	private String credentialsFolder = "/credentials.json";
	private InputStream clientSecretDir = GmailService.class.getResourceAsStream(credentialsFolder);
	private String applicationName = "HoC DaP";
	private String tokensDirectoryPath = "tokens";
    private NetHttpTransport httpTransport = null;
    private String oAuth2CallbackUrl = "/oAuth2Callback";
    /**
     * Instantiate the NetHttpTransport specifically for Google.
     * @throws IOException nothing special
     * @throws GeneralSecurityException nothing special
     */
	public Config() throws IOException, GeneralSecurityException {
		httpTransport = GoogleNetHttpTransport.newTrustedTransport();
	}
	/**
	 * get the URL you want to redirect when authentication is done.
	 * @return relative url after domain name
	 */
	public String getoAuth2CallbackUrl() {
		return oAuth2CallbackUrl;
	}
	/**
	 * set the URL you want to redirect when authentication is done.
	 * @param oAuth2CallbackUrl
	 */
	public void setoAuth2CallbackUrl(String oAuth2CallbackUrl) {
		this.oAuth2CallbackUrl = oAuth2CallbackUrl;
	}
	/**
	 * set the httpTransport.
	 * @param http_transport
	 */
	public  void setHttpTransport(NetHttpTransport http_transport) {
		this.httpTransport = http_transport;
	}
	/**
	 * set the directory where the credentials.json is stored.
	 * @param credentialsFolder
	 */
	public void setCredentialsFolder(String credentialsFolder) {
		this.credentialsFolder = credentialsFolder;
	}
	/**
	 * The InputStream is the superclass of all classes representing
	 * an input stream of bytes.
	 * @param clientSecretDir directory of credentials
	 */
	public void setClientSecretDir(InputStream clientSecretDir) {
		this.clientSecretDir = clientSecretDir;
	}
	/**
	 * sets the application name.
	 * @param applicationName of your application
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	/**
	 * Sets the directory path to the stored credentials.
	 * @param tokensDirectoryPath a string that contains the path
	 */
	public void setTokensDirectoryPath(String tokensDirectoryPath) {
		this.tokensDirectoryPath = tokensDirectoryPath;
	}
	/**
	 * Gets the directory path to the stored credentials.
	 * @return a string that contains the relative path in the project
	 */
	public String getCredentialsFolder() {
		return credentialsFolder;
	}
	/**
	 * The InputStream is the superclass of all classes representing
	 * an input stream of bytes.
	 * @return the inputStream
	 */
	public InputStream getClientSecretDir() {
		return clientSecretDir;
	}
	/**
	 * Gets the application name.
	 * @return  String the application name
	 */
	public String getApplicationName() {
		return applicationName;
	}
	/**
	 * gets the HTTPTransport that is need to communicate.
	 * @return an object to connect in HTTP
	 */
	public NetHttpTransport getHttpTransport() {
		return httpTransport;
	}
	/**
	 * gets the path where tokens are stored (relative in the project).
	 * @return a string that contains the path
	 */
	public String getTokensDirectoryPath() {
		return tokensDirectoryPath;
	}
}