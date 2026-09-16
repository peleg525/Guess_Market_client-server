package gm.engine.model;

import gm.engine.exception.GmOperationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A user's cash balance. Ordinary spending ({@link #debit}) is never allowed to push the balance
 * below zero - it is rejected up front with a clear reason. Settlement math that is out of the
 * user's direct control ({@link #settle}, used for market-maker subsidy/close accounting) is
 * allowed to push the balance negative; when it does, the account is blocked from further
 * spending, matching the assignment's "allow it, notify, then block the user" rule.
 */
public class Account {

    private double balance;
    private boolean blocked = false;
    private final List<AccountEntry> history = new ArrayList<>();

    public Account(double initialBalance) {
        this.balance = initialBalance;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public List<AccountEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void requireNotBlocked() {
        if (blocked) {
            throw new GmOperationException("This account is blocked (its balance went negative during a "
                    + "past settlement) and can no longer perform actions.");
        }
    }

    /** Spends money the user chose to spend. Rejected outright if funds are insufficient. */
    public void debit(double amount, String reason, long sequence) {
        requireNotBlocked();
        if (amount > balance) {
            throw new GmOperationException("Insufficient funds: this action costs " + amount
                    + " but the account balance is only " + balance + ".");
        }
        balance -= amount;
        history.add(new AccountEntry(sequence, -amount, balance, reason));
    }

    public void credit(double amount, String reason, long sequence) {
        balance += amount;
        history.add(new AccountEntry(sequence, amount, balance, reason));
    }

    /**
     * Applies settlement money movement that isn't a direct spending choice (market-maker
     * subsidy return, event close sweep, ...). May push the balance negative; if it does, the
     * account becomes blocked and {@code true} is returned so the caller can notify the user.
     */
    public boolean settle(double delta, String reason, long sequence) {
        balance += delta;
        history.add(new AccountEntry(sequence, delta, balance, reason));
        if (balance < 0 && !blocked) {
            blocked = true;
            return true;
        }
        return false;
    }
}
