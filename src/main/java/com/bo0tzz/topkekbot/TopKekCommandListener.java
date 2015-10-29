package com.bo0tzz.topkekbot;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Created by bo0tzz
 */
public class TopKekCommandListener implements Listener {

    private final TelegramBot bot;

    private final Tweeter tweeter;

    private static final String[] OPTIONS_8BALL = {
        "It is certain",
        "It is decidedly so",
        "Without a doubt",
        "Yes definitely",
        "You may rely on it",
        "As I see it, yes",
        "Most likely",
        "Outlook good",
        "Yes",
        "Signs point to yes",
        "Reply hazy try again",
        "Ask again later",
        "Better not tell you now",
        "Cannot predict now",
        "Concentrate and ask again",
        "Don't count on it",
        "My reply is no",
        "My sources say no",
        "Outlook not so good",
        "Very doubtful"
    };


    private final Map<String, Consumer<CommandMessageReceivedEvent>> commands = new HashMap<String, Consumer<CommandMessageReceivedEvent>>() {{
        TopKekCommandListener that = TopKekCommandListener.this;
        put("choice", that::choice);
        put("define", that::define);
        put("fuckingweather", that::fuckingweather);
        put("8ball", that::eightball);
        put("lmgtfy", that::lmgtfy);
        put("lenny", (event) -> event.getChat().sendMessage("( ͡° ͜ʖ ͡°)", bot));
        put("idk", (event) -> event.getChat().sendMessage("¯\\_(ツ)_/¯", bot));
        put("flip", (event) -> event.getChat().sendMessage("(╯°□°）╯︵ ┻━┻", bot));
        put("topkek", (event) -> event.getChat().sendMessage("http://s.mzn.pw/index.swf Gotta be safe while keking!", bot));
        put("wat", (event) -> event.getChat().sendMessage("http://waitw.at 0.o", bot));
        put("source", (event) -> event.getChat().sendMessage("The bot's source can be found over on https://github.com/bo0tzz/TopKekBot", bot));
        put("tweet", that::tweet);
        put("roll", that::roll);

    }};

    public TopKekCommandListener(TelegramBot bot, Tweeter tweeter) {
        this.bot = bot;
        this.tweeter = tweeter;
    }

    @Override
    public void onCommandMessageReceived(CommandMessageReceivedEvent event) {
        this.commands.getOrDefault(event.getCommand(), (c) -> {}).accept(event);
    }

    private void choice(CommandMessageReceivedEvent event) {
        String[] args = event.getArgsString().split(",");
        if (args.length <= 1) {
            event.getChat().sendMessage("Give me choices!", bot);
        } else {
            String choice = args[ThreadLocalRandom.current().nextInt(args.length)];
            event.getChat().sendMessage(SendableTextMessage.builder().message("I say " + choice).replyTo(event.getMessage()).build(), bot);
        }
    }

    private void define(CommandMessageReceivedEvent event) {
        try  {
            HttpResponse<String> response = Unirest.get("https://mashape-community-urban-dictionary.p.mashape.com/define?term=" + event.getArgsString().replace(" ", "+"))
                    .header("X-Mashape-Key", Util.KEY_URBAND)
                    .header("Accept", "text/plain")
                    .asString();
            JSONObject object = new JSONObject(response.getBody());

            if (object.getJSONArray("list").length() == 0) {
                event.getChat().sendMessage("No definition found for " + event.getArgsString() + "!", bot);
                return;
            }

            JSONObject definition = object.getJSONArray("list").getJSONObject(0);

            event.getChat().sendMessage("Definition of " + event.getArgsString() + ": " + definition.getString("definition"), bot);
            event.getChat().sendMessage(definition.getString("example"), bot);
        } catch (UnirestException ex) {
            event.getChat().sendMessage("Failed to find definition of " + event.getArgsString(), bot);
            ex.printStackTrace();
        }
    }

    private void fuckingweather(CommandMessageReceivedEvent event) {
        try {
            for (String message : Util.getWeather(event.getArgsString()))
            {
                event.getChat().sendMessage(SendableTextMessage.builder()
                        .message(message)
                        .replyTo(event.getMessage())
                        .build(), bot);
            }
        } catch (Exception e) {
            event.getChat().sendMessage(SendableTextMessage.builder().message("THE FUCKING WEATHER MODULE FAILED FUCK!").build(), bot);
            e.printStackTrace();
        }
    }

    private void eightball(CommandMessageReceivedEvent event) {
        int chosen = ThreadLocalRandom.current().nextInt(OPTIONS_8BALL.length);
        event.getChat().sendMessage(SendableTextMessage.builder().message(OPTIONS_8BALL[chosen]).replyTo(event.getMessage()).build(), bot);
    }

    @SuppressWarnings("deprecation")
    private void lmgtfy(CommandMessageReceivedEvent event) {
        String encoded = URLEncoder.encode(event.getArgsString());
        event.getChat().sendMessage(SendableTextMessage.builder().message("http://lmgtfy.com/?q=" + encoded).build(), bot);
    }

    private void tweet(CommandMessageReceivedEvent event) {
        String tweet;
        if (event.getMessage().getSender().getUsername().equals("bo0tzz")) {
            tweet = event.getArgsString();
        } else {
            tweet = event.getMessage().getSender().getUsername() + " says: " + event.getArgsString();
        }
        System.out.println(("Tweeting: " + tweet));
        this.tweeter.sendTweet(tweet);
    }

    private void roll(CommandMessageReceivedEvent event) {
        new Thread(() -> {
            String[] num = event.getArgsString().split("d");
            StringBuilder out = new StringBuilder("Results: [");
            try {
                int count = Integer.parseInt(num[0]);
                int val = Integer.parseInt(num[1]) + 1;
                if (count < 1 || val < 1 || count > 1000 || val > 1001) {
                    throw new IllegalArgumentException("topkek");
                }
                int[] results = new int[count];
                for (int i = 0; i < count; i++) {
                    results[i] = ThreadLocalRandom.current().nextInt(1, val);
                }
                for (int result : results) {
                    out.append(result).append(",");
                }
                out.deleteCharAt(out.length() - 1).append("]");
                event.getChat().sendMessage(SendableTextMessage.builder()
                        .message(out.toString())
                        .replyTo(event.getMessage())
                        .build(), bot);
            } catch (IllegalArgumentException ex) {
                event.getChat().sendMessage("Incorrect args or numbers too large! Format: /roll 2d10", bot);
            } catch (OutOfMemoryError ex) {
                event.getChat().sendMessage("Numbers too large - not enough memory!", bot);
            }
        }).start();
    }
}
