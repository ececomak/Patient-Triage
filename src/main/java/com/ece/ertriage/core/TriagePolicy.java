package com.ece.ertriage.core;

import com.ece.ertriage.model.Patient;

public final class TriagePolicy {
    private TriagePolicy() {}

    public static int score(Patient p, long nowMillis) {
        int base = p.severity().level() * 100;

        long waitingMinutes = Math.max(0, (nowMillis - p.arrivalMillis()) / 60_000L);
        int waitingBonus = (int) Math.min(300, waitingMinutes * 2);

        int ageBonus = 0;
        if (p.age() >= 65) ageBonus += 20;
        if (p.age() <= 6) ageBonus += 15;

        int riskBonus = 0;
        if (p.chronic()) riskBonus += 15;
        if (p.pregnant()) riskBonus += 15;

        int painBonus = 0;
        int ps = p.painScore();
        if (ps >= 9) painBonus += 30;
        else if (ps >= 7) painBonus += 20;
        else if (ps >= 4) painBonus += 10;

        return base + waitingBonus + ageBonus + riskBonus + painBonus;
    }
}