package io.neolab.internship.coins.client.bot.ai.bim.model;

public enum FunctionType {
    /* Учитывается отношение побед к общему числу случаев */
    MAX_PERCENT,
    MIN_PERCENT,
    MIN_MAX_PERCENT,

    /* Учитывается число монет */
    MAX_VALUE,
    MIN_VALUE,
    MIN_MAX_VALUE,
    ;
}
