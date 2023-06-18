package ru.otus.streams;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class AppSerdes {
    private static <T> Serde<T> serde(Class<T> cls) {
        return new Serdes.WrapperSerde<>(new JsonSerializer<>(), new JsonDeserializer<>(cls));
    }
    public static Serde<Purchase> purchase() {
        return serde(Purchase.class);
    }

    public static Serde<PurchasePattern> purchasePattern() {
        return serde(PurchasePattern.class);
    }

    public static Serde<RewardAccumulator> rewardAccumulator() {
        return serde(RewardAccumulator.class);
    }

}
