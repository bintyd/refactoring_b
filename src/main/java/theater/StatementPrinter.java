package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private static final int TRAGEDY_BASE_AMOUNT = 40000;
    private static final int TRAGEDY_AUDIENCE_THRESHOLD = 30;
    private static final int TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON = 1000;
    private static final int CENTS_IN_DOLLAR = 100;

    private Invoice invoice;
    private Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    public Map<String, Play> getPlays() {
        return plays;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        
        final StringBuilder result = new StringBuilder("Statement for " + invoice.getCustomer()
                + System.lineSeparator());
        for (Performance p : invoice.getPerformances()) {
            // print line for this order
            result.append(String.format("  %s: %s (%s seats)%n", getPlay(p).getName(),
                    usd(getAmount(p)), p.getAudience()));
        }
        final int volumeCredits = getTotalVolumeCredits(invoice);

        final int totalAmount = getTotalAmount(invoice);
        
        result.append(String.format("Amount owed is %s%n", usd(totalAmount)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    private int getTotalAmount(Invoice anInvoice) {
        int result = 0;
        for (Performance p: anInvoice.getPerformances()) {
            result += getAmount(p);
        }
        return result;
    }

    private int getTotalVolumeCredits(Invoice anInvoice) {
        int result = 0;
        for (Performance p : anInvoice.getPerformances()) {
            result += getVolumeCredits(p);
        }
        return result;    
    }

    private static String usd(int totalAmount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(totalAmount / CENTS_IN_DOLLAR);
    }

    private int getVolumeCredits(Performance performance) {
        int result = 0;
        result += Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        if ("comedy".equals(getPlay(performance).getType())) {
            result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;     
    }

    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    private int getAmount(Performance performance) {
        int result;
        switch (getPlay(performance).getType()) {
            case "tragedy":
                result = TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result +=
                            TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON * (performance.getAudience()
                                    - TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", getPlay(performance).getType()));
        }
        return result;
    }
}
