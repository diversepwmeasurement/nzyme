package app.nzyme.core.events;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.events.db.EventActionEntry;
import app.nzyme.core.events.db.EventEntry;
import app.nzyme.core.events.db.SubscriptionEntry;
import app.nzyme.core.events.types.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EventEngineImpl implements EventEngine {

    private final NzymeNode nzyme;

    public EventEngineImpl(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    public void processEvent(SystemEvent event, @Nullable UUID eventOwnerOrganizationId, @Nullable UUID eventOwnerTenantId) {
        // Store in database.
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO events(organization_id, tenant_id, event_type, reference, " +
                                "details, created_at) VALUES(:organization_id, :tenant_id, :event_type, :reference, " +
                                ":details, NOW())")
                        .bind("organization_id", eventOwnerOrganizationId)
                        .bind("tenant_id", eventOwnerTenantId)
                        .bind("event_type", EventType.SYSTEM)
                        .bind("reference", event.type())
                        .bind("details", event.details())
                        .execute()
        );

        // Find all subscribers of event.

        // Process.
    }

    public long countAllEventsOfAllOrganizations() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM events")
                        .mapTo(Long.class)
                        .one()
        );
    }

    public long countAllEventsOfOrganization(UUID organizationId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM events WHERE organization_id = :organization_id")
                        .bind("organization_id", organizationId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<EventEntry> findAllEventsOfAllOrganizations(List<String> eventTypes, int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM events WHERE event_type IN (<event_types>) " +
                                "ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
                        .bindList("event_types", eventTypes)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(EventEntry.class)
                        .list()
        );
    }

    public List<EventEntry> findAllEventsOfOrganization(List<String> eventTypes, UUID organizationId, int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM events WHERE organization_id = :organization_id " +
                                "AND event_type IN (<event_types>) ORDER BY created_at DESC " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bindList("event_types", eventTypes)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(EventEntry.class)
                        .list()
        );
    }

    public long countAllEventActionsOfSuperadministrators() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM event_actions WHERE organization_id IS NULL")
                        .mapTo(Long.class)
                        .one()
        );
    }

    public long countAllEventActionsOfOrganization(UUID organizationId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM event_actions WHERE organization_id = :organization_id")
                        .bind("organization_id", organizationId)
                        .mapTo(Long.class)
                        .one()
        );
    }

    public List<EventActionEntry> findAllEventActionsOfSuperadministrators(int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM event_actions " +
                                "WHERE organization_id IS NULL ORDER BY name ASC " +
                                "LIMIT :limit OFFSET :offset")
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(EventActionEntry.class)
                        .list()
        );
    }

    public List<EventActionEntry> findAllEventActionsOfOrganization(UUID organizationId, int limit, int offset) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM event_actions WHERE organization_id = :organization_id " +
                                "ORDER BY name ASC LIMIT :limit OFFSET :offset")
                        .bind("organization_id", organizationId)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(EventActionEntry.class)
                        .list()
        );
    }

    public Optional<EventActionEntry> findEventAction(UUID actionId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM event_actions WHERE uuid = :action_id")
                        .bind("action_id", actionId)
                        .mapTo(EventActionEntry.class)
                        .findOne()
        );
    }


    public Optional<EventActionEntry> findEventActionOfOrganization(UUID organizationId, UUID actionId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM event_actions WHERE organization_id = :organization_id " +
                                "AND uuid = :action_id")
                        .bind("organization_id", organizationId)
                        .bind("action_id", actionId)
                        .mapTo(EventActionEntry.class)
                        .findOne()
        );
    }

    public void createEventAction(@Nullable UUID organizationId,
                                  EventActionType actionType,
                                  String name,
                                  String description,
                                  String configuration) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO event_actions(uuid, action_type, organization_id, name, " +
                                "description, configuration, created_at, updated_at) VALUES(:uuid, :action_type, " +
                                ":organization_id, :name, :description, :configuration, NOW(), NOW())")
                        .bind("uuid", UUID.randomUUID())
                        .bind("action_type", actionType)
                        .bind("organization_id", organizationId)
                        .bind("name", name)
                        .bind("description", description)
                        .bind("configuration", configuration)
                        .execute()
        );
    }

    public void updateAction(UUID actionId, String name, String description, String configuration) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE event_actions SET name = :name, description = :description, " +
                                "configuration = :configuration, updated_at = NOW() WHERE uuid = :id")
                        .bind("id", actionId)
                        .bind("name", name)
                        .bind("description", description)
                        .bind("configuration", configuration)
                        .execute()
        );
    }

    public void deleteEventAction(UUID actionId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM event_actions WHERE uuid = :action_id")
                        .bind("action_id", actionId)
                        .execute()
        );
    }

    public void subscribeActionToEvent(@Nullable UUID organizationId, EventType eventType, String reference, UUID actionId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO event_subscriptions(uuid, organization_id, event_type, reference, action_id) " +
                                "VALUES(:uuid, :organization_id, :event_type, :reference, :action_id)")
                        .bind("uuid", UUID.randomUUID())
                        .bind("organization_id", organizationId)
                        .bind("event_type", eventType)
                        .bind("reference", reference)
                        .bind("action_id", actionId)
                        .execute()
        );
    }

    public void unsubscribeActionFromEvent(UUID subscriptionId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM event_subscriptions WHERE uuid = :uuid")
                        .bind("uuid", subscriptionId)
                        .execute()
        );
    }

    public Optional<UUID> findActionOfSubscription(UUID subscriptionId) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT action_id FROM event_subscriptions WHERE uuid = :uuid")
                        .bind("uuid", subscriptionId)
                        .mapTo(UUID.class)
                        .findOne()
        );
    }

    public List<SubscriptionEntry> findAllActionsOfSubscription(@Nullable UUID organizationId, String reference) {
        if (organizationId == null) {
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT * FROM event_subscriptions " +
                                    "WHERE reference = :reference AND organization_id IS NULL")
                            .bind("reference", reference)
                            .mapTo(SubscriptionEntry.class)
                            .list()
            );
        } else {
            return nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT * FROM event_subscriptions " +
                                    "WHERE reference = :reference AND organization_id = :organization_id")
                            .bind("reference", reference)
                            .bind("organization_id", organizationId)
                            .mapTo(SubscriptionEntry.class)
                            .list()
            );
        }
    }

}
