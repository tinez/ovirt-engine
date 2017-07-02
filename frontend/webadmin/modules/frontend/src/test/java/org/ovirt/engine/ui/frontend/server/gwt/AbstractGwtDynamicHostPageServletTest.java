package org.ovirt.engine.ui.frontend.server.gwt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.ovirt.engine.core.branding.BrandingManager;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractGwtDynamicHostPageServletTest<T extends GwtDynamicHostPageServlet> {

    protected static final String SELECTOR_SCRIPT = "myapp.nocache.js"; //$NON-NLS-1$

    @Mock
    protected HttpServletRequest mockRequest;

    @Mock
    protected HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    @Mock
    private BackendLocal mockBackend;

    @Mock
    private BrandingManager mockBrandingManager;

    @Mock
    private ServletContext mockServletContext;

    @Mock
    private DbUser mockUser;

    @Mock
    private VdcQueryParametersBase mockQueryParams;

    @Mock
    private GetConfigurationValueParameters mockConfigQueryParams;

    @Mock
    protected MessageDigest mockDigest;

    @Mock
    private ObjectNode mockUserInfoObject;

    @Captor
    protected ArgumentCaptor<byte[]> byteArrayCaptor;

    protected T testServlet;

    @Before
    public void setUp() throws NoSuchAlgorithmException {
        when(mockRequest.getAttribute(GwtDynamicHostPageServlet.
                MD5Attributes.ATTR_SELECTOR_SCRIPT.getKey())).
                thenReturn(SELECTOR_SCRIPT);
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockRequest.getSession().getServletContext()).thenReturn(mockServletContext);
        when(mockUser.getId()).thenReturn(Guid.newGuid());
        when(mockUser.getLoginName()).thenReturn("admin"); //$NON-NLS-1$
        when(mockUser.getDomain()).thenReturn("internal"); //$NON-NLS-1$
        stubGetUserBySessionIdQuery();
        stubGetConfigurationValuePublicQuery();
        setUpTestServlet();
    }

    protected void setUpTestServlet() throws NoSuchAlgorithmException {
        testServlet = getTestServletSpy();
        testServlet.setBackend(mockBackend);
        testServlet.init(new ObjectMapper(), mockBrandingManager);
        doReturn(mockDigest).when(testServlet).createMd5Digest();
    }

    protected abstract T getTestServletSpy();

    @Test
    public void testDoGet_WithoutUserInfoObject() throws IOException, ServletException, NoSuchAlgorithmException {
        doReturn(null).when(testServlet).getLoggedInUser(nullable(String.class));
        doReturn(mockDigest).when(testServlet).getMd5Digest(nullable(HttpServletRequest.class));
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockRequest).setAttribute(eq(GwtDynamicHostPageServlet.MD5Attributes.ATTR_SELECTOR_SCRIPT.getKey()),
                nullable(String.class));
        verify(mockRequest, never()).setAttribute(eq(GwtDynamicHostPageServlet.MD5Attributes.ATTR_USER_INFO.getKey()),
                nullable(ObjectNode.class));
        verify(mockRequest).setAttribute(GwtDynamicHostPageServlet.MD5Attributes.ATTR_MESSAGES.getKey(),
                null); //$NON-NLS-1$
    }

    @Test
    public void testDoGet_WithUserInfoObject() throws IOException, ServletException, NoSuchAlgorithmException {
        doReturn(mockDigest).when(testServlet).getMd5Digest(nullable(HttpServletRequest.class));
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockRequest).setAttribute(eq(GwtDynamicHostPageServlet.MD5Attributes.ATTR_SELECTOR_SCRIPT.getKey()),
                nullable(String.class));
        verify(mockRequest).setAttribute(eq(GwtDynamicHostPageServlet.MD5Attributes.ATTR_USER_INFO.getKey()),
                nullable(ObjectNode.class));
        verify(mockRequest).setAttribute(GwtDynamicHostPageServlet.MD5Attributes.ATTR_MESSAGES.getKey(),
                null); //$NON-NLS-1$
    }

    @Test
    public void testDoGet_CalculateMd5_ResourceNotModifiedResponse() throws IOException, ServletException,
            NoSuchAlgorithmException {
        String md5sum = "md5sum"; //$NON-NLS-1$
        doReturn(md5sum).when(testServlet).getMd5Sum(mockRequest);
        when(mockRequest.getHeader(GwtDynamicHostPageServlet.IF_NONE_MATCH_HEADER)).thenReturn(md5sum);
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }

    @Test
    public void testDoGet_CalculateMd5_ResourceModifiedEtagResponse() throws IOException, ServletException,
            NoSuchAlgorithmException {
        String md5sum = "md5sum"; //$NON-NLS-1$
        doReturn(md5sum).when(testServlet).getMd5Sum(mockRequest);
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockResponse).addHeader(GwtDynamicHostPageServlet.ETAG_HEADER, md5sum);
    }

    @Test
    public void testInitQueryParams() {
        String sessionId = "sessionId"; //$NON-NLS-1$
        testServlet.initQueryParams(mockQueryParams, sessionId);
        verify(mockQueryParams).setSessionId(sessionId);
        verify(mockQueryParams).setFiltered(testServlet.filterQueries());
    }

    @Test
    public void testRunQuery_GetUserBySessionId() {
        String sessionId = "sessionId"; //$NON-NLS-1$
        QueryType queryType = QueryType.GetUserBySessionId;
        Object result = testServlet.runQuery(queryType, mockQueryParams, sessionId);
        assertEquals(result, mockUser);
        verify(mockQueryParams).setSessionId(sessionId);
        verify(mockQueryParams).setFiltered(testServlet.filterQueries());
        verify(mockBackend).runQuery(queryType, mockQueryParams);
    }

    @Test
    public void testRunPublicQuery_GetConfigurationValue() {
        String sessionId = "sessionId"; //$NON-NLS-1$
        QueryType queryType = QueryType.GetConfigurationValue;
        VdcQueryReturnValue returnIntValue = new VdcQueryReturnValue();
        returnIntValue.setSucceeded(true);
        returnIntValue.setReturnValue(Integer.valueOf(255));
        when(mockBackend.runPublicQuery(eq(QueryType.GetConfigurationValue),
                eq(mockConfigQueryParams))).thenReturn(returnIntValue);
        Object result = testServlet.runPublicQuery(queryType, mockConfigQueryParams, sessionId);
        assertThat(result, is(instanceOf(Integer.class)));
        verify(mockConfigQueryParams).setSessionId(sessionId);
        verify(mockConfigQueryParams).setFiltered(testServlet.filterQueries());
        verify(mockBackend).runPublicQuery(queryType, mockConfigQueryParams);
    }

    @Test
    public void testGetUserInfoObject() {
        ObjectNode result = testServlet.getUserInfoObject(mockUser, "mockSsoToken"); //$NON-NLS-1$ //$NON-NLS-2$
        assertNotNull(result.get("id")); //$NON-NLS-1$
        assertEquals("admin", result.get("userName").asText()); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("internal", result.get("domain").asText()); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("mockSsoToken", result.get("ssoToken").asText()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetMd5Digest_WithoutUserInfoObject() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest result = testServlet.getMd5Digest(mockRequest);
        assertEquals(result, mockDigest);
        verify(mockDigest, atLeast(1)).update(byteArrayCaptor.capture());
        assertArrayEquals(SELECTOR_SCRIPT.getBytes(), byteArrayCaptor.getAllValues().get(0));
    }

    @Test
    public void testGetMd5Digest_WithUserInfoObject() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String userInfo = "{ \"foo\": \"bar\" }"; //$NON-NLS-1$
        when(mockUserInfoObject.toString()).thenReturn(userInfo);
        when(mockRequest.getAttribute(GwtDynamicHostPageServlet.
                MD5Attributes.ATTR_USER_INFO.getKey())).
                thenReturn(mockUserInfoObject);
        MessageDigest result = testServlet.getMd5Digest(mockRequest);
        assertEquals(result, mockDigest);
        verify(mockDigest, atLeast(2)).update(byteArrayCaptor.capture());
        assertArrayEquals(SELECTOR_SCRIPT.getBytes(), byteArrayCaptor.getAllValues().get(0));
        assertArrayEquals(userInfo.getBytes(), byteArrayCaptor.getAllValues().get(1));
    }

    void stubGetUserBySessionIdQuery() {
        VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(mockUser);

        when(mockBackend.runQuery(
                eq(QueryType.GetUserBySessionId),
                isA(VdcQueryParametersBase.class)
        )).thenReturn(returnValue);
    }

    void stubGetConfigurationValuePublicQuery() {
        VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue("1.2.3"); //$NON-NLS-1$

        when(mockBackend.runPublicQuery(
                eq(QueryType.GetConfigurationValue),
                argThat(configValueParams(ConfigValues.ProductRPMVersion))
        )).thenReturn(returnValue);
    }

    ArgumentMatcher<GetConfigurationValueParameters> configValueParams(final ConfigValues configValue) {
        return argument -> argument.getConfigValue() == configValue;
    }

}
