package org.deidentifier.arx.certificate;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithFormat;
import org.deidentifier.arx.certificate.elements.ElementListBullet;
import org.deidentifier.arx.certificate.elements.ElementSubtitle;
import org.deidentifier.arx.certificate.elements.ElementTitle;
import org.deidentifier.arx.criteria.AverageReidentificationRisk;
import org.deidentifier.arx.criteria.DDisclosurePrivacy;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.KMap;
import org.deidentifier.arx.criteria.PopulationUniqueness;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.deidentifier.arx.criteria.RiskBasedCriterion;
import org.deidentifier.arx.criteria.SampleUniqueness;
import org.deidentifier.arx.metric.Metric.AggregateFunction;

import rst.pdfbox.layout.elements.ControlElement;
import rst.pdfbox.layout.elements.VerticalSpacer;
import rst.pdfbox.layout.text.BaseFont;

/**
 * Renders Certificates for arx input and output properties
 * 
 * @author Annika Saken
 *
 */
public class CertificateRenderer {

	ARXDocument document;

	private DataDefinition definition;
	private ARXConfiguration config;
	private DataHandle input;
	private DataHandle output;
	private ARXNode transformation;

	public CertificateRenderer(DataDefinition definition, ARXConfiguration config, DataHandle input, DataHandle output,
			ARXNode transformation) {

		this.definition = definition;
		this.config = config;
		this.input = input;
		this.output = output;
		this.transformation = transformation;
		this.document = new ARXDocument();
	}

