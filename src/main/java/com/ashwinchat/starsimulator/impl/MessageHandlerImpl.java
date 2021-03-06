package com.ashwinchat.starsimulator.impl;

import com.ashwinchat.scrollsimulator.impl.ScrollSimulatorImpl;
import com.ashwinchat.scrollsimulator.pojos.ScrollResult;
import com.ashwinchat.scrollsimulator.utils.ScrollUtils;
import com.ashwinchat.starsimulator.Utils.StarUtils;
import com.ashwinchat.starsimulator.enums.ItemType;
import com.ashwinchat.starsimulator.pojos.StarResult;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public class MessageHandlerImpl {
    private StarSimulatorImpl starSimulator;
    private ScrollSimulatorImpl scrollSimulator;
    private static MessageHandlerImpl instance = new MessageHandlerImpl();
    private static final Logger logger = Logger.getLogger(StarSimulatorImpl.class);
    private static final String MAPLE_COMMAND = "/star";
    private static final String MAPLE_SCROLL_COMMAND = "/scroll";
    private static final int NORMAL_MAX_STARS = 20;
    private static final int SUPERIOR_MAX_STARS = 15;
    private static final int REASONABLE_SUPERIOR_MAX_STARS = 13;

    public static MessageHandlerImpl getInstance() {
        return instance;
    }

    private MessageHandlerImpl() {
        this.starSimulator = StarSimulatorImpl.getInstance();
        this.scrollSimulator = ScrollSimulatorImpl.getInstance();
    }

    public String processAndFormatReply(String[] userInput, String content) {
        if (userInput.length > 0 && StringUtils.equals(userInput[0], MAPLE_COMMAND)) {
            String messageToSend = handleMapleResponse(userInput, content);
            return messageToSend;
        } else if (userInput.length > 0 && StringUtils.equals(userInput[0], MAPLE_SCROLL_COMMAND)) {
            String messageToSend = handleMapleScrollResponse(userInput, content);
            return messageToSend;
        }
        return null;
    }

    private String handleMapleScrollResponse(String[] userInput, String content) {
        String messageToSend = null;
        logger.info("Message Received: " + content);
        if (userInput.length != 3) {
            messageToSend = "USAGE: /scroll <number of Upgrades> <diligence level>";
            logger.warn("Wrong number of argument given. # of arguments given = " + userInput.length);
        } else {
            try {
                int numberOfUpgrades = Integer.parseInt(userInput[1]);
                int diligenceLevel = Integer.parseInt(userInput[2]);
                if (numberOfUpgrades < 0 || diligenceLevel < 0) {
                    messageToSend = "Please enter numbers greater than 0.";
                } else if (numberOfUpgrades > 12) {
                    messageToSend = "Please enter a upgrade level lower than 13";
                } else if (diligenceLevel > 100) {
                    messageToSend = "Diligence level has a maximum of level 100";
                } else {
                    logger.info(ScrollUtils.formatStartSimulation(numberOfUpgrades, diligenceLevel));
                    Map<Integer, ScrollResult> resultMap = scrollSimulator.runSimulation(numberOfUpgrades, diligenceLevel);
                    messageToSend = ScrollUtils.formatScrollString(resultMap);
                }
            } catch (NumberFormatException e) {
                messageToSend = "Please enter numbers, not letters.";
            }
        }

        return messageToSend;
    }

    private String handleMapleResponse(String[] userInput, String content) {
        String messageToSend = null;
        logger.info("Message Received: " + content);
        if (userInput.length != 3) {
            messageToSend = "USAGE: /star <normal/superior> <desired stars>";
            logger.warn("Wrong number of argument given. # of arguments given = " + userInput.length);
        } else {
            String userItemTypeInput = StringUtils.upperCase(userInput[1]);
            try {
                int desiredStarLevel = Integer.parseInt(userInput[2]);
                if (StringUtils.equals(userItemTypeInput, StringUtils.upperCase(ItemType.NORMAL.getItemDescription()))) {
                    if (desiredStarLevel <= NORMAL_MAX_STARS) {
                        StarResult result = this.starSimulator.runSimulation(desiredStarLevel, ItemType.NORMAL);
                        messageToSend = StarUtils.formatStarString(result);
                    } else {
                        messageToSend = "Normal items can only be starred to " + NORMAL_MAX_STARS + " stars.";
                        logger.warn("Person sent number greater than " + NORMAL_MAX_STARS + ". Number = " + desiredStarLevel);
                    }
                } else if (StringUtils.equals(userItemTypeInput, StringUtils.upperCase(ItemType.SUPERIOR.getItemDescription()))) {
                    if (desiredStarLevel <= SUPERIOR_MAX_STARS) {
                        if (desiredStarLevel <= REASONABLE_SUPERIOR_MAX_STARS) {
                            StarResult result = this.starSimulator.runSimulation(desiredStarLevel, ItemType.SUPERIOR);
                            messageToSend = StarUtils.formatStarString(result);
                        } else {
                            messageToSend = "Simulations for superior items over " + REASONABLE_SUPERIOR_MAX_STARS + " stars take too long.";
                            logger.warn("Person tried for starring simulation greater than " + REASONABLE_SUPERIOR_MAX_STARS + " stars.");
                        }
                    } else {
                        messageToSend = "Superior items can only be starred to " + SUPERIOR_MAX_STARS + " stars.";
                        logger.warn("Person sent number greater than " + SUPERIOR_MAX_STARS + ". Number = " + desiredStarLevel);
                    }
                } else {
                    messageToSend = "I can only simulate normal or superior items.";
                    logger.warn("Person named an unknown item the 2nd field. Item = " + userItemTypeInput);
                }
            } catch (NumberFormatException e) {
                messageToSend = "Number of stars must be an integer.";
                logger.warn("Person did not send an integer in the 3rd field.");
            }
        }
        return messageToSend;
    }
}
