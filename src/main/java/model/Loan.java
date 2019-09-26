package main.java.model;

import java.time.LocalDate;

public class Loan {

    private InterestRateType interestRateType;
    private int numYears;
    private double principal, rate;
    private LocalDate firstPayment;

    public Loan() {

    }
    public InterestRateType getInterestRateType() {
        return interestRateType;
    }

    public void setInterestRateType(InterestRateType interestRateType) {
        this.interestRateType = interestRateType;
    }

    public int getNumYears() {
        return numYears;
    }

    public void setNumYears(int numYears) {
        this.numYears = numYears;
    }

    public double getPrincipal() {
        return principal;
    }

    public void setPrincipal(double principal) {
        this.principal = principal;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public LocalDate getFirstPayment() {
        return firstPayment;
    }

    public void setFirstPayment(LocalDate firstPayment) {
        this.firstPayment = firstPayment;
    }

}
