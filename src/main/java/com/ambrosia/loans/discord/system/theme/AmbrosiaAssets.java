package com.ambrosia.loans.discord.system.theme;

import com.ambrosia.loans.discord.DiscordBot;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

public class AmbrosiaAssets {

    public static final String ACTIVE = png("active");
    public static final String CHECK = png("check");
    public static final String FOOTER_ACCEPTED = gif("loanacceptedfooter");
    public static final String FOOTER_PROMOTE = gif("ambrosiacreditsfooter");
    public static final String FOOTER_PENDING_LOAN = gif("loanpendingfooter");
    public static final String FOOTER_REQUEST_LOAN = gif("loanrequestfooter");
    public static final String FOOTER_ERROR = gif("boterrorfooter");
    public static final String EMERALD = gif("emeraldembeddmark");

    private static String gif(String file) {
        return img(file + ".gif");
    }

    private static String png(String file) {
        return img(file + ".png");
    }

    private static String img(String file) {
        return "https://static.voltskiya.com/ambrosia/loans/" + file;
    }

    public enum AmbrosiaEmoji {
        LOAN_ACTIVE(1201671148870574130L),
        LOAN_FROZEN(1201671145124790312L),
        LOAN_PAID(1201671141027217458L),
        LOAN_DEFAULTED(1201669303745650730L),
        REQUEST_UNCLAIMED(1201669303745650730L),
        REQUEST_CLAIMED(1201671145124790312L),
        REQUEST_ACCEPTED(1201671148870574130L),
        REQUEST_COMPLETED(1201671141027217458L),
        REQUEST_ERROR(1202020940930621501L),
        LOAN_BALANCE(1201636693267193866L),
        LOAN_PAYMENT(1202017326720090243L),
        LOAN_REPAYMENT_PLAN(1202020919992909845L),
        LOAN_RATE(LOAN_REPAYMENT_PLAN.emojiId),
        COLLATERAL(1202020936883109928L),
        INVESTMENT_BALANCE(1202020945506861067L),
        DATE(1202020953991946241L),
        PROFITS(1201636697650249748L),
        INVESTOR_STAKE(1202020962359574568L),
        AMBROSIA(1201663071353839617L),
        MINECRAFT(1202020968462303262L),
        INTEREST(PROFITS.emojiId),
        WITHDRAWAL(1202017229315784724L),
        TRADE(LOAN_REPAYMENT_PLAN.emojiId),
        FILTER(1202020928037601300L),
        SORT(1202020933276012635L),
        PAYMENT_REMINDER(1202020939592900618L);

        private final long emojiId;
        private RichCustomEmoji emoji;
        private String emojiStr;

        AmbrosiaEmoji(long emojiId) {
            this.emojiId = emojiId;
        }

        AmbrosiaEmoji(String emojiStr) {
            this.emojiId = 0;
            this.emojiStr = emojiStr;
        }

        public RichCustomEmoji getEmoji() {
            if (this.emoji != null) return this.emoji;
            return this.emoji = DiscordBot.dcf.jda().getEmojiById(emojiId);
        }

        @Override
        public String toString() {
            if (this.emojiStr != null) return emojiStr;
            return this.emojiStr = getEmoji().getFormatted();
        }

        public String spaced() {
            return this + " ";
        }
    }
}
