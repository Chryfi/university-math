package utils;

public enum ComparisonOperators
{
    GREATER(">"),
    GREATER_EQUAL(">="),
    LESSER("<"),
    LESSER_EQUAL("<=");

    private final String identifier;

    ComparisonOperators(String identifier)
    {
        this.identifier = identifier;
    }

    public String getIdentifier()
    {
        return this.identifier;
    }

    public ComparisonOperators fromIdentifier(String identifier)
    {
        switch (identifier)
        {
            case ">":
                return GREATER;
            case ">=":
                return GREATER_EQUAL;
            case "<":
                return LESSER;
            case "<=":
                return LESSER_EQUAL;
        }

        return null;
    }
}
