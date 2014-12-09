package org.activityinfo.store.hrd;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.activityinfo.model.auth.AccessControlRule;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.expr.ExprValue;
import org.activityinfo.store.hrd.entity.workspace.AcrEntry;
import org.activityinfo.store.hrd.entity.workspace.AcrEntryKey;
import org.activityinfo.store.hrd.entity.workspace.LatestVersionKey;
import org.activityinfo.store.hrd.entity.workspace.WorkspaceEntityGroup;
import org.activityinfo.store.hrd.tx.ReadTx;

import javax.ws.rs.WebApplicationException;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.activityinfo.model.resource.Resources.ROOT_ID;

public class Authorization {

    private ReadTx transaction;
    private ResourceId userResourceId;

    final private AccessControlRule accessControlRule;
    private WorkspaceEntityGroup workspace;
    private AuthenticatedUser authenticatedUser;

    /**
     * Standard constructor. Extracts one {@link AccessControlRule} corresponding to a user and resource from the index.
     * @param authenticatedUser The {@link AuthenticatedUser} this {@link AccessControlRule} should correspond with.
     * @param resourceId The id of the {@link Resource} this {@link AccessControlRule} should correspond with.
     * @param transaction The {@link org.activityinfo.store.hrd.tx.ReadTx} that should be used to extract the {@link AccessControlRule}.
     */
    public Authorization(WorkspaceEntityGroup workspace, AuthenticatedUser authenticatedUser, ResourceId resourceId, ReadTx transaction) {
        this.workspace = workspace;
        this.authenticatedUser = authenticatedUser;
        Preconditions.checkNotNull(authenticatedUser);
        Preconditions.checkNotNull(resourceId);
        Preconditions.checkNotNull(transaction);

        this.userResourceId = Preconditions.checkNotNull(authenticatedUser.getUserResourceId());
        this.transaction = transaction;

        this.accessControlRule = findRule(transaction, resourceId);
    }

    public Authorization(AccessControlRule accessControlRule) {
        this.accessControlRule = accessControlRule;
    }

    private AccessControlRule findRule(ReadTx tx, ResourceId resourceId) {

        if(AccessControlRule.isAcrId(resourceId)) {
            // todo: not quite right
            return null;
        }

        while(!ROOT_ID.equals(resourceId)) {
            LatestVersionKey resourceKey = new LatestVersionKey(workspace, resourceId);
            Optional<AcrEntry> rule = tx.getIfExists(new AcrEntryKey(resourceKey, authenticatedUser));
            if (rule.isPresent()) {
                return rule.get().toAccessControlRule();
            }

            // ACRs are inherited from the owner, so if we don't find an ACR here,
            // ascend to this resource's owner in search of an applicable rule.
            resourceId = tx.getOrThrow(resourceKey).getOwnerId();
        }
        return null;
    }

    /**
     * Special-purpose constructor. Checks that the ACR (as a {@link Resource}) refers to the {@link AuthenticatedUser}.
     * @param authenticatedUser The {@link AuthenticatedUser} this {@link AccessControlRule} should correspond with.
     * @param rule The requested {@link AccessControlRule}, represented as a {@link Resource}.
     */
    Authorization(AuthenticatedUser authenticatedUser, Resource rule) {
        if (authenticatedUser != null && rule != null) {
            AccessControlRule accessControlRule = AccessControlRule.fromResource(rule);
            ResourceId userResourceId = authenticatedUser.getUserResourceId();
            assert userResourceId != null;

            if (userResourceId.equals(accessControlRule.getPrincipalId())) {
                this.accessControlRule = accessControlRule;
                return;
            }
        }

        accessControlRule = null;
    }

    /**
     * @return whether the user can view the resource
     */
    public boolean canView() {
        return isOwner() || evaluate(getViewCondition());
    }

    /**
     * @return whether the user can edit the resource
     */
    public boolean canEdit() {
        return isOwner() || evaluate(getEditCondition());
    }

    /**
     * @return the id of the {@link AccessControlRule}
     */
    public ResourceId getId() {
        return accessControlRule != null ? accessControlRule.getId() : null;
    }

    /**
     * @return the id of the resource to which the {@link AccessControlRule} applies
     */
    public ResourceId getResourceId() {
        return accessControlRule != null ? accessControlRule.getResourceId() : null;
    }

    /**
     * This method is determines if a change in authorization has made a resource newly visible.
     * @param oldAuthorization the old authorization object to compare this one to
     * @return whether the user can now view the resource, but could not view it previously
     */
    public boolean canViewNowButNotAsOf(Optional<Authorization> oldAuthorization) {
        if (oldAuthorization.isPresent()) {
            return canView() && !oldAuthorization.get().canView();
        } else {
            return false;   // If the object was newly created, its previous visibility is undefined, so nothing changed
        }
    }

    public void assertCanEdit() {
        if(!canEdit()) {
            throw new WebApplicationException(UNAUTHORIZED);
        }
    }

    public void assertCanView() {
        if (!canView()) {
            throw new WebApplicationException(UNAUTHORIZED);
        }
    }

    public void assertIsOwner() {
        if (!isOwner()) {
            throw new WebApplicationException(UNAUTHORIZED);
        }
    }

    private static boolean evaluate(ExprValue exprValue) {
        return exprValue != null && "true".equals(exprValue.getExpression());
    }

    private ExprValue getEditCondition() {
        return accessControlRule != null ? accessControlRule.getEditCondition() : null;
    }

    private ExprValue getViewCondition() {
        return accessControlRule != null ? accessControlRule.getViewCondition() : null;
    }

    public boolean isOwner() {
        return accessControlRule != null && accessControlRule.isOwner();
    }

}