package org.activityinfo.api.shared.adapter;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.api.client.Dispatcher;
import org.activityinfo.api.shared.command.*;
import org.activityinfo.api.shared.command.result.AttributeGroupResult;
import org.activityinfo.api.shared.command.result.CommandResult;
import org.activityinfo.api.shared.command.result.SiteResult;
import org.activityinfo.api.shared.model.ActivityDTO;
import org.activityinfo.api.shared.model.AttributeDTO;
import org.activityinfo.api.shared.model.AttributeGroupDTO;
import org.activityinfo.api.shared.model.SchemaDTO;
import org.activityinfo.api2.client.*;
import org.activityinfo.api2.shared.Cuid;
import org.activityinfo.api2.shared.Iri;
import org.activityinfo.api2.shared.Resource;
import org.activityinfo.api2.shared.criteria.InstanceCriteria;
import org.activityinfo.api2.shared.form.FormClass;
import org.activityinfo.api2.shared.form.FormInstance;
import org.activityinfo.reports.shared.model.DimensionType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.activityinfo.api.shared.adapter.BuiltinFormClasses.*;
import static org.activityinfo.api.shared.adapter.CuidAdapter.*;

/**
 * Exposes a legacy {@code Dispatcher} implementation as new {@code ResourceLocator}
 */
public class ResourceLocatorAdaptor implements ResourceLocator {

    private final Dispatcher dispatcher;

    public ResourceLocatorAdaptor(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Promise<FormClass> getFormClass(Cuid formId) {
        switch(formId.getDomain()) {
            case ACTIVITY_DOMAIN:
                int activityId = getLegacyIdFromCuid(formId);
                return execute(new GetSchema()).then(new ActivityAdapter(activityId));

            case PARTNER_FORM_CLASS_DOMAIN:
                return Promise.resolved(BuiltinFormClasses.partnerFormClass(getLegacyIdFromCuid(formId)));

            case PROJECT_DOMAIN:
                return Promise.resolved(BuiltinFormClasses.projectFormClass(getLegacyIdFromCuid(formId)));

            case ATTRIBUTE_GROUP_DOMAIN:
                return execute(new GetSchema()).then(new AttributeGroupAdapter(getLegacyIdFromCuid(formId)));

            case ADMIN_LEVEL_DOMAIN:
                return execute(new GetSchema()).then(new AdminLevelFormClassAdapter(getLegacyIdFromCuid(formId)));

            case LOCATION_TYPE_DOMAIN:
                return execute(new GetSchema()).then(new LocationTypeFormClassAdapter(getLegacyIdFromCuid(formId)));

            default:
                return Promise.rejected(new NotFoundException(formId.asIri()));
        }
    }

    @Override
    public Promise<FormInstance> getFormInstance(Cuid instanceId) {
        if (instanceId.getDomain() == SITE_DOMAIN) {
            int siteId = getLegacyIdFromCuid(instanceId);
            return execute(GetSites.byId(siteId)).then(new SiteAdapter());
        }
        return Promise.rejected(new NotFoundException(instanceId.asIri()));
    }

    @Override
    public Promise<Void> persist(Resource resource) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<Integer> countInstances(InstanceCriteria criteria) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Promise<List<FormInstance>> queryInstances(InstanceCriteria criteria) {
        if (criteria != null && criteria.getClasses() != null) {
            final Filter filter = new Filter();
            final List<Integer> idList = Lists.newArrayList();
            for (Iri iri : criteria.getClasses()) {
                try {
                    idList.add(CuidAdapter.getLegacyIdFromCuidIri(iri));
                } catch (Exception e) {
                    // ignore : right now we may get exception for hardcodes, e.g. partner iri
                    // org.activityinfo.api2.shared.Namespace.PARTNER
                }
            }
            filter.addRestriction(DimensionType.AttributeGroup, idList);
            return execute(new GetAttributeGroupsDimension(filter)).then(new Function<AttributeGroupResult, List<FormInstance>>() {
                @Nullable
                @Override
                public List<FormInstance> apply(@Nullable AttributeGroupResult input) {
                    final List<AttributeGroupDTO> data = input.getData();
                    final List<FormInstance> list = new ArrayList<FormInstance>();
                    for (AttributeGroupDTO attributeGroup : data) {
                        final List<AttributeDTO> attributes = attributeGroup.getAttributes();
                        for (AttributeDTO attribute : attributes) {
                            list.add(InstanceAdapters.fromAttribute(attribute, attributeGroupFormClass(attributeGroup)));
                        }
                    }
                    return list;
                }
            });
        }
        return Promise.rejected(new UnsupportedOperationException());
    }

    /**
     * Wraps a legacy command dispatch in a new {@code Remote} object
     *
     * @param command the command to execute
     * @param <R>     the type of the {@code Command}'s {@code CommandResult}
     */
    private <R extends CommandResult> Promise<R> execute(final Command<R> command) {
        return new Promise<R>(new Promise.AsyncOperation<R>() {

            @Override
            public void start(final Promise<R> promise) {
                try {
                    dispatcher.execute(command, new AsyncCallback<R>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            promise.reject(throwable);
                        }

                        @Override
                        public void onSuccess(R result) {
                            promise.resolve(result);
                        }
                    });
                } catch (Throwable caught) {
                    promise.reject(caught);
                }
            }
        });
    }

    private static class ActivityAdapter implements Function<SchemaDTO, FormClass> {

        private int activityId;

        private ActivityAdapter(int activityId) {
            this.activityId = activityId;
        }

        @Nullable
        @Override
        public FormClass apply(@Nullable SchemaDTO schemaDTO) {
            ActivityDTO activity = schemaDTO.getActivityById(activityId);
            ActivityUserFormBuilder builder = new ActivityUserFormBuilder(activity);
            return builder.build();
        }
    }

    private static class SiteAdapter implements Function<SiteResult, FormInstance> {

        @Nullable
        @Override
        public FormInstance apply(@Nullable SiteResult siteResult) {
            if (siteResult.getData().isEmpty()) {
                throw new NotFoundException();
            }
            return InstanceAdapters.fromSite(siteResult.getData().get(0));
        }
    }

}
