import java.net.InetAddress;
import java.net.UnknownHostException;
public class Peer {

	public Peer(byte[] addr, int port) throws UnknownHostException {
		this.address = InetAddress.getByAddress(addr);
		this.port = port;
		System.out.println("address: " + this.address.getHostAddress() + ", port: " + this.port);
	}

	private InetAddress address;
	private int port;
	private boolean chocked = true;
	private boolean interested = false;
}