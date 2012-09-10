import java.util.concurrent.locks.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class PeerDownloader implements Runnable {
    public PeerDownloader(Peer peer, Torrent torrent) {
        this.peer = peer;
        this.torrent = torrent;
        this.currentPieceIndex = -1;
    }

    public void run() {
        while(Arrays.asList(this.torrent.getFlags()).contains(0)) {
            // select piece to download via this connection
            dataLock.lock();
            byte[] flags = this.torrent.getFlags();
            for(int i = 0; i < flags.length; i++) {
                if(flags[i] == 0) {
                    this.torrent.setFlag(i, (byte)1);
                    this.currentPieceIndex = i;
                }
            }
            dataLock.unlock();
            connect();
            interested();
            request(this.currentPieceIndex, 0, Math.pow(2, 14));
        }
    }

    private void connect() {
        // establish connection
        try {
            Socket connection = new Socket(this.peer.getAddress(), this.peer.getPort());
            input = connection.getInputStream();
            output = connection.getOutputStream();
            // send handshake
            byte[] handshake = new byte[49+19];
            //shitty shit 
            handshake[0] = 19;
            byte[] temp = new byte[100];
            char[] t = "BitTorrent protocol".toCharArray();
            for(int i = 0; i < t.length; i++) {
                temp[i] = (byte)t[i];
            }
            t = "-SH0001-121234567890".toCharArray();
            System.arraycopy(temp, 0, handshake, 1, "BitTorrent protocol".length());
            System.arraycopy(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, handshake, 20, 8);
            System.arraycopy(this.torrent.getInfoHash(), 0, handshake, 28, this.torrent.getInfoHash().length);
            for(int i = 0; i < t.length; i++) {
                temp[i] = (byte)t[i];
            }
            System.arraycopy(temp, 0, handshake, 48, 20);
            temp = null;
            System.out.println(handshake);
            output.write(handshake);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void interested() {

    }

    private Lock dataLock = new ReentrantLock();
    private Peer peer;
    private Torrent torrent;
    private int currentPieceIndex;
    private InputStream input;
    private OutputStream output;
}