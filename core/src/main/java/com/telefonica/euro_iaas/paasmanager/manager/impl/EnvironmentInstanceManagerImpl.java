package com.telefonica.euro_iaas.paasmanager.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.telefonica.euro_iaas.commons.dao.AlreadyExistsEntityException;
import com.telefonica.euro_iaas.commons.dao.EntityNotFoundException;
import com.telefonica.euro_iaas.commons.dao.InvalidEntityException;
import com.telefonica.euro_iaas.paasmanager.dao.EnvironmentInstanceDao;
import com.telefonica.euro_iaas.paasmanager.dao.TierInstanceDao;

import com.telefonica.euro_iaas.paasmanager.exception.InfrastructureException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidEnvironmentRequestException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidOVFException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidProductInstanceRequestException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidVappException;
import com.telefonica.euro_iaas.paasmanager.exception.NotUniqueResultException;
import com.telefonica.euro_iaas.paasmanager.exception.ProductInstallatorException;
import com.telefonica.euro_iaas.paasmanager.manager.EnvironmentInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.EnvironmentManager;
import com.telefonica.euro_iaas.paasmanager.manager.InfrastructureManager;
import com.telefonica.euro_iaas.paasmanager.manager.ProductInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.TierInstanceManager;
import com.telefonica.euro_iaas.paasmanager.installator.ProductInstallator;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Environment;
import com.telefonica.euro_iaas.paasmanager.model.EnvironmentInstance;
import com.telefonica.euro_iaas.paasmanager.model.InstallableInstance.Status;
import com.telefonica.euro_iaas.paasmanager.model.ProductInstance;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;
import com.telefonica.euro_iaas.paasmanager.model.Tier;
import com.telefonica.euro_iaas.paasmanager.model.TierInstance;

import com.telefonica.euro_iaas.paasmanager.model.searchcriteria.EnvironmentInstanceSearchCriteria;
import com.telefonica.euro_iaas.paasmanager.util.EnvironmentUtils;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;

import com.telefonica.euro_iaas.sdc.model.dto.ChefClient;

