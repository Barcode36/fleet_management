package main.java.util;

import main.java.model.InterestRateType;
import main.java.model.Loan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoanUtility {
    public static final String PAYMENT_AMOUNT = "payment_amount";
    public static final String INTEREST = "interest";
    public static final String PRINCIPAL = "principal";
    public static final String BALANCE = "balance";
    public static final String PERIOD = "period";
    public static double calculateMonthlyInstalments(Loan loan) {
        if (loan.getInterestRateType() == InterestRateType.REDUCING_BALANCE) {
            double n = loan.getNumYears() * 12;
            double i = loan.getRate() / (12 * 100);
            double pow = Math.pow(1 + i, n);
            double dFactor = (pow - 1) / (i * pow);
            return (loan.getPrincipal() / dFactor);
        } else{
            double a = loan.getPrincipal() + (loan.getPrincipal() * loan.getRate() / 100 * loan.getNumYears());
            return (a/(12 * loan.getNumYears()));
        }
    }

    public static List<Map<String, String>> getRepaymentSchedule(Loan loan) {
        //return mapping of month, payment amount, interest, principal, and balance
        List<Map<String, String>> list = new ArrayList<>();
        double balance = loan.getPrincipal();
        double fixedInterest = 0;
        if (loan.getInterestRateType() == InterestRateType.FIXED) {
            fixedInterest = calculateMonthlyFixedInterest(loan);
        }
        LocalDate start = loan.getFirstPayment();
        for (int i = 0 ; i < loan.getNumYears() * 12; i++) {
            Map<String, String> map = new HashMap<>();
            LocalDate period = start.plusMonths(i);
            map.put(PERIOD, period.getMonth() + " " + period.getYear());
            map.put(PAYMENT_AMOUNT, CurrencyUtil.formatCurrency(calculateMonthlyInstalments(loan)));
            if (loan.getInterestRateType() == InterestRateType.FIXED) {
                map.put(INTEREST, CurrencyUtil.formatCurrency(fixedInterest));
            } else{
                map.put(INTEREST, CurrencyUtil.formatCurrency(calculateReducingInterest(loan.getRate(), balance)));
            }
            map.put(PRINCIPAL, CurrencyUtil.formatCurrency
                    (CurrencyUtil.parseCurrency(map.get(PAYMENT_AMOUNT)) -
                            CurrencyUtil.parseCurrency(map.get(INTEREST))));
            balance = balance - CurrencyUtil.parseCurrency(map.get(PRINCIPAL));
            map.put(BALANCE, CurrencyUtil.formatCurrency(balance));
            list.add(map);
        }
        return list;

    }

    private static double calculateReducingInterest(double rate, double balance) {
        return (balance * rate / (100 * 12));
    }

    private static double calculateMonthlyFixedInterest(Loan loan) {
        return  loan.getPrincipal() * loan.getNumYears() * loan.getRate() /
                (loan.getNumYears() * 12 * 100);

    }

}
