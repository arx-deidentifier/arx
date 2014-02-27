ARX - Powerful Data Anonymization
====

This project aims at providing a comprehensive, open source anonymization framework for sensitive personal data. It is able to alter the data in a way that guarantees minimal information loss while making sure that the transformed data adheres to well-known privacy criteria, such as k-anonymity, l-diversity or t-closeness. 
It implements a variety of globally optimal full-domain anonymity algorithms and implements several optimizations which result in a highly efficient anonymization process. This includes an implementation of the Flash algorithm which uses a novel search strategy and fully exploits the implementation framework while offering stable execution times.

Overview
------

This project aims at providing a comprehensive, open source data anonymization framework for sensitive personal data. It is able to alter the data in a way that guarantees minimal information loss while making sure that the transformed data adheres to well-known privacy criteria, such as k-anonymity, l-diversity, t-closeness or δ-presence. For generalizing data items, the framework employs generalization hierarchies, which can easily be constructed by end-users to meet their requirements. Because its implementation is highly efficient in terms of execution times, it enables users to explore a large number of possible privacy-preserving transformations in near real-time and pick the one that best suites their needs. Furthermore, the framework can be integrated into other systems for de-identifying sensitive information on-demand, e.g., when exporting data. It consists of several independent components:

1. An open source implementation of a highly efficient k-anonymity, l-diversity, t-closeness and δ-presence algorithm.
2. A dedicated data management framework, that has been carefuly engineered to meet the needs of data anonymization algorithms and can serve as a testbed for further research.
3. A clean API that brings powerful data anonymization capabilities to any Java program.
4. A comprehensive cross-platform graphical user interface (GUI), which is oriented towards end-users and provides several advanced features, such as a wizard for easily creating generalization hierarchies, an intuitive way to explore the solution space and ways to compare transformed datasets to the original dataset.

When anonymizing data, the framework distinguishes between four different types of attributes:

* Identifiying attributes (such as name) are removed from the dataset.
* Quasi-identifiers (such as age or zipcode) are generalized by applying the provided generalization hierarchies.
* Sensitive attributes are kept as is and can be utilized to derive t-close or l-diverse transformations.
* Insensitive attributes are kept as is.

Currently, the supported metrics for information loss include monotonic and non-monotonic variants of Height, Precision, Discernability and Entropy.

Highlights
------

The framework provides highly efficient implementations of several well-known data anonymization criteria as well as metrics for information loss. In many cases, it outperforms pevious implementations by up to several orders of magnitude. It is able to find optimal solutions for monotonic and non-monotonic metrics as well as the k-anonymity, l-diversity and t-closeness criteria while allowing for tuple suppression. When enabling tuple suppression, a subset of the data items of pre-defined relative size is allowed to not adhere to the specified anonymity criterion. During anonymization this subset is completely suppressed. This allows to further reduce information loss. When employing tuple suppression with l-diversity or t-closeness or non-monotonic metrics, the framework utilizes a modified algorithm that finds an optimal solution and consistently characterizes the solution space. This comes at the cost of increased execution times. Alternatively, the framework can be configured to assume practical monotonicity and find a solution very fast, which will be the global optimum in the majority of cases although this is not guaranteed. The framework provides implementations of classical k-anonymity as well as various variants of l-diversity and t-closeness. Variants of l-diversity include recursive (c,l)-diversity, entropy l-diversity and distinct l-diversity. The t-closeness criterion can be based on the equal or hierarchical earth-movers-distance. The codebase is extensively tested, well documented and suitable for implementing a wide variety of data anonymization algorithms.
Examples

1. The framework can be used via an intuitive graphical user interface

![Image](https://raw.github.com/arx-deidentifier/arx/blob/master/doc/img/overview_view3.png)

It allows to import datasets, create generalization hierarchies, specify the desired anonymity criterion and search the complete solution space in just a few seconds. Afterwards, the solution space can be explored in order to find an anonymous transformation that meets the requirements. To this end, the transformed dataset can be compared to the original input dataset.

2. The framework provides an API for seamless integration into other systems
	
```Java
// Load data
Data data = Data.create("input.csv");
 
// Set attribute types and load hierarchies
data.getDefinition().setAttributeType("age", Hierarchy.create("age.csv"));
data.getDefinition().setAttributeType("zipcode", Hierarchy.create("zipcode.csv"));
data.getDefinition().setAttributeType("disease", AttributeType.SENSITIVE_ATTRIBUTE);
 
// Define privacy requirements
ARXConfiguration config = ARXConfiguration.create();
config.addCriterion(new KAnonymity(5));
config.addCriterion(new HierarchicalDistanceTCloseness("disease", 0.6d, Hierarchy.create("disease.csv")));
config.setMaxOutliers(0d);
config.setMetric(Metric.createEntropyMetric());
 
// Perform anonymization
ARXAnonymizer anonymizer = new ARXAnonymizer();
ARXResult result = anonymizer.anonymize(data, config);
 
// Write result
result.getHandle().write("output.csv");
```

3. More examples are available in the repository

Limitations
------

The ARX framework implements data anonymization with full-domain global-recording based on generalization hierarchies. This means that, by design, the same generalization strategy (i.e., hierarchy) is applied to all values of an attribute, which can result in low data quality and might thus require enabling tuple suppression to produce practicable results. The framework can only anonymize datasets with roughly 10 quasi-identifiers (see the curse of dimensionality). The implementation employs in-memory data management and is thus not able to handle datasets with more than a few million data items on common desktop machines.


WEBSITE
------

More details can be found at: http://arx.deidentifier.org/

LICENSE
------

The ARX framework is copyright (C) 2012 Florian Kohlmayer and Fabian Prasser. It is licensed under the GNU GPL3:

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.

EXTERNAL LIBRARIES
------

The framework uses external libraries. The according licenses are listed in the respective lib folders.
