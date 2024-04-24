package me.thomazz.dusk.check;

import lombok.Getter;
import me.thomazz.dusk.check.impl.ReachCheck;
import me.thomazz.dusk.check.impl.TimingCheck;
import me.thomazz.dusk.player.PlayerData;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

@Getter
public enum CheckType {
    REACH(ReachCheck.class, ReachCheck::new),
    TIMING(TimingCheck.class, TimingCheck::new),
    UNKNOWN(Check.class, data -> null);

    private static Map<Class<? extends Check>, CheckType> typeMapping;

    private final Class<? extends Check> type;
    private final Function<PlayerData, Check> function;
    private final CheckInfo info;

    CheckType(Class<? extends Check> type, Function<PlayerData, Check> function) {
        this.type = type;
        this.function = function;
        this.info = type.getAnnotation(CheckInfo.class);
        CheckType.registerMapping(type, this);
    }

    private static void registerMapping(Class<? extends Check> clazz, CheckType type) {
        if (CheckType.typeMapping == null) {
            CheckType.typeMapping = new IdentityHashMap<>();
        }

        CheckType.typeMapping.put(clazz, type);
    }

    public static Stream<Check> createChecks(PlayerData data) {
        return Arrays.stream(CheckType.values())
            .filter(type -> type != CheckType.UNKNOWN)
            .map(type -> type.function.apply(data));
    }

    public static CheckType fromClass(Class<? extends Check> type) {
        return CheckType.typeMapping.getOrDefault(type, CheckType.UNKNOWN);
    }
}
