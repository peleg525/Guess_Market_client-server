package gm.engine.dto;

import gm.engine.model.EventStatus;

import java.util.Set;

/**
 * Filter criteria for the events list. An empty/null set on any field means "no filter on that
 * field" (show all), matching the "all" option each filter group must offer per the UI sketch.
 */
public class EventFilter {

    private final Set<TradeMethod> methods;
    private final Set<EventStatus> statuses;
    private final Set<String> commissionTypes;

    public EventFilter(Set<TradeMethod> methods, Set<EventStatus> statuses, Set<String> commissionTypes) {
        this.methods = methods;
        this.statuses = statuses;
        this.commissionTypes = commissionTypes;
    }

    public static EventFilter all() {
        return new EventFilter(Set.of(), Set.of(), Set.of());
    }

    public boolean matchesMethod(TradeMethod method) {
        return methods == null || methods.isEmpty() || methods.contains(method);
    }

    public boolean matchesStatus(EventStatus status) {
        return statuses == null || statuses.isEmpty() || statuses.contains(status);
    }

    public boolean matchesCommissionType(String commissionType) {
        return commissionTypes == null || commissionTypes.isEmpty() || commissionTypes.contains(commissionType);
    }
}
