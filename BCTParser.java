import java.io.*;

public class BCTParser {
    
    public BCTParser(String source) {
        File torFile = new File(source);
        try {
            FileReader torReader = new FileReader(torFile);
            metaInfo = new String();
            char buffChar = (char) torReader.read();
            while((int)buffChar > 0) {
                metaInfo = metaInfo + buffChar;
                buffChar = (char) torReader.read();
            }
        }
        catch(FileNotFoundException e) {
            System.err.println("No such file, unfortunatly.");
            metaInfo = null;
        }
        catch(IOException e) {}
    
    }
    
    private int whatIs(int index) {
        switch (metaInfo.charAt(index)) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return 0; break; //String
            case 'i':
                return 1; break; //Integer
            case 'l':
                return 2; break; //List
            case 'd':
                return 3; break; //Dictionary
            default:
                return -1; break; //Wrong element
        }
    }
    
    private int whereEnds(int index) {
    
    }
    
    private void toParse(int index) {
        
    }
    private void parseDict() {
    
    }
    
    private String[] parseList(int index) {
        
    }
    private int parseNumber() {
    
    }
    
    private String parseString(int index) {
        String numString = new String();
        String result = new String();
        while (metaInfo.charAt(index) != ':'){
            numString = numString + metaInfo.charAt(index);
            index ++;
        }
        int length = stringToNumber(numString);
        index ++;
        int counter;
        for(counter=0; counter < length; counter ++)
        {
            result = result + metaInfo.charAt(index+counter);
        }
        return result;
    }
    
    private int stringToNumber (String numString)
    {
        int length = numString.length();
        int counter;
        int aCounter;
        int power;
        int result = 0;
        for(counter = 0; counter < length; counter++)
        {
            power = 1;
            for(aCounter = 0; aCounter < length - 1 - counter; aCounter++) {
                power = power*10;
            }
            System.out.println(result);
            result = result + (numString.charAt(counter) - '0')*power;
        }
        System.out.println(result);
        return result;
    }
    
    public String getURL() {
        int counter;
        int index = metaInfo.indexOf("announce");
        if(index<0)
        {
            System.err.println("File doesn't contents such information: announce");
            return null;
        }
        return parseString(index + "announce".length());    
    }
    
    public String[][] getURLList() {
        int index = metaInfo.indexOf("announce-list");
        int counter;
        if(index<0)
        {
            System.err.println("File doesn't contents such information: announce-list");
            return null;
        }
        return parse
    }
    
    private String metaInfo;    
    //private String announceURL;
    private String info;
    private int crDate;
    private String comment;
    private String crBy;
    private String encodingFormat;
    private String name;
    private int pieceLength;
    private String pieces;
    private int privateFlag;
    private String files;
    private int[] fileLength;
    private String md5sum;
    private String path;
    
    public static void main(String[] args)
    {
        BCTParser shit = new BCTParser("C:\\Users\\alex-mayzlah\\Desktop\\[rutracker.org].t4157545.torrent");
        System.out.println(shit.getURL());
    }
}