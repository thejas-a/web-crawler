package com.assessment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Crawler {

    public String url;
    private String domain;

    //extract domain name from the given url
    private static String extractDomain(String url){
        String regex = "[a-zA-Z]+\\.(com|net|io|ai)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if(matcher.find()){
            return matcher.group(0);
        }else{
            return ".com";  //defaults to .com
        }
    }

    public Crawler(String url){
        this.url = url;
        this.domain = extractDomain(url);
    }


    private boolean isSameDomain(String url){
        return url.contains(this.domain);
    }

    private boolean validHttpUrl(String url){
        return url.contains("https") || url.contains("http");
    }

    private Set<String> extractLinks(String htmlContent){
        Document document = Jsoup.parse(htmlContent);
        Elements elements = document.select("a[href]");
        return elements.stream().map(e -> e.attr("abs:href")).collect(Collectors.toSet());
    }

    private String readHtmlContent(BufferedReader reader) throws IOException {
        StringBuilder content = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null){
            content.append(line);
        }
        return content.toString();
    }

    public void crawl(){
        LinkedList<String> crawledUrls = new LinkedList<>();
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        URL website = null;
        HttpURLConnection connection = null;
        queue.add(this.url);

        while(!queue.isEmpty()){
            String url = queue.poll();
            visited.add(url);

            try {
                website = new URL(url);
                connection = (HttpURLConnection) website.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    System.out.println("Crawling: "+url);
                    crawledUrls.add(url);
                    try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                        String htmlContent = this.readHtmlContent(reader);
                        Set<String> links = this.extractLinks(htmlContent);
                        for(String link : links){
                            if(this.validHttpUrl(link) && this.isSameDomain(link) && !visited.contains(link))
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
        }
        if(connection != null)
            connection.disconnect();
        System.out.println("\n---------------------------------------------\n");
        System.out.println("**** Crawling Finished ****");
        System.out.println("Following links were visited");
        crawledUrls.forEach(System.out::println);
    }

    public static void main(String[] args) {
        Crawler crawler = new Crawler("https://www.teya.com");
        crawler.crawl();
    }
}