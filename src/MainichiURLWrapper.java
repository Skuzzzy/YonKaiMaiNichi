import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Daniel on 5/20/2015.
 */
public class MainichiURLWrapper {
    private URL javaURL;

    public MainichiURLWrapper(String fullURL) {
        fullURL = purgeMLinks(fullURL);
        try {
            javaURL = new URL(fullURL);
        } catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    public MainichiURLWrapper(String baseURL, String relativeURL) {
        relativeURL = purgeMLinks(relativeURL);
        try {
            URL base = new URL(baseURL);
            javaURL = new URL(base, relativeURL);
        } catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    public String getFullURL() {
        return javaURL.getProtocol()+"://"+javaURL.getHost()+javaURL.getPath();
    }

    public String getContext() {
        return javaURL.getProtocol()+"://"+javaURL.getHost();
    }

    public String getProtocol() {
        return javaURL.getProtocol();
    }

    public String getPath() {
        return javaURL.getPath();
    }

    private String purgeMLinks(String s) {
        return s.replace("/news/m","/news/");
    }

    public String toString() {
        return getFullURL();
    }
}
