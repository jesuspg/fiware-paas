package com.telefonica.euro_iaas.paasmanager.installator;

import static com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider.SDC_SERVER_URL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;

import static com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider.SDC_SERVER_MEDIATYPE;
import static com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider.SDC_SERVER_URL;

import com.telefonica.euro_iaas.paasmanager.installator.ProductInstallatorSdcImpl;
import com.telefonica.euro_iaas.paasmanager.installator.sdc.util.SDCUtil;
import com.telefonica.euro_iaas.paasmanager.manager.ProductReleaseManager;
import com.telefonica.euro_iaas.paasmanager.manager.TierInstanceManager;
import com.telefonica.euro_iaas.paasmanager.model.Attribute;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.OS;
import com.telefonica.euro_iaas.paasmanager.model.ProductInstance;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;
import com.telefonica.euro_iaas.paasmanager.model.ProductType;
import com.telefonica.euro_iaas.paasmanager.model.TierInstance;
import com.telefonica.euro_iaas.paasmanager.model.InstallableInstance.Status;
import com.telefonica.euro_iaas.paasmanager.model.dto.VM;
import com.telefonica.euro_iaas.paasmanager.model.searchcriteria.ProductInstanceSearchCriteria;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;

import com.telefonica.euro_iaas.sdc.client.SDCClient;
import com.telefonica.euro_iaas.sdc.client.services.ProductInstanceService;
import com.telefonica.euro_iaas.sdc.client.services.impl.ProductInstanceServiceImpl;
import com.telefonica.euro_iaas.sdc.model.dto.ProductInstanceDto;
import com.telefonica.euro_iaas.sdc.model.Task;

/**
 * Unit test suite for ProductInstallatorSdcImpl.
 * 
 * @author Jesus M. Movilla
 * 
 */
public class ProductInstallatorSdcImplTest {

	private SystemPropertiesProvider systemPropertiesProvider;
	private SDCClient sdcClient;
	private ProductInstanceService service;
	private ProductReleaseManager productReleaseManager;
	private TierInstanceManager tierInstanceManager;
	private SDCUtil sDCUtil;

	private ProductInstance expectedProductInstance;
	private TierInstance tierInstance;
	private ProductRelease productRelease;
	private OS os;
	private Attribute attribute;
	private VM host = new VM("fqn", "ip", "hostname", "domain");
	private Task task;
	private com.telefonica.euro_iaas.sdc.model.ProductInstance pInstanceSDC;

	@Before
	public void setUp() throws Exception {
		
		os = new OS("os1", "1", "os1 description", "v1");
		host.setOsType(os.getOsType());
		attribute = new Attribute("key", "value");

		ProductType productType = new ProductType("type A", "Type A desc");
		tierInstance = new TierInstance();
		productRelease = new ProductRelease("productPrueba", "1.0",
				"Product Prueba desc", Arrays.asList(attribute), null, Arrays
						.asList(os), true, productType);

		expectedProductInstance = new ProductInstance(productRelease,
				Status.INSTALLED, "vdc");
		expectedProductInstance.setPrivateAttributes(Arrays.asList(attribute));
		
		tierInstance.addProductInstance(expectedProductInstance);
		tierInstance.setVM(host);
		
		systemPropertiesProvider = mock(SystemPropertiesProvider.class);
		when(systemPropertiesProvider.getProperty(SDC_SERVER_URL)).thenReturn(
				"url");
		when(systemPropertiesProvider.getProperty(SDC_SERVER_MEDIATYPE))
				.thenReturn("MediaType");
		
		tierInstanceManager = mock(TierInstanceManager.class);
		
		productReleaseManager = mock(ProductReleaseManager.class);
		when(productReleaseManager.load(any(String.class))).thenReturn(productRelease);
		
		task = new Task ();
		task.setHref("http://130.206.80.119:8081/sdc2/rest/vdc/60b4125450fc4a109f50357894ba2e28/task/581");
		
		pInstanceSDC = mock(com.telefonica.euro_iaas.sdc.model.ProductInstance.class);
		when(pInstanceSDC.getVm()).thenReturn(new com.telefonica.euro_iaas.sdc.model.dto.VM("aa","bb","cc","dd"));
		
		service = mock(ProductInstanceService.class);
		when(
				service.install(Mockito.anyString(), Mockito
						.any(ProductInstanceDto.class), Mockito.anyString()))
				.thenReturn(task);
		when(service.load(Mockito.anyString(), Mockito.anyString())).thenReturn(pInstanceSDC);
		
		sdcClient = mock(SDCClient.class);
		when(
				sdcClient.getProductInstanceService(Mockito.anyString(),
						Mockito.anyString())).thenReturn(service);
		
		sDCUtil = mock(SDCUtil.class);
		/*when(sDCUtil.checkTaskStatus(any(Task.class),
						Mockito.anyString()));*/
	}