	/** 
	 * Generates a pdf certificate, saves it and optionally shows it via the system's default pdf application
	 * @param name	the name of the file 
	 * @param show	whether to show or not show the pdf
	 */
	public void getCertificate(String name, boolean show) {
		renderInput(definition, input, config);	  
		document.add(ControlElement.NEWPAGE);
		renderOutput(definition, output, transformation, config);

		try {
			final OutputStream outputStream = new FileOutputStream(name);
			document.save(outputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (show && Desktop.isDesktopSupported()) {
			try {
				File myFile = new File(name);
				Desktop.getDesktop().open(myFile);
			} catch (IOException e) {
				System.out.println("No application registered for PDFs");
			}
		}
	}

	/**
	 * Renders the input properties to the document
	 * 
	 * @param definition
	 * @param data
	 * @param config
	 */
	private void renderInput(DataDefinition definition, DataHandle data,
			ARXConfiguration config) {

		document.add(new ElementTitle("Utility Analysis - Input properties"));
		document.add(new VerticalSpacer(30));

		// data properties
		addBulletList("Data Properties",
				createListItem(StringResources.getMessage("Certificate.9"), String.valueOf(data.getNumRows())));

		{
			// UtilityMeasure
			List<String> bulletitems = new ArrayList<>();
			bulletitems.add(createListItem(StringResources.getMessage("Certificate.114"), config.getMetric().toString()));
			AggregateFunction aggregateFunction = config.getMetric().getAggregateFunction();
			bulletitems.add(createListItem(StringResources.getMessage("Certificate.149"), aggregateFunction == null
					? StringResources.getMessage("Certificate.150") : aggregateFunction.toString()));
			addBulletList(StringResources.getMessage("Certificate.114"), bulletitems);
		}

		//Transformation & Attributes
		Map<String, Map<String, List<String>>> transformationBulletitems = new LinkedHashMap<>();
		Map<String, Map<String, List<String>>> attributesBulletitems = new LinkedHashMap<>();

		//TODO: Check	
		/*
		 * config.getAllowedOutliers()
		 */
		transformationBulletitems.put(createListItem(StringResources.getMessage("Certificate.10"), StringUtil.getPrettyString(config.getMaxOutliers() * 100d)
				+ StringResources.getMessage("Certificate.11")), new LinkedHashMap<String, List<String>>());


		{
			// Print identifying attributes
			Map<String, List<String>> attributes = new LinkedHashMap<>();
			String identifyingItem = StringResources.getMessage("Certificate.13") + " ["+ String.valueOf(definition.getIdentifyingAttributes().size()) +"]";

			for (int i = 0; i < data.getNumColumns(); i++) {
				final String s = data.getAttributeName(i);
				if (definition.getIdentifyingAttributes().contains(s)) {
					String attributeItem = createListItem("Attribute", s);
					List<String> attributeInformationItems = new ArrayList<>();
					attributeInformationItems.add(createListItem(StringResources.getMessage("Certificate.5"), definition.getDataType(s).toString()));
					attributes.put(attributeItem, attributeInformationItems);
				}
			}
			attributesBulletitems.put(identifyingItem, attributes);
		}


		{
			// Print quasi-identifying attributes
			Map<String, List<String>> attributes = new LinkedHashMap<>();
			String quasiIdentifyingItem = StringResources.getMessage("Certificate.20") + " ["+ String.valueOf(definition.getQuasiIdentifyingAttributes().size()) + "]";

			for (int i = 0; i < data.getNumColumns(); i++) {
				final String s = data.getAttributeName(i);
				if (definition.getQuasiIdentifyingAttributes().contains(s)) {
					String attributeItem = createListItem("Attribute", s);
					List<String> attributeInformationItems = new ArrayList<>();
					Map<String, List<String>> tAttributeInformationItems = new LinkedHashMap<>();

					if (definition.getHierarchy(s) != null) {
						DataType<?> type = definition.getDataType(s);
						attributeInformationItems.add(createListItem(StringResources.getMessage("Certificate.5"), type.getDescription().getLabel()));
						if (type.getDescription().hasFormat() && ((DataTypeWithFormat) type).getFormat() != null) {
							attributeInformationItems.add(createListItem(StringResources.getMessage("Certificate.101"), ((DataTypeWithFormat) type).getFormat()));
						}

						attributes.put(attributeItem, attributeInformationItems);
						tAttributeInformationItems.put(createListItem(StringResources.getMessage("Certificate.113"), StringUtil.getPrettyString(config.getAttributeWeight(s))), new ArrayList<String>());

						// Determine height of hierarchy
						int height = 0;
						String[][] hierarchy = definition.getHierarchy(s);
						if (hierarchy != null && hierarchy.length != 0 && hierarchy[0] != null) {
							height = hierarchy[0].length;
						}

						List<String> generalizationItems = new ArrayList<>();
						generalizationItems.add(createListItem(StringResources.getMessage("Certificate.6"), String.valueOf(height)));
						generalizationItems.add(createListItem("Minimum", String.valueOf(definition.getMinimumGeneralization(s))));
						generalizationItems.add(createListItem("Maximum", String.valueOf(definition.getMaximumGeneralization(s))));
						tAttributeInformationItems.put("Generalization", generalizationItems);
					}

					if (definition.getMicroAggregationFunction(s) != null) {
						List<String> microaggregationItems = new ArrayList<>();
						microaggregationItems.add(createListItem(StringResources.getMessage("Certificate.126"), definition.getMicroAggregationFunction(s).getLabel()));
						tAttributeInformationItems.put("Microaggregation", microaggregationItems);
					}

					transformationBulletitems.put(attributeItem, tAttributeInformationItems);
				}
			}

			attributesBulletitems.put(quasiIdentifyingItem, attributes);
		}

		{
			// Print sensitive attributes
			Map<String, List<String>> attributes = new LinkedHashMap<>();
			String sensitiveItem = StringResources.getMessage("Certificate.27") + " ["+ String.valueOf(definition.getSensitiveAttributes().size())  +"]";

			for (int i = 0; i < data.getNumColumns(); i++) {
				final String s = data.getAttributeName(i);
				if (definition.getSensitiveAttributes().contains(s)) {
					String attributeItem = createListItem("Attribute", s);
					List<String> attributeInformationItems = new ArrayList<>();
					//TODO: check
					/*if (config.getHierarchy(s) != null && config.getHierarchy(s).getHierarchy() != null) {
							int height = 0;
							if (config.getHierarchy(s).getHierarchy().length > 0) {
								height = config.getHierarchy(s).getHierarchy()[0].length;
							} 
					 */
					if (definition.getHierarchy(s) != null) {
						int height = 0;
						if (definition.getHierarchy(s).length > 0) {
							height = definition.getHierarchy(s)[0].length;
						}

						attributeInformationItems.add(createListItem(StringResources.getMessage("Certificate.5"), definition.getDataType(s).toString()));

						//TODO: Check why there is height 
						//values[2] = String.valueOf(height);
					}
					attributes.put(attributeItem, attributeInformationItems);
				}
			}

			attributesBulletitems.put(sensitiveItem, attributes);
		}

		{
			// Print insensitive attributes
			Map<String, List<String>> attributes = new LinkedHashMap<>();
			String insensitiveItem = StringResources.getMessage("Certificate.34") + " ["+ String.valueOf(definition.getInsensitiveAttributes().size()) +"]";

			for (int i = 0; i < data.getNumColumns(); i++) {
				final String s = data.getAttributeName(i);
				if (definition.getInsensitiveAttributes().contains(s)) {
					String attributeItem = createListItem("Attribute", s);
					List<String> attributeInformationItems = new ArrayList<>();
					attributeInformationItems.add(createListItem(StringResources.getMessage("Certificate.5"), definition.getDataType(s).toString()));
					attributes.put(attributeItem, attributeInformationItems);
				}
			}

			attributesBulletitems.put(insensitiveItem, attributes);
		}

		addThreeStageBulletList("Transformation", transformationBulletitems);
		addThreeStageBulletList(StringResources.getMessage("Certificate.12"), attributesBulletitems);
	}

	/**
	 * Renders the output properties to the document
	 * @param definition
	 * @param data
	 * @param transformation
	 * @param config
	 */
	private void renderOutput(DataDefinition definition, DataHandle data, ARXNode transformation,
			ARXConfiguration config) {

		document.add(new ElementTitle("Utility Analysis - Output properties"));
		document.add(new VerticalSpacer(30));

		// Print information loss
		if (transformation.getMaximumInformationLoss().getValue()
				.equals(transformation.getMinimumInformationLoss().getValue())) {

			addBulletList(StringResources.getMessage("Certificate.46"), createListItem(StringResources.getMessage("Certificate.46"), transformation.getMinimumInformationLoss().toString()));
			// TODO handle % later
			// final String infoloss =
			// node.getMinimumInformationLoss().toString() +
			// " [" +
			// StringUtil.getPrettyString(asRelativeValue(node.getMinimumInformationLoss(),
			// result)) + "%]"; //$NON-NLS-1$ //$NON-NLS-2$

		}

		// Print basic info on neighboring nodes
		{	
			List<String> bulletitems = new ArrayList<>();

			bulletitems.add(createListItem(StringResources.getMessage("Certificate.48"), String.valueOf(transformation.getSuccessors().length)));
			bulletitems.add(createListItem(StringResources.getMessage("Certificate.49"), String.valueOf(transformation.getPredecessors().length)));
			bulletitems.add(createListItem(StringResources.getMessage("Certificate.50"), Arrays.toString(transformation.getTransformation())));
			addBulletList(StringResources.getMessage("Certificate.50"), bulletitems);
		}

		// If the node is anonymous
		if (transformation.getAnonymity() == Anonymity.ANONYMOUS) {
			Map<String, List<String>> bulletItemsWithSublists = new LinkedHashMap<>();

			// Print info about d-presence
			if (config.containsCriterion(DPresence.class)) {
				DPresence criterion = config.getCriterion(DPresence.class);
				// Only if its not an auto-generated criterion
				if (!(criterion.getDMin() == 0d && criterion.getDMax() == 1d)) {
					List<String> sublist= new ArrayList<>();

					String bulletItem = createListItem(StringResources.getMessage("Certificate.92"), StringResources.getMessage("Certificate.93"));
					sublist.add(createListItem(StringResources.getMessage("Certificate.94"), StringUtil.getPrettyString(criterion.getDMin())));
					sublist.add(createListItem(StringResources.getMessage("Certificate.95"), StringUtil.getPrettyString(criterion.getDMax())));
					bulletItemsWithSublists.put(bulletItem, sublist);
				}
			}
			// Print info about k-anonymity
			if (config.containsCriterion(KAnonymity.class)) {
				KAnonymity criterion = config.getCriterion(KAnonymity.class);
				List<String> sublist = new ArrayList<>();

				String bulletItem = createListItem(StringResources.getMessage("Certificate.51"), StringResources.getMessage("Certificate.52"));
				sublist.add(createListItem(StringResources.getMessage("Certificate.53"), StringUtil.getPrettyString(criterion.getK())));
				bulletItemsWithSublists.put(bulletItem, sublist);
			}
			// Print info about k-map
			if (config.containsCriterion(KMap.class)) {
				KMap criterion = config.getCriterion(KMap.class);
				List<String> sublist = new ArrayList<>();

				String bulletItem = createListItem(StringResources.getMessage("Certificate.51"), StringResources.getMessage("Certificate.132"));
				sublist.add(createListItem(StringResources.getMessage("Certificate.53"), StringUtil.getPrettyString(criterion.getK())));
				if (!criterion.isAccurate()) {
					sublist.add(createListItem(StringResources.getMessage("Certificate.146"), StringUtil.getPrettyString(criterion.getDerivedK())));
					sublist.add(createListItem(StringResources.getMessage("Certificate.133"), StringUtil.getPrettyString(((KMap) criterion).getPopulationModel().getPopulationSize())));
					sublist.add(createListItem(StringResources.getMessage("Certificate.147"), StringUtil.getPrettyString(criterion.getSignificanceLevel())));
					sublist.add(createListItem(StringResources.getMessage("Certificate.148"), StringUtil.getPrettyString(criterion.getType1Error())));
				}
				bulletItemsWithSublists.put(bulletItem, sublist);
			}

			// Print info about (e,d)-dp
			if (config.containsCriterion(EDDifferentialPrivacy.class)) {
				EDDifferentialPrivacy criterion = config.getCriterion(EDDifferentialPrivacy.class);
				List<String> sublist = new ArrayList<>();

				String bulletItem = createListItem(StringResources.getMessage("Certificate.51"), StringResources.getMessage("Certificate.141"));
				sublist.add(createListItem(StringResources.getMessage("Certificate.142"),StringUtil.getPrettyString(criterion.getEpsilon())));
				sublist.add(createListItem(StringResources.getMessage("Certificate.143"), StringUtil.getPrettyString(criterion.getDelta())));
				sublist.add(createListItem(StringResources.getMessage("Certificate.144"), StringUtil.getPrettyString(criterion.getK())));
				sublist.add(createListItem(StringResources.getMessage("Certificate.145"), StringUtil.getPrettyString(criterion.getBeta())));
				bulletItemsWithSublists.put(bulletItem, sublist);
			}

			// Print info about l-diversity or t-closeness
			int index = 0;
			for (PrivacyCriterion c : config.getCriteria()) {
				if (c instanceof DistinctLDiversity) {
					DistinctLDiversity criterion = (DistinctLDiversity) c;
					List<String> sublist = new ArrayList<>();

					String bulletItem = createListItem(StringResources.getMessage("Certificate.57"), StringResources.getMessage("Certificate.58"));
					sublist.add(createListItem(StringResources.getMessage("Certificate.59"), StringUtil.getPrettyString(criterion.getL())));
					sublist.add(createListItem(StringResources.getMessage("Certificate.100"), criterion.getAttribute()));
					bulletItemsWithSublists.put(bulletItem, sublist);

				} else if (c instanceof EntropyLDiversity) {
					EntropyLDiversity criterion = (EntropyLDiversity) c;
					List<String> sublist = new ArrayList<>();

					String bulletItem = createListItem(StringResources.getMessage("Certificate.63"), StringResources.getMessage("Certificate.64"));
					sublist.add(createListItem(StringResources.getMessage("Certificate.65"), StringUtil.getPrettyString(criterion.getL())));
					sublist.add(createListItem(StringResources.getMessage("Certificate.100"), criterion.getAttribute()));
					bulletItemsWithSublists.put(bulletItem, sublist);

				} else if (c instanceof RecursiveCLDiversity) {
					RecursiveCLDiversity criterion = (RecursiveCLDiversity) c;
					List<String> sublist = new ArrayList<>();

					String bulletItem = createListItem(StringResources.getMessage("Certificate.69"), StringResources.getMessage("Certificate.70"));
					sublist.add(createListItem(StringResources.getMessage("Certificate.71"), StringUtil.getPrettyString(criterion.getC())));
					sublist.add(createListItem(StringResources.getMessage("Certificate.72"), StringUtil.getPrettyString(criterion.getL())));
					sublist.add(createListItem(StringResources.getMessage("Certificate.100"), criterion.getAttribute()));
					bulletItemsWithSublists.put(bulletItem, sublist);
				} else if (c instanceof EqualDistanceTCloseness) {
					EqualDistanceTCloseness criterion = (EqualDistanceTCloseness) c;
					List<String> sublist = new ArrayList<>();

					String bulletItem = createListItem(StringResources.getMessage("Certificate.77"), StringResources.getMessage("Certificate.78"));
					sublist.add(createListItem(StringResources.getMessage("Certificate.79"), StringUtil.getPrettyString(criterion.getT())));
					sublist.add(createListItem(StringResources.getMessage("Certificate.100"), criterion.getAttribute()));
					bulletItemsWithSublists.put(bulletItem, sublist);
				} else if (c instanceof HierarchicalDistanceTCloseness) {
					HierarchicalDistanceTCloseness criterion = (HierarchicalDistanceTCloseness) c;
					List<String> sublist = new ArrayList<>();

					String bulletItem = createListItem(StringResources.getMessage("Certificate.83"), StringResources.getMessage("Certificate.84"));
					sublist.add(createListItem(StringResources.getMessage("Certificate.85"), StringUtil.getPrettyString(criterion.getT())));
					sublist.add(createListItem(StringResources.getMessage("Certificate.100"), criterion.getAttribute()));
					final int height = definition.getHierarchy(criterion.getAttribute())[0].length;
					sublist.add(createListItem("SE-" + (index++), StringResources.getMessage("Certificate.87") + String.valueOf(height)));
					bulletItemsWithSublists.put(bulletItem, sublist);
				} else if (c instanceof DDisclosurePrivacy) {
					DDisclosurePrivacy criterion = (DDisclosurePrivacy) c;
					List<String> sublist = new ArrayList<>();

					String bulletItem = createListItem(StringResources.getMessage("Certificate.83"), StringResources.getMessage("Certificate.130"));
					sublist.add(createListItem(StringResources.getMessage("Certificate.131"), StringUtil.getPrettyString(criterion.getD())));
					sublist.add(createListItem(StringResources.getMessage("Certificate.100"), criterion.getAttribute()));
					bulletItemsWithSublists.put(bulletItem, sublist);
				}
			}

			// Print info about risk-based criteria
			Set<RiskBasedCriterion> criteria = config.getCriteria(RiskBasedCriterion.class);
			for (RiskBasedCriterion criterion : criteria) {
				String type = ""; //$NON-NLS-1$
				if (criterion instanceof AverageReidentificationRisk) {
					type = StringResources.getMessage("Certificate.123"); //$NON-NLS-1$
				} else if (criterion instanceof PopulationUniqueness) {
					type = StringResources.getMessage("Certificate.125"); //$NON-NLS-1$
				} else if (criterion instanceof SampleUniqueness) {
					type = StringResources.getMessage("Certificate.124"); //$NON-NLS-1$
				}

				List<String> sublist = new ArrayList<>();
				String bulletItem = createListItem(StringResources.getMessage("Certificate.51"), type);
				sublist.add(createListItem(StringResources.getMessage("Certificate.120"), StringUtil.getPrettyString(criterion.getRiskThreshold())));

				if (criterion instanceof PopulationUniqueness) {
					sublist.add(createListItem(StringResources.getMessage("Certificate.133"), StringUtil.getPrettyString(
							((PopulationUniqueness) criterion).getPopulationModel().getPopulationSize())));
					sublist.add(createListItem(StringResources.getMessage("Certificate.122"), ((PopulationUniqueness) criterion).getStatisticalModel().toString()));
				}

				bulletItemsWithSublists.put(bulletItem, sublist);
			}

			addTwoStageBulletList("Privacy guarantees", bulletItemsWithSublists);
		} else {
			addBulletList("Privacy guarantees", createListItem(StringResources.getMessage("Certificate.90"), StringResources.getMessage("Certificate.91")));
		}
	}

	//Helper methods
	/**
	 * Creates a standard key value list item
	 * @param key
	 * @param value
	 * @return
	 */
	private String createListItem(String key, String value) {
		return key + ": " + value;
	}

	/**
	 * Adds a bullet list with a title and a single item to the document
	 * @param title
	 * @param item
	 */
	private void addBulletList(String title, String item) {
		List<String> items = new ArrayList<>();
		items.add(item);
		addBulletList(title, items);
	}

	/**
	 * Adds a bullet list with a title and multiple items to the document
	 * @param title
	 * @param items
	 */
	private void addBulletList(String title, List<String> items) {
		document.add(new ElementSubtitle(title));
		ElementListBullet list = new ElementListBullet();

		for(String item: items)
			list.addListItem(item, 11, BaseFont.Helvetica);
		document.add(list);
	}

	/**
	 * Adds a two-stage bullet list with a title and multiple items to the document
	 * @param title
	 * @param items
	 */
	private void addTwoStageBulletList(String title, Map<String, List<String>> items) {
		document.add(new ElementSubtitle(title));
		ElementListBullet list = new ElementListBullet();

		for(Map.Entry<String, List<String>> entry : items.entrySet()) {
			ElementListBullet subList = new ElementListBullet();
			list.addList(subList);
			list.addListItem(entry.getKey(), 11, BaseFont.Helvetica);

			for(String subItem: entry.getValue()){
				subList.addListItem(subItem, 11, BaseFont.Helvetica);
			}
		}

		document.add(list);
	}

	/**
	 * Adds a three-stage bullet list with a title and multiple items to the document
	 * @param title
	 * @param items
	 */
	private void addThreeStageBulletList(String title, Map<String,Map<String, List<String>>> items) {
		document.add(new ElementSubtitle(title));
		ElementListBullet list0 = new ElementListBullet();

		for(Map.Entry<String,Map<String, List<String>>> entry0 : items.entrySet()) {
			ElementListBullet list1 = new ElementListBullet();
			list0.addList(list1);
			list0.addListItem(entry0.getKey(), 11, BaseFont.Helvetica);
			Map<String, List<String>>items1 = (Map<String, List<String>>) entry0.getValue();

			for(Map.Entry<String, List<String>> entry1 : items1.entrySet()) {
				ElementListBullet list2 = new ElementListBullet();
				list1.addList(list2);
				list1.addListItem(entry1.getKey(), 11, BaseFont.Helvetica);

				for(String item2: entry1.getValue()) {
					list2.addListItem(item2, 11, BaseFont.Helvetica);
				}
			}
		}

		document.add(list0);
	}
}
