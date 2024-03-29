package util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * BlindSSLSocketFactoryTest
 *  Simple test to show an Active Directory (LDAP)
 *  and HTTPS connection without verifying the 
 *  server's certificate.
 *  
 * @author Mike McKinney, Platinum Solutions, Inc.
 */
public class BlindSSLSocketFactoryTest extends SocketFactory 
{
	private static SocketFactory blindFactory = null;
	
	/**
	 * Builds an all trusting "blind" ssl socket factory.
	 */
	static 
	{
		// create a trust manager that will purposefully fall down on the
		// job
		TrustManager[] blindTrustMan = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() { return null; }
			public void checkClientTrusted(X509Certificate[] c, String a) { }
			public void checkServerTrusted(X509Certificate[] c, String a) { }
		} };

		// create our "blind" ssl socket factory with our lazy trust manager
		try 
		{
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, blindTrustMan, new java.security.SecureRandom());
			blindFactory = sc.getSocketFactory();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * @see javax.net.SocketFactory#getDefault()
	 */
	public static SocketFactory getDefault() 
	{
		return new BlindSSLSocketFactoryTest();
	}


	/**
	 * @see javax.net.SocketFactory#createSocket(java.lang.String, int)
	 */
	public Socket createSocket(String arg0, int arg1) throws IOException,UnknownHostException 
	{
		return blindFactory.createSocket(arg0, arg1);
	}

	/**
	 * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int)
	 */
	public Socket createSocket(InetAddress arg0, int arg1) throws IOException 
	{
		return blindFactory.createSocket(arg0, arg1);
	}

	/**
	 * @see javax.net.SocketFactory#createSocket(java.lang.String, int,
	 *      java.net.InetAddress, int)
	 */
	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException, UnknownHostException 
	{
		return blindFactory.createSocket(arg0, arg1, arg2, arg3);
	}

	/**
	 * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int,
	 *      java.net.InetAddress, int)
	 */
	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException 
	{
		return blindFactory.createSocket(arg0, arg1, arg2, arg3);
	}

	/**
	 * Our test...
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// do you want to validate the server's SSL cert?
		//   if you have invalid certs, true will produce errors
		boolean validateCert = false; 
		
		// ****************************************************>
		// LDAPS CONNECTION
		// ****************************************************>
		System.out.println("Testing LDAPS connection with validateCert: " + validateCert);
		Hashtable<String, String> env = new Hashtable<String, String>();
		
		// complete URL of Active Directory/LDAP server running SSL with invalid cert
		String url = "ldaps://srvslsidm001.uct.ac.za:636";
		// domain is the Active Directory domain i.e. "yourdomain.com"
		String domain = "<YOUR AD DOMAIN>";
		// the sAMAccountName (i.e. jsmith)
		String login = "<LOGIN>";
		String password = "84503119THDI3217";
		
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, url);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, "cn=DKGTHA005,ou=users,o=uct");
		env.put(Context.SECURITY_CREDENTIALS, password);

		if (url.startsWith("ldaps") && !validateCert) 
		{
			env.put("java.naming.ldap.factory.socket", BlindSSLSocketFactoryTest.class.getName());
			System.out.println(BlindSSLSocketFactoryTest.class.getName());
		}

		try 
		{
			LdapContext ctx = new InitialLdapContext(env, null);
			System.out.println("Successfull bind to " + url + "!");
		} 
		catch (AuthenticationException e) 
		{
			System.out.println("The credentials could not be validated!");
		} 
		catch (NamingException e) 
		{
			e.printStackTrace();
		}
	}
}
