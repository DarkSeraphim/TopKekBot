package com.bo0tzz.topkekbot;


import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.nio.file.Files;

/**
 * Created by bo0tzz
 */
public class Tweeter {
    private static Tweeter instance;

    public final String TWITTER_CONSUMER_KEY;
    public final String TWITTER_CONSUMER_SECRET;
    public final String TWITTER_ACCESS_TOKEN;
    public final String TWITTER_ACCESS_SECRET;

    private Twitter twitter;
    private Configuration config;

    private Tweeter() {
        File file = new File("twitter.json");
        try {
            if (!file.exists() || file.isDirectory()) {
                if (!file.createNewFile()) {
                    throw new RuntimeException("Failed to create new file");
                }
                InputStream in = getClass().getClassLoader().getResourceAsStream("twitter.json");
                Files.copy(in, file.toPath());
            }
            byte[] data = Files.readAllBytes(file.toPath());
            GsonBuilder builder  = new GsonBuilder();
            builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 InputStreamReader in = new InputStreamReader(bais))
            {
                TwitterAuth auth = builder.create().fromJson(in, TwitterAuth.class);
                this.TWITTER_CONSUMER_KEY = auth.consumerKey;
                this.TWITTER_CONSUMER_SECRET = auth.consumerSecret;
                this.TWITTER_ACCESS_TOKEN = auth.accessToken;
                this.TWITTER_ACCESS_SECRET = auth.accessSecret;
            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        config = new ConfigurationBuilder()
                .setDebugEnabled(true)
                .setOAuthConsumerKey(TWITTER_CONSUMER_KEY)
                .setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET)
                .setOAuthAccessToken(TWITTER_ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(TWITTER_ACCESS_SECRET)
                .build();
        twitter = new TwitterFactory(config).getInstance();
        createListener();

    }

    public static Tweeter getInstance() {
        if (instance == null)
            instance = new Tweeter();
        return instance;
    }

    public void sendTweet(String content) {
        try {
            twitter.updateStatus(content);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    private void createListener() {
        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                if (status.getText().contains("@topkekbot")) {
                    SendableTextMessage message = SendableTextMessage.builder()
                            .message("Bot was mentioned in a tweet!\n"
                                    + "@" + status.getUser().getScreenName()
                                    + " (" + status.getUser().getName() + ")"
                                    + ": " + status.getText()).build();
                    TopKekBot.bot.sendMessage(TelegramBot.getChat(-17349250), message);
                } else if ((status.getUser().getScreenName().equalsIgnoreCase("telegram") || status.getUser().getScreenName().equalsIgnoreCase("bo0tzzz")) && !status.getText().contains("@")) {
                    SendableTextMessage message = SendableTextMessage.builder()
                            .message("Tweet by @" + status.getUser().getScreenName() + ": "
                            + status.getText()).build();
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            @Override
            public void onTrackLimitationNotice(int i) {

            }

            @Override
            public void onScrubGeo(long l, long l1) {

            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }

            @Override
            public void onException(Exception e) {

            }
        };
        TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
        twitterStream.addListener(listener);
        String[] track = {"@topkekbot"};
        long[] follow = {1689053928L, 780156121L};
        twitterStream.filter(new FilterQuery(0, follow, track));
    }

    private class TwitterAuth {
        private String consumerKey;

        private String consumerSecret;

        private String accessToken;

        private String accessSecret;
    }
}
