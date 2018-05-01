package org.ovirt.engine.api.restapi.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendFilesResourceTest
    extends AbstractBackendCollectionResourceTest<File, String, BackendFilesResource> {

    public BackendFilesResourceTest() {
        super(new BackendFilesResource(GUIDS[0].toString()), null, null);
    }

    @Test
    @Ignore
    @Override
    public void testQuery() {
    }

    @Test
    public void testGet() {
        BackendFileResource resource = new BackendFileResource(NAMES[0], collection);
        collection.setUriInfo(setUpUriExpectations(null));
        setUpQueryExpectations("", null);
        verifyModel(resource.get(), 0);
    }

    @Test
    public void testBadGet() {
        BackendFileResource resource = new BackendFileResource("foo", collection);
        collection.setUriInfo(setUpUriExpectations(null));
        setUpQueryExpectations("", null);
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Override
    @Test
    public void testList() {
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);
        setupGetStorageDomainExpectations(StorageDomainType.ISO);
        setUpQueryExpectations("", null);

        verifyCollection(getCollection());
    }

    @Test
    public void testListNonIso() {
        try {
            UriInfo uriInfo = setUpUriExpectations(null);

            setupGetStorageDomainExpectations(StorageDomainType.Data);

            collection.setUriInfo(uriInfo);
            verifyCollection(getCollection());

            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Override
    @Test
    public void testListCrashClientLocale() {
        UriInfo uriInfo = setUpUriExpectations(null);
        locales.add(CLIENT_LOCALE);

        Throwable t = new RuntimeException(FAILURE);
        setUpEntityQueryExpectations(QueryType.GetImagesList,
                GetImagesListParameters.class,
                new String[] { "StorageDomainId", "ImageType" },
                new Object[] { GUIDS[0], ImageFileType.All },
                setUpFiles(),
                AbstractBackendCollectionResourceTest.FAILURE);
        collection.setUriInfo(uriInfo);
        setupGetStorageDomainExpectations(StorageDomainType.ISO);
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_CLIENT_LOCALE, t);
        } finally {
            locales.clear();
        }
    }

    @Test
    @Override
    public void testListCrash() {
        UriInfo uriInfo = setUpUriExpectations(null);

        Throwable t = new RuntimeException(FAILURE);
        setUpEntityQueryExpectations(QueryType.GetImagesList,
                GetImagesListParameters.class,
                new String[] { "StorageDomainId", "ImageType" },
                new Object[] { GUIDS[0], ImageFileType.All },
                setUpFiles(),
                AbstractBackendCollectionResourceTest.FAILURE);
        setupGetStorageDomainExpectations(StorageDomainType.ISO);
        collection.setUriInfo(uriInfo);
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_SERVER_LOCALE, t);
        }
    }

    @Override
    protected void verifyFault(WebApplicationException wae, String reason, Throwable t) {
        assertEquals(BAD_REQUEST, wae.getResponse().getStatus());
        assertTrue(wae.getResponse().getEntity() instanceof Fault);
        Fault fault = (Fault) wae.getResponse().getEntity();
        assertEquals(reason, fault.getReason());
        assertNotNull(fault.getDetail());
        assertTrue("expected detail to include: " + t.getMessage(), fault.getDetail().contains(t.getMessage()));
    }

    @Test
    @Override
    public void testListFailure() {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpEntityQueryExpectations(QueryType.GetImagesList,
                GetImagesListParameters.class,
                new String[] { "StorageDomainId", "ImageType" },
                new Object[] { GUIDS[0], ImageFileType.All },
                setUpFiles(),
                AbstractBackendCollectionResourceTest.FAILURE);
        setupGetStorageDomainExpectations(StorageDomainType.ISO);
        collection.setUriInfo(uriInfo);
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertTrue(wae.getResponse().getEntity() instanceof Fault);
            assertEquals(mockl10n(FAILURE), ((Fault) wae.getResponse().getEntity()).getDetail());
        }
    }

    private void setupGetStorageDomainExpectations(org.ovirt.engine.core.common.businessentities.StorageDomainType type) {
        setUpEntityQueryExpectations(QueryType.GetStorageDomainById,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[0] },
                                     getStorageDomain(GUIDS[0], type));
    }

    private org.ovirt.engine.core.common.businessentities.StorageDomain getStorageDomain(Guid id, StorageDomainType type) {
        org.ovirt.engine.core.common.businessentities.StorageDomain sd =  new org.ovirt.engine.core.common.businessentities.StorageDomain();
        sd.setId(id);
        sd.setStorageDomainType(type);
        return sd;
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        assertEquals("", query);

        setUpEntityQueryExpectations(QueryType.GetImagesList,
                                     GetImagesListParameters.class,
                                     new String[] { "StorageDomainId", "ImageType" },
                                     new Object[] { GUIDS[0], ImageFileType.All },
                                     setUpFiles(),
                                     failure);
    }

    private List<RepoImage> setUpFiles() {
        List<RepoImage> isos = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            RepoImage file = new RepoImage();
            file.setRepoImageId(NAMES[i]);
            isos.add(file);
        }
        return isos;
    }

    @Override
    protected String getEntity(int index) {
        return NAMES[index];
    }

    @Override
    protected void verifyModel(File model, int index) {
        assertEquals(NAMES[index], model.getId());
        assertEquals(NAMES[index], model.getName());
        assertNotNull(model.getStorageDomain());
        assertEquals(GUIDS[0].toString(), model.getStorageDomain().getId());
        verifyLinks(model);
    }

    @Override
    protected List<File> getCollection() {
        return collection.list().getFiles();
    }

    @Override
    protected void verifyCollection(List<File> collection) {
        assertNotNull(collection);
        assertEquals(NAMES.length, collection.size());
        for (int i = 0; i < NAMES.length; i++) {
            verifyModel(collection.get(i), i);
        }
    }
}
