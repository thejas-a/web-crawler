package com.assessment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static com.assessment.utils.UrlUtils.*;

public class Crawler {

    public String url;
    private String domain;

    public Crawler(String url){
        this.url = url;
        this.domain = extractDomain(url);
    }


    /**
     * Check if the url belongs to the same domain
     * @param url
     * @return
     */
    private boolean isSameDomain(String url){
        return url.contains(this.domain);
    }


    /**
     * Parse response body into a string
     * @param reader
     * @return
     * @throws IOException
     */
    private String readHtmlContent(BufferedReader reader) throws IOException {
        StringBuilder content = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null){
            content.append(line);
        }
        return content.toString();
    }

    public void crawl(){
        LinkedList<String> crawledUrls = new LinkedList<>();    //stores websites that are crawled successfully
        Queue<String> queue = new LinkedList<>();   //to process each website in the order
        URL website = null;
        HttpURLConnection connection = null;
        queue.add(sanitizeUrl(this.url));

        while(!queue.isEmpty()){
            String url = queue.poll();
            //exception handling to continue crawling of next url from queue if present url throws any exceptions
            try {
                website = new URL(url);
                connection = (HttpURLConnection) website.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    //crawl website only for which we were able to get OK response
                    System.out.println("Crawling: "+url);
                    crawledUrls.add(url);
                    try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                        String htmlContent = this.readHtmlContent(reader);      //read response body
                        Set<String> links = extractLinks(htmlContent);     //parse html content and extract links
                        for(String link : links){
                            //prechecks of URLs before adding to queue
                            /*
                            1. It should be http url
                            2. It should be of same domain
                            3. It must not be already present in queue for processing
                            4. It must not have already been crawled
                             */
                            link = sanitizeUrl(link); //make sure we sanitize url
                            if(validHttpUrl(link) && this.isSameDomain(link)
                                    && !queue.contains(link) && !crawledUrls.contains(link))
                                queue.add(link);
                        }
                    }
                    catch (IOException e){
                        System.err.println("Exception in getting response for URL: "+url);
                    }
                }else{
                    System.out.println("Not OK response " + responseCode + " for " + url);
                }
            } catch (MalformedURLException e) {
                System.err.println("Malformed URL: "+url);
            }
            catch (IOException e) {
                System.err.println("Exception in getting response for URL: "+url);
            }
            catch (Exception e){
                System.err.println("Generic Exception during crawling of URL: "+url);
            }
        }
        if(connection != null)
            connection.disconnect();
        System.out.println("\n---------------------------------------------\n");
        System.out.println("**** Crawling Finished ****\n");
        System.out.println("Following links were visited");
        crawledUrls.forEach(System.out::println);
    }

    public static void main(String[] args) {
        Crawler crawler = new Crawler("https://www.teya.com/");
        crawler.crawl();
    }
}