public class EnvironmentInstanceManagerImpl implements
		EnvironmentInstanceManager {

	private EnvironmentInstanceDao environmentInstanceDao;
	private TierInstanceDao tierInstanceDao;
	private SystemPropertiesProvider systemPropertiesProvider;

	private ProductInstanceManager productInstanceManager;
	private EnvironmentManager environmentManager;
	private InfrastructureManager infrastructureManager;
	private TierInstanceManager tierInstanceManager;
	private EnvironmentUtils environmentUtils;
	private ProductInstallator productInstallator;

	/** The log. */
	private static Logger log = Logger
			.getLogger(EnvironmentInstanceManagerImpl.class);

	/** Max lenght of an OVF */
	private static final Integer tam_max = 90000;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.telefonica.euro_iaas.paasmanager.manager.EnvironmentInstanceManager
	 * #findByCriteria
	 * (com.telefonica.euro_iaas.paasmanager.model.searchcriteria.
	 * EnvironmentInstanceSearchCriteria)
	 */
	public List<EnvironmentInstance> findByCriteria(
			EnvironmentInstanceSearchCriteria criteria) {

		return environmentInstanceDao.findByCriteria(criteria);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.telefonica.euro_iaas.paasmanager.manager.EnvironmentInstanceManager
	 * #findAll()
	 */
	public List<EnvironmentInstance> findAll() {
		return environmentInstanceDao.findAll();
	}

	public EnvironmentInstance create(ClaudiaData claudiaData,EnvironmentInstance environmentInstance) 
			throws AlreadyExistsEntityException, 
			InvalidEntityException, EntityNotFoundException, InvalidVappException, InvalidOVFException, 
			InfrastructureException, ProductInstallatorException {
			
		Environment environment = insertEnvironemntInDatabase (claudiaData, environmentInstance.getEnvironment());
	
		if (environmentInstance.getEnvironment().getOvf() != null)
			environment.setOvf(environmentInstance.getEnvironment().getOvf());
		
	//	environmentInstance.setVdc(claudiaData.getVdc());
		environmentInstance.setName(environmentInstance.getVdc() + "-" + environment.getName());
		
		environmentInstance.setEnvironment(environment);
		environmentInstance.setStatus(Status.INIT);

		environmentInstance = insertEnvironenttInstanceInDatabase(environmentInstance);

		log.info("Creating the infrastructure");
		environmentInstance.setStatus(Status.DEPLOYING);
		environmentInstanceDao.update(environmentInstance);

		try {
			environmentInstance = infrastructureManager
					.createInfrasctuctureEnvironmentInstance(
							environmentInstance, environment.getTiers(),
							claudiaData);

		} catch (InvalidVappException e) {
			environmentInstance.setStatus(Status.ERROR);
			environmentInstanceDao.update(environmentInstance);
			throw new InvalidVappException(e);
		} catch (InvalidOVFException e) {
			environmentInstance.setStatus(Status.ERROR);
			environmentInstanceDao.update(environmentInstance);
			throw new InvalidOVFException(e);
		} catch (InfrastructureException e) {
			environmentInstance.setStatus(Status.ERROR);
			environmentInstanceDao.update(environmentInstance);
			throw new InfrastructureException(e);
		}

		// environment = environmentUtils.resolveMacros(environmentInstance);
		// environmentManager.update(environment);

		environmentInstance.setStatus(Status.INSTALLING);
		environmentInstanceDao.update(environmentInstance);

		log.info("Installing software");
		boolean bScalableEnvironment;
		try {
			bScalableEnvironment = installSoftwareInEnvironmentInstance(
					claudiaData, environmentInstance);
		} catch (ProductInstallatorException e) {
			environmentInstance.setStatus(Status.ERROR);
			environmentInstanceDao.update(environmentInstance);
			throw new ProductInstallatorException(e);
		} catch (InvalidProductInstanceRequestException e) {
			environmentInstance.setStatus(Status.ERROR);
			environmentInstanceDao.update(environmentInstance);
			throw new ProductInstallatorException(e);
		} catch (NotUniqueResultException e) {
			environmentInstance.setStatus(Status.ERROR);
			environmentInstanceDao.update(environmentInstance);
			throw new ProductInstallatorException(e);
		}

		environmentInstance.setStatus(Status.INSTALLED);
		environmentInstanceDao.update(environmentInstance);

		infrastructureManager.StartStopScalability(claudiaData,
				bScalableEnvironment);

		environmentInstance.setStatus(Status.INSTALLED);
		environmentInstanceDao.update(environmentInstance);

		log.info("Enviroment Instance installed correctly");

		// EnvironmentInstance environmentInstanceDB =
		// insertEnvironmentInstanceDB( claudiaData, environmentInstance);

		return environmentInstance;
	}

	public boolean installSoftwareInEnvironmentInstance(
			ClaudiaData claudiaData, EnvironmentInstance environmentInstance)
			throws ProductInstallatorException,
			InvalidProductInstanceRequestException, NotUniqueResultException,
			InfrastructureException, InvalidEntityException {
		// TierInstance by TierInstance let's check if we have to install
		// software
		boolean bScalableEnvironment = false;

		for (TierInstance tierInstance : environmentInstance.getTierInstances()) {
			log.info("Install software in tierInstance "
					+ tierInstance.getName() + " from tier "
					+ tierInstance.getTier().getName());
			// check if the tier is scalable
			boolean state = checkScalability(tierInstance.getTier());
			if (!bScalableEnvironment) {
				bScalableEnvironment = (state) ? true : false;
			}
			tierInstance.setStatus(Status.INSTALLING);
			tierInstanceDao.update(tierInstance);
			String newOVF = " ";
			if ((tierInstance.getTier().getProductReleases() != null)
					&& (tierInstance.getTier().getProductReleases().size() != 0)) {

				for (ProductRelease productRelease : tierInstance.getTier()
						.getProductReleases()) {
					log.info("Install software " + productRelease.getProduct()
							+ " " + productRelease.getVersion());
					
					try {
						ProductInstance productInstance = productInstanceManager.install(tierInstance, claudiaData,
									environmentInstance.getEnvironment().getName(),
									productRelease, productRelease
											.getAttributes());
						log.info("Adding product instance "+ productInstance.getName());
						tierInstance.setStatus(Status.INSTALLED);
						tierInstance.addProductInstance(productInstance);
					} catch (ProductInstallatorException pie) {
						String message = " Error installing product " + productRelease.getName();
						tierInstance.setStatus(Status.ERROR);
						tierInstanceDao.update(tierInstance);
						log.error(message);
						throw new ProductInstallatorException (message, pie);
					}
				}
				
				if (state && tierInstance.getNumberReplica() == 1) {
					log.info("Setup scalabiliy ");
					String image_Name;
					// claudiaData.setFqn(tierInstance.getVM().getFqn());

					image_Name = infrastructureManager.ImageScalability(
							claudiaData, tierInstance);
					log.info("Generating image " + image_Name);
					log.info("Updating OVF ");
					newOVF = environmentUtils.updateVmOvf(
							tierInstance.getOvf(), image_Name);
					tierInstance.setOvf(newOVF);
					// tierInstance.setOvf(newOVF);
				}

				if (state && tierInstance.getNumberReplica() > 1) {
					log.info("Updating OVF replica more than 1 ");
					if (!newOVF.equals(" "))
						tierInstance.setOvf(newOVF);
				}
				
				tierInstance.setStatus(Status.INSTALLED);
				tierInstanceDao.update(tierInstance);
			}
		}
		return bScalableEnvironment;
	}

	private boolean checkScalability(Tier tier) {
		boolean state;
		if (tier.getMaximumNumberInstances() > tier.getMinimumNumberInstances()) {
			state = true;
		} else {
			state = false;
		}
		return state;
	}

	public EnvironmentInstance load(String vdc, String name)
			throws EntityNotFoundException {
		EnvironmentInstance instance = null;
		try
		{
		 instance = environmentInstanceDao.load(name);
		}
		catch (Exception e)
		{
			throw new EntityNotFoundException(EnvironmentInstance.class, "vdc",
					vdc);
		}
		if (!instance.getVdc().equals(vdc)) {
			throw new EntityNotFoundException(EnvironmentInstance.class, "vdc",
					vdc);
		}
		return instance;
	}

	public EnvironmentInstance loadForDelete(String vdc, String name)
			throws EntityNotFoundException {
		EnvironmentInstance instance = null;
		try {
			instance = environmentInstanceDao.loadForDelete(name);
		} catch (EntityNotFoundException e) {
			instance = environmentInstanceDao.load(name);
			instance.setTierInstances(null);
		}
		return instance;
	}

	public EnvironmentInstance update(EnvironmentInstance envInst)
			throws InvalidEntityException {
		try {
			return environmentInstanceDao.update(envInst);
		} catch (InvalidEntityException e) {
			// TODO Auto-generated catch block
			log.error("It is not possible to update the environment "
					+ envInst.getName() + " : " + e.getMessage());
			throw new InvalidEntityException(EnvironmentInstance.class, e);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.telefonica.euro_iaas.paasmanager.manager.EnvironmentInstanceManager
	 * #destroy(com.telefonica.euro_iaas.paasmanager.model.EnvironmentInstance)
	 */
	public void destroy(ClaudiaData claudiaData, EnvironmentInstance envInstance)
			throws InvalidEntityException {
		log.info("Destroying enviornment isntance "
				+ envInstance.getBlueprintName() + " with environment "
				+ envInstance.getEnvironment().getName() + " vdc " + envInstance.getVdc());
		
		try {			
			// Borrado de nodos en el chefServer
			envInstance.setStatus(Status.UNINSTALLING);
			envInstance = environmentInstanceDao.update(envInstance);
			for (int i = 0; i < envInstance.getTierInstances().size(); i++) {
				TierInstance tierInstance = envInstance.getTierInstances().get(i);
				log.info("Deleting node "
					+ tierInstance.getVM().getHostname());			
				tierInstance.setStatus(Status.UNINSTALLING);
				tierInstanceDao.update(tierInstance);			
				try{
					ChefClient chefClient = productInstallator.loadNode(
						tierInstance.getVdc(), 
						tierInstance.getVM().getHostname());
					
					productInstallator.deleteNode(tierInstance.getVdc(),
							chefClient.getName());
					
					tierInstance.setStatus(Status.UNINSTALLED);
					tierInstanceDao.update(tierInstance);
				} catch (EntityNotFoundException enfe) {
					String errorMsg = "The ChefClient " +
							tierInstance.getVM().getHostname() + 
							" is not at ChefServer";
					log.error(errorMsg);
				} catch (Exception e) {
					String errorMsg = "Error deleting node from Node Manager : "
							+ tierInstance.getVM().getFqn() + "" + e.getMessage();
					log.error(errorMsg);
					throw new InvalidEntityException(EnvironmentInstance.class, e);
				}
			//}
			
				envInstance.setStatus(Status.UNINSTALLED);
				envInstance = environmentInstanceDao.update(envInstance);

				// Borrado de VMs
				try {
					log.info("Deleting Virtual Machines for environmetn instance "
					+ envInstance.getBlueprintName());
					envInstance.setStatus(Status.UNDEPLOYING);
					envInstance = environmentInstanceDao.update(envInstance);
					infrastructureManager.deleteEnvironment(claudiaData, envInstance);
				} catch (InfrastructureException e) {
					log.error("It is not possible to delete the environment "
					+ envInstance.getName() + " : " + e.getMessage());
					throw new InvalidEntityException(EnvironmentInstance.class, e);
				}

				envInstance.setStatus(Status.UNDEPLOYED);
			}
			
			// Borrado del registro en BBDD paasmanager
			log.info("Deleting the environment instance "
					+ envInstance.getBlueprintName() + " in the database ");
		
			List<TierInstance> tierInstances = envInstance.getTierInstances();

			if (tierInstances != null) {
				envInstance.setTierInstances(null);
				try {
					envInstance = environmentInstanceDao.update(envInstance);
				} catch (InvalidEntityException e) {
					log.error(e.getMessage());
					throw new InvalidEntityException(EnvironmentInstance.class, e);
				}
				for (TierInstance tierInstance : tierInstances) {
					tierInstanceManager.remove(tierInstance);
				}
			}
		} catch (NullPointerException ne){
			log.info("Environment Instance " + envInstance.getBlueprintName()
					+ " does not have any TierInstances associated");
		} finally {
			environmentInstanceDao.remove(envInstance);
			log.info("Environment Instance " + envInstance.getBlueprintName()
					+ " DESTROYED");
		}
		
	}

	// PRVATE METHODS

	private Environment insertEnvironemntInDatabase(ClaudiaData claudiaData,
			Environment env) throws InvalidEntityException,
			EntityNotFoundException {
		log.info("Insert in database");
		Environment environment = null;
		if (env.getVdc()== null) {
			env.setVdc(claudiaData.getVdc());
		}
		if (env.getOrg()== null) {
			env.setOrg(claudiaData.getOrg());
		}

		if (systemPropertiesProvider.getProperty(
				SystemPropertiesProvider.CLOUD_SYSTEM).equals("FIWARE")) {
			try {
				environment = environmentManager.load(env.getName(), env.getVdc()); 
				if (environment.getOvf() == null && env.getOvf() != null) {
					environment.setOvf(env.getOvf());
					environment = environmentManager.update(environment);
				}
				return environment;
			} catch (EntityNotFoundException e1) {
				throw new EntityNotFoundException(Environment.class,
						"The environment should have been already created", e1);
			}
		}

		try {
			environment = environmentManager.load(env.getName(), env.getVdc()); 
			if (environment.getOvf() == null && env.getOvf() != null) {
				environment.setOvf(env.getOvf());
				environment =environmentManager.update (environment);
			}
			return environment;
		} catch (EntityNotFoundException e1) {
			try {
				environment = environmentManager.create(claudiaData,
						env);
			} catch (InvalidEnvironmentRequestException e) {
				// TODO Auto-generated catch block
				String errorMessage = " Error to create the environment . "
						+ environment.getName() + ". " + "Desc: "
						+ e.getMessage();
				log.error(errorMessage);
				throw new InvalidEntityException(Environment.class, e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				String errorMessage = " Error to create the environment . "
						+ environment.getName() + ". " + "Desc: "
						+ e.getMessage();
				log.error(errorMessage);
				throw new InvalidEntityException(Environment.class, e);
			}
		}
		return environment;
	}

	private EnvironmentInstance insertEnvironenttInstanceInDatabase(
			EnvironmentInstance environmentInstance)
			throws InvalidEntityException {
		try {
			environmentInstance = environmentInstanceDao
					.create(environmentInstance);
		} catch (InvalidEntityException e) {
			String errorMessage = " Invalid environmentInstance objetc . Desc: "
					+ e.getMessage();
			log.error(errorMessage);
			throw new InvalidEntityException(EnvironmentInstance.class, e);
		} catch (AlreadyExistsEntityException e) {
			String errorMessage = " Already exists environmentInstance objetc . "
					+ environmentInstance.getName()
					+ ". "
					+ "Desc: "
					+ e.getMessage();
			log.error(errorMessage);
			throw new InvalidEntityException(EnvironmentInstance.class, e);
		}

		return environmentInstance;
	}

	/**
	 * @param tierInstanceDao
	 *            the tierInstanceDao to set
	 */
	public void setTierInstanceDao(
			TierInstanceDao tierInstanceDao) {
		this.tierInstanceDao = tierInstanceDao;
	}

	/**
	 * @param environmentInstanceDao
	 *            the environmentInstanceDao to set
	 */
	public void setEnvironmentInstanceDao(
			EnvironmentInstanceDao environmentInstanceDao) {
		this.environmentInstanceDao = environmentInstanceDao;
	}

	/**
	 * @param productInstanceManager
	 *            the productInstanceManager to set
	 */
	public void setProductInstanceManager(
			ProductInstanceManager productInstanceManager) {
		this.productInstanceManager = productInstanceManager;
	}

	/**
	 * @param tierInstanceManager
	 *            the tierInstanceManager to set
	 */
	public void setTierInstanceManager(TierInstanceManager tierInstanceManager) {
		this.tierInstanceManager = tierInstanceManager;
	}

	/**
	 * @param environmentManager
	 *            the environmentManager to set
	 */
	public void setEnvironmentManager(EnvironmentManager environmentManager) {
		this.environmentManager = environmentManager;
	}

	/**
	 * @param infrastructureManager
	 *            the infrastructureManager to set
	 */
	public void setInfrastructureManager(
			InfrastructureManager infrastructureManager) {
		this.infrastructureManager = infrastructureManager;
	}

	/**
	 * @param environmentUtils
	 *            the environmentUtils to set
	 */
	public void setEnvironmentUtils(EnvironmentUtils environmentUtils) {
		this.environmentUtils = environmentUtils;
	}

	/**
	 * @param productInstallator
	 *            the productInstallator to set
	 */
	public void setProductInstallator(ProductInstallator productInstallator) {
		this.productInstallator = productInstallator;
	}

	public void setSystemPropertiesProvider(
			SystemPropertiesProvider systemPropertiesProvider) {
		this.systemPropertiesProvider = systemPropertiesProvider;
	}

}