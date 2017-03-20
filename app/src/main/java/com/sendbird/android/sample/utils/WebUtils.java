package com.sendbird.android.sample.utils;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Patterns;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * A class with static util methods.
 */

public class WebUtils {

    // This class should not be initialized
    private WebUtils() {

    }

    /**
     * Extract urls from string.
     * @param input
     * @return
     */
    public static List<String> extractUrls(String input)
    {
        List<String> result = new ArrayList<String>();

        String[] words = input.split("\\s+");


        Pattern pattern = Patterns.WEB_URL;
        for(String word : words)
        {
            if(pattern.matcher(word).find())
            {
                if(!word.toLowerCase().contains("http://") && !word.toLowerCase().contains("https://"))
                {
                    word = "http://" + word;
                }
                result.add(word);
            }
        }

        return result;
    }

    /**
     * Scrap page information of given URL.
     *
     * ScrapInfo will contain below information.
     *
     * site_name
     * title
     * description
     * image
     * url
     */
    public static abstract class UrlPreviewAsyncTask extends AsyncTask<String, Void, UrlPreviewInfo> {
        private final int TIMEOUT_MILLIS = 10 * 1000;

        @Override
        protected abstract void onPostExecute(UrlPreviewInfo info);

        @Override
        protected UrlPreviewInfo doInBackground(String... params) {
            Hashtable<String, String> result = new Hashtable<>();
            String url = params[0];
            Document doc = null;
            try {
                doc = Jsoup.connect(url).followRedirects(true).timeout(TIMEOUT_MILLIS).get();

                Elements ogTags = doc.select("meta[property^=og:]");
                for (int i = 0; i < ogTags.size(); i++) {
                    Element tag = ogTags.get(i);

                    String text = tag.attr("property");
                    if ("og:image".equals(text)) {
                        result.put("image", tag.attr("content"));
                    } else if ("og:description".equals(text)) {
                        result.put("description", tag.attr("content"));
                    } else if ("og:title".equals(text)) {
                        result.put("title", tag.attr("content"));
                    } else if ("og:site_name".equals(text)) {
                        result.put("site_name", tag.attr("content"));
                    } else if ("og:url".equals(text)) {
                        result.put("url", tag.attr("content"));
                    }
                }

                ogTags = doc.select("meta[property^=twitter:]");
                for (int i = 0; i < ogTags.size(); i++) {
                    Element tag = ogTags.get(i);

                    String text = tag.attr("property");
                    if ("twitter:image".equals(text)) {
                        if(!result.containsKey("image")) {
                            result.put("image", tag.attr("content"));
                        }
                    } else if ("twitter:description".equals(text)) {
                        if(!result.containsKey("description")) {
                            result.put("description", tag.attr("content"));
                        }
                    } else if ("twitter:title".equals(text)) {
                        if(!result.containsKey("title")) {
                            result.put("title", tag.attr("content"));
                        }
                    } else if ("twitter:site".equals(text)) {
                        if(!result.containsKey("site_name")) {
                            result.put("site_name", tag.attr("content"));
                        }
                    } else if ("twitter:url".equals(text)) {
                        if(!result.containsKey("url")) {
                            result.put("url", tag.attr("content"));
                        }
                    }
                }

                if(!result.containsKey("url")) {
                    result.put("url", url);
                }

                /**
                 * site_name, title, image, description, url
                 */
                if(result.keySet().size() == 5) {
                    return new UrlPreviewInfo(
                            result.get("url"),
                            result.get("site_name"),
                            result.get("title"),
                            result.get("description"),
                            result.get("image")
                    );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
