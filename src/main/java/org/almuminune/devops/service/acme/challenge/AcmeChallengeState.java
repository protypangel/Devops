package org.almuminune.devops.service.acme.challenge;

import java.util.concurrent.atomic.AtomicInteger;

public class AcmeChallengeState {
    Runnable changeState;
    Runnable delete;

    public AcmeChallengeState (String key, AcmeChallenge challenge, int countChallenges) {
        delete = () -> challenge.states.remove(key);
        AtomicInteger counter = new AtomicInteger();
        changeState = () -> {
            counter.getAndIncrement();
            if (counter.get() != countChallenges) return;
            delete.run();
            challenge.challengeIsClose();
        };
    }
    public void HttpChallenge () {

    }
}
