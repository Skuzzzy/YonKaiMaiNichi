import com.google.gson.stream.JsonWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Created by Daniel on 5/21/2015.
 */
public class MainichiCrawler {
    private String[] seedLinks = {
            "http://mainichi.jp/select/shakai/",
            "http://mainichi.jp/select/seiji/",
            "http://mainichi.jp/select/biz/",
            "http://mainichi.jp/select/world/",
            "http://mainichi.jp/select/science/"
    };

    private ArrayList<MainichiURLWrapper> articlesToProcess;

    private HashSet<String> alreadyProcessedArticles;

    public MainichiCrawler() {
        articlesToProcess = new ArrayList<MainichiURLWrapper>();
        alreadyProcessedArticles = new HashSet<String>();
    }

    public void run() {
        for(String seed : seedLinks) {
            MainichiURLWrapper currentURL = new MainichiURLWrapper(seed);
            articlesToProcess.addAll(getLinksFromURL(currentURL));
        }

        for(MainichiURLWrapper document : articlesToProcess) {
            if(alreadyProcessedArticles.contains(document.getFullURL())) {
                continue;
            } else {
                // Process here
                try {
                    processDocument(document);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                alreadyProcessedArticles.add(document.getFullURL());
            }
        }
    }

    private ArrayList<MainichiURLWrapper> getLinksFromURL(MainichiURLWrapper url) {
        ArrayList<MainichiURLWrapper> links = new ArrayList<MainichiURLWrapper>();
        try {
            Document doc = Jsoup.connect(url.getFullURL()).get();
            Elements linkBlocks = doc.select("body a");
            for(Element linkB : linkBlocks) {
                String extractedURL = linkB.attr("href");
                if(extractedURL.endsWith(".html") && extractedURL.contains("/news/")) {
                    links.add(new MainichiURLWrapper(extractedURL));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return links;
    }

    private static int docnum = 0;

    public void processDocument(MainichiURLWrapper document) throws Exception {
        System.out.println(docnum);
        JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream((docnum++)+".json"),"UTF-8"));
        Document doc = Jsoup.connect(document.getFullURL()).get();
        Element story = doc.select("Div.NewsBody").first();
        Elements pars = story.select("p");

        jsonWriter.beginObject();
        jsonWriter.name(document.getFullURL());
        jsonWriter.beginArray();

        for(Element e : pars) {
            jsonWriter.value(e.text().trim());
        }

        jsonWriter.endArray();
        jsonWriter.endObject();
        jsonWriter.close();
    }
}