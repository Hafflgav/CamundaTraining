package com.camunda.training;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

@Component
public class TweetDelegate implements JavaDelegate {

    private final Logger LOGGER = LoggerFactory.getLogger(TweetDelegate.class.getName());
    private final AccessToken ACCESS_TOKEN = new AccessToken("220324559-jet1dkzhSOeDWdaclI48z5txJRFLCnLOK45qStvo", "B28Ze8VDucBdiE38aVQqTxOyPc7eHunxBVv7XgGim4say");
    private Twitter twitter = new TwitterFactory().getInstance();
    private String message;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        message = (String) delegateExecution.getVariable("Tweet");
        LOGGER.info("Publishing Tweet: "+ message);
        twitter.setOAuthConsumer("lRhS80iIXXQtm6LM03awjvrvk", "gabtxwW8lnSL9yQUNdzAfgBOgIMSRqh7MegQs79GlKVWF36qLS");
        twitter.setOAuthAccessToken(ACCESS_TOKEN);
        twitter.updateStatus(message);
        LOGGER.info("Tweet published successfully");
    }
}
