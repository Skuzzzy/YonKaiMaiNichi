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
            "http://mainichi.jp/",
            "http://mainichi.jp/select/shakai/",
            "http://mainichi.jp/select/seiji/",
            "http://mainichi.jp/select/biz/",
            "http://mainichi.jp/select/world/",
            "http://mainichi.jp/select/science/",

            "http://mainichi.jp/sports/",
            "http://mainichi.jp/enta/",
            "http://mainichi.jp/culture/",
            "http://mainichi.jp/life/"

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
                processDocument(document);
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

    public void processDocument(MainichiURLWrapper document) {
        System.out.println("Processing: " + document.getFullURL());

        ArrayList<MainichiURLWrapper> pages = new ArrayList<MainichiURLWrapper>();

        try {
            String fileName = document.getArticleIdentifier();
            Document doc = Jsoup.connect(document.getFullURL()).get();
            Elements storyPars = doc.select("div.NewsBody").first().select("p");
            Element title = doc.select("h1.NewsTitle").first();
            Element publishingInfo = doc.select("p.credit").first();
            Elements pageLinks = doc.select("ul.SearchPageWrap li a");

            for(int i=0; i<pageLinks.size()-1; i++) { // Ignoring the last page returned was a concious decision to avoid ?????
                MainichiURLWrapper anotherPage = new MainichiURLWrapper(document.getContext()+pageLinks.get(i).attr("href"));
                pages.add(anotherPage);
            }

            JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream("output/"+fileName+".json"),"UTF-8"));

            jsonWriter.beginObject();

            jsonWriter.name("title");
            jsonWriter.value(title.text());

            jsonWriter.name("link");
            jsonWriter.value(document.getFullURL());

            jsonWriter.name("datetimeInformation");
            jsonWriter.value(publishingInfo.text());

            jsonWriter.name("pages");
            jsonWriter.value(""+(pages.size()+1));

            jsonWriter.name("contents");
            jsonWriter.beginArray();

            for(Element e : storyPars) {
                jsonWriter.value(e.text().trim());
            }
            for(MainichiURLWrapper additionalPage : pages) {

                try {
                    Document docPage = Jsoup.connect(additionalPage.getFullURL()).get();
                    Elements storyPage = docPage.select("div.NewsBody").first().select("p");
                    for(Element e : storyPage) {
                        jsonWriter.value(e.text().trim());
                    }
                    System.out.println("\t\tProcessed additional page");
                } catch (Exception e) {
                    System.out.println("Failed to get an additional page");
                    e.printStackTrace();
                }
            }


            jsonWriter.endArray();
            jsonWriter.endObject();
            jsonWriter.close();
            System.out.println("\tCreated JSON document: " + fileName+".json");

            Thread.sleep(500);
        } catch (Exception e) {
            System.out.println("\tFailed to process article, possible paywall");
        }

    }
}
