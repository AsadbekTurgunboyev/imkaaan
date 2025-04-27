package com.example.taxi.translate;


import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TranslateApi {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CompletableFuture<String> translate(String text, String sourceLang, String targetLang) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String encode = URLEncoder.encode(text, "utf-8");
                String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" +
                        sourceLang + "&tl=" + targetLang + "&dt=t&q=" + encode;
                HttpResponse execute = new DefaultHttpClient().execute(new HttpGet(url));
                StatusLine statusLine = execute.getStatusLine();
                if (statusLine.getStatusCode() == 200) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    execute.getEntity().writeTo(byteArrayOutputStream);
                    String result = byteArrayOutputStream.toString();
                    byteArrayOutputStream.close();
                    JSONArray jsonArray = new JSONArray(result).getJSONArray(0);
                    StringBuilder translatedText = new StringBuilder();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        translatedText.append(jsonArray.getJSONArray(i).getString(0));
                    }
                    return translatedText.toString();
                }
                execute.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            } catch (Exception e) {
                Log.e("TranslateApi", e.getMessage());
                return null;
            }
        }, executor);
    }


}

