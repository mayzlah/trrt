import java.io.*;
import java.net.*;
import java.util.*;
import java.math.BigInteger;
public class Torrent
{
    public Torrent(String fileName) {
        //parse bdecoder info
        BDecoder t = null;
        BEValue peers = null;
        BEValue interval = null;
        try {
            torrent = new HashMap<String, Object>();
            // get decoded .torrent content
            t = new BDecoder(new FileInputStream(fileName));
            BEValue result = (BEValue)(t.bdecode());
            // parse announce url
            BEValue announce = (BEValue)(result.getMap().get("announce"));
            torrent.put("announce", announce.getString());
            // parse info dictionary
            BEValue info = (BEValue)(result.getMap().get("info"));
            torrent.put("name", ((BEValue)info.getMap().get("name")).getString());
            torrent.put("length", ((BEValue)info.getMap().get("length")).getNumber());
            torrent.put("piece length", ((BEValue)info.getMap().get("piece length")).getNumber());
            torrent.put("pieces", (byte[])((BEValue)info.getMap().get("pieces")).getBytes());
            torrent.put("info_hash", t.get_special_map_digest());

            // convertation byte array to string LOOKS LIKE SHIT - REWRITE
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            for(byte b : (byte[])torrent.get("info_hash") ) {
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

            String announce_url = torrent.get("announce") + "&info_hash=" + encoded_info_hash
                + "&peer_id=-SH0001-121234567890&port=6885&uploaded=0&downloaded=0&left=" + torrent.get("length") + "&event=started&numwant=10";

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
            peers = (BEValue)(result.getMap().get("peers"));
            bis.close();
            is.close();

        }
        catch(IOException e) {
            System.out.println("Shit shit SHIIIIT!");
        }
    }

    private void start() {
    }

    private void stop() {
    }

    private HashMap<String, Object> torrent;

    public static void main(String[] args) {
        Torrent torrent = new Torrent("a.torrent");
    }
}