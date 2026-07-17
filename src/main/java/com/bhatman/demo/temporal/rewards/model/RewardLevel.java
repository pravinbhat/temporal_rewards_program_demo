package com.bhatman.demo.temporal.rewards.model;

/**
 * Represents the three reward tiers in the program.
 *
 * <ul>
 *   <li>{@link #BASIC}    — default on enrolment (0 points)</li>
 *   <li>{@link #GOLD}     — ≥ 500 points</li>
 *   <li>{@link #PLATINUM} — ≥ 1 000 points</li>
 * </ul>
 */
public enum RewardLevel {
    BASIC,
    GOLD,
    PLATINUM;

    private static final int GOLD_THRESHOLD     = 500;
    private static final int PLATINUM_THRESHOLD = 1_000;

    /**
     * Returns the appropriate {@link RewardLevel} for the given points total.
     *
     * @param points accumulated points (must be ≥ 0)
     * @return the matching tier
     */
    public static RewardLevel forPoints(int points) {
        if (points >= PLATINUM_THRESHOLD) {
            return PLATINUM;
        } else if (points >= GOLD_THRESHOLD) {
            return GOLD;
        } else {
            return BASIC;
        }
    }
}
