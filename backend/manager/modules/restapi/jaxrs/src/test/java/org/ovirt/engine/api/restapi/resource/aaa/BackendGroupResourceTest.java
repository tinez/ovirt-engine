package org.ovirt.engine.api.restapi.resource.aaa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendGroupResourceTest
    extends AbstractBackendSubResourceTest<Group, DbGroup, BackendGroupResource> {

    public BackendGroupResourceTest() {
        super(new BackendGroupResource(GUIDS[0].toString(), new BackendGroupsResource()));
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testBadGuid() {
        try {
            new BackendGroupResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations();
        verifyModel(resource.get(), 0);
    }

    @Test
    public void testRemove() {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(
                ActionType.RemoveGroup,
                IdParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                true,
                true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() {
        setUpGetEntityExpectations(GUIDS[0], true);
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    private void setUpGetEntityExpectations(Guid entityId, boolean returnNull) {
        setUpGetEntityExpectations(
                QueryType.GetDbGroupById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { entityId },
                returnNull ? null : getEntity(0));
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    private void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(
                ActionType.RemoveGroup,
                IdParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                valid,
                success));
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    private void setUpGetEntityExpectations() {
        setUpGetEntityExpectations(false);
    }

    private void setUpGetEntityExpectations(boolean notFound) {
        setUpGetEntityExpectations(
             QueryType.GetDbGroupById,
             IdQueryParameters.class,
             new String[] { "Id" },
             new Object[] { GUIDS[0] },
             notFound ? null : getEntity(0)
        );
    }

    @Override
    protected DbGroup getEntity(int index) {
        DbGroup entity = new DbGroup();
        entity.setId(GUIDS[index]);
        entity.setExternalId(EXTERNAL_IDS[index]);
        entity.setName(NAMES[index]);
        entity.setDomain(DOMAIN);
        return entity;
    }

    protected void verifyModel(Group model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertNotNull(model.getDomain());
        verifyLinks(model);
    }
}
