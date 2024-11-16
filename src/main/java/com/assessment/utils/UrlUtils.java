package com.assessment.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UrlUtils {
    /**
     * extract domain name from the given url
     * @param url
     * @return
     */
    public static String extractDomain(String url){
        String regex = "[a-zA-Z]+\\.(com|net|io|ai)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if(matcher.find()){
            return matcher.group(0);
        }else{
            return ".com";  //defaults to .com
        }
    }

    /**
     * Sanitize the given url
     * @param url
     * @return
     */
    public static String sanitizeUrl(String url){
        //remove trailing slash to treat "www.teya.com/" and "www.teya.com" are same
        if(url.endsWith("/")){
            url = url.substring(0, url.length()-1);
        }
        return url;
    }

    /**
     * Check if the given url is of http type
     * @param url
     * @return
     */
    public static boolean validHttpUrl(String url){
        return url.contains("https") || url.contains("http");
    }

    /**
     * Extract links from the given html content
     * @param htmlContent
     * @return
     */
    public static Set<String> extractLinks(String htmlContent){
        Document document = Jsoup.parse(htmlContent);
        Elements elements = document.select("a[href]");
        return elements.stream().map(e -> e.attr("abs:href")).collect(Collectors.toSet());
    }
}