	@Test
	public void testInstallWhenEverithingIsOk() throws Exception {
		ProductInstallatorSdcImpl installator = new ProductInstallatorSdcImpl();
		installator.setSDCClient(sdcClient);
		installator.setSystemPropertiesProvider(systemPropertiesProvider);
		installator.setProductReleaseManager(productReleaseManager);
		installator.setSDCUtil(sDCUtil);
		installator.setTierInstanceManager(tierInstanceManager);

		ClaudiaData data = new ClaudiaData ("org", "vdc", "");
		ProductInstance installedProduct = installator.install(data, "env", tierInstance,
				expectedProductInstance.getProductRelease(), null);
		// make verifications
		assertEquals(expectedProductInstance, installedProduct);

		verify(systemPropertiesProvider, times(1)).getProperty(SDC_SERVER_URL);
		verify(systemPropertiesProvider, times(1)).getProperty(
				SDC_SERVER_MEDIATYPE);

		// only one product will be installed, the other one causes error.
		// verify(productInstanceDao,
		// times(1)).update(any(ProductInstance.class));
		// verify(productInstanceDao, times(1)).findUniqueByCriteria(
		// any(ProductInstanceSearchCriteria.class));
	}
	
	@Test
	public void testObtainProductInstanceName() throws Exception {
		ProductInstallatorSdcImpl installator = new ProductInstallatorSdcImpl();
		installator.setSDCClient(sdcClient);
		installator.setSystemPropertiesProvider(systemPropertiesProvider);
		installator.setProductReleaseManager(productReleaseManager);
		installator.setSDCUtil(sDCUtil);
		installator.setTierInstanceManager(tierInstanceManager);

		ClaudiaData data = new ClaudiaData ("FIWARE", "60b4125450fc4a109f50357894ba2e28", "deploytm");
		expectedProductInstance.setName("deploytm-contextbrokr-1_mongos_2.2.3");
		String name = installator.getProductInstanceName(data, expectedProductInstance);
		// make verifications
		assertEquals(name, "FIWARE.customers.60b4125450fc4a109f50357894ba2e28.services.deploytm.vees.contextbrokr.replicas.1_mongos_2.2.3");

			

		// only one product will be installed, the other one causes error.
		// verify(productInstanceDao,
		// times(1)).update(any(ProductInstance.class));
		// verify(productInstanceDao, times(1)).findUniqueByCriteria(
		// any(ProductInstanceSearchCriteria.class));
	}

	@Test
	public void testConfigureWhenEverithingIsOk() throws Exception {
		ProductInstallatorSdcImpl installator = new ProductInstallatorSdcImpl();
		installator.setSDCClient(sdcClient);
		installator.setSystemPropertiesProvider(systemPropertiesProvider);
		installator.setSDCUtil(sDCUtil);
		
		ProductInstance productInstance = new ProductInstance(productRelease,
				Status.INSTALLED, "vdc");
		productInstance.setName("service-tier-1_"+ productRelease.getProduct()+"_"+productRelease.getVersion());

		productInstance.setPrivateAttributes(Arrays.asList(attribute));
		List<Attribute> attributes = new ArrayList();
		attributes.add(new Attribute("dd", "ddd"));
		
		ClaudiaData data = new ClaudiaData ("org", "vdc", "service");
		installator.configure(data,productInstance, attributes);

		verify(systemPropertiesProvider, times(1)).getProperty(SDC_SERVER_URL);
		verify(systemPropertiesProvider, times(1)).getProperty(
				SDC_SERVER_MEDIATYPE);

		// only one product will be installed, the other one causes error.
		// verify(productInstanceDao,
		// times(1)).update(any(ProductInstance.class));
		// verify(productInstanceDao, times(1)).findUniqueByCriteria(
		// any(ProductInstanceSearchCriteria.class));
	}
}