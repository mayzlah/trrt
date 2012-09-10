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
            this.piece_length = ((BEValue)info.getMap().get("piece length")).getInt();
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
                + "&peer_id=-SH0001-121234567890&port=5000&uploaded=0&downloaded=0&left=" + this.length + "&event=started&numwant=10";

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
        
    }

    private void stop() {
    }

    public static void main(String[] args) {
        Torrent torrent = new Torrent("a.torrent");
    }

    String announce;
    String name;
    int length;
    int piece_length;
    byte[] pieces; 
    byte[] info_hash;
    ArrayList<Peer> peers;
}