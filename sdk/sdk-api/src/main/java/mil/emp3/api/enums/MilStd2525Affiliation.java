package mil.emp3.api.enums;

/**
 * This class enumerates the MilStd affiliation values.
 */
public enum MilStd2525Affiliation {
    PENDING,
    UNKNOWN,
    FRIEND,
    NEUTRAL,
    HOSTILE,
    ASSUMED_FRIEND,
    SUSPECT,
    EXERCISE_PENDING,
    EXERCISE_UNKNOWN,
    EXERCISE_FRIEND,
    EXERCISE_NEUTRAL,
    EXERCISE_ASSUMED_FRIEND,
    JOKER,
    FAKER;

    @Override
    public String toString() {
        switch (this) {
            case PENDING:
                return "P";
            case UNKNOWN:
                return "U";
            case FRIEND:
                return "F";
            case NEUTRAL:
                return "N";
            case HOSTILE:
                return "H";
            case ASSUMED_FRIEND:
                return "A";
            case SUSPECT:
                return "S";
            case EXERCISE_PENDING:
                return "G";
            case EXERCISE_UNKNOWN:
                return "W";
            case EXERCISE_FRIEND:
                return "D";
            case EXERCISE_NEUTRAL:
                return "L";
            case EXERCISE_ASSUMED_FRIEND:
                return "M";
            case JOKER:
                return "J";
            case FAKER:
                return "K";
        }
        
        return "*";
    }
}
