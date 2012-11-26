import java.io.*;
import java.net.*;
import java.util.*;
import java.math.BigInteger;
import java.nio.*;

public class Torrent
{
    public Torrent(String fileName) {
        //parse bdecoder info
        BDecoder t = null;
        byte[] peers = null;
        BEValue interval = null;
        try {
            // get decoded .torrent content
            t = new BDecoder(new FileInputStream(fileName));
            BEValue result = (BEValue)(t.bdecode());
            // parse announce url
            BEValue announce = (BEValue)(result.getMap().get("announce"));
            this.announce = announce.getString();
            // parse info dictionary
            BEValue info = (BEValue)(result.getMap().get("info"));
            this.name = ((BEValue)info.getMap().get("name")).getString();
            this.length = ((BEValue)info.getMap().get("length")).getInt();
            this.pieceLength = ((BEValue)info.getMap().get("piece length")).getInt();
            this.pieces = (byte[])((BEValue)info.getMap().get("pieces")).getBytes();
            this.info_hash = t.get_special_map_digest();

            // convertation byte array to string LOOKS LIKE SHIT - REWRITE
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            for(byte b : this.info_hash ) {
                ps.printf("%02x",b);
            }
            ps.println();
            String info_hash = baos.toString();
            // info hash contains hash in human readable format
            String encoded_info_hash = new String();

            for(int i=0; i < info_hash.length()/2; i++)
            {
                encoded_info_hash += "%" + info_hash.substring(i*2, i*2 + 2);
            }

            System.out.println(encoded_info_hash);

            String announce_url = this.announce + "&info_hash=" + encoded_info_hash
                + "&peer_id=-SH0001-121234567890&port=" + PORT + "&uploaded=0&downloaded=0&left=" + this.length + "&event=started&numwant=10";

            URL url = new URL(announce_url);
            System.out.println(announce_url);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            System.out.println("connection established");
            System.out.println("response code: " + connection.getResponseCode());
            InputStream is = connection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            t = new BDecoder(bis);
            result = (BEValue)t.bdecode();
            System.out.println("encoder created: " + result);
            interval = (BEValue)(result.getMap().get("interval"));
            System.out.println("interval is: "+interval.getInt());
            peers = (byte[])(((BEValue)result.getMap().get("peers")).getBytes());
            bis.close();
            is.close();
            if(peers.length%6 == 0) {
                parseBinaryPeers(peers);
            }
            else {
                parseDictPeers((BEValue)result.getMap().get("peers"));
            }
            this.start();
        }
        catch(IOException e) {
            System.out.println("Shit happens =(");
        }
    }

    public byte[] getFlags() {
        return flags;
    }

    public void setFlag(int index, byte value) {
        flags[index] = value;
    }

    public byte[] getPieceHash(int index) {
        return Arrays.copyOfRange(this.pieces, index * 20, index * 20 + 20);
    }

    public byte[] getInfoHash() {
        return this.info_hash;
    }

    public static void main(String[] args) {
        Torrent torrent = new Torrent("a.torrent");
    }

    private void parseBinaryPeers(byte[] peers) throws UnknownHostException {
        System.out.println("List of peers\n===============================================================================================");
        this.peers = new ArrayList<Peer>(peers.length/6);
        byte[] ip = new byte[4];
        int port;
        for(int i=0; i < peers.length/6; i++) {
            for(int j=0; j < 4; j++) {
                ip[j]=peers[i*6+j];    
            }
            byte hb = peers[i*6+4];
            byte lb = peers[i*6+5];
            port = parsePort(hb, lb);
            this.peers.add(new Peer(ip, port));
        }
        flags = new byte[this.pieces.length / 20 + (this.pieces.length % 20 == 0 ? 0 : 1)];
    }

    private void parseDictPeers(BEValue peers) {
        ;
    }

    private int parsePort(byte hb, byte lb) {
        int i = 0;
        i |= hb & 0xFF;
        i <<= 8;
        i |= lb  & 0xFF;
        return i;
    }

    private void start() {
        System.out.println("In start: peers length:" + this.peers.size());
        int i = 0;
        Peer peer = peers.get(0);
        
        Runnable p = new PeerDownloader(peer, this);
        downloaders.add( (PeerDownloader)p );
        p.run();
    }

    private void stop() {
    }

    public static final int PORT = 7600;

    private String announce;
    private String name;
    private int length;
    private int pieceLength;
    private byte[] pieces; 
    private byte[] info_hash;
    private byte[] flags; // array of flags of each piece state: 0 - not downloaded, 1 - in progress, 2 - downloaded
    private ArrayList<Peer> peers;
    private ArrayList<PeerDownloader> downloaders = new ArrayList<PeerDownloader>();
}