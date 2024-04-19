package me.thomazz.dusk.check;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import me.thomazz.dusk.check.impl.ReachCheck;
import me.thomazz.dusk.check.impl.TimingCheck;
import me.thomazz.dusk.player.PlayerData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class CheckRegistry {
    private final Map<Class<? extends Check>, CheckRegistryEntry> registryMapping = new LinkedHashMap<>();

    public void init() {
        CheckRegistry.registerCheck(ReachCheck.class, ReachCheck::new);
        CheckRegistry.registerCheck(TimingCheck.class, TimingCheck::new);
    }

    private void registerCheck(Class<? extends Check> type, Function<PlayerData, Check> function) {
        CheckInfo info = type.getAnnotation(CheckInfo.class);
        CheckRegistry.registryMapping.put(type, new CheckRegistryEntry(function, info));
    }

    public List<Check> constructChecks(PlayerData data) {
        return CheckRegistry.registryMapping.values().stream()
            .map(CheckRegistryEntry::getFunction)
            .map(function -> function.apply(data))
            .collect(Collectors.toList());
    }

    public CheckInfo getInfo(Class<? extends Check> check) {
        return CheckRegistry.registryMapping.get(check).info;
    }

    @Getter
    @RequiredArgsConstructor
    private static class CheckRegistryEntry {
        private final Function<PlayerData, Check> function;
        private final CheckInfo info;
    }
}
