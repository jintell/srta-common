package org.meldtech.platform.srta.common.domain;

/**
 * Standard lifecycle statuses for trades.
 */
public enum TradeStatus {
    PENDING,
    SUBMITTED,
    EXECUTED,
    SETTLED,
    CANCELLED,
    REJECTED
}
