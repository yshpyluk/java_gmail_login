import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.http.NameValuePair;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuriy on 3/16/2015.
 */
public class GmailLogin {
    private CloseableHttpClient client;
    private final String USER_AGENT = "Mozilla/5.0";

    private String loginUrl = "https://accounts.google.com/ServiceLoginAuth";
    private String gmailUrl = "https://mail.google.com/mail/";

    @Test
    public void loginToGmail() throws IOException {
        CookieStore cookieStore = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

        String page = getPageContent(loginUrl);
        List<NameValuePair> postParams = getFormParams(page, "{LOGIN}", "{PASSWORD}");

        sendPost(loginUrl, postParams);

        WebDriver driver = new FirefoxDriver();
        driver.get("https://mail.google.com/mail/");


        List<Cookie> cookies = cookieStore.getCookies();

        for (Cookie cookie: cookies){
            org.openqa.selenium.Cookie seleniumCookie = new org.openqa.selenium.Cookie(
                    cookie.getName(),
                    cookie.getValue(),
                    cookie.getDomain(),
                    cookie.getPath(),
                    cookie.getExpiryDate()
            );

            driver.manage().addCookie(seleniumCookie);
        }

        driver.navigate().to(gmailUrl);
    }

    private void sendPost(String url, List<NameValuePair> postParams) throws IOException {
        HttpPost post = new HttpPost(url);

        post.setHeader("Host", "accounts.google.com");
        post.setHeader("User-Agent", USER_AGENT);
        post.setHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        post.setHeader("Accept-Language", "en-US,en;q=0.5");
        post.setHeader("Connection", "keep-alive");
        post.setHeader("Referer", "https://accounts.google.com/ServiceLoginAuth");
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(new UrlEncodedFormEntity(postParams));

        client.execute(post);
    }

    private String getPageContent(String url) throws IOException {
        HttpGet request = new HttpGet(url);

        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");

        HttpResponse response = client.execute(request);

        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();

        String line;
        while ((line = reader.readLine()) != null){
            result.append(line);
        }

        return result.toString();
    }

    private List<NameValuePair> getFormParams(String html, String username, String password){
        Document doc = Jsoup.parse(html);

        Element loginForm = doc.getElementById("gaia_loginform");
        Elements inputElements = loginForm.getElementsByTag("input");

        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        for(Element inputElement: inputElements){
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");

            if (key.equals("Email")){
                value = username;
            } else if (key.equals("Passwd")){
                value = password;
            }

            paramList.add(new BasicNameValuePair(key, value));
        }

        return paramList;
    }
}
