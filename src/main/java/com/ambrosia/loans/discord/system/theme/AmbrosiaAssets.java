package com.ambrosia.loans.discord.system.theme;

import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.DiscordModule;
import java.util.List;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

public class AmbrosiaAssets {

    public static final String EMERALD = gif("Emerald.v1");
    public static final String ERROR = png("CheckError.v1");
    public static final String SUCCESS = png("CheckSuccess.v1");
    public static final String WARNING = png("StatusPending.v1");
    public static final String FOOTER_ACCEPTED = gif("FooterLoanAccepted.v1");
    public static final String FOOTER_PROMOTE = gif("FooterAmbrosiaCredits.v1");
    public static final String FOOTER_PENDING_LOAN = gif("FooterLoanPending.v1");
    public static final String FOOTER_REQUEST_LOAN = gif("FooterLoanRequest.v1");
    public static final String FOOTER_ERROR = gif("FooterError.v1");
    public static final String JOKER = gif("Joker.v1");

    private static String gif(String file) {
        return img(file + ".gif");
    }

    private static String png(String file) {
        return img(file + ".png");
    }

    private static String img(String file) {
        return "https://static.voltskiya.com/ambrosia/loans/" + file;
    }

    public static String skinUrl(String player) {
        return "https://mc-heads.net/head/" + player;
    }

    public enum AmbrosiaEmoji {
        ANY_DATE(1208248441805738044L),
        ANY_WITHDRAWAL(1202017229315784724L),
        CHECK_SUCCESS(1208248444632965211L),
        CHECK_ERROR(1208252574726357092L),
        CLIENT_ACCOUNT(1208251350593314817L),
        CLIENT_MINECRAFT(1208248447702933574L),
        EMERALD(1201663071353839617L),
        INVESTMENT_BALANCE(1208252575753834496L),
        INVESTMENT_PROFITS(1208248451775594516L),
        INVESTMENT_STAKE(1208252576810803250L),
        KEY_ID(1208248576182853683L),
        KEY_ID_CHANGES(1208248577181225000L),
        STATUS_ACTIVE(1208248428992270376L),
        STATUS_COMPLETE(1208248429994573854L),
        STATUS_PENDING(1208248433023123517L),
        STATUS_OFFLINE(1208248432028950598L),
        STATUS_ERROR(1208248431076708433L),
        LOAN_BALANCE(1208248640888512582L),
        LOAN_COLLATERAL(1208248642583011339L),
        LOAN_DISCOUNT(1208248876960587856L),
        LOAN_INTEREST(1208252577901187179L),
        LOAN_PAYMENT(1202017326720090243L),
        LOAN_RATE(1208252573560209439L),
        LOAN_REASON(1208248425750208572L),
        LOAN_REPAYMENT_PLAN(1208248426836267078L),
        TRADE(LOAN_REPAYMENT_PLAN.emojiId),
        UNUSED_PAYMENT_REMINDER(1208248436105678928L),
        UNUSED_SORT(1208248437037076502L),
        TOS("\uD83D\uDCDC"),
        COLLATERAL_TEXT(CLIENT_MINECRAFT.emojiId);

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

        public static List<AmbrosiaEmoji> statusInOrder() {
            return List.of(STATUS_OFFLINE, STATUS_ERROR, STATUS_PENDING, STATUS_ACTIVE, STATUS_COMPLETE);
        }

        public RichCustomEmoji getEmoji() {
            if (this.emoji != null) return this.emoji;
            return this.emoji = DiscordBot.dcf.jda().getEmojiById(emojiId);
        }

        @Override
        public String toString() {
            if (this.emojiStr != null) return emojiStr;
            RichCustomEmoji em = getEmoji();
            if (em == null) {
                DiscordModule.get().logger().error("{} emoji not found!!", name());
                return "Emoji not found";
            }
            this.emojiStr = em.getFormatted();
            return emojiStr;
        }

        public String spaced() {
            return this + " ";
        }

        public String spaced(Object msg) {
            return spaced() + msg;
        }

        public Emoji getDiscordEmoji() {
            return Emoji.fromUnicode(emojiStr);
        }
    }
}
