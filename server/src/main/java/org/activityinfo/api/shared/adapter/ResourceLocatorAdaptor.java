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

/**
 * Exposes a legacy {@code Dispatcher} implementation as new {@code ResourceLocator}
 */
public class ResourceLocatorAdaptor implements ResourceLocator {

    private final Dispatcher dispatcher;

    public ResourceLocatorAdaptor(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Remote<FormClass> getUserForm(Cuid formId) {
        if (formId.getDomain() == CuidAdapter.ACTIVITY_DOMAIN) {
            int activityId = CuidAdapter.getLegacyIdFromCuid(formId);
            return Remotes.transform(execute(new GetSchema()), new ActivityAdapter(activityId));
        }
        return Remotes.error(new NotFoundException(formId.asIri()));
    }

    @Override
    public Remote<FormInstance> getFormInstance(Cuid instanceId) {
        if (instanceId.getDomain() == CuidAdapter.SITE_DOMAIN) {
            int siteId = CuidAdapter.getLegacyIdFromCuid(instanceId);
            return Remotes.transform(execute(GetSites.byId(siteId)), new SiteAdapter());
        }
        return Remotes.error(new NotFoundException(instanceId.asIri()));
    }

    @Override
    public Promise<Void> persist(Resource resource) {
        return Promise.rejected(new UnsupportedOperationException());
    }

    @Override
    public Remote<Integer> countInstances(InstanceCriteria criteria) {
        return Remotes.error(new UnsupportedOperationException());
    }

    @Override
    public Remote<List<FormInstance>> queryInstances(InstanceCriteria criteria) {
        if (criteria != null && criteria.getClasses() != null) {
            final Filter filter = new Filter();
            final List<Integer> idList = Lists.newArrayList(Iterables.transform(criteria.getClasses(), new Function<Iri, Integer>() {
                @Nullable
                @Override
                public Integer apply(@Nullable Iri input) {
                    return CuidAdapter.attributeGroupLegacyId(input);
                }
            }));
            filter.addRestriction(DimensionType.AttributeGroup, idList);
            return Remotes.transform(execute(new GetAttributeGroupsDimension(filter)), new Function<AttributeGroupResult, List<FormInstance>>() {
                @Nullable
                @Override
                public List<FormInstance> apply(@Nullable AttributeGroupResult input) {
                    final List<AttributeGroupDTO> data = input.getData();
                    final List<FormInstance> list = new ArrayList<FormInstance>();
                    for (AttributeGroupDTO attributeGroup : data) {
                        final List<AttributeDTO> attributes = attributeGroup.getAttributes();
                        for (AttributeDTO attribute : attributes) {
                            list.add(InstanceAdapters.fromAttribute(attribute, CuidAdapter.attributeGroupFormClass(attributeGroup)));
                        }
                    }
                    return list;
                }
            });
        }
        return Remotes.error(new UnsupportedOperationException());
    }

    /**
     * Wraps a legacy command dispatch in a new {@code Remote} object
     *
     * @param command the command to execute
     * @param <R>     the type of the {@code Command}'s {@code CommandResult}
     */
    private <R extends CommandResult> Remote<R> execute(final Command<R> command) {
        return new Remote<R>() {

            @Override
            public Promise<R> fetch() {
                try {
                    final Promise<R> promise = new Promise<>();
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
                    return promise;
                } catch (Throwable caught) {
                    return Promise.rejected(caught);
                }
            }
        };
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