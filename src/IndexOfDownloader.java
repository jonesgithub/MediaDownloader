import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by robin on 15.06.2016.
 *
 * Created for Crawling default IndexOf pages
 * generated by Apache Webserver
 *
 */

public class IndexOfDownloader extends Downloader {

    // DEBUG
    private Document site;
    private ArrayList<String> alLinks;
    private HTTPCredentials credentials;

    public IndexOfDownloader(){}
    public IndexOfDownloader(HTTPCredentials cred){
        credentials = cred;
    }

    public void parse(String sRootUrl){

    }


    public ArrayList<String> getLinks(){return alLinks;}

    /**
     * Returns all files which match the pattern. Files
     * are also found in subdirectories of the root-url.
     * @param sRootUrl Start adress of the Index-Of page where
     *                 recursive search shall begin.
     * @param sEnding Pattern for files to search, e.g. ".mkv"
     */
    public ArrayList<String> getFilesRecursive(String sRootUrl, String sEnding){
        ArrayList<String> alLnkFiles = new ArrayList<>();
        LinkCollection curLinks = getLinks(sRootUrl,sEnding);

        // Add files on current hierarchy
        alLnkFiles.addAll(curLinks.files);

        // Recursion over directories
        for (String dir : curLinks.directories){
            alLnkFiles.addAll(getFilesRecursive(dir,sEnding));
        }

        return alLnkFiles;
    }

    private LinkCollection getLinks(String sCurrentUrl, String sEnding){
        LinkCollection lnkCol = new LinkCollection();

        HTTPAnalyzer webObj;
        if(credentials == null)
            webObj = new HTTPAnalyzer(sCurrentUrl);
        else
            webObj = new HTTPAnalyzer(sCurrentUrl, credentials);

        // TODO: May throw ex if retCode was sth. unlike 200
        int retCode = webObj.parse();
        Elements elemImg = webObj.getDocument().select("img");
        for(Element elem : elemImg){

            // Look for ending of the image-filename
            String src = elem.attr("src");
            String[] split = src.split("/");
            String ending = split[split.length-1];

            // (do not recognize "back-button"
            // & table header)
            if(ending.startsWith("back") ||
                    ending.startsWith("blank"))
                continue;

            // Select row and therefore link in other column
            Element tr = elem.parent().parent();
            String link = tr.select("td > a").attr("href");

            if(link.equals(""))
                continue;
            // Absolute link -> normally have to combine
            // current site url and relative link
            String absLink = !link.startsWith("http") ?
                    combineLinks(sCurrentUrl,link) : link;

            // Folder image
            if(ending.startsWith("folder"))
                lnkCol.directories.add(absLink);

            // File image
            else if(link.endsWith(sEnding))
                lnkCol.files.add(absLink);
        }
        return lnkCol;
    }

    private String combineLinks(String sBaseUrl, String sLink){
        if(!sBaseUrl.endsWith("/"))
            sBaseUrl += "/";
        if(sLink.startsWith("/"))
            sLink.replaceFirst("/","");
        return sBaseUrl + sLink;
    }

    private class LinkCollection{
        public ArrayList<String> directories;
        public ArrayList<String> files;
        public LinkCollection(){
            directories = new ArrayList<>();
            files = new ArrayList<>();
        }
    }

    public void test() throws IOException {
        //site = Jsoup.parse(new File("C:\\Users\\gehtdich\\Desktop\\Index of _HDD_JDownloader.html"),
          //      "UTF-8");
        ArrayList<String> filesRecursive;
        credentials = new HTTPCredentials("robin","dhbwfn");
        try {
            filesRecursive = getFilesRecursive("http://r3dst0rm.ddns.net/HDD/JDownloader/Shows/The%20Walking%20Dead/", ".mkv");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
            String s = "";
    }
}